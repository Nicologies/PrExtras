package com.nicologies.prextras

import com.nicologies.prextras.common.PrExtrasConstants
import com.nicologies.prextras.common.SettingsKeys

class SettingsBean {
    val githubToken: String
        get() = SettingsKeys.GithubToken

    val authType: String
        get() = SettingsKeys.AuthType

    val password: String
        get() = SettingsKeys.Password
    val tokenAuthType: String
        get() = PrExtrasConstants.TokenAuthType
    val usernameAuthType: String
        get() = PrExtrasConstants.UsernameAuthType
    val githubUsername: String
        get() = SettingsKeys.GithubUserName
    val githubPassword: String
        get() = SettingsKeys.GithubPassword

    val appendToBuildNum: String
        get() = SettingsKeys.AppendToBuildNum

    val systemWideTokenAuthType: String
        get() = PrExtrasConstants.SystemWideTokenAuthType
    val failBuildIfConflict: String
        get() = SettingsKeys.FailBuildIfConflict
}

