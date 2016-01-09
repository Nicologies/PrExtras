package com.nicologies.prbranch;

import com.nicologies.prbranch.common.PrBranchConstants;
import com.nicologies.prbranch.common.SettingsKeys;
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.runner.BuildServiceAdapter;
import jetbrains.buildServer.agent.runner.ProgramCommandLine;
import jetbrains.buildServer.util.StringUtil;
import org.eclipse.egit.github.core.PullRequest;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.service.PullRequestService;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class BuildService extends BuildServiceAdapter {
    private boolean _isOnWindows = true;
    public BuildService() {
    }

    @Override
    public void beforeProcessStarted() throws RunBuildException {
        getLogger().progressMessage("Running PrBranch");
        Map<String,String> runnerParams = getRunnerParameters();
        _isOnWindows = getConfigParameters().get("teamcity.agent.jvm.os.name").toLowerCase().startsWith("win");

        try {
            String paramName = runnerParams.get(SettingsKeys.ExportParamName);
            if(StringUtil.isEmptyOrSpaces(paramName)){
                String msg = "Please set the "+ SettingsKeys.ExportParamName +" in the build step's settings page";
                getLogger().error(msg);
                throw new RunBuildException(msg);
            }
            String pullReq = getConfigParameters().get("teamcity.build.branch");
            String branchName = getBranchName(pullReq);
            if(!StringUtil.isEmptyOrSpaces(paramName)) {
                getBuild().addSharedConfigParameter(paramName, branchName);
            }

            String buildNum = getConfigParameters().get("build.number") + "-" + branchName;
            String param = "##teamcity[buildNumber " + "'"+ buildNum +"']";
            getLogger().message(param);
        } catch (Exception e) {
            String msg = "unable to get branch name: " + e.getMessage();
            getLogger().error(msg);
            throw new RunBuildException(msg, e);
        }
    }

    private String getBranchName(String prNum) throws RunBuildException{
        if(!StringUtil.isNumber(prNum)){
            return prNum;
        }
        final Map<String, String> runnerParams = getRunnerParameters();
        final Map<String, String> configParams = getConfigParameters();
        String token = runnerParams.get(SettingsKeys.GithubToken);
        if(StringUtil.isEmptyOrSpaces(token)){
            throw new InvalidParameterException("Please set the github token in the build step's settings page");
        }
        PullRequestService service = new PullRequestService();
        String authType = runnerParams.get(SettingsKeys.AuthType);
        if(authType.equals(PrBranchConstants.TokenAuthType)){
            service.getClient().setOAuth2Token(token);
        }
        else{
            String password = runnerParams.get(SettingsKeys.GithubPassword);
            if(StringUtil.isEmptyOrSpaces(password)){
                password = null;
            }
            service.getClient().setCredentials(runnerParams.get(SettingsKeys.GithubUserName), password);
        }

        String repoUrl = configParams.get("vcsroot.url");
        RepositoryId repo = RepoInfoParser.Parse(repoUrl);

        PullRequest pr;
        int prNumInteger;
        try {
            prNumInteger = Integer.parseInt(prNum);
        } catch (Exception e) {
            throw new RunBuildException(prNum + " is not pull request number: " + e.getMessage() , e);
        }

        try {
            pr = service.getPullRequest(repo, prNumInteger);
        } catch (IOException e) {
            throw new RunBuildException("unable to get pull request info: " + e.getMessage(), e);
        }

        return pr.getHead().getRef();
    }

    @NotNull
    public ProgramCommandLine makeProgramCommandLine() throws RunBuildException {
        return new ProgramCommandLine() {
            @NotNull
            public String getExecutablePath() throws RunBuildException {
                return _isOnWindows? "cmd" : "sh";
            }

            @NotNull
            public String getWorkingDirectory() throws RunBuildException {
                return getCheckoutDirectory().getPath();
            }

            @NotNull
            public List<String> getArguments() throws RunBuildException {
                List<String > ret = new Vector<String>();
                if(_isOnWindows)
                {
                    ret.add("/c");
                }
                ret.add("echo");
                ret.add("Branch name is " + getConfigParameters().get(getRunnerParameters().get(SettingsKeys.ExportParamName)));
                return ret;
            }

            @NotNull
            public Map<String, String> getEnvironment() throws RunBuildException {
                return getBuildParameters().getEnvironmentVariables();
            }
        };
    }
}
