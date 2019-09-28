package com.apploidxxx.heliosrestapispring.util;


import java.io.IOException;
import java.util.Properties;

/**
 * @author Arthur Kupriyanov
 */
public class PropertyManager {

    /**
     * Getting property from System env
     *
     * If property doesn't exist - gets property from local properties file  : "local_env.properties"
     * @param property key of property
     * @return property value or null if property doesn't exist both of system env and local properties
     * @throws IOException exception while trying read local properties file
     */
    public static String getProperty(String property) throws IOException {
        String prop = System.getenv(property);
        if (prop == null) {
            Properties properties = new Properties();
            properties.load(PropertyManager.class.getClassLoader().getResourceAsStream("local_env.properties"));
            return properties.getProperty(property);
        } else {
            return prop;
        }
    }
}
