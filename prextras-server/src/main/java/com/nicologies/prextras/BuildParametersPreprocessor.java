package com.nicologies.prextras;

import com.nicologies.prextras.common.PrExtrasConstants;
import jetbrains.buildServer.serverSide.ParametersPreprocessor;
import jetbrains.buildServer.serverSide.SRunningBuild;
import jetbrains.buildServer.serverSide.TriggeredBy;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class BuildParametersPreprocessor implements ParametersPreprocessor {
    @Override
    public void fixRunBuildParameters(@NotNull SRunningBuild build, @NotNull Map<String, String> runParameters,
                                      @NotNull Map<String, String> buildParams) {
        TriggeredBy triggeredBy = build.getTriggeredBy();
        if(triggeredBy.isTriggeredByUser()){
            buildParams.put("teamcity.build.triggered_by.mapped_user",
                    MapUser(triggeredBy.getUser().getUsername(), buildParams));
        }
    }

    private static String MapUser(String name, Map<String, String> configParams) {
        String mappedName = configParams.get(PrExtrasConstants.PrefixOfUserMapping + name);
        if(StringUtil.isEmptyOrSpaces(mappedName)){
            return name;
        }else{
            return mappedName;
        }
    }
}
