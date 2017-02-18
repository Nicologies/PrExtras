# PrExtras
A teamcity plugin to get extra information of the GitHub pull request being built.

## Install

Copy the [plugin](https://github.com/Nicologies/PrExtras/releases/latest) to teamcity's plugins folder and restart the server

## Configuration

Add a build step with run type of `PullRequest Extra Info` to your build configuration

### Authorization

- `Token`
See GitHub's help page for how to [create a personal access token](https://help.github.com/articles/creating-an-access-token-for-command-line-use/)

- `GitHub Account` (highly not recommended)
Just the username and password

- `System Wide Token`
This is also the personal access token.
But instead of put it in a specific build configuration, you can define `system.prextras.github_token` parameter that can be shared with amoung multiple build conifgurations

### Build label

Tick the `Append branch name to build num label` to display the branch name instead of pull request number

### Fail when conflicts

Tick the `Fail the build if the pull request has conflict` to stop building a pull request that has conflicts.

## Outputs

it will output the follow parameters

- `teamcity.build.pull_req.branch_name`: the branch name of the pull request or branch being built
- `teamcity.build.pull_req.is_pull_req`: boolean to indicate whether a pull request is being built or not
- `teamcity.build.pull_req.author_email`: pull request author's email
- `teamcity.build.pull_req.author`: the author
- `teamcity.build.pull_req.url`: the pull request url, e.g. https://github.com/owner/repo/pull/123
- `teamcity.build.pull_req.assignee`: the assignee
- `teamcity.build.pull_req.assignee_email`:assignee's email
- `teamcity.build.pull_req.participants`: paritcipants separated by `;` who commented on the pull request (nothing to do with commits)
- `teamcity.build.pull_req.is_merged`: indicate if the pull request is merged or not
- `teamcity.build.pull_req.is_closed`: indicate if the pull request is closed or not
- `teamcity.build.pull_req.labels`: labels separated by `;`

