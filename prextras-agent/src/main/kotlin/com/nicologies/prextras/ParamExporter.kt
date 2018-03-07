package com.nicologies.prextras

import jetbrains.buildServer.agent.AgentRunningBuild

object ParamExporter{
    @JvmStatic
    fun exportConfigParam(build: AgentRunningBuild, key: String, value: String?){
        var safeValue = value?:""
        build.buildLogger.message("Exporting config param '$key' with value of '$safeValue'")
        build.addSharedConfigParameter(key, safeValue)
    }
}
