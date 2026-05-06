package com.attendance.system.util;

import com.attendance.system.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;

/**
 * Cache manager for frequently accessed data to improve system performance.
 * Implements LRU (Least Recently Used) eviction policy with TTL (Time To Live).
 */
public class CacheManager {
    private static final Logger logger = LoggerFactory.getLogger(CacheManager.class);
    
    private static CacheManager instance;
    
    // Cache storage with TTL support
    private final ConcurrentHashMap<String, CacheEntry<?>> cache = new ConcurrentHashMap<>();
    
    // Cache statistics
    private final AtomicLong hits = new AtomicLong(0);
    private final AtomicLong misses = new AtomicLong(0);
    private final AtomicLong evictions = new AtomicLong(0);
    
    // Configuration
    private static final int MAX_CACHE_SIZE = 1000;
    private static final long DEFAULT_TTL_MINUTES = 30;
    private static final long CLEANUP_INTERVAL_MINUTES = 5;
    
    // Cache keys
    public static final String USERS_CACHE_PREFIX = "users:";
    public static final String COURSES_CACHE_PREFIX = "courses:";
    public static final String ATTENDANCE_CACHE_PREFIX = "attendance:";
    public static final String STATISTICS_CACHE_PREFIX = "stats:";
    
    private CacheManager() {
        // Start cleanup thread
        startCleanupThread();
    }
    
    public static synchronized CacheManager getInstance() {
        if (instance == null) {
            instance = new CacheManager();
        }
        return instance;
    }
    
    /**
     * Gets a value from cache.
     * @param key cache key
     * @return cached value or null if not found or expired
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        CacheEntry<?> entry = cache.get(key);
        
        if (entry == null) {
            misses.incrementAndGet();
            return null;
        }
        
        // Check if expired
        if (entry.isExpired()) {
            cache.remove(key);
            evictions.incrementAndGet();
            misses.incrementAndGet();
            logger.debug("Cache entry expired: {}", key);
            return null;
        }
        
        // Update access time for LRU
        entry.updateAccessTime();
        hits.incrementAndGet();
        
        return (T) entry.getValue();
    }
    
    /**
     * Puts a value in cache with default TTL.
     * @param key cache key
     * @param value value to cache
     */
    public <T> void put(String key, T value) {
        put(key, value, DEFAULT_TTL_MINUTES);
    }
    
    /**
     * Puts a value in cache with custom TTL.
     * @param key cache key
     * @param value value to cache
     * @param ttlMinutes time to live in minutes
     */
    public <T> void put(String key, T value, long ttlMinutes) {
        // Check cache size and evict if necessary
        if (cache.size() >= MAX_CACHE_SIZE) {
            evictLRU();
        }
        
        CacheEntry<T> entry = new CacheEntry<>(value, ttlMinutes);
        cache.put(key, entry);
        
        logger.debug("Cache entry added: {} (TTL: {} minutes)", key, ttlMinutes);
    }
    
    /**
     * Removes a value from cache.
     * @param key cache key
     */
    public void remove(String key) {
        cache.remove(key);
        logger.debug("Cache entry removed: {}", key);
    }
    
    /**
     * Removes all cache entries matching a prefix.
     * @param prefix cache key prefix
     */
    public void removeByPrefix(String prefix) {
        cache.keySet().removeIf(key -> key.startsWith(prefix));
        logger.debug("Cache entries removed with prefix: {}", prefix);
    }
    
    /**
     * Clears all cache entries.
     */
    public void clear() {
        cache.clear();
        hits.set(0);
        misses.set(0);
        evictions.set(0);
        logger.info("Cache cleared");
    }
    
    /**
     * Gets cache statistics.
     * @return map containing cache statistics
     */
    public Map<String, Object> getStatistics() {
        long totalRequests = hits.get() + misses.get();
        double hitRate = totalRequests > 0 ? (double) hits.get() / totalRequests * 100 : 0;
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("size", cache.size());
        stats.put("maxSize", MAX_CACHE_SIZE);
        stats.put("hits", hits.get());
        stats.put("misses", misses.get());
        stats.put("evictions", evictions.get());
        stats.put("hitRate", String.format("%.2f%%", hitRate));
        stats.put("totalRequests", totalRequests);
        
        return stats;
    }
    
    /**
     * Evicts the least recently used entry from cache.
     */
    private void evictLRU() {
        String lruKey = cache.entrySet().stream()
            .min(Comparator.comparingLong(e -> e.getValue().getLastAccessTime()))
            .map(Map.Entry::getKey)
            .orElse(null);
        
        if (lruKey != null) {
            cache.remove(lruKey);
            evictions.incrementAndGet();
            logger.debug("LRU cache entry evicted: {}", lruKey);
        }
    }
    
    /**
     * Starts a background thread to clean up expired cache entries.
     */
    private void startCleanupThread() {
        Thread cleanupThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(CLEANUP_INTERVAL_MINUTES * 60 * 1000);
                    
                    // Remove expired entries
                    cache.entrySet().removeIf(entry -> entry.getValue().isExpired());
                    
                    logger.debug("Cache cleanup completed. Current size: {}", cache.size());
                    
                } catch (InterruptedException e) {
                    logger.debug("Cache cleanup thread interrupted");
                    break;
                }
            }
        });
        
        cleanupThread.setDaemon(true);
        cleanupThread.setName("CacheCleanupThread");
        cleanupThread.start();
    }
    
    /**
     * Inner class for cache entries with TTL support.
     */
    private static class CacheEntry<T> {
        private final T value;
        private final long expirationTime;
        private long lastAccessTime;
        
        public CacheEntry(T value, long ttlMinutes) {
            this.value = value;
            this.expirationTime = System.currentTimeMillis() + (ttlMinutes * 60 * 1000);
            this.lastAccessTime = System.currentTimeMillis();
        }
        
        public T getValue() {
            return value;
        }
        
        public boolean isExpired() {
            return System.currentTimeMillis() > expirationTime;
        }
        
        public void updateAccessTime() {
            this.lastAccessTime = System.currentTimeMillis();
        }
        
        public long getLastAccessTime() {
            return lastAccessTime;
        }
    }
    
    /**
     * Atomic wrapper for long values.
     */
    private static class AtomicLong {
        private long value = 0;
        
        public synchronized void incrementAndGet() {
            value++;
        }
        
        public synchronized long get() {
            return value;
        }
        
        public synchronized void set(long newValue) {
            value = newValue;
        }
    }
}
