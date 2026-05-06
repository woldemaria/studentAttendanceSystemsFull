package com.attendance.system.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;

/**
 * Performance metrics collection and analysis for system monitoring.
 * Tracks response times, throughput, resource usage, and other performance indicators.
 */
public class PerformanceMetrics {
    private static final Logger logger = LoggerFactory.getLogger(PerformanceMetrics.class);
    
    private static PerformanceMetrics instance;
    
    // Metrics storage
    private final ConcurrentHashMap<String, OperationMetrics> operationMetrics = new ConcurrentHashMap<>();
    private final ConcurrentLinkedQueue<SystemMetric> systemMetrics = new ConcurrentLinkedQueue<>();
    
    // Configuration
    private static final int MAX_METRICS_HISTORY = 10000;
    private static final long METRICS_RETENTION_MINUTES = 60;
    
    private PerformanceMetrics() {
        startMetricsCleanupThread();
    }
    
    public static synchronized PerformanceMetrics getInstance() {
        if (instance == null) {
            instance = new PerformanceMetrics();
        }
        return instance;
    }
    
    /**
     * Records the start of an operation.
     * @param operationName name of the operation
     * @return operation timer for tracking
     */
    public OperationTimer startOperation(String operationName) {
        return new OperationTimer(operationName);
    }
    
    /**
     * Records the completion of an operation.
     * @param operationName name of the operation
     * @param durationMs duration in milliseconds
     */
    public void recordOperation(String operationName, long durationMs) {
        operationMetrics.computeIfAbsent(operationName, k -> new OperationMetrics(operationName))
            .recordOperation(durationMs);
        
        // Keep system metrics history bounded
        if (systemMetrics.size() >= MAX_METRICS_HISTORY) {
            systemMetrics.poll();
        }
        
        systemMetrics.offer(new SystemMetric(operationName, durationMs));
    }
    
    /**
     * Records a database operation.
     * @param query SQL query
     * @param durationMs duration in milliseconds
     */
    public void recordDatabaseOperation(String query, long durationMs) {
        recordOperation("DB:" + query, durationMs);
    }
    
    /**
     * Records an RMI operation.
     * @param methodName RMI method name
     * @param durationMs duration in milliseconds
     */
    public void recordRMIOperation(String methodName, long durationMs) {
        recordOperation("RMI:" + methodName, durationMs);
    }
    
    /**
     * Gets metrics for a specific operation.
     * @param operationName operation name
     * @return operation metrics or null if not found
     */
    public OperationMetrics getOperationMetrics(String operationName) {
        return operationMetrics.get(operationName);
    }
    
    /**
     * Gets all operation metrics.
     * @return map of all operation metrics
     */
    public Map<String, OperationMetrics> getAllOperationMetrics() {
        return new HashMap<>(operationMetrics);
    }
    
    /**
     * Gets overall system performance statistics.
     * @return map containing system statistics
     */
    public Map<String, Object> getSystemStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // Calculate overall metrics
        long totalOperations = 0;
        long totalDuration = 0;
        long minDuration = Long.MAX_VALUE;
        long maxDuration = 0;
        
        for (OperationMetrics metrics : operationMetrics.values()) {
            totalOperations += metrics.getCount();
            totalDuration += metrics.getTotalDuration();
            minDuration = Math.min(minDuration, metrics.getMinDuration());
            maxDuration = Math.max(maxDuration, metrics.getMaxDuration());
        }
        
        stats.put("totalOperations", totalOperations);
        stats.put("totalDuration", totalDuration);
        stats.put("averageDuration", totalOperations > 0 ? totalDuration / totalOperations : 0);
        stats.put("minDuration", minDuration == Long.MAX_VALUE ? 0 : minDuration);
        stats.put("maxDuration", maxDuration);
        stats.put("operationCount", operationMetrics.size());
        stats.put("metricsHistorySize", systemMetrics.size());
        
        // Add memory statistics
        Runtime runtime = Runtime.getRuntime();
        stats.put("memoryUsed", runtime.totalMemory() - runtime.freeMemory());
        stats.put("memoryMax", runtime.maxMemory());
        stats.put("memoryFree", runtime.freeMemory());
        
        // Add thread statistics
        ThreadGroup rootGroup = Thread.currentThread().getThreadGroup();
        stats.put("threadCount", rootGroup.activeCount());
        
        return stats;
    }
    
    /**
     * Gets top N slowest operations.
     * @param limit number of operations to return
     * @return list of slowest operations
     */
    public List<Map<String, Object>> getSlowOperations(int limit) {
        List<Map<String, Object>> slowOps = new ArrayList<>();
        
        operationMetrics.values().stream()
            .sorted(Comparator.comparingLong(OperationMetrics::getAverageDuration).reversed())
            .limit(limit)
            .forEach(metrics -> {
                Map<String, Object> op = new HashMap<>();
                op.put("operation", metrics.getOperationName());
                op.put("count", metrics.getCount());
                op.put("averageDuration", metrics.getAverageDuration());
                op.put("minDuration", metrics.getMinDuration());
                op.put("maxDuration", metrics.getMaxDuration());
                slowOps.add(op);
            });
        
        return slowOps;
    }
    
    /**
     * Gets top N most frequent operations.
     * @param limit number of operations to return
     * @return list of most frequent operations
     */
    public List<Map<String, Object>> getMostFrequentOperations(int limit) {
        List<Map<String, Object>> frequentOps = new ArrayList<>();
        
        operationMetrics.values().stream()
            .sorted(Comparator.comparingLong(OperationMetrics::getCount).reversed())
            .limit(limit)
            .forEach(metrics -> {
                Map<String, Object> op = new HashMap<>();
                op.put("operation", metrics.getOperationName());
                op.put("count", metrics.getCount());
                op.put("averageDuration", metrics.getAverageDuration());
                op.put("totalDuration", metrics.getTotalDuration());
                frequentOps.add(op);
            });
        
        return frequentOps;
    }
    
    /**
     * Clears all metrics.
     */
    public void clear() {
        operationMetrics.clear();
        systemMetrics.clear();
        logger.info("Performance metrics cleared");
    }
    
    /**
     * Starts a background thread to clean up old metrics.
     */
    private void startMetricsCleanupThread() {
        Thread cleanupThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(METRICS_RETENTION_MINUTES * 60 * 1000);
                    
                    // Remove old metrics
                    long cutoffTime = System.currentTimeMillis() - (METRICS_RETENTION_MINUTES * 60 * 1000);
                    systemMetrics.removeIf(metric -> metric.getTimestamp() < cutoffTime);
                    
                    logger.debug("Metrics cleanup completed. Current history size: {}", systemMetrics.size());
                    
                } catch (InterruptedException e) {
                    logger.debug("Metrics cleanup thread interrupted");
                    break;
                }
            }
        });
        
        cleanupThread.setDaemon(true);
        cleanupThread.setName("MetricsCleanupThread");
        cleanupThread.start();
    }
    
    /**
     * Inner class for tracking individual operation metrics.
     */
    public static class OperationMetrics {
        private final String operationName;
        private long count = 0;
        private long totalDuration = 0;
        private long minDuration = Long.MAX_VALUE;
        private long maxDuration = 0;
        private final long createdAt = System.currentTimeMillis();
        
        public OperationMetrics(String operationName) {
            this.operationName = operationName;
        }
        
        public synchronized void recordOperation(long durationMs) {
            count++;
            totalDuration += durationMs;
            minDuration = Math.min(minDuration, durationMs);
            maxDuration = Math.max(maxDuration, durationMs);
        }
        
        public String getOperationName() {
            return operationName;
        }
        
        public long getCount() {
            return count;
        }
        
        public long getTotalDuration() {
            return totalDuration;
        }
        
        public long getAverageDuration() {
            return count > 0 ? totalDuration / count : 0;
        }
        
        public long getMinDuration() {
            return minDuration == Long.MAX_VALUE ? 0 : minDuration;
        }
        
        public long getMaxDuration() {
            return maxDuration;
        }
        
        public long getCreatedAt() {
            return createdAt;
        }
    }
    
    /**
     * Inner class for system metrics history.
     */
    private static class SystemMetric {
        private final String operationName;
        private final long duration;
        private final long timestamp;
        
        public SystemMetric(String operationName, long duration) {
            this.operationName = operationName;
            this.duration = duration;
            this.timestamp = System.currentTimeMillis();
        }
        
        public String getOperationName() {
            return operationName;
        }
        
        public long getDuration() {
            return duration;
        }
        
        public long getTimestamp() {
            return timestamp;
        }
    }
    
    /**
     * Helper class for timing operations.
     */
    public class OperationTimer {
        private final String operationName;
        private final long startTime;
        
        public OperationTimer(String operationName) {
            this.operationName = operationName;
            this.startTime = System.currentTimeMillis();
        }
        
        public void stop() {
            long duration = System.currentTimeMillis() - startTime;
            recordOperation(operationName, duration);
        }
        
        public long getDuration() {
            return System.currentTimeMillis() - startTime;
        }
    }
}
