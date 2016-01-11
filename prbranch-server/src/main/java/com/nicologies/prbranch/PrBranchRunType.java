package com.nicologies.prbranch;

import com.nicologies.prbranch.common.PrBranchConstants;
import com.nicologies.prbranch.common.SettingsKeys;
import jetbrains.buildServer.requirements.Requirement;
import jetbrains.buildServer.serverSide.PropertiesProcessor;
import jetbrains.buildServer.serverSide.RunTypeRegistry;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PrBranchRunType extends jetbrains.buildServer.serverSide.RunType{
    private final PluginDescriptor _pluginDescriptor;
    public PrBranchRunType(final RunTypeRegistry runTypeRegistry, final PluginDescriptor pluginDescriptor){
        _pluginDescriptor = pluginDescriptor;
        runTypeRegistry.registerRunType(this);
    }
    @NotNull
    @Override
    public String getType() {
        return PrBranchConstants.RunnerType;
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "PR2BranchName";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "A plugin to get branch name when building Github pull request";
    }

    @Nullable
    @Override
    public PropertiesProcessor getRunnerPropertiesProcessor() {
        return new RunTypePropertiesProcessor();
    }

    @Nullable
    @Override
    public String getEditRunnerParamsJspFilePath() {
        return _pluginDescriptor.getPluginResourcesPath("editPrBranchParams.jsp");
    }

    @Nullable
    @Override
    public String getViewRunnerParamsJspFilePath() {
        return _pluginDescriptor.getPluginResourcesPath("viewPrBranchParams.jsp");
    }

    @Nullable
    @Override
    public Map<String, String> getDefaultRunnerProperties() {
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put(SettingsKeys.GithubToken, "");
        parameters.put(SettingsKeys.ExportParamName, PrBranchConstants.DefaultExportParamName);
        parameters.put(SettingsKeys.AuthType, PrBranchConstants.TokenAuthType);
        parameters.put(SettingsKeys.AppendToBuildNum, "true");
        return parameters;
    }
    @NotNull
    @Override
    public List<Requirement> getRunnerSpecificRequirements(@NotNull final Map<String, String> runParameters) {
        return RequirementsUtil.getRequirements(runParameters);
    }
}
