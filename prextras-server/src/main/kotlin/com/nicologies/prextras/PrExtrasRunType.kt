package com.nicologies.prextras

import com.nicologies.prextras.common.PrExtrasConstants
import com.nicologies.prextras.common.SettingsKeys
import jetbrains.buildServer.requirements.Requirement
import jetbrains.buildServer.serverSide.PropertiesProcessor
import jetbrains.buildServer.serverSide.RunTypeRegistry
import jetbrains.buildServer.web.openapi.PluginDescriptor

import java.util.HashMap

class PrExtrasRunType(runTypeRegistry: RunTypeRegistry, private val _pluginDescriptor: PluginDescriptor) : jetbrains.buildServer.serverSide.RunType() {
    init {
        runTypeRegistry.registerRunType(this)
    }

    override fun getType(): String {
        return PrExtrasConstants.RunnerType
    }

    override fun getDisplayName(): String {
        return "PullRequest Extra Info"
    }

    override fun getDescription(): String {
        return "A plugin to get pull request info(e.g. branch name) when building Github pull request"
    }

    override fun getRunnerPropertiesProcessor(): PropertiesProcessor? {
        return RunTypePropertiesProcessor()
    }

    override fun getEditRunnerParamsJspFilePath(): String? {
        return _pluginDescriptor.getPluginResourcesPath("editPrExtrasParams.jsp")
    }

    override fun getViewRunnerParamsJspFilePath(): String? {
        return _pluginDescriptor.getPluginResourcesPath("viewPrExtrasParams.jsp")
    }

    override fun getDefaultRunnerProperties(): Map<String, String>? {
        val parameters = HashMap<String, String>()
        parameters.put(SettingsKeys.GithubToken, "")
        parameters.put(SettingsKeys.AuthType, PrExtrasConstants.TokenAuthType)
        parameters.put(SettingsKeys.AppendToBuildNum, "true")
        parameters.put(SettingsKeys.FailBuildIfConflict, "true")
        return parameters
    }

    override fun getRunnerSpecificRequirements(runParameters: Map<String, String>): List<Requirement> {
        return RequirementsUtil.getRequirements(runParameters)
    }
}
