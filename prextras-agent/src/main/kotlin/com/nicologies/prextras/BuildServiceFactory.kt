package com.nicologies.prextras

import com.nicologies.prextras.common.PrExtrasConstants
import jetbrains.buildServer.agent.AgentBuildRunnerInfo
import jetbrains.buildServer.agent.BuildAgentConfiguration
import jetbrains.buildServer.agent.runner.CommandLineBuildService
import jetbrains.buildServer.agent.runner.CommandLineBuildServiceFactory

class BuildServiceFactory : CommandLineBuildServiceFactory, AgentBuildRunnerInfo {

    override fun getType(): String {
        return PrExtrasConstants.RunnerType
    }

    override fun canRun(agentConfiguration: BuildAgentConfiguration): Boolean {
        return true
    }


    override fun createService(): CommandLineBuildService {
        return BuildService()
    }

    override fun getBuildRunnerInfo(): AgentBuildRunnerInfo {
        return this
    }
}