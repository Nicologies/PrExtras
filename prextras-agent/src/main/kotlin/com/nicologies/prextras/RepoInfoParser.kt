package com.nicologies.prextras

import org.eclipse.egit.github.core.RepositoryId

object RepoInfoParser {
    @JvmStatic
    fun Parse(repoUrl: String): RepositoryId {
        val regex = Regex("((ssh://(.+?@)?)|(git@)|(http(s)?://(.+?@)?)).+?(:|/)(.+?)/(.+?)(\\.git)?(/)?")
        val matches = regex.matchEntire(repoUrl)
        return RepositoryId(matches?.groups?.get(9)?.value, matches?.groups?.get(10)?.value)
    }
}
