package com.nicologies.prextras

import jetbrains.buildServer.util.StringUtil

object PrNumParser {
    fun parse(teamcityBuildBranch: String) : Pair<String, Boolean>{
        var replaced = Regex("""(^|.*/)(\d+)($|/(?:head|merge)$)""", setOf(RegexOption.IGNORE_CASE))
                .replace(teamcityBuildBranch, "$2")
        if(StringUtil.isNumber(replaced)) {
            return Pair(replaced, true)
        }

        return Pair(teamcityBuildBranch, false)
    }
}