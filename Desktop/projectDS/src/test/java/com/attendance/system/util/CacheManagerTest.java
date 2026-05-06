package com.attendance.system.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CacheManager.
 */
public class CacheManagerTest {
    
    private CacheManager cacheManager;
    
    @BeforeEach
    public void setUp() {
        cacheManager = CacheManager.getInstance();
        cacheManager.clear();
    }
    
    @Test
    public void testCachePutAndGet() {
        String key = "test:key";
        String value = "test value";
        
        cacheManager.put(key, value);
        String retrieved = cacheManager.get(key);
        
        assertEquals(value, retrieved);
    }
    
    @Test
    public void testCacheGetNonExistent() {
        String retrieved = cacheManager.get("non:existent");
        assertNull(retrieved);
    }
    
    @Test
    public void testCacheRemove() {
        String key = "test:key";
        String value = "test value";
        
        cacheManager.put(key, value);
        cacheManager.remove(key);
        
        String retrieved = cacheManager.get(key);
        assertNull(retrieved);
    }
    
    @Test
    public void testCacheRemoveByPrefix() {
        cacheManager.put("users:1", "user1");
        cacheManager.put("users:2", "user2");
        cacheManager.put("courses:1", "course1");
        
        cacheManager.removeByPrefix("users:");
        
        assertNull(cacheManager.get("users:1"));
        assertNull(cacheManager.get("users:2"));
        assertEquals("course1", cacheManager.get("courses:1"));
    }
    
    @Test
    public void testCacheClear() {
        cacheManager.put("key1", "value1");
        cacheManager.put("key2", "value2");
        
        cacheManager.clear();
        
        assertNull(cacheManager.get("key1"));
        assertNull(cacheManager.get("key2"));
    }
    
    @Test
    public void testCacheStatistics() {
        cacheManager.put("key1", "value1");
        cacheManager.get("key1"); // hit
        cacheManager.get("key2"); // miss
        
        Map<String, Object> stats = cacheManager.getStatistics();
        
        assertEquals(1, stats.get("hits"));
        assertEquals(1, stats.get("misses"));
        assertTrue(stats.get("size").toString().contains("1"));
    }
    
    @Test
    public void testCacheWithCustomTTL() throws InterruptedException {
        String key = "test:key";
        String value = "test value";
        
        // Put with 1 second TTL
        cacheManager.put(key, value, 1);
        
        // Should be available immediately
        assertEquals(value, cacheManager.get(key));
        
        // Wait for expiration
        Thread.sleep(1100);
        
        // Should be expired
        assertNull(cacheManager.get(key));
    }
    
    @Test
    public void testCacheMultipleTypes() {
        cacheManager.put("string:key", "string value");
        cacheManager.put("int:key", 42);
        cacheManager.put("long:key", 1000L);
        
        assertEquals("string value", cacheManager.get("string:key"));
        assertEquals(42, cacheManager.get("int:key"));
        assertEquals(1000L, cacheManager.get("long:key"));
    }
    
    @Test
    public void testCacheHitRate() {
        cacheManager.put("key1", "value1");
        
        // Generate hits and misses
        for (int i = 0; i < 10; i++) {
            cacheManager.get("key1"); // hit
        }
        
        for (int i = 0; i < 5; i++) {
            cacheManager.get("non:existent"); // miss
        }
        
        Map<String, Object> stats = cacheManager.getStatistics();
        assertEquals(10, stats.get("hits"));
        assertEquals(5, stats.get("misses"));
    }
    
    @Test
    public void testCacheMaxSize() {
        // Fill cache to max size
        for (int i = 0; i < 1000; i++) {
            cacheManager.put("key:" + i, "value:" + i);
        }
        
        Map<String, Object> stats = cacheManager.getStatistics();
        assertEquals(1000, stats.get("size"));
        
        // Add one more - should trigger LRU eviction
        cacheManager.put("key:1000", "value:1000");
        
        stats = cacheManager.getStatistics();
        assertEquals(1000, stats.get("size"));
        assertTrue((long) stats.get("evictions") > 0);
    }
    
    @Test
    public void testCacheUpdateAccessTime() {
        cacheManager.put("key1", "value1");
        cacheManager.put("key2", "value2");
        
        // Access key1 multiple times to update its access time
        for (int i = 0; i < 5; i++) {
            cacheManager.get("key1");
        }
        
        // Fill cache to trigger LRU eviction
        for (int i = 0; i < 1000; i++) {
            cacheManager.put("key:" + i, "value:" + i);
        }
        
        // key1 should still be in cache (recently accessed)
        // key2 should be evicted (not recently accessed)
        assertNotNull(cacheManager.get("key1"));
    }
}
