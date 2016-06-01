package com.nicologies.prextras;

import com.nicologies.prextras.common.PrExtrasConstants;
import com.nicologies.prextras.common.SettingsKeys;
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.AgentRunningBuild;
import jetbrains.buildServer.agent.runner.BuildServiceAdapter;
import jetbrains.buildServer.agent.runner.ProgramCommandLine;
import jetbrains.buildServer.util.StringUtil;
import org.eclipse.egit.github.core.PullRequest;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.User;
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
        getLogger().progressMessage("Running PrExtras");
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
            AgentRunningBuild build = getBuild();

            PullRequestService service = getPullRequestService(runnerParams);
            String prNum = configParams.get("teamcity.build.branch");
            PullRequest pullRequest = null;
            boolean isPullRequestBuild = configParams.containsValue("refs/pull/" + prNum + "/merge")
                    || configParams.containsValue("refs/pull/" + prNum + "/head");
            isPullRequestBuild &= StringUtil.isNumber(prNum);
            if(isPullRequestBuild){
                pullRequest = getPullRequest(service, prNum);
            }

            String branchName = getBranchName(pullRequest, prNum);

            if(!StringUtil.isEmptyOrSpaces(paramName)) {
                build.addSharedConfigParameter(paramName, branchName);
                build.addSharedConfigParameter("teamcity.build.pull_req.branch_name", branchName);
            }

            ExportPullReqMetaInfo(pullRequest, build, configParams);

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

            FailBuildIfConflict(runnerParams, pullRequest);
        } catch(RunBuildException e){
            throw e;
        }
        catch (Exception e) {
            throw new RunBuildException(e.getMessage(), e);
        }
    }

    private void ExportPullReqMetaInfo(PullRequest pullRequest, AgentRunningBuild build,
                                       Map<String, String> configParams) {
        if(pullRequest == null){
            return;
        }
        User user = pullRequest.getUser();
        String email = user.getEmail();
        if(!StringUtil.isEmptyOrSpaces(email)) {
            build.addSharedConfigParameter("teamcity.build.pull_req.author_email", email);
        }

        String login = user.getLogin();
        if(!StringUtil.isEmptyOrSpaces(login)){
            build.addSharedConfigParameter("teamcity.build.pull_req.author", MapUser(login, configParams));
        }

        String htmlUrl = pullRequest.getHtmlUrl();
        if(!StringUtil.isEmptyOrSpaces(htmlUrl)) {
            build.addSharedConfigParameter("teamcity.build.pull_req.url", htmlUrl);
        }

        User assignee = pullRequest.getAssignee();
        if(assignee != null){
            login = assignee.getLogin();
            if(!StringUtil.isEmptyOrSpaces(login)) {
                build.addSharedConfigParameter("teamcity.build.pull_req.assignee",
                        MapUser(login, configParams));
            }
            email = assignee.getEmail();
            if(!StringUtil.isEmptyOrSpaces(email)) {
                build.addSharedConfigParameter("teamcity.build.pull_req.assignee_email", email);
            }
        }

    }

    private static String MapUser(String name, Map<String, String> configParams) {
        String mappedName = configParams.get(PrExtrasConstants.PrefixOfUserMapping + name);
        if(StringUtil.isEmptyOrSpaces(mappedName)){
            return name;
        }else{
            return mappedName;
        }
    }

    private void FailBuildIfConflict(Map<String, String> runnerParams,
                                     PullRequest pr) throws RunBuildException {
        if(pr == null){
            return;
        }
        String failBuildIfConflict = runnerParams.get(SettingsKeys.FailBuildIfConflict);

        if(!StringUtil.isEmptyOrSpaces(failBuildIfConflict) && failBuildIfConflict.equalsIgnoreCase("true")){
            if(!pr.isMerged() && !pr.isMergeable()){
                String msg = "The pull request has conflicts " + pr.getHtmlUrl();
                throw new RunBuildException(msg);
            }
        }
    }

    private String getBranchName(PullRequest pr, String prNum) throws RunBuildException{
        if(!StringUtil.isNumber(prNum)) {
            return prNum;
        }
        try {
            return pr.getHead().getRef();
        }catch (Exception ex){
            throw new RunBuildException("Unable to get branch name for pull request " + ex.getMessage(), ex);
        }
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
        if(authType.equals(PrExtrasConstants.SystemWideTokenAuthType)){
            String token = getSystemProperties().get(SettingsKeys.GithubToken);
            if(StringUtil.isEmptyOrSpaces(token)){
                throw new InvalidParameterException("Please set the system wide github token");
            }
            service.getClient().setOAuth2Token(token);
        }
        else if(authType.equals(PrExtrasConstants.TokenAuthType)){
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
