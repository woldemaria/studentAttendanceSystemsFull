# Performance Monitoring and Optimization Guide

## Overview

The Student Attendance System includes comprehensive performance monitoring and optimization features to ensure reliable operation under load conditions. This guide covers the performance optimization components, their usage, and best practices.

## Performance Optimization Components

### 1. CacheManager

The `CacheManager` provides an in-memory caching layer for frequently accessed data to reduce database load and improve response times.

#### Features
- **LRU Eviction Policy**: Automatically evicts least recently used entries when cache reaches maximum size
- **TTL Support**: Configurable time-to-live for cache entries with automatic expiration
- **Cache Statistics**: Tracks hits, misses, evictions, and hit rate
- **Prefix-based Removal**: Efficiently clear related cache entries by prefix
- **Thread-safe**: Concurrent access support using ConcurrentHashMap

#### Usage

```java
CacheManager cache = CacheManager.getInstance();

// Put a value in cache with default TTL (30 minutes)
cache.put("users:123", user);

// Put a value with custom TTL (5 minutes)
cache.put("courses:456", course, 5);

// Get a value from cache
User user = cache.get("users:123");

// Remove a specific entry
cache.remove("users:123");

// Remove all entries with a prefix
cache.removeByPrefix("users:");

// Get cache statistics
Map<String, Object> stats = cache.getStatistics();
System.out.println("Cache hits: " + stats.get("hits"));
System.out.println("Cache misses: " + stats.get("misses"));
System.out.println("Hit rate: " + stats.get("hitRate"));
```

#### Cache Keys

- `users:` - User data cache
- `courses:` - Course data cache
- `attendance:` - Attendance records cache
- `stats:` - Statistics cache

#### Configuration

- **MAX_CACHE_SIZE**: 1000 entries
- **DEFAULT_TTL_MINUTES**: 30 minutes
- **CLEANUP_INTERVAL_MINUTES**: 5 minutes

### 2. PerformanceMetrics

The `PerformanceMetrics` class collects and analyzes performance data for all system operations.

#### Features
- **Operation Tracking**: Records duration and frequency of all operations
- **Statistics Collection**: Calculates average, min, max, and total durations
- **Slow Operations Detection**: Identifies and ranks slowest operations
- **Frequent Operations Analysis**: Identifies most frequently called operations
- **Memory Monitoring**: Tracks memory usage and availability
- **Thread Monitoring**: Tracks active thread count
- **Automatic Cleanup**: Removes old metrics to prevent memory bloat

#### Usage

```java
PerformanceMetrics metrics = PerformanceMetrics.getInstance();

// Record an operation
metrics.recordOperation("getUserById", 150);

// Use operation timer for automatic duration tracking
PerformanceMetrics.OperationTimer timer = metrics.startOperation("markAttendance");
// ... perform operation ...
timer.stop();

// Record database operation
metrics.recordDatabaseOperation("SELECT * FROM attendance_records", 200);

// Record RMI operation
metrics.recordRMIOperation("authenticateUser", 300);

// Get operation metrics
PerformanceMetrics.OperationMetrics opMetrics = metrics.getOperationMetrics("getUserById");
System.out.println("Count: " + opMetrics.getCount());
System.out.println("Average duration: " + opMetrics.getAverageDuration() + "ms");
System.out.println("Min duration: " + opMetrics.getMinDuration() + "ms");
System.out.println("Max duration: " + opMetrics.getMaxDuration() + "ms");

// Get system statistics
Map<String, Object> stats = metrics.getSystemStatistics();
System.out.println("Total operations: " + stats.get("totalOperations"));
System.out.println("Memory used: " + stats.get("memoryUsed") + " bytes");

// Get slowest operations
List<Map<String, Object>> slowOps = metrics.getSlowOperations(10);
for (Map<String, Object> op : slowOps) {
    System.out.println(op.get("operation") + ": " + op.get("averageDuration") + "ms");
}

// Get most frequent operations
List<Map<String, Object>> frequentOps = metrics.getMostFrequentOperations(10);
for (Map<String, Object> op : frequentOps) {
    System.out.println(op.get("operation") + ": " + op.get("count") + " calls");
}
```

### 3. OverloadHandler

The `OverloadHandler` manages system load and gracefully handles overload conditions through request queuing and priority handling.

#### Features
- **Request Queuing**: Queues requests when system is at capacity
- **Priority Handling**: Supports HIGH, NORMAL, and LOW priority requests
- **Graceful Degradation**: Rejects low-priority requests during overload
- **Automatic Recovery**: Automatically recovers when load decreases
- **Load Monitoring**: Tracks load factor and system capacity
- **Request Statistics**: Tracks accepted, rejected, and processed requests

#### Usage

```java
OverloadHandler handler = OverloadHandler.getInstance();

// Set maximum concurrent requests
handler.setMaxConcurrentRequests(100);

// Submit a request with default priority
OverloadHandler.Request request = new OverloadHandler.Request() {
    @Override
    public void execute() throws Exception {
        // Perform operation
    }
    
    @Override
    public String getOperationName() {
        return "myOperation";
    }
};

boolean accepted = handler.submitRequest(request);

// Submit a request with high priority
boolean accepted = handler.submitRequest(request, OverloadHandler.RequestPriority.HIGH);

// Check if system is overloaded
if (handler.isSystemOverloaded()) {
    System.out.println("System is overloaded");
}

// Get load factor (0.0 to 1.0)
double loadFactor = handler.getLoadFactor();
System.out.println("Load factor: " + String.format("%.2f", loadFactor));

// Get statistics
Map<String, Object> stats = handler.getStatistics();
System.out.println("Active requests: " + stats.get("activeRequests"));
System.out.println("Queued requests: " + stats.get("queuedRequests"));
System.out.println("Rejected requests: " + stats.get("rejectedRequests"));
System.out.println("Load factor: " + stats.get("loadFactor"));
```

#### Configuration

- **MAX_QUEUE_SIZE**: 500 requests
- **PROCESSING_THREADS**: 5 threads
- **REQUEST_TIMEOUT_MS**: 30 seconds
- **OVERLOAD_THRESHOLD**: 80% capacity
- **RECOVERY_THRESHOLD**: 50% capacity

### 4. DatabaseManager Connection Pool Monitoring

The `DatabaseManager` provides enhanced connection pool monitoring and statistics.

#### Features
- **HikariCP Integration**: Uses HikariCP for efficient connection pooling
- **Pool Statistics**: Tracks active, idle, and total connections
- **Utilization Monitoring**: Calculates pool utilization percentage
- **Health Checks**: Verifies database connectivity
- **Detailed Metrics**: Provides comprehensive pool statistics

#### Usage

```java
DatabaseManager dbManager = DatabaseManager.getInstance();

// Get pool statistics string
String stats = dbManager.getPoolStats();
System.out.println(stats);

// Get detailed pool statistics
Map<String, Object> detailedStats = dbManager.getDetailedPoolStats();
System.out.println("Active connections: " + detailedStats.get("activeConnections"));
System.out.println("Idle connections: " + detailedStats.get("idleConnections"));
System.out.println("Total connections: " + detailedStats.get("totalConnections"));

// Get pool utilization percentage
double utilization = dbManager.getPoolUtilization();
System.out.println("Pool utilization: " + String.format("%.2f%%", utilization));

// Check pool health
boolean healthy = dbManager.isHealthy();
System.out.println("Pool is healthy: " + healthy);

// Get individual connection counts
int activeConnections = dbManager.getActiveConnections();
int idleConnections = dbManager.getIdleConnections();
int totalConnections = dbManager.getTotalConnections();
```

#### Configuration

- **Maximum Pool Size**: 20 connections
- **Minimum Idle**: 5 connections
- **Connection Timeout**: 30 seconds
- **Idle Timeout**: 10 minutes
- **Max Lifetime**: 30 minutes

## Performance Requirements

The system is designed to meet the following performance requirements:

| Metric | Target | Status |
|--------|--------|--------|
| RMI Response Time | < 2 seconds | ✓ |
| Report Generation (10,000 records) | < 10 seconds | ✓ |
| System Startup | < 30 seconds | ✓ |
| Database Operations | 1000 records/minute | ✓ |
| Concurrent Users | Up to 100 | ✓ |
| Graceful Degradation | Under overload | ✓ |

## Monitoring Dashboard

### Key Metrics to Monitor

1. **System Load**
   - Load factor (0.0 to 1.0)
   - Active connections
   - Queued requests

2. **Performance**
   - Average response time
   - Slowest operations
   - Most frequent operations

3. **Resource Usage**
   - Memory utilization
   - Thread count
   - Database connection pool utilization

4. **Reliability**
   - Request acceptance rate
   - Request rejection rate
   - System recovery time

### Monitoring Implementation

```java
// Create a monitoring dashboard
public class PerformanceMonitoringDashboard {
    private final PerformanceMetrics metrics;
    private final OverloadHandler overloadHandler;
    private final DatabaseManager dbManager;
    
    public void displayDashboard() {
        // System load
        System.out.println("=== System Load ===");
        System.out.println("Load factor: " + overloadHandler.getLoadFactor());
        System.out.println("Overloaded: " + overloadHandler.isSystemOverloaded());
        
        // Performance metrics
        System.out.println("\n=== Performance ===");
        Map<String, Object> stats = metrics.getSystemStatistics();
        System.out.println("Total operations: " + stats.get("totalOperations"));
        System.out.println("Average duration: " + stats.get("averageDuration") + "ms");
        
        // Database pool
        System.out.println("\n=== Database Pool ===");
        System.out.println(dbManager.getPoolStats());
        
        // Slowest operations
        System.out.println("\n=== Slowest Operations ===");
        List<Map<String, Object>> slowOps = metrics.getSlowOperations(5);
        for (Map<String, Object> op : slowOps) {
            System.out.println(op.get("operation") + ": " + op.get("averageDuration") + "ms");
        }
    }
}
```

## Best Practices

### 1. Cache Management
- Use appropriate TTL values based on data volatility
- Clear cache entries when data is updated
- Monitor cache hit rate and adjust size if needed
- Use prefix-based removal for related data

### 2. Performance Monitoring
- Regularly review slow operations and optimize them
- Monitor memory usage and adjust cache size if needed
- Track most frequent operations for optimization opportunities
- Use metrics to identify performance bottlenecks

### 3. Overload Handling
- Set appropriate maximum concurrent request limits
- Use priority levels for critical operations
- Monitor load factor and adjust capacity if needed
- Implement graceful degradation for non-critical operations

### 4. Database Connection Pool
- Monitor pool utilization regularly
- Adjust pool size based on usage patterns
- Ensure pool health checks pass regularly
- Monitor connection timeout and idle timeout settings

## Troubleshooting

### High Cache Miss Rate
- Increase cache TTL for stable data
- Increase cache size if available memory allows
- Review cache key patterns for consistency

### System Overload
- Increase maximum concurrent request limit
- Optimize slow operations
- Implement request prioritization
- Consider horizontal scaling

### Database Connection Pool Issues
- Check database connectivity
- Verify connection pool configuration
- Monitor for connection leaks
- Review slow database queries

### Memory Issues
- Monitor memory usage trends
- Reduce cache size if necessary
- Implement garbage collection tuning
- Review for memory leaks

## Integration with RMI Server

The performance monitoring components are integrated with the RMI server to provide comprehensive monitoring:

```java
// In AttendanceServer
private final PerformanceMetrics metrics = PerformanceMetrics.getInstance();
private final OverloadHandler overloadHandler = OverloadHandler.getInstance();

@Override
public Map<String, Object> getSystemStatistics(String sessionToken) {
    // Get performance metrics
    Map<String, Object> stats = metrics.getSystemStatistics();
    
    // Add overload handler statistics
    stats.putAll(overloadHandler.getStatistics());
    
    // Add database pool statistics
    stats.putAll(dbManager.getDetailedPoolStats());
    
    return stats;
}
```

## Testing

Comprehensive unit tests and property-based tests are provided:

- **CacheManagerTest**: Tests cache operations, TTL, LRU eviction
- **PerformanceMetricsTest**: Tests metrics collection and analysis
- **OverloadHandlerTest**: Tests request queuing and priority handling
- **DatabaseManagerTest**: Tests connection pool monitoring
- **OverloadHandlerPropertyTest**: Property-based tests for overload handling
- **PerformanceMetricsPropertyTest**: Property-based tests for metrics accuracy

Run tests with:
```bash
mvn test -Dtest=CacheManagerTest,PerformanceMetricsTest,OverloadHandlerTest,DatabaseManagerTest
mvn test -Dtest=OverloadHandlerPropertyTest,PerformanceMetricsPropertyTest
```

## Conclusion

The performance monitoring and optimization features provide comprehensive visibility into system performance and enable graceful handling of overload conditions. By following the best practices and monitoring the key metrics, the system can maintain reliable operation under expected load conditions.
