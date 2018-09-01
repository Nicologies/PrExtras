package com.nicologies.prextras

import jetbrains.buildServer.serverSide.InvalidProperty
import jetbrains.buildServer.serverSide.PropertiesProcessor
import java.util.Vector

class RunTypePropertiesProcessor : PropertiesProcessor {
    override fun process(properties: Map<String, String>): Collection<InvalidProperty> {
        return Vector()
    }
}