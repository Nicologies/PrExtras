package com.nicologies.prextras

import com.nicologies.prextras.common.PrExtrasConstants
import com.nicologies.prextras.common.SettingsKeys
import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.agent.AgentRunningBuild
import jetbrains.buildServer.agent.runner.BuildServiceAdapter
import jetbrains.buildServer.agent.runner.ProgramCommandLine
import jetbrains.buildServer.util.StringUtil
import org.eclipse.egit.github.core.*
import org.eclipse.egit.github.core.client.GitHubClient
import org.eclipse.egit.github.core.service.GitHubService
import org.eclipse.egit.github.core.service.IssueService
import org.eclipse.egit.github.core.service.PullRequestService

import java.io.IOException
import java.net.URL
import java.security.InvalidParameterException
import java.util.*

class BuildService : BuildServiceAdapter() {
    private var _isOnWindows = true

    private val gitHubApiUrl: URL
        get() {
            val url = GitHubApiUrlHelper.getURL(this.systemProperties)
            logger.message("GitHub API URL: " + url.toString())
            return url
        }

    @Throws(RunBuildException::class)
    override fun beforeProcessStarted() {
        logger.progressMessage("Running PrExtras")
        val runnerParams = runnerParameters
        val configParams = configParameters

        _isOnWindows = configParams["teamcity.agent.jvm.os.name"]!!.toLowerCase().startsWith("win")

        try {
            val build = build
            val repoUrl = getRepoUrl(configParams)
            val githubApiUrl = gitHubApiUrl

            val service = PullRequestService(GitHubClient(githubApiUrl.host,
                    githubApiUrl.port, githubApiUrl.protocol))
            initService(service)
            val issueService = IssueService(service.client)

            val prNum = configParams["teamcity.build.branch"]!!
            var pullRequest: PullRequest? = null
            val isPullRequestBuild = StringUtil.isNumber(prNum)
            val repo = getRepositoryId(repoUrl)
            if (isPullRequestBuild) {
                pullRequest = getPullRequest(service, repo, prNum)
            }
            exportConfigParam(build, "teamcity.build.pull_req.is_pull_req",
                    if (isPullRequestBuild) "true" else "false")

            val headBranchName = getHeadBranchName(pullRequest, prNum)

            exportConfigParam(build, HeadBranchNameParamName, headBranchName)

            val baseBranchName = getBaseBranchName(pullRequest, prNum)

            exportConfigParam(build, BaseBranchNameParamName, baseBranchName)

            ExportPullRequestExtraInfo(pullRequest, build, configParams, issueService)

            if (pullRequest != null && pullRequest.comments > 0) {
                exportPrParticipants(service, repo, pullRequest.number)
            }

            val appendToBuildNum = runnerParams[SettingsKeys.AppendToBuildNum]
            if (!StringUtil.isEmptyOrSpaces(appendToBuildNum) && appendToBuildNum!!.toLowerCase() == "true") {
                var buildNum = configParameters["build.number"]!!
                val isGitVersionBuildNum = buildNum.matches("(?i).*PullRequest\\.\\d*\\+.*".toRegex())
                if (isGitVersionBuildNum) {
                    buildNum = buildNum.replace("(?i)PullRequest\\.\\d*".toRegex(), headBranchName)
                } else if (!buildNum.contains(headBranchName)) {
                    buildNum = buildNum + "-" + headBranchName
                }
                buildNum = buildNum.replace("(?i)PullRequest".toRegex(), "PR")
                val param = "##teamcity[buildNumber '$buildNum']"
                logger.message(param)
            }

            failBuildIfConflict(runnerParams, pullRequest)
        } catch (e: RunBuildException) {
            throw e
        } catch (e: Exception) {
            throw RunBuildException(e.message, e)
        }

    }

    private fun exportConfigParam(build: AgentRunningBuild, key: String, value: String?) {
        ParamExporter.exportConfigParam(build, key, value)
    }

    private fun ExportPullRequestExtraInfo(pullRequest: PullRequest?,
                                           build: AgentRunningBuild,
                                           configParams: Map<String, String>,
                                           issueService: IssueService) {
        if (pullRequest == null) {
            exportConfigParam(build, "teamcity.build.pull_req.is_merged", "false")
            exportConfigParam(build, "teamcity.build.pull_req.is_closed", "false")
            return
        }
        exportConfigParam(build, "teamcity.build.pull_req.is_merged", if (pullRequest.isMerged) "true" else "false")
        exportConfigParam(build, "teamcity.build.pull_req.is_closed",
                if (pullRequest.closedAt != null) "true" else "false")

        val user = pullRequest.user
        var email = user.email
        if (!StringUtil.isEmptyOrSpaces(email)) {
            exportConfigParam(build, "teamcity.build.pull_req.author_email", email)
        }

        val authorName = getGitHubUser(user)
        if (!StringUtil.isEmptyOrSpaces(authorName)) {
            exportConfigParam(build, "teamcity.build.pull_req.author", authorName)
        }

        val htmlUrl = pullRequest.htmlUrl
        if (!StringUtil.isEmptyOrSpaces(htmlUrl)) {
            exportConfigParam(build, "teamcity.build.pull_req.url", htmlUrl)
        }

        val assignee = pullRequest.assignee
        if (assignee != null) {
            val assigneeName = getGitHubUser(assignee)
            if (!StringUtil.isEmptyOrSpaces(assigneeName)) {
                exportConfigParam(build, "teamcity.build.pull_req.assignee", assigneeName)
            }
            email = assignee.email
            if (!StringUtil.isEmptyOrSpaces(email)) {
                exportConfigParam(build, "teamcity.build.pull_req.assignee_email", email)
            }
        }
        val repo = getRepositoryId(getRepoUrl(configParams))
        try {
            val issue = issueService.getIssue(repo, pullRequest.number)
            val labels = issue.labels
            if (!labels.isEmpty()) {
                val labelsString = StringUtil.join(";", *labels.toTypedArray())
                exportConfigParam(build, "teamcity.build.pull_req.labels", labelsString)
            }
        } catch (e: IOException) {
            logger.message(e.message + e.stackTrace)
            throw RuntimeException(e.message, e)
        }

    }

    private fun exportPrParticipants(service: PullRequestService, repo: RepositoryId, prNum: Int) {
        try {
            val participants = HashSet<String>()
            getParticipantsFromComments(participants, service.getComments(repo, prNum))
            val issueService = IssueService(service.client)
            getParticipantsFromComments(participants, issueService.getComments(repo, prNum))
            if (participants.isEmpty()) {
                return
            }

            exportConfigParam(build, "teamcity.build.pull_req.participants",
                    StringUtil.join(";", participants))
        } catch (e: IOException) {
            e.printStackTrace()
            logger.error(e.message)
        }

    }

    private fun getParticipantsFromComments(participants: HashSet<String>, comments: List<Comment>) {
        comments
                .map { getGitHubUser(it.user) }
                .filterNot { StringUtil.isEmptyOrSpaces(it) }
                .filterNotNullTo(participants)
    }

    @Throws(RunBuildException::class)
    private fun failBuildIfConflict(runnerParams: Map<String, String>,
                                    pr: PullRequest?) {
        if (pr == null) {
            return
        }
        val failBuildIfConflict = runnerParams[SettingsKeys.FailBuildIfConflict]

        if (!StringUtil.isEmptyOrSpaces(failBuildIfConflict) && failBuildIfConflict.equals("true", ignoreCase = true)) {
            if (!pr.isMerged && !pr.isMergeable) {
                val msg = "The pull request has conflicts " + pr.htmlUrl
                throw RunBuildException(msg)
            }
        }
    }

    @Throws(RunBuildException::class)
    private fun getHeadBranchName(pr: PullRequest?, prNum: String): String {
        if (!StringUtil.isNumber(prNum)) {
            return prNum
        }
        try {
            return if (pr != null) pr.head.ref else prNum
        } catch (ex: Exception) {
            throw RunBuildException("Unable to get head branch name for pull request " + ex.message, ex)
        }

    }

    @Throws(RunBuildException::class)
    private fun getBaseBranchName(pr: PullRequest?, prNum: String): String {
        if (!StringUtil.isNumber(prNum)) {
            return prNum
        }
        try {
            return if (pr != null) pr.base.ref else prNum
        } catch (ex: Exception) {
            throw RunBuildException("Unable to get base branch name for pull request " + ex.message, ex)
        }

    }

    @Throws(RunBuildException::class)
    private fun getPullRequest(service: PullRequestService, repo: RepositoryId, prNum: String): PullRequest {
        val pr: PullRequest
        val prNumInteger: Int
        try {
            prNumInteger = Integer.parseInt(prNum)
        } catch (e: Exception) {
            throw RunBuildException(prNum + " is not pull request number: " + e.message, e)
        }

        logger.message("Trying to get extra information of pull request " + prNum + " from "
                + repo.owner + "'s " + repo.name)
        try {
            pr = service.getPullRequest(repo, prNumInteger)
        } catch (e: IOException) {
            throw RunBuildException("unable to get pull request info: " + e.message, e)
        }

        return pr
    }

    private fun getRepositoryId(repoUrl: String): RepositoryId {
        val id = RepoInfoParser.Parse(repoUrl)
        logger.message("owner: " + id.owner + " repo: " + id.name)
        return id
    }

    private fun getRepoUrl(configParams: Map<String, String>): String {
        return configParams["vcsroot.url"]!!
    }

    private fun initService(service: GitHubService) {
        val runnerParams = runnerParameters
        val authType = runnerParams[SettingsKeys.AuthType]
        if (authType == PrExtrasConstants.SystemWideTokenAuthType) {
            val token = systemProperties[SettingsKeys.GithubToken]
            if (StringUtil.isEmptyOrSpaces(token)) {
                throw InvalidParameterException("Please set the system wide github token")
            }
            service.client.setOAuth2Token(token)
        } else if (authType == PrExtrasConstants.TokenAuthType) {
            val token = runnerParams[SettingsKeys.GithubToken]
            if (StringUtil.isEmptyOrSpaces(token)) {
                throw InvalidParameterException("Please set the github token in the build step's settings page")
            }
            service.client.setOAuth2Token(token)
        } else {
            var password: String? = runnerParams[SettingsKeys.GithubPassword]
            if (StringUtil.isEmptyOrSpaces(password)) {
                password = null
            }
            val user = runnerParams[SettingsKeys.GithubUserName]
            if (StringUtil.isNotEmpty(user)) {
                service.client.setCredentials(user, password)
            }
        }
    }

    @Throws(RunBuildException::class)
    override fun makeProgramCommandLine(): ProgramCommandLine {
        return object : ProgramCommandLine {
            @Throws(RunBuildException::class)
            override fun getExecutablePath(): String {
                return if (_isOnWindows) "cmd" else "sh"
            }

            @Throws(RunBuildException::class)
            override fun getWorkingDirectory(): String {
                return checkoutDirectory.path
            }

            @Throws(RunBuildException::class)
            override fun getArguments(): List<String> {
                val ret = Vector<String>()
                if (_isOnWindows) {
                    ret.add("/c")
                } else {
                    ret.add("-c")
                }
                ret.add("echo")
                ret.add("Branch name is " + configParameters[HeadBranchNameParamName])
                return ret
            }

            @Throws(RunBuildException::class)
            override fun getEnvironment(): Map<String, String> {
                return buildParameters.environmentVariables
            }
        }
    }

    companion object {
        private val HeadBranchNameParamName = "teamcity.build.pull_req.branch_name"
        private val BaseBranchNameParamName = "teamcity.build.pull_req.base_branch_name"

        private fun getGitHubUser(user: User?): String? {
            if (user == null) {
                return null
            }
            val login = user.login
            return if (StringUtil.isEmptyOrSpaces(login)) {
                null
            } else login
        }
    }
}
