package com.oass.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Loads ~/oass-data/config.properties at startup.
 * AppStartListener calls Config.load(); every other class calls Config.get().
 * Syllabus: Section D (servlet listener, application-scope config).
 */
public final class Config {

    private static final Logger LOG = Logger.getLogger(Config.class.getName());
    private static final Properties PROPS = new Properties();

    private Config() {}

    /** Called once by AppStartListener at application startup. */
    public static void load() {
        String home = System.getProperty("user.home");
        String path = home + "/oass-data/config.properties";
        try (FileInputStream fis = new FileInputStream(path)) {
            PROPS.load(fis);
            LOG.info("Loaded config from " + path);
        } catch (IOException e) {
            String msg = "FATAL: Cannot read " + path + ". "
                    + "Create it from config.properties.example and restart.";
            LOG.severe(msg);
            throw new IllegalStateException(msg, e);
        }
    }

    /** Returns a required config value; throws if the key is missing. */
    public static String get(String key) {
        String value = PROPS.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing required config key: " + key);
        }
        return value.trim();
    }

    /** Returns an optional config value, or the provided default. */
    public static String get(String key, String defaultValue) {
        String value = PROPS.getProperty(key);
        return (value == null || value.isBlank()) ? defaultValue : value.trim();
    }
}
