package com.nicologies.prbranch.common;

import java.io.File;
import java.net.URISyntaxException;

public class PathUtils {
    public static String GetExecutionPath() throws URISyntaxException {
        File jar = new File(PathUtils.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
        return jar.getParent();
    }
}
