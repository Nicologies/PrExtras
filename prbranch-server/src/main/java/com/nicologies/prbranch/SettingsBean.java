package com.nicologies.prbranch;

import com.nicologies.prbranch.common.PrBranchConstants;
import com.nicologies.prbranch.common.SettingsKeys;
import org.jetbrains.annotations.NotNull;

public class SettingsBean {
    @NotNull
    public String getGithubToken() {
        return SettingsKeys.GithubToken;
    }

    @NotNull
    public String getExportParamName(){
        return SettingsKeys.ExportParamName;
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
        return PrBranchConstants.TokenAuthType;
    }
    @NotNull
    public String getUsernameAuthType(){
        return PrBranchConstants.UsernameAuthType;
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
        return PrBranchConstants.SystemWideTokenAuthType;
    }
}

