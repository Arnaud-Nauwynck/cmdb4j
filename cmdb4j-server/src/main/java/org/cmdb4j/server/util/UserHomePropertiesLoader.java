package org.cmdb4j.server.util;

import java.io.File;
import java.util.Properties;

public class UserHomePropertiesLoader {

    public static Properties doLoadProperties(String propFileName) {
        File file = resolveHomeFile(propFileName);
        if (! file.exists()) {
            throw new RuntimeException("File not found: " + file);
        }
        return PropertiesUtils.readPropertiesFile(file);
    }

    public static File resolveHomeFile(String propFileName) {
        String fileName = propFileName;
        if (fileName.startsWith("~/")) {
            fileName = System.getProperty("user.home") + fileName.substring(1);
        }
        File file = new File(fileName);
        return file;
    }

}
