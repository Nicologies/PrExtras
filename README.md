[![Github Releases (by Release)](https://img.shields.io/github/downloads/Nicologies/PrExtras/latest/total.svg?maxAge=86400)](https://github.com/PrExtras/PrExtras/releases/latest)

[![Build Status](https://travis-ci.org/Nicologies/PrExtras.svg?branch=master)](https://travis-ci.org/Nicologies/PrExtras)


# PrExtras

A teamcity plugin to get extra information of the GitHub pull request being built. Such as the 

- Branch name, and it will also be displayed as part of the TeamCity build number label

![Image of branch name in build number label](/BranchNameOfPr.png)

- Base branch name
- Author, assignee, participants who commented on the pull request
- Pull request URL
- Labels

## Install

Copy the [plugin](https://github.com/Nicologies/PrExtras/releases/latest) to teamcity's plugins folder and restart the server

## Configuration

Add a build step with run type of `PullRequest Extra Info` to your build configuration

![Image of Adding a build step](/AddABuildStep.png)

### Authorization

- `Token`
See GitHub's help page for how to [create a personal access token](https://help.github.com/articles/creating-an-access-token-for-command-line-use/)

- `GitHub Account` (highly not recommended)
Just the username and password

- `System Wide Token`
This is also the personal access token.
But instead of adding it in a specific build configuration, you can define `system.prextras.github_token` parameter that can be shared with other build conifgurations

### Build label

Tick the `Append branch name to build num label` to display the branch name as part of the TeamCity build number label instead of the pull request number

### Fail when conflicts

Tick the `Fail the build if the pull request has conflicts` to stop building a pull request that has conflicts.

## Outputs

it will output the follow parameters

- `teamcity.build.pull_req.branch_name`: the branch name of the pull request or branch being built
- `teamcity.build.pull_req.base_branch_name`: the base branch name of the pull request or branch being built
- `teamcity.build.pull_req.is_pull_req`: true if building a pull request, false otherwise
- `teamcity.build.pull_req.number`: the pull request number if it's a pull request
- `teamcity.build.pull_req.author_email`: the pull request author's email
- `teamcity.build.pull_req.author`: the author
- `teamcity.build.pull_req.url`: the pull request url, e.g. https://github.com/owner/repo/pull/123
- `teamcity.build.pull_req.assignee`: the assignee
- `teamcity.build.pull_req.assignee_email`:assignee's email
- `teamcity.build.pull_req.participants`: paritcipants separated by `;` who commented on the pull request (nothing to do with commits)
- `teamcity.build.pull_req.is_merged`: indicate if the pull request has been merged or not
- `teamcity.build.pull_req.is_closed`: indicate if the pull request has been closed or not
- `teamcity.build.pull_req.labels`: labels of the pull request separated by `;`

## GitHub Enterprise

For github enterprise, you will need to define a system property;therefore this plugin can know where is the API server.

The property name is `system.prextras.github_api_url` and the value should be the API URL of your GitHub Enterprise server, for example: `https://<githubserver.com>/api/v3`. 

By default the plugin will use `https`, if your server doesn't have `https` support, then you need to explicitly prefix the URL with `http`.


## Integrate with other plugins

When using this plugin with [Hedwig](https://github.com/Nicologies/Hedwig) and [User Mapping](https://github.com/Nicologies/usermapping), you will be able to send Slack/HipChat notification of the build failures to the pull request author, assignee, participants and the guy who triggerred the build. 
