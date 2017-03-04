package com.nicologies.prextras;

import com.nicologies.prextras.common.PrExtrasConstants;
import com.nicologies.prextras.common.SettingsKeys;
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.AgentRunningBuild;
import jetbrains.buildServer.agent.runner.BuildServiceAdapter;
import jetbrains.buildServer.agent.runner.ProgramCommandLine;
import jetbrains.buildServer.util.StringUtil;
import org.eclipse.egit.github.core.*;
import org.eclipse.egit.github.core.service.GitHubService;
import org.eclipse.egit.github.core.service.IssueService;
import org.eclipse.egit.github.core.service.PullRequestService;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.*;

public class BuildService extends BuildServiceAdapter {
    private boolean _isOnWindows = true;
    private static final String BranchNameParamName = "teamcity.build.pull_req.branch_name";
    public BuildService() {
    }

    @Override
    public void beforeProcessStarted() throws RunBuildException {
        getLogger().progressMessage("Running PrExtras");
        Map<String,String> runnerParams = getRunnerParameters();
        Map<String,String> configParams = getConfigParameters();

        _isOnWindows = configParams.get("teamcity.agent.jvm.os.name").toLowerCase().startsWith("win");

        try {
            AgentRunningBuild build = getBuild();

            PullRequestService service = new PullRequestService();
            initService(service);
            IssueService issueService = new IssueService(service.getClient());

            String prNum = configParams.get("teamcity.build.branch");
            PullRequest pullRequest = null;
            boolean isPullRequestBuild = configParams.containsValue("refs/pull/" + prNum + "/merge")
                    || configParams.containsValue("refs/pull/" + prNum + "/head");
            isPullRequestBuild &= StringUtil.isNumber(prNum);
            RepositoryId repo = getRepository(configParams);
            if(isPullRequestBuild){
                pullRequest = getPullRequest(service, repo, prNum);
            }
            build.addSharedConfigParameter("teamcity.build.pull_req.is_pull_req",
                    isPullRequestBuild? "true":"false");

            String branchName = getBranchName(pullRequest, prNum);

            build.addSharedConfigParameter(BranchNameParamName, branchName);

            ExportPullRequestExtraInfo(pullRequest, build, configParams, issueService);

            if(pullRequest != null && pullRequest.getComments() > 0){
                ExportPrParticipants(service, repo, pullRequest.getNumber());
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

            FailBuildIfConflict(runnerParams, pullRequest);
        } catch(RunBuildException e){
            throw e;
        }
        catch (Exception e) {
            throw new RunBuildException(e.getMessage(), e);
        }
    }

    private void ExportPullRequestExtraInfo(PullRequest pullRequest,
                                            AgentRunningBuild build,
                                            Map<String, String> configParams,
                                            IssueService issueService) {
        if(pullRequest == null){
            build.addSharedConfigParameter("teamcity.build.pull_req.is_merged", "false");
            build.addSharedConfigParameter("teamcity.build.pull_req.is_closed", "false");
            return;
        }
        build.addSharedConfigParameter("teamcity.build.pull_req.is_merged", pullRequest.isMerged()? "true" : "false");
        build.addSharedConfigParameter("teamcity.build.pull_req.is_closed",
                pullRequest.getClosedAt() != null? "true" : "false");

        User user = pullRequest.getUser();
        String email = user.getEmail();
        if(!StringUtil.isEmptyOrSpaces(email)) {
            build.addSharedConfigParameter("teamcity.build.pull_req.author_email", email);
        }

        String authorName = getGitHubUser(user);
        if(!StringUtil.isEmptyOrSpaces(authorName)){
            build.addSharedConfigParameter("teamcity.build.pull_req.author", authorName);
        }

        String htmlUrl = pullRequest.getHtmlUrl();
        if(!StringUtil.isEmptyOrSpaces(htmlUrl)) {
            build.addSharedConfigParameter("teamcity.build.pull_req.url", htmlUrl);
        }

        User assignee = pullRequest.getAssignee();
        if(assignee != null){
            String assigneeName = getGitHubUser(assignee);
            if(!StringUtil.isEmptyOrSpaces(assigneeName)) {
                build.addSharedConfigParameter("teamcity.build.pull_req.assignee", assigneeName);
            }
            email = assignee.getEmail();
            if(!StringUtil.isEmptyOrSpaces(email)) {
                build.addSharedConfigParameter("teamcity.build.pull_req.assignee_email", email);
            }
        }
        RepositoryId repo = getRepository(configParams);
        try {
            Issue issue = issueService.getIssue(repo, pullRequest.getNumber());
            List<Label> labels = issue.getLabels();
            if(!labels.isEmpty()) {
                String labelsString = StringUtil.join(";", labels.toArray());
                build.addSharedConfigParameter("teamcity.build.pull_req.labels", labelsString);
            }
        } catch (IOException e) {
            getLogger().message(e.getMessage() + e.getStackTrace());
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private void ExportPrParticipants(PullRequestService service, RepositoryId repo, int prNum) {
        try {
            HashSet<String> participants = new HashSet<String>();
            getParticipantsFromComments(participants, service.getComments(repo, prNum));
            IssueService issueService = new IssueService();
            initService(issueService);
            getParticipantsFromComments(participants, issueService.getComments(repo, prNum));
            if(participants.isEmpty()){
                return;
            }

            getBuild().addSharedConfigParameter("teamcity.build.pull_req.participants",
                    StringUtil.join(";", participants));
        } catch (IOException e) {
            e.printStackTrace();
            getLogger().error(e.getMessage());
        }
    }

    private void getParticipantsFromComments(HashSet<String> participants, List<? extends Comment> comments) {
        for(Comment comment : comments){
            String user = getGitHubUser(comment.getUser());
            if(StringUtil.isEmptyOrSpaces(user)){
                continue;
            }
            participants.add(user);
        }
    }

    private static String getGitHubUser(User user){
        if(user == null){
            return null;
        }
        String login = user.getLogin();
        if(StringUtil.isEmptyOrSpaces(login)){
            return null;
        }
        return login;
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
            return pr != null? pr.getHead().getRef(): prNum;
        }catch (Exception ex){
            throw new RunBuildException("Unable to get branch name for pull request " + ex.getMessage(), ex);
        }
    }

    private PullRequest getPullRequest(PullRequestService service, RepositoryId repo, String prNum)
            throws RunBuildException {
        PullRequest pr;
        int prNumInteger;
        try {
            prNumInteger = Integer.parseInt(prNum);
        } catch (Exception e) {
            throw new RunBuildException(prNum + " is not pull request number: " + e.getMessage() , e);
        }

        getLogger().message("Trying to get extra information of pull request " + prNum + " from "
                + repo.getOwner() + "'s " + repo.getName());
        try {
            pr = service.getPullRequest(repo, prNumInteger);
        } catch (IOException e) {
            throw new RunBuildException("unable to get pull request info: " + e.getMessage(), e);
        }
        return pr;
    }

    private RepositoryId getRepository(Map<String, String> configParams) {
        String repoUrl = configParams.get("vcsroot.url");
        return RepoInfoParser.Parse(repoUrl);
    }

    @NotNull
    private void initService(GitHubService service) {
        Map<String, String> runnerParams = getRunnerParameters();
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
            String user = runnerParams.get(SettingsKeys.GithubUserName);
            if(StringUtil.isNotEmpty(user)) {
                service.getClient().setCredentials(user, password);
            }
        }
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
                ret.add("Branch name is " + getConfigParameters().get(BranchNameParamName));
                return ret;
            }

            @NotNull
            public Map<String, String> getEnvironment() throws RunBuildException {
                return getBuildParameters().getEnvironmentVariables();
            }
        };
    }
}
