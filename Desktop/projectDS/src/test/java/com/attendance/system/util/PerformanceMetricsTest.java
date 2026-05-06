package com.attendance.system.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PerformanceMetrics.
 */
public class PerformanceMetricsTest {
    
    private PerformanceMetrics metrics;
    
    @BeforeEach
    public void setUp() {
        metrics = PerformanceMetrics.getInstance();
        metrics.clear();
    }
    
    @Test
    public void testRecordOperation() {
        metrics.recordOperation("testOp", 100);
        
        PerformanceMetrics.OperationMetrics opMetrics = metrics.getOperationMetrics("testOp");
        assertNotNull(opMetrics);
        assertEquals(1, opMetrics.getCount());
        assertEquals(100, opMetrics.getTotalDuration());
    }
    
    @Test
    public void testOperationTimer() {
        PerformanceMetrics.OperationTimer timer = metrics.startOperation("timedOp");
        
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        timer.stop();
        
        PerformanceMetrics.OperationMetrics opMetrics = metrics.getOperationMetrics("timedOp");
        assertNotNull(opMetrics);
        assertTrue(opMetrics.getTotalDuration() >= 50);
    }
    
    @Test
    public void testMultipleOperations() {
        metrics.recordOperation("op1", 100);
        metrics.recordOperation("op1", 200);
        metrics.recordOperation("op1", 150);
        
        PerformanceMetrics.OperationMetrics opMetrics = metrics.getOperationMetrics("op1");
        assertEquals(3, opMetrics.getCount());
        assertEquals(450, opMetrics.getTotalDuration());
        assertEquals(150, opMetrics.getAverageDuration());
        assertEquals(100, opMetrics.getMinDuration());
        assertEquals(200, opMetrics.getMaxDuration());
    }
    
    @Test
    public void testDatabaseOperation() {
        metrics.recordDatabaseOperation("SELECT * FROM users", 50);
        
        PerformanceMetrics.OperationMetrics opMetrics = 
            metrics.getOperationMetrics("DB:SELECT * FROM users");
        assertNotNull(opMetrics);
        assertEquals(1, opMetrics.getCount());
    }
    
    @Test
    public void testRMIOperation() {
        metrics.recordRMIOperation("authenticateUser", 200);
        
        PerformanceMetrics.OperationMetrics opMetrics = 
            metrics.getOperationMetrics("RMI:authenticateUser");
        assertNotNull(opMetrics);
        assertEquals(1, opMetrics.getCount());
    }
    
    @Test
    public void testGetSystemStatistics() {
        metrics.recordOperation("op1", 100);
        metrics.recordOperation("op2", 200);
        metrics.recordOperation("op3", 300);
        
        Map<String, Object> stats = metrics.getSystemStatistics();
        
        assertEquals(3, stats.get("totalOperations"));
        assertEquals(600, stats.get("totalDuration"));
        assertEquals(200, stats.get("averageDuration"));
        assertEquals(100, stats.get("minDuration"));
        assertEquals(300, stats.get("maxDuration"));
    }
    
    @Test
    public void testGetSlowOperations() {
        metrics.recordOperation("fast", 10);
        metrics.recordOperation("medium", 100);
        metrics.recordOperation("slow", 1000);
        
        List<Map<String, Object>> slowOps = metrics.getSlowOperations(2);
        
        assertEquals(2, slowOps.size());
        assertEquals("slow", slowOps.get(0).get("operation"));
        assertEquals("medium", slowOps.get(1).get("operation"));
    }
    
    @Test
    public void testGetMostFrequentOperations() {
        metrics.recordOperation("op1", 100);
        metrics.recordOperation("op1", 100);
        metrics.recordOperation("op1", 100);
        
        metrics.recordOperation("op2", 100);
        metrics.recordOperation("op2", 100);
        
        metrics.recordOperation("op3", 100);
        
        List<Map<String, Object>> frequentOps = metrics.getMostFrequentOperations(2);
        
        assertEquals(2, frequentOps.size());
        assertEquals("op1", frequentOps.get(0).get("operation"));
        assertEquals(3, frequentOps.get(0).get("count"));
        assertEquals("op2", frequentOps.get(1).get("operation"));
        assertEquals(2, frequentOps.get(1).get("count"));
    }
    
    @Test
    public void testClear() {
        metrics.recordOperation("op1", 100);
        metrics.recordOperation("op2", 200);
        
        metrics.clear();
        
        assertNull(metrics.getOperationMetrics("op1"));
        assertNull(metrics.getOperationMetrics("op2"));
    }
    
    @Test
    public void testMemoryStatistics() {
        Map<String, Object> stats = metrics.getSystemStatistics();
        
        assertTrue(stats.containsKey("memoryUsed"));
        assertTrue(stats.containsKey("memoryMax"));
        assertTrue(stats.containsKey("memoryFree"));
        
        long memoryUsed = (long) stats.get("memoryUsed");
        long memoryMax = (long) stats.get("memoryMax");
        
        assertTrue(memoryUsed > 0);
        assertTrue(memoryMax > 0);
        assertTrue(memoryUsed <= memoryMax);
    }
    
    @Test
    public void testThreadStatistics() {
        Map<String, Object> stats = metrics.getSystemStatistics();
        
        assertTrue(stats.containsKey("threadCount"));
        int threadCount = (int) stats.get("threadCount");
        assertTrue(threadCount > 0);
    }
    
    @Test
    public void testOperationMetricsCreatedAt() {
        metrics.recordOperation("op1", 100);
        
        PerformanceMetrics.OperationMetrics opMetrics = metrics.getOperationMetrics("op1");
        long createdAt = opMetrics.getCreatedAt();
        
        assertTrue(createdAt > 0);
        assertTrue(createdAt <= System.currentTimeMillis());
    }
}
