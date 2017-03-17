package com.nicologies.prextras

import com.nicologies.prextras.common.SettingsKeys
import jetbrains.buildServer.util.StringUtil
import java.net.URL


object GitHubApiUrlHelper{
    fun getURL(systemProperties: Map<String, String>) : URL {
        var githubApiUrl = systemProperties.get(SettingsKeys.GitHubApiUrl)
        if (StringUtil.isEmptyOrSpaces(githubApiUrl)) {
            githubApiUrl = "api.github.com"
        }
        if (!githubApiUrl!!.toLowerCase().startsWith("http")) {
            githubApiUrl = "https://" + githubApiUrl
        }
        return URL(githubApiUrl)
    }
}
