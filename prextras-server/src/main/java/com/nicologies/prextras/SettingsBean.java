package com.nicologies.prextras;

import com.nicologies.prextras.common.PrExtrasConstants;
import com.nicologies.prextras.common.SettingsKeys;
import org.jetbrains.annotations.NotNull;

public class SettingsBean {
    @NotNull
    public String getGithubToken() {
        return SettingsKeys.GithubToken;
    }

    @NotNull
    public String getAuthType(){
        return SettingsKeys.AuthType;
    }

    @NotNull
    public String getPassword(){
        return SettingsKeys.Password;
    }
    @NotNull
    public String getTokenAuthType(){
        return PrExtrasConstants.TokenAuthType;
    }
    @NotNull
    public String getUsernameAuthType(){
        return PrExtrasConstants.UsernameAuthType;
    }
    @NotNull
    public String getGithubUsername(){
        return SettingsKeys.GithubUserName;
    }
    @NotNull
    public String getGithubPassword(){
        return SettingsKeys.GithubPassword;
    }

    @NotNull
    public String getAppendToBuildNum(){
        return SettingsKeys.AppendToBuildNum;
    }

    @NotNull
    public String getSystemWideTokenAuthType(){
        return PrExtrasConstants.SystemWideTokenAuthType;
    }
    @NotNull
    public String getFailBuildIfConflict(){
        return SettingsKeys.FailBuildIfConflict;
    }
}

