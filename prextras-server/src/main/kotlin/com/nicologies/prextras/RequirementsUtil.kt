package com.nicologies.prextras

import com.nicologies.prextras.common.PrExtrasConstants
import com.nicologies.prextras.common.SettingsKeys
import jetbrains.buildServer.requirements.Requirement

import java.util.ArrayList

object RequirementsUtil {
    fun getRequirements(runParameters: Map<String, String>): List<Requirement> {
        val list = ArrayList<Requirement>()
        val authType = runParameters[SettingsKeys.AuthType]
        if (authType != null && authType == PrExtrasConstants.SystemWideTokenAuthType) {
            // seems teamcity cannot find the system wide property even if defined
            // list.add(new Requirement("system." + SettingsKeys.GithubToken, null, RequirementType.EXISTS));
        }
        return list
    }
}
