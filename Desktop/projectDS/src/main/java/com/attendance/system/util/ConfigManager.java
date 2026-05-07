package com.attendance.system.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Configuration manager for loading and managing application properties.
 */
public class ConfigManager {
    private static ConfigManager instance;
    private Properties properties;
    private Properties testProperties;
    
    private ConfigManager() {
        loadProperties();
    }
    
    public static synchronized ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
        }
        return instance;
    }
    
    private void loadProperties() {
        properties = new Properties();
        testProperties = new Properties();
        
        // Load main properties
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("database.properties")) {
            if (is != null) {
                properties.load(is);
            }
        } catch (IOException e) {
            System.err.println("Warning: Could not load database.properties: " + e.getMessage());
        }
        
        // Load server properties
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("server.properties")) {
            if (is != null) {
                properties.load(is);
            }
        } catch (IOException e) {
            System.err.println("Warning: Could not load server.properties: " + e.getMessage());
        }
        
        // Load test properties
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("test-database.properties")) {
            if (is != null) {
                testProperties.load(is);
            }
        } catch (IOException e) {
            System.err.println("Warning: Could not load test-database.properties: " + e.getMessage());
        }
    }
    
    /**
     * Gets a property value.
     * @param key the property key
     * @return the property value or null if not found
     */
    public String getProperty(String key) {
        return properties.getProperty(key);
    }
    
    /**
     * Gets a property value with a default.
     * @param key the property key
     * @param defaultValue the default value if key not found
     * @return the property value or default value
     */
    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
    
    /**
     * Gets a test property value.
     * @param key the property key
     * @return the test property value or null if not found
     */
    public String getTestProperty(String key) {
        return testProperties.getProperty(key, properties.getProperty(key));
    }
    
    /**
     * Gets an integer property value.
     * @param key the property key
     * @param defaultValue the default value if key not found or invalid
     * @return the integer property value
     */
    public int getIntProperty(String key, int defaultValue) {
        String value = getProperty(key);
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                System.err.println("Warning: Invalid integer value for property " + key + ": " + value);
            }
        }
        return defaultValue;
    }
    
    /**
     * Gets a boolean property value.
     * @param key the property key
     * @param defaultValue the default value if key not found
     * @return the boolean property value
     */
    public boolean getBooleanProperty(String key, boolean defaultValue) {
        String value = getProperty(key);
        if (value != null) {
            return Boolean.parseBoolean(value);
        }
        return defaultValue;
    }
    
    /**
     * Gets a long property value.
     * @param key the property key
     * @param defaultValue the default value if key not found or invalid
     * @return the long property value
     */
    public long getLongProperty(String key, long defaultValue) {
        String value = getProperty(key);
        if (value != null) {
            try {
                return Long.parseLong(value);
            } catch (NumberFormatException e) {
                System.err.println("Warning: Invalid long value for property " + key + ": " + value);
            }
        }
        return defaultValue;
    }
    
    // Database configuration getters
    public String getDatabaseUrl() {
        return getProperty("db.url");
    }
    
    public String getDatabaseUsername() {
        return getProperty("db.username");
    }
    
    public String getDatabasePassword() {
        return getProperty("db.password");
    }
    
    public String getDatabaseDriver() {
        return getProperty("db.driver", "com.mysql.cj.jdbc.Driver");
    }
    
    public int getMaxPoolSize() {
        return getIntProperty("db.pool.maximumPoolSize", 20);
    }
    
    public int getMinIdleConnections() {
        return getIntProperty("db.pool.minimumIdle", 5);
    }
    
    public long getConnectionTimeout() {
        return getLongProperty("db.pool.connectionTimeout", 30000);
    }
    
    public long getIdleTimeout() {
        return getLongProperty("db.pool.idleTimeout", 600000);
    }
    
    public long getMaxLifetime() {
        return getLongProperty("db.pool.maxLifetime", 1800000);
    }
    
    // Application configuration getters
    public long getSessionTimeout() {
        return getLongProperty("app.session.timeout", 1800000); // 30 minutes
    }
    
    public int getRmiPort() {
        return getIntProperty("app.rmi.port", 1099);
    }
    
    public String getRmiHost() {
        return getProperty("app.rmi.host", "localhost");
    }
    
    public String getBackupSchedule() {
        return getProperty("app.backup.schedule", "0 2 * * *");
    }
    
    public int getNotificationBatchSize() {
        return getIntProperty("app.notification.batch.size", 100);
    }
    
    // Test configuration getters
    public String getTestDatabaseUrl() {
        return getTestProperty("db.url");
    }
    
    public String getTestDatabaseUsername() {
        return getTestProperty("db.username");
    }
    
    public String getTestDatabasePassword() {
        return getTestProperty("db.password");
    }
    
    /**
     * Reloads the configuration properties from files.
     */
    public void reload() {
        loadProperties();
    }

    // Static convenience methods for server usage
    
    /**
     * Gets a string property value with default.
     * @param key the property key
     * @param defaultValue the default value
     * @return the property value or default
     */
    public static String getString(String key, String defaultValue) {
        return getInstance().getProperty(key, defaultValue);
    }
    
    /**
     * Gets an integer property value with default.
     * @param key the property key
     * @param defaultValue the default value
     * @return the property value or default
     */
    public static int getInt(String key, int defaultValue) {
        return getInstance().getIntProperty(key, defaultValue);
    }
    
    /**
     * Gets a boolean property value with default.
     * @param key the property key
     * @param defaultValue the default value
     * @return the property value or default
     */
    public static boolean getBoolean(String key, boolean defaultValue) {
        return getInstance().getBooleanProperty(key, defaultValue);
    }
    
    /**
     * Gets a long property value with default.
     * @param key the property key
     * @param defaultValue the default value
     * @return the property value or default
     */
    public static long getLong(String key, long defaultValue) {
        return getInstance().getLongProperty(key, defaultValue);
    }
}
