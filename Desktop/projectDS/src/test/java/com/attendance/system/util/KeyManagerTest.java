package com.attendance.system.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for KeyManager secure key management.
 */
public class KeyManagerTest {
    
    @TempDir
    Path tempDir;
    
    @BeforeEach
    public void setUp() {
        SecurityUtil.setEncryptionKey("MySecretKey123456789012345678901");
    }
    
    @Test
    public void testLoadEncryptionKey() {
        String key = KeyManager.loadEncryptionKey();
        assertNotNull(key);
        assertFalse(key.isEmpty());
    }
    
    @Test
    public void testSaveKeyToFile() throws IOException {
        String key = SecurityUtil.generateEncryptionKey();
        Path keyFile = tempDir.resolve("test_key");
        
        boolean result = KeyManager.saveKeyToFile(key, keyFile.toString());
        assertTrue(result);
        assertTrue(Files.exists(keyFile));
    }
    
    @Test
    public void testLoadKeyFromFile() throws IOException {
        String originalKey = SecurityUtil.generateEncryptionKey();
        Path keyFile = tempDir.resolve("test_key");
        
        // Save key
        KeyManager.saveKeyToFile(originalKey, keyFile.toString());
        
        // Load key
        String loadedKey = KeyManager.loadKeyFromFile(keyFile.toString());
        assertEquals(originalKey, loadedKey);
    }
    
    @Test
    public void testLoadKeyFromNonExistentFile() {
        String key = KeyManager.loadKeyFromFile("/nonexistent/path/key");
        assertNull(key);
    }
    
    @Test
    public void testLoadKeyFromEmptyFile() throws IOException {
        Path keyFile = tempDir.resolve("empty_key");
        Files.createFile(keyFile);
        
        String key = KeyManager.loadKeyFromFile(keyFile.toString());
        assertNull(key);
    }
    
    @Test
    public void testValidateKey() {
        String validKey = "ValidKey123456789012345678901234";
        assertTrue(KeyManager.validateKey(validKey));
    }
    
    @Test
    public void testValidateKeyNull() {
        assertFalse(KeyManager.validateKey(null));
    }
    
    @Test
    public void testValidateKeyEmpty() {
        assertFalse(KeyManager.validateKey(""));
    }
    
    @Test
    public void testValidateKeyTooShort() {
        String shortKey = "short";
        assertFalse(KeyManager.validateKey(shortKey));
    }
    
    @Test
    public void testRotateEncryptionKey() throws IOException {
        Path keyFile = tempDir.resolve("test_key");
        System.setProperty("ATTENDANCE_KEY_FILE", keyFile.toString());
        
        String newKey = KeyManager.rotateEncryptionKey();
        assertNotNull(newKey);
        assertTrue(Files.exists(keyFile));
        
        String loadedKey = KeyManager.loadKeyFromFile(keyFile.toString());
        assertEquals(newKey, loadedKey);
    }
    
    @Test
    public void testInitialize() {
        boolean result = KeyManager.initialize();
        assertTrue(result);
    }
    
    @Test
    public void testKeyPersistence() throws IOException {
        String key = SecurityUtil.generateEncryptionKey();
        Path keyFile = tempDir.resolve("persistent_key");
        
        // Save key
        KeyManager.saveKeyToFile(key, keyFile.toString());
        
        // Load key multiple times
        String loaded1 = KeyManager.loadKeyFromFile(keyFile.toString());
        String loaded2 = KeyManager.loadKeyFromFile(keyFile.toString());
        
        assertEquals(key, loaded1);
        assertEquals(key, loaded2);
        assertEquals(loaded1, loaded2);
    }
    
    @Test
    public void testKeyFilePermissions() throws IOException {
        String key = SecurityUtil.generateEncryptionKey();
        Path keyFile = tempDir.resolve("secure_key");
        
        boolean result = KeyManager.saveKeyToFile(key, keyFile.toString());
        assertTrue(result);
        
        // Verify file exists and is readable
        assertTrue(Files.exists(keyFile));
        assertTrue(Files.isReadable(keyFile));
    }
}
