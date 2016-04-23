package com.nicologies.prextras;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.ServerPaths;
import jetbrains.buildServer.serverSide.parameters.AbstractBuildParametersProvider;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import com.nicologies.prextras.common.PrExtrasConstants;

public class UserMappingProvider extends AbstractBuildParametersProvider {
    private ServerPaths _serverPaths;

    public UserMappingProvider(ServerPaths serverPaths) {
        this._serverPaths = serverPaths;
    }

    @NotNull
    public Map<String, String> getParameters(@NotNull SBuild build, boolean emulationMode) {
        Map parameters = super.getParameters(build, emulationMode);
        File configDir = new File(this._serverPaths.getPluginDataDirectory(), "prextras");
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
        File configFile = new File(configDir, "usermapping.txt");
        if (configFile.exists()) {
            try {
                List<String> allLines = Files.readAllLines(configFile.toPath(), StandardCharsets.UTF_8);
                for (String line : allLines) {
                    if(line.startsWith("--") || line.startsWith("//") || !line.contains("=>")){
                        continue;
                    }
                    String[] split = line.split("=>");
                    if(split.length != 2){
                        continue;
                    }
                    String orgName = split[0].trim();
                    String newName = split[1].trim();
                    if(StringUtil.isEmpty(orgName) || StringUtil.isEmpty(newName)){
                        continue;
                    }
                    parameters.put(PrExtrasConstants.PrefixOfUserMapping + orgName, newName);
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        return parameters;
    }
}

