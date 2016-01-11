
package com.nicologies.prbranch;

import com.nicologies.prbranch.common.PrBranchConstants;
import com.nicologies.prbranch.common.SettingsKeys;
import jetbrains.buildServer.requirements.Requirement;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RequirementsUtil {
    @NotNull
    public static List<Requirement> getRequirements(final Map<String, String> runParameters) {
        final List<Requirement> list = new ArrayList<Requirement>();
        final String authType = runParameters.get(SettingsKeys.AuthType);
        if (authType != null && authType.equals(PrBranchConstants.SystemWideTokenAuthType)) {
            // seems teamcity cannot find the system wide property even if defined
            // list.add(new Requirement("system." + SettingsKeys.GithubToken, null, RequirementType.EXISTS));
        }
        return list;
    }
}
