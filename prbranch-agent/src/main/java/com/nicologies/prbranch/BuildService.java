package com.nicologies.prbranch;

import com.nicologies.prbranch.common.PrBranchConstants;
import com.nicologies.prbranch.common.SettingsKeys;
import jetbrains.buildServer.BuildProblemData;
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
        Map<String,String> configParams = getConfigParameters();

        _isOnWindows = configParams.get("teamcity.agent.jvm.os.name").toLowerCase().startsWith("win");

        try {
            String paramName = runnerParams.get(SettingsKeys.ExportParamName);
            if(StringUtil.isEmptyOrSpaces(paramName)){
                String msg = "Please set the "+ SettingsKeys.ExportParamName +" in the build step's settings page";
                getLogger().error(msg);
                throw new RunBuildException(msg);
            }

            PullRequestService service = getPullRequestService(runnerParams);
            String prNum = configParams.get("teamcity.build.branch");
            FailBuildIfConflict(runnerParams, service, prNum);
            String branchName = getBranchName(service, prNum);
            if(!StringUtil.isEmptyOrSpaces(paramName)) {
                getBuild().addSharedConfigParameter(paramName, branchName);
            }

            String appendToBuildNum = runnerParams.get(SettingsKeys.AppendToBuildNum);
            if(!StringUtil.isEmptyOrSpaces(appendToBuildNum)
                    && appendToBuildNum.toLowerCase().equals("true")) {
                String buildNum = getConfigParameters().get("build.number");
                boolean isGitVersionBuildNum = buildNum.matches("(?i).*PullRequest\\.\\d*\\+.*");
                if(isGitVersionBuildNum){
                    buildNum = buildNum.replaceAll("(?i)PullRequest\\.\\d*", branchName);
                }else if(!buildNum.contains(branchName)){
                    buildNum = buildNum + "-" + branchName;
                }
                buildNum = buildNum.replaceAll("(?i)PullRequest", "PR");
                String param = "##teamcity[buildNumber " + "'" + buildNum + "']";
                getLogger().message(param);
            }
        } catch (Exception e) {
            String msg = e.getMessage();
            getLogger().error(msg);
            throw new RunBuildException(msg, e);
        }
    }

    private void FailBuildIfConflict(Map<String, String> runnerParams,
                                     PullRequestService service, String prNum) throws RunBuildException {
        if(!StringUtil.isNumber(prNum)){
            return;
        }
        String failBuildIfConflict = runnerParams.get(SettingsKeys.FailBuildIfConflict);

        if(!StringUtil.isEmptyOrSpaces(failBuildIfConflict) && failBuildIfConflict.equalsIgnoreCase("true")){
            PullRequest pr = getPullRequest(service, prNum);
            if(!pr.isMergeable()){
                throw new RunBuildException("The pull request has conflicts");
            }
        }
    }

    private String getBranchName(PullRequestService service, String prNum) throws RunBuildException{
        if(!StringUtil.isNumber(prNum)){
            return prNum;
        }

        PullRequest pr = getPullRequest(service, prNum);

        return pr.getHead().getRef();
    }

    private PullRequest getPullRequest(PullRequestService service, String prNum) throws RunBuildException {
        final Map<String, String> configParams = getConfigParameters();

        String repoUrl = configParams.get("vcsroot.url");
        RepositoryId repo = RepoInfoParser.Parse(repoUrl);

        PullRequest pr;
        int prNumInteger;
        try {
            prNumInteger = Integer.parseInt(prNum);
        } catch (Exception e) {
            throw new RunBuildException(prNum + " is not pull request number: " + e.getMessage() , e);
        }

        getLogger().message("Trying to get branch name for pull request " + prNum + " from "
                + repo.getOwner() + "'s " + repo.getName());
        try {
            pr = service.getPullRequest(repo, prNumInteger);
        } catch (IOException e) {
            throw new RunBuildException("unable to get pull request info: " + e.getMessage(), e);
        }
        return pr;
    }

    @NotNull
    private PullRequestService getPullRequestService(Map<String, String> runnerParams) {
        PullRequestService service = new PullRequestService();
        String authType = runnerParams.get(SettingsKeys.AuthType);
        if(authType.equals(PrBranchConstants.SystemWideTokenAuthType)){
            String token = getSystemProperties().get(SettingsKeys.GithubToken);
            if(StringUtil.isEmptyOrSpaces(token)){
                throw new InvalidParameterException("Please set the system wide github token");
            }
            service.getClient().setOAuth2Token(token);
        }
        else if(authType.equals(PrBranchConstants.TokenAuthType)){
            String token = runnerParams.get(SettingsKeys.GithubToken);
            if(StringUtil.isEmptyOrSpaces(token)){
                throw new InvalidParameterException("Please set the github token in the build step's settings page");
            }
            service.getClient().setOAuth2Token(token);
        }
        else{
            String password = runnerParams.get(SettingsKeys.GithubPassword);
            if(StringUtil.isEmptyOrSpaces(password)){
                password = null;
            }
            service.getClient().setCredentials(runnerParams.get(SettingsKeys.GithubUserName), password);
        }
        return service;
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
