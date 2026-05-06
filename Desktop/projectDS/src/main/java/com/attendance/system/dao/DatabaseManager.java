package com.attendance.system.dao;

import com.attendance.system.exception.DatabaseException;
import com.attendance.system.util.ConfigManager;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Database connection manager using HikariCP connection pooling.
 */
public class DatabaseManager {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseManager.class);
    
    private static DatabaseManager instance;
    private HikariDataSource dataSource;
    private final ConfigManager configManager;
    private boolean isTestMode = false;
    
    private DatabaseManager() {
        this.configManager = ConfigManager.getInstance();
        initializeConnectionPool();
    }
    
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }
    
    /**
     * Creates a test instance with test database configuration.
     * @return test database manager instance
     */
    public static synchronized DatabaseManager getTestInstance() {
        DatabaseManager testInstance = new DatabaseManager();
        testInstance.isTestMode = true;
        testInstance.initializeConnectionPool();
        return testInstance;
    }
    
    /**
     * Initializes the HikariCP connection pool.
     */
    private void initializeConnectionPool() {
        try {
            HikariConfig config = new HikariConfig();
            
            if (isTestMode) {
                config.setJdbcUrl(configManager.getTestDatabaseUrl());
                config.setUsername(configManager.getTestDatabaseUsername());
                config.setPassword(configManager.getTestDatabasePassword());
                config.setMaximumPoolSize(5);
                config.setMinimumIdle(2);
            } else {
                config.setJdbcUrl(configManager.getDatabaseUrl());
                config.setUsername(configManager.getDatabaseUsername());
                config.setPassword(configManager.getDatabasePassword());
                config.setMaximumPoolSize(configManager.getMaxPoolSize());
                config.setMinimumIdle(configManager.getMinIdleConnections());
            }
            
            config.setDriverClassName(configManager.getDatabaseDriver());
            config.setConnectionTimeout(configManager.getConnectionTimeout());
            config.setIdleTimeout(configManager.getIdleTimeout());
            config.setMaxLifetime(configManager.getMaxLifetime());
            config.setLeakDetectionThreshold(60000); // 1 minute
            
            // Connection pool settings
            config.setConnectionTestQuery("SELECT 1");
            config.setAutoCommit(false); // We'll manage transactions manually
            
            // Performance settings
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            config.addDataSourceProperty("useServerPrepStmts", "true");
            config.addDataSourceProperty("useLocalSessionState", "true");
            config.addDataSourceProperty("rewriteBatchedStatements", "true");
            config.addDataSourceProperty("cacheResultSetMetadata", "true");
            config.addDataSourceProperty("cacheServerConfiguration", "true");
            config.addDataSourceProperty("elideSetAutoCommits", "true");
            config.addDataSourceProperty("maintainTimeStats", "false");
            
            this.dataSource = new HikariDataSource(config);
            
            logger.info("Database connection pool initialized successfully");
            
            // Test the connection
            if (!testConnection()) {
                throw new DatabaseException("Failed to establish database connection");
            }
            
        } catch (Exception e) {
            logger.error("Failed to initialize database connection pool", e);
            throw new RuntimeException("Database initialization failed", e);
        }
    }
    
    /**
     * Gets a database connection from the pool.
     * @return database connection
     * @throws DatabaseException if connection cannot be obtained
     */
    public Connection getConnection() throws DatabaseException {
        try {
            if (dataSource == null || dataSource.isClosed()) {
                throw new DatabaseException("Database connection pool is not available");
            }
            
            Connection connection = dataSource.getConnection();
            if (connection == null) {
                throw new DatabaseException("Failed to obtain database connection from pool");
            }
            
            return connection;
            
        } catch (SQLException e) {
            logger.error("Failed to get database connection", e);
            throw DatabaseException.connectionFailed(e);
        }
    }
    
    /**
     * Tests the database connection.
     * @return true if connection is successful
     */
    public boolean testConnection() {
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {
            
            statement.executeQuery("SELECT 1");
            logger.debug("Database connection test successful");
            return true;
            
        } catch (Exception e) {
            logger.error("Database connection test failed", e);
            return false;
        }
    }
    
    /**
     * Closes the connection pool and releases all resources.
     */
    public void closeConnectionPool() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            logger.info("Database connection pool closed");
        }
    }
    
    /**
     * Gets connection pool statistics.
     * @return connection pool info string
     */
    public String getPoolStats() {
        if (dataSource == null) {
            return "Connection pool not initialized";
        }
        
        return String.format(
            "Pool Stats - Active: %d, Idle: %d, Total: %d, Waiting: %d",
            dataSource.getHikariPoolMXBean().getActiveConnections(),
            dataSource.getHikariPoolMXBean().getIdleConnections(),
            dataSource.getHikariPoolMXBean().getTotalConnections(),
            dataSource.getHikariPoolMXBean().getThreadsAwaitingConnection()
        );
    }
    
    /**
     * Gets detailed connection pool statistics as a map.
     * @return map containing detailed pool statistics
     */
    public java.util.Map<String, Object> getDetailedPoolStats() {
        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        
        if (dataSource == null) {
            stats.put("status", "Connection pool not initialized");
            return stats;
        }
        
        com.zaxxer.hikari.HikariPoolMXBean poolMXBean = dataSource.getHikariPoolMXBean();
        
        stats.put("activeConnections", poolMXBean.getActiveConnections());
        stats.put("idleConnections", poolMXBean.getIdleConnections());
        stats.put("totalConnections", poolMXBean.getTotalConnections());
        stats.put("threadsAwaitingConnection", poolMXBean.getThreadsAwaitingConnection());
        stats.put("maxPoolSize", poolMXBean.getMaximumPoolSize());
        stats.put("minimumIdle", poolMXBean.getMinimumIdle());
        stats.put("connectionTimeout", "30 seconds");
        stats.put("idleTimeout", "10 minutes");
        stats.put("maxLifetime", "30 minutes");
        stats.put("poolHealthy", isHealthy());
        
        return stats;
    }
    
    /**
     * Gets connection pool utilization percentage.
     * @return utilization percentage (0-100)
     */
    public double getPoolUtilization() {
        if (dataSource == null) {
            return 0;
        }
        
        com.zaxxer.hikari.HikariPoolMXBean poolMXBean = dataSource.getHikariPoolMXBean();
        int activeConnections = poolMXBean.getActiveConnections();
        int totalConnections = poolMXBean.getTotalConnections();
        
        if (totalConnections == 0) {
            return 0;
        }
        
        return (double) activeConnections / totalConnections * 100;
    }
    
    /**
     * Executes a transaction with automatic rollback on failure.
     * @param transaction the transaction to execute
     * @return the result of the transaction
     * @throws DatabaseException if transaction fails
     */
    public <T> T executeTransaction(DatabaseTransaction<T> transaction) throws DatabaseException {
        Connection connection = null;
        try {
            connection = getConnection();
            connection.setAutoCommit(false);
            
            T result = transaction.execute(connection);
            
            connection.commit();
            logger.debug("Transaction committed successfully");
            
            return result;
            
        } catch (Exception e) {
            if (connection != null) {
                try {
                    connection.rollback();
                    logger.debug("Transaction rolled back due to error");
                } catch (SQLException rollbackException) {
                    logger.error("Failed to rollback transaction", rollbackException);
                }
            }
            
            logger.error("Transaction failed", e);
            throw DatabaseException.transactionFailed(e);
            
        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                    connection.close();
                } catch (SQLException e) {
                    logger.error("Failed to close connection", e);
                }
            }
        }
    }
    
    /**
     * Functional interface for database transactions.
     */
    @FunctionalInterface
    public interface DatabaseTransaction<T> {
        T execute(Connection connection) throws Exception;
    }
    
    /**
     * Checks if the database manager is in test mode.
     * @return true if in test mode
     */
    public boolean isTestMode() {
        return isTestMode;
    }
    
    /**
     * Gets the current number of active connections.
     * @return number of active connections
     */
    public int getActiveConnections() {
        return dataSource != null ? dataSource.getHikariPoolMXBean().getActiveConnections() : 0;
    }
    
    /**
     * Gets the current number of idle connections.
     * @return number of idle connections
     */
    public int getIdleConnections() {
        return dataSource != null ? dataSource.getHikariPoolMXBean().getIdleConnections() : 0;
    }
    
    /**
     * Gets the total number of connections in the pool.
     * @return total number of connections
     */
    public int getTotalConnections() {
        return dataSource != null ? dataSource.getHikariPoolMXBean().getTotalConnections() : 0;
    }
    
    /**
     * Checks if the connection pool is healthy.
     * @return true if pool is healthy
     */
    public boolean isHealthy() {
        return dataSource != null && !dataSource.isClosed() && testConnection();
    }
}