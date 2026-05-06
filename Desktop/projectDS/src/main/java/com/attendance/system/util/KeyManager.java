package com.attendance.system.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

/**
 * Secure key management utility for handling encryption keys.
 * Provides methods to load, store, and manage encryption keys securely.
 */
public class KeyManager {
    private static final Logger logger = LoggerFactory.getLogger(KeyManager.class);
    
    // Default key file location
    private static final String DEFAULT_KEY_FILE = ".attendance_key";
    private static final String KEY_FILE_ENV = "ATTENDANCE_KEY_FILE";
    
    /**
     * Loads encryption key from file or environment variable.
     * Priority: Environment variable > Key file > Default key
     * @return the encryption key
     */
    public static String loadEncryptionKey() {
        // First, try environment variable
        String envKey = System.getenv("ATTENDANCE_ENCRYPTION_KEY");
        if (envKey != null && !envKey.isEmpty()) {
            logger.info("Encryption key loaded from environment variable");
            return envKey;
        }
        
        // Second, try key file
        String keyFilePath = System.getenv(KEY_FILE_ENV);
        if (keyFilePath == null) {
            keyFilePath = DEFAULT_KEY_FILE;
        }
        
        String fileKey = loadKeyFromFile(keyFilePath);
        if (fileKey != null) {
            logger.info("Encryption key loaded from file: " + keyFilePath);
            return fileKey;
        }
        
        // If no key found, generate and save a new one
        logger.warn("No encryption key found. Generating new key.");
        String newKey = SecurityUtil.generateEncryptionKey();
        saveKeyToFile(newKey, keyFilePath);
        return newKey;
    }
    
    /**
     * Loads encryption key from a file.
     * @param filePath the path to the key file
     * @return the encryption key, or null if file doesn't exist
     */
    public static String loadKeyFromFile(String filePath) {
        try {
            Path path = Paths.get(filePath);
            if (!Files.exists(path)) {
                logger.debug("Key file not found: " + filePath);
                return null;
            }
            
            String content = new String(Files.readAllBytes(path), StandardCharsets.UTF_8).trim();
            if (content.isEmpty()) {
                logger.warn("Key file is empty: " + filePath);
                return null;
            }
            
            return content;
        } catch (IOException e) {
            logger.error("Failed to load key from file: " + filePath, e);
            return null;
        }
    }
    
    /**
     * Saves encryption key to a file with restricted permissions.
     * @param key the encryption key to save
     * @param filePath the path to save the key to
     * @return true if save was successful
     */
    public static boolean saveKeyToFile(String key, String filePath) {
        try {
            Path path = Paths.get(filePath);
            
            // Write key to file
            Files.write(path, key.getBytes(StandardCharsets.UTF_8));
            
            // Set file permissions to owner read/write only (600)
            try {
                Set<PosixFilePermission> perms = new HashSet<>();
                perms.add(PosixFilePermission.OWNER_READ);
                perms.add(PosixFilePermission.OWNER_WRITE);
                Files.setPosixFilePermissions(path, perms);
            } catch (UnsupportedOperationException e) {
                // Windows doesn't support POSIX permissions
                logger.debug("POSIX file permissions not supported on this platform");
            }
            
            logger.info("Encryption key saved to file: " + filePath);
            return true;
        } catch (IOException e) {
            logger.error("Failed to save key to file: " + filePath, e);
            return false;
        }
    }
    
    /**
     * Rotates the encryption key by generating a new one and saving it.
     * Note: This requires re-encrypting all existing data with the new key.
     * @return the new encryption key
     */
    public static String rotateEncryptionKey() {
        String newKey = SecurityUtil.generateEncryptionKey();
        String keyFilePath = System.getenv(KEY_FILE_ENV);
        if (keyFilePath == null) {
            keyFilePath = DEFAULT_KEY_FILE;
        }
        
        if (saveKeyToFile(newKey, keyFilePath)) {
            SecurityUtil.setEncryptionKey(newKey);
            logger.info("Encryption key rotated successfully");
            return newKey;
        } else {
            logger.error("Failed to rotate encryption key");
            return null;
        }
    }
    
    /**
     * Validates that an encryption key is properly formatted.
     * @param key the key to validate
     * @return true if key is valid
     */
    public static boolean validateKey(String key) {
        if (key == null || key.isEmpty()) {
            return false;
        }
        
        // Key should be at least 32 characters for AES-256
        if (key.length() < 32) {
            logger.warn("Encryption key is too short: " + key.length() + " characters");
            return false;
        }
        
        return true;
    }
    
    /**
     * Initializes the key manager by loading the encryption key.
     * Should be called during application startup.
     * @return true if initialization was successful
     */
    public static boolean initialize() {
        try {
            String key = loadEncryptionKey();
            if (key != null && validateKey(key)) {
                SecurityUtil.setEncryptionKey(key);
                logger.info("Key manager initialized successfully");
                return true;
            } else {
                logger.error("Failed to initialize key manager: invalid key");
                return false;
            }
        } catch (Exception e) {
            logger.error("Failed to initialize key manager", e);
            return false;
        }
    }
}
