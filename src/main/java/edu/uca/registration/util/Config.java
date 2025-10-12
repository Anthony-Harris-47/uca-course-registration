package edu.uca.registration.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


//Configuration management utility and reads from environment variables, system properties, or application.properties file.

public class Config {
    private static final String CONFIG_FILE = "application.properties";
    private final Properties properties;

    public Config() {
        this.properties = new Properties();
        loadDefaults();
        loadFromFile();
    }

    private void loadDefaults() {
        properties.setProperty("students.file", "students.csv");
        properties.setProperty("courses.file", "courses.csv");
        properties.setProperty("enrollments.file", "enrollments.csv");
        properties.setProperty("log.level", "INFO");
    }

    private void loadFromFile() {
        File file = new File(CONFIG_FILE);
        if (file.exists()) {
            try (InputStream input = new FileInputStream(file)) {
                properties.load(input);
            } catch (IOException e) {
                System.err.println("Warning: Could not load " + CONFIG_FILE + ": " + e.getMessage());
            }
        }
    }

    public String get(String key) {
        // Priority: Environment Variable --- System Property -- Config File - Default
        String envKey = key.replace('.', '_').toUpperCase();
        String value = System.getenv(envKey);
        if (value != null) return value;
        
        value = System.getProperty(key);
        if (value != null) return value;
        
        return properties.getProperty(key);
    }

    public String get(String key, String defaultValue) {
        String value = get(key);
        return value != null ? value : defaultValue;
    }

    public int getInt(String key, int defaultValue) {
        String value = get(key);
        if (value == null) return defaultValue;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public String getStudentsFile() {
        return get("students.file");
    }

    public String getCoursesFile() {
        return get("courses.file");
    }

    public String getEnrollmentsFile() {
        return get("enrollments.file");
    }

    public Logger.LogLevel getLogLevel() {
        String level = get("log.level", "INFO").toUpperCase();
        try {
            return Logger.LogLevel.valueOf(level);
        } catch (IllegalArgumentException e) {
            return Logger.LogLevel.INFO;
        }
    }
}