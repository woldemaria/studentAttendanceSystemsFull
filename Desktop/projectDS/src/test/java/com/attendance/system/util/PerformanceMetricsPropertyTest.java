package com.attendance.system.util;

import net.java.quickcheck.QuickCheck;
import net.java.quickcheck.generator.PrimitiveGenerators;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based tests for PerformanceMetrics.
 * **Validates: Requirements 9.1, 9.4, 9.5**
 */
public class PerformanceMetricsPropertyTest {
    
    private PerformanceMetrics metrics;
    
    @BeforeEach
    public void setUp() {
        metrics = PerformanceMetrics.getInstance();
        metrics.clear();
    }
    
    /**
     * Property: Operation Metrics Accuracy
     * For any recorded operation, the metrics should accurately track count, duration,
     * average, min, and max values.
     */
    @Test
    public void testOperationMetricsAccuracy() {
        QuickCheck.forAll(
            PrimitiveGenerators.integers(1, 100),
            operationCount -> {
                metrics.clear();
                
                long totalDuration = 0;
                long minDuration = Long.MAX_VALUE;
                long maxDuration = 0;
                
                // Record operations
                for (int i = 0; i < operationCount; i++) {
                    long duration = (long) (Math.random() * 1000);
                    metrics.recordOperation("testOp", duration);
                    
                    totalDuration += duration;
                    minDuration = Math.min(minDuration, duration);
                    maxDuration = Math.max(maxDuration, duration);
                }
                
                // Verify metrics
                PerformanceMetrics.OperationMetrics opMetrics = metrics.getOperationMetrics("testOp");
                assertNotNull(opMetrics);
                assertEquals(operationCount, opMetrics.getCount());
                assertEquals(totalDuration, opMetrics.getTotalDuration());
                assertEquals(totalDuration / operationCount, opMetrics.getAverageDuration());
                assertEquals(minDuration, opMetrics.getMinDuration());
                assertEquals(maxDuration, opMetrics.getMaxDuration());
                
                return true;
            }
        );
    }
    
    /**
     * Property: System Statistics Consistency
     * For any set of recorded operations, the system statistics should be consistent
     * and mathematically correct.
     */
    @Test
    public void testSystemStatisticsConsistency() {
        QuickCheck.forAll(
            PrimitiveGenerators.integers(1, 50),
            operationCount -> {
                metrics.clear();
                
                long expectedTotalOperations = 0;
                long expectedTotalDuration = 0;
                
                // Record operations
                for (int i = 0; i < operationCount; i++) {
                    long duration = (long) (Math.random() * 1000);
                    metrics.recordOperation("op" + (i % 5), duration);
                    
                    expectedTotalOperations++;
                    expectedTotalDuration += duration;
                }
                
                // Get system statistics
                Map<String, Object> stats = metrics.getSystemStatistics();
                
                // Verify consistency
                assertEquals(expectedTotalOperations, stats.get("totalOperations"));
                assertEquals(expectedTotalDuration, stats.get("totalDuration"));
                
                if (expectedTotalOperations > 0) {
                    long expectedAverage = expectedTotalDuration / expectedTotalOperations;
                    assertEquals(expectedAverage, stats.get("averageDuration"));
                }
                
                return true;
            }
        );
    }
    
    /**
     * Property: Slow Operations Ranking
     * For any set of operations with varying durations, the slowest operations
     * should be correctly identified and ranked.
     */
    @Test
    public void testSlowOperationsRanking() {
        QuickCheck.forAll(
            PrimitiveGenerators.integers(5, 50),
            operationCount -> {
                metrics.clear();
                
                // Record operations with varying durations
                for (int i = 0; i < operationCount; i++) {
                    long duration = (long) (Math.random() * 10000);
                    metrics.recordOperation("op" + i, duration);
                }
                
                // Get slowest operations
                List<Map<String, Object>> slowOps = metrics.getSlowOperations(5);
                
                // Verify ranking
                for (int i = 0; i < slowOps.size() - 1; i++) {
                    long duration1 = (long) slowOps.get(i).get("averageDuration");
                    long duration2 = (long) slowOps.get(i + 1).get("averageDuration");
                    
                    assertTrue(duration1 >= duration2, "Slow operations should be ranked by duration");
                }
                
                return true;
            }
        );
    }
    
    /**
     * Property: Most Frequent Operations Ranking
     * For any set of operations with varying frequencies, the most frequent operations
     * should be correctly identified and ranked.
     */
    @Test
    public void testMostFrequentOperationsRanking() {
        QuickCheck.forAll(
            PrimitiveGenerators.integers(5, 50),
            operationCount -> {
                metrics.clear();
                
                // Record operations with varying frequencies
                for (int i = 0; i < operationCount; i++) {
                    int frequency = (i % 5) + 1;
                    for (int j = 0; j < frequency; j++) {
                        metrics.recordOperation("op" + i, 100);
                    }
                }
                
                // Get most frequent operations
                List<Map<String, Object>> frequentOps = metrics.getMostFrequentOperations(5);
                
                // Verify ranking
                for (int i = 0; i < frequentOps.size() - 1; i++) {
                    long count1 = (long) frequentOps.get(i).get("count");
                    long count2 = (long) frequentOps.get(i + 1).get("count");
                    
                    assertTrue(count1 >= count2, "Frequent operations should be ranked by count");
                }
                
                return true;
            }
        );
    }
    
    /**
     * Property: Database Operation Tracking
     * For any database operation recorded, the metrics should correctly track it
     * with the "DB:" prefix.
     */
    @Test
    public void testDatabaseOperationTracking() {
        QuickCheck.forAll(
            PrimitiveGenerators.integers(1, 50),
            operationCount -> {
                metrics.clear();
                
                // Record database operations
                for (int i = 0; i < operationCount; i++) {
                    long duration = (long) (Math.random() * 1000);
                    metrics.recordDatabaseOperation("SELECT * FROM table" + i, duration);
                }
                
                // Verify all operations are tracked with DB prefix
                Map<String, PerformanceMetrics.OperationMetrics> allMetrics = metrics.getAllOperationMetrics();
                
                for (String operationName : allMetrics.keySet()) {
                    assertTrue(operationName.startsWith("DB:"), "Database operations should have DB: prefix");
                }
                
                return true;
            }
        );
    }
    
    /**
     * Property: RMI Operation Tracking
     * For any RMI operation recorded, the metrics should correctly track it
     * with the "RMI:" prefix.
     */
    @Test
    public void testRMIOperationTracking() {
        QuickCheck.forAll(
            PrimitiveGenerators.integers(1, 50),
            operationCount -> {
                metrics.clear();
                
                // Record RMI operations
                for (int i = 0; i < operationCount; i++) {
                    long duration = (long) (Math.random() * 1000);
                    metrics.recordRMIOperation("method" + i, duration);
                }
                
                // Verify all operations are tracked with RMI prefix
                Map<String, PerformanceMetrics.OperationMetrics> allMetrics = metrics.getAllOperationMetrics();
                
                for (String operationName : allMetrics.keySet()) {
                    assertTrue(operationName.startsWith("RMI:"), "RMI operations should have RMI: prefix");
                }
                
                return true;
            }
        );
    }
    
    /**
     * Property: Memory Statistics Validity
     * For any system state, the memory statistics should be valid and consistent.
     */
    @Test
    public void testMemoryStatisticsValidity() {
        QuickCheck.forAll(
            PrimitiveGenerators.integers(1, 100),
            operationCount -> {
                // Record some operations
                for (int i = 0; i < operationCount; i++) {
                    metrics.recordOperation("op" + i, 100);
                }
                
                // Get system statistics
                Map<String, Object> stats = metrics.getSystemStatistics();
                
                // Verify memory statistics
                long memoryUsed = (long) stats.get("memoryUsed");
                long memoryMax = (long) stats.get("memoryMax");
                long memoryFree = (long) stats.get("memoryFree");
                
                assertTrue(memoryUsed > 0, "Memory used should be positive");
                assertTrue(memoryMax > 0, "Memory max should be positive");
                assertTrue(memoryFree >= 0, "Memory free should be non-negative");
                assertTrue(memoryUsed <= memoryMax, "Memory used should not exceed max");
                
                return true;
            }
        );
    }
}
