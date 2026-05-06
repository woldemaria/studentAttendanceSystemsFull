package com.attendance.system.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DatabaseManager connection pool monitoring.
 */
public class DatabaseManagerTest {
    
    private DatabaseManager databaseManager;
    
    @BeforeEach
    public void setUp() {
        databaseManager = DatabaseManager.getTestInstance();
    }
    
    @Test
    public void testGetConnection() throws Exception {
        java.sql.Connection connection = databaseManager.getConnection();
        
        assertNotNull(connection);
        assertFalse(connection.isClosed());
        
        connection.close();
    }
    
    @Test
    public void testTestConnection() {
        boolean result = databaseManager.testConnection();
        assertTrue(result);
    }
    
    @Test
    public void testGetPoolStats() {
        String stats = databaseManager.getPoolStats();
        
        assertNotNull(stats);
        assertTrue(stats.contains("Pool Stats"));
        assertTrue(stats.contains("Active"));
        assertTrue(stats.contains("Idle"));
    }
    
    @Test
    public void testGetDetailedPoolStats() {
        Map<String, Object> stats = databaseManager.getDetailedPoolStats();
        
        assertNotNull(stats);
        assertTrue(stats.containsKey("activeConnections"));
        assertTrue(stats.containsKey("idleConnections"));
        assertTrue(stats.containsKey("totalConnections"));
        assertTrue(stats.containsKey("threadsAwaitingConnection"));
        assertTrue(stats.containsKey("maxPoolSize"));
        assertTrue(stats.containsKey("minimumIdle"));
        assertTrue(stats.containsKey("poolHealthy"));
    }
    
    @Test
    public void testGetPoolUtilization() {
        double utilization = databaseManager.getPoolUtilization();
        
        assertTrue(utilization >= 0);
        assertTrue(utilization <= 100);
    }
    
    @Test
    public void testGetActiveConnections() {
        int activeConnections = databaseManager.getActiveConnections();
        
        assertTrue(activeConnections >= 0);
    }
    
    @Test
    public void testGetIdleConnections() {
        int idleConnections = databaseManager.getIdleConnections();
        
        assertTrue(idleConnections >= 0);
    }
    
    @Test
    public void testGetTotalConnections() {
        int totalConnections = databaseManager.getTotalConnections();
        
        assertTrue(totalConnections > 0);
    }
    
    @Test
    public void testIsHealthy() {
        boolean healthy = databaseManager.isHealthy();
        
        assertTrue(healthy);
    }
    
    @Test
    public void testIsTestMode() {
        assertTrue(databaseManager.isTestMode());
    }
    
    @Test
    public void testMultipleConnections() throws Exception {
        java.sql.Connection conn1 = databaseManager.getConnection();
        java.sql.Connection conn2 = databaseManager.getConnection();
        java.sql.Connection conn3 = databaseManager.getConnection();
        
        assertNotNull(conn1);
        assertNotNull(conn2);
        assertNotNull(conn3);
        
        int activeConnections = databaseManager.getActiveConnections();
        assertTrue(activeConnections >= 3);
        
        conn1.close();
        conn2.close();
        conn3.close();
    }
    
    @Test
    public void testConnectionPoolRecovery() throws Exception {
        // Get a connection
        java.sql.Connection connection = databaseManager.getConnection();
        assertNotNull(connection);
        
        // Close it
        connection.close();
        
        // Should be able to get another connection
        java.sql.Connection connection2 = databaseManager.getConnection();
        assertNotNull(connection2);
        
        connection2.close();
    }
    
    @Test
    public void testPoolStatsConsistency() {
        Map<String, Object> stats1 = databaseManager.getDetailedPoolStats();
        Map<String, Object> stats2 = databaseManager.getDetailedPoolStats();
        
        // Stats should be consistent
        assertEquals(stats1.get("maxPoolSize"), stats2.get("maxPoolSize"));
        assertEquals(stats1.get("minimumIdle"), stats2.get("minimumIdle"));
    }
    
    @Test
    public void testPoolUtilizationCalculation() throws Exception {
        double utilization1 = databaseManager.getPoolUtilization();
        
        // Get a connection to increase utilization
        java.sql.Connection connection = databaseManager.getConnection();
        
        double utilization2 = databaseManager.getPoolUtilization();
        
        // Utilization should increase or stay the same
        assertTrue(utilization2 >= utilization1);
        
        connection.close();
    }
}
