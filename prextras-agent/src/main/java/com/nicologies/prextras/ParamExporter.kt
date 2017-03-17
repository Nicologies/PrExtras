package com.nicologies.prextras

import jetbrains.buildServer.agent.AgentRunningBuild

object ParamExporter{
    @JvmStatic
    fun exportConfigParam(build: AgentRunningBuild, key: String, value: String){
        build.buildLogger.message("Exporting config param '$key' with value of '$value'")
        build.addSharedConfigParameter(key, value)
    }
}
