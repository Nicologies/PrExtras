package com.nicologies.prextras;

import com.nicologies.prextras.common.PrExtrasConstants;
import com.nicologies.prextras.common.SettingsKeys;
import jetbrains.buildServer.requirements.Requirement;
import jetbrains.buildServer.serverSide.PropertiesProcessor;
import jetbrains.buildServer.serverSide.RunTypeRegistry;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PrExtrasRunType extends jetbrains.buildServer.serverSide.RunType{
    private final PluginDescriptor _pluginDescriptor;
    public PrExtrasRunType(final RunTypeRegistry runTypeRegistry, final PluginDescriptor pluginDescriptor){
        _pluginDescriptor = pluginDescriptor;
        runTypeRegistry.registerRunType(this);
    }
    @NotNull
    @Override
    public String getType() {
        return PrExtrasConstants.RunnerType;
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "PullRequest Extra Info";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "A plugin to get pull request info(e.g. branch name) when building Github pull request";
    }

    @Nullable
    @Override
    public PropertiesProcessor getRunnerPropertiesProcessor() {
        return new RunTypePropertiesProcessor();
    }

    @Nullable
    @Override
    public String getEditRunnerParamsJspFilePath() {
        return _pluginDescriptor.getPluginResourcesPath("editPrExtrasParams.jsp");
    }

    @Nullable
    @Override
    public String getViewRunnerParamsJspFilePath() {
        return _pluginDescriptor.getPluginResourcesPath("viewPrExtrasParams.jsp");
    }

    @Nullable
    @Override
    public Map<String, String> getDefaultRunnerProperties() {
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put(SettingsKeys.GithubToken, "");
        parameters.put(SettingsKeys.ExportParamName, PrExtrasConstants.DefaultExportParamName);
        parameters.put(SettingsKeys.AuthType, PrExtrasConstants.TokenAuthType);
        parameters.put(SettingsKeys.AppendToBuildNum, "true");
        parameters.put(SettingsKeys.FailBuildIfConflict, "true");
        return parameters;
    }
    @NotNull
    @Override
    public List<Requirement> getRunnerSpecificRequirements(@NotNull final Map<String, String> runParameters) {
        return RequirementsUtil.getRequirements(runParameters);
    }
}
