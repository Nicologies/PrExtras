package com.nicologies.prextras;

import jetbrains.buildServer.serverSide.InvalidProperty;
import jetbrains.buildServer.serverSide.PropertiesProcessor;

import java.util.Collection;
import java.util.Map;
import java.util.Vector;

public class RunTypePropertiesProcessor implements PropertiesProcessor {
    public Collection<InvalidProperty> process(Map<String, String> properties) {
        return new Vector<InvalidProperty>();
    }
}
