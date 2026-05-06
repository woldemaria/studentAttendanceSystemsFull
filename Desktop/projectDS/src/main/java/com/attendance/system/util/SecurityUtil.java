package com.attendance.system.util;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.attendance.system.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.regex.Pattern;

/**
 * Security utility class for password hashing, encryption, and validation.
 * Provides comprehensive data encryption for sensitive fields using AES-256.
 */
public class SecurityUtil {
    private static final Logger logger = LoggerFactory.getLogger(SecurityUtil.class);
    
    // BCrypt cost factor (higher = more secure but slower)
    private static final int BCRYPT_COST = 12;
    
    // AES encryption algorithm with CBC mode for better security
    private static final String AES_ALGORITHM = "AES";
    private static final String AES_TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final String AES_ECB_TRANSFORMATION = "AES/ECB/PKCS5Padding"; // For backward compatibility
    
    // AES-256 requires 32-byte key
    private static final int AES_256_KEY_SIZE = 256;
    private static final int IV_SIZE = 16; // 128-bit IV for CBC mode
    
    // Password policy patterns
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
        "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$"
    );
    
    // Email validation pattern
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$"
    );
    
    // Username pattern (alphanumeric and underscore, 3-20 characters)
    private static final Pattern USERNAME_PATTERN = Pattern.compile(
        "^[A-Za-z0-9_]{3,20}$"
    );
    
    private static final SecureRandom secureRandom = new SecureRandom();
    
    // Encryption key loaded from secure storage (environment variable or keystore)
    private static String encryptionKey;
    
    // Default encryption key for development (in production, this should be loaded from secure storage)
    private static final String DEFAULT_ENCRYPTION_KEY = "MySecretKey123456789012345678901"; // 32 bytes for AES-256
    
    static {
        // Initialize encryption key from environment or use default
        encryptionKey = System.getenv("ATTENDANCE_ENCRYPTION_KEY");
        if (encryptionKey == null || encryptionKey.isEmpty()) {
            logger.warn("ATTENDANCE_ENCRYPTION_KEY environment variable not set. Using default key for development only.");
            encryptionKey = DEFAULT_ENCRYPTION_KEY;
        }
        // Ensure key is proper length for AES-256
        if (encryptionKey.length() < 32) {
            logger.warn("Encryption key is too short. Padding to 32 bytes.");
            encryptionKey = String.format("%-32s", encryptionKey).replace(' ', '0');
        } else if (encryptionKey.length() > 32) {
            logger.warn("Encryption key is too long. Truncating to 32 bytes.");
            encryptionKey = encryptionKey.substring(0, 32);
        }
    }
    
    /**
     * Hashes a password using BCrypt with salt.
     * @param password the plain text password
     * @return the hashed password
     */
    public static String hashPassword(String password) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        return BCrypt.withDefaults().hashToString(BCRYPT_COST, password.toCharArray());
    }
    
    /**
     * Verifies a password against its hash.
     * @param password the plain text password
     * @param hash the stored hash
     * @return true if password matches the hash
     */
    public static boolean verifyPassword(String password, String hash) {
        if (password == null || hash == null) {
            return false;
        }
        BCrypt.Result result = BCrypt.verifyer().verify(password.toCharArray(), hash);
        return result.verified;
    }
    
    /**
     * Validates password against security policy.
     * @param password the password to validate
     * @throws ValidationException if password doesn't meet policy
     */
    public static void validatePassword(String password) throws ValidationException {
        if (password == null || password.isEmpty()) {
            throw ValidationException.required("password");
        }
        
        if (password.length() < 8) {
            throw ValidationException.tooShort("password", 8);
        }
        
        if (password.length() > 128) {
            throw ValidationException.tooLong("password", 128);
        }
        
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            throw new ValidationException("password", 
                "Password must contain at least one lowercase letter, one uppercase letter, one digit, and one special character (@$!%*?&)");
        }
    }
    
    /**
     * Validates email format.
     * @param email the email to validate
     * @throws ValidationException if email format is invalid
     */
    public static void validateEmail(String email) throws ValidationException {
        if (email == null || email.isEmpty()) {
            throw ValidationException.required("email");
        }
        
        if (email.length() > 100) {
            throw ValidationException.tooLong("email", 100);
        }
        
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw ValidationException.invalidFormat("email", "user@domain.com");
        }
    }
    
    /**
     * Validates username format.
     * @param username the username to validate
     * @throws ValidationException if username format is invalid
     */
    public static void validateUsername(String username) throws ValidationException {
        if (username == null || username.isEmpty()) {
            throw ValidationException.required("username");
        }
        
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            throw new ValidationException("username", 
                "Username must be 3-20 characters long and contain only letters, numbers, and underscores");
        }
    }
    
    /**
     * Validates that a string is not null or empty.
     * @param value the value to validate
     * @param fieldName the field name for error messages
     * @throws ValidationException if value is null or empty
     */
    public static void validateRequired(String value, String fieldName) throws ValidationException {
        if (value == null || value.trim().isEmpty()) {
            throw ValidationException.required(fieldName);
        }
    }
    
    /**
     * Encrypts sensitive data using AES-256 encryption with CBC mode.
     * @param data the data to encrypt
     * @return the encrypted data as Base64 string with IV prepended
     */
    public static String encryptData(String data) {
        return encryptData(data, encryptionKey);
    }
    
    /**
     * Encrypts sensitive data using AES-256 encryption with custom key.
     * Uses CBC mode with random IV for better security.
     * @param data the data to encrypt
     * @param key the encryption key (should be 32 bytes for AES-256)
     * @return the encrypted data as Base64 string with IV prepended
     */
    public static String encryptData(String data, String key) {
        try {
            if (data == null || data.isEmpty()) {
                return data;
            }
            
            // Generate random IV
            byte[] iv = new byte[IV_SIZE];
            secureRandom.nextBytes(iv);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            
            // Prepare key
            byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
            if (keyBytes.length < 32) {
                keyBytes = padKey(keyBytes, 32);
            } else if (keyBytes.length > 32) {
                keyBytes = java.util.Arrays.copyOf(keyBytes, 32);
            }
            
            SecretKeySpec secretKey = new SecretKeySpec(keyBytes, 0, 32, AES_ALGORITHM);
            
            // Encrypt
            Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);
            byte[] encryptedData = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
            
            // Combine IV and encrypted data, then encode
            byte[] combined = new byte[iv.length + encryptedData.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encryptedData, 0, combined, iv.length, encryptedData.length);
            
            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            logger.error("Encryption failed for data", e);
            throw new RuntimeException("Encryption failed", e);
        }
    }
    
    /**
     * Decrypts data using AES-256 decryption with CBC mode.
     * @param encryptedData the encrypted data as Base64 string with IV prepended
     * @return the decrypted data
     */
    public static String decryptData(String encryptedData) {
        return decryptData(encryptedData, encryptionKey);
    }
    
    /**
     * Decrypts data using AES-256 decryption with custom key.
     * Expects IV to be prepended to the encrypted data.
     * @param encryptedData the encrypted data as Base64 string with IV prepended
     * @param key the decryption key (should be 32 bytes for AES-256)
     * @return the decrypted data
     */
    public static String decryptData(String encryptedData, String key) {
        try {
            if (encryptedData == null || encryptedData.isEmpty()) {
                return encryptedData;
            }
            
            // Decode from Base64
            byte[] combined = Base64.getDecoder().decode(encryptedData);
            
            // Extract IV and encrypted data
            byte[] iv = new byte[IV_SIZE];
            byte[] encrypted = new byte[combined.length - IV_SIZE];
            System.arraycopy(combined, 0, iv, 0, IV_SIZE);
            System.arraycopy(combined, IV_SIZE, encrypted, 0, encrypted.length);
            
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            
            // Prepare key
            byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
            if (keyBytes.length < 32) {
                keyBytes = padKey(keyBytes, 32);
            } else if (keyBytes.length > 32) {
                keyBytes = java.util.Arrays.copyOf(keyBytes, 32);
            }
            
            SecretKeySpec secretKey = new SecretKeySpec(keyBytes, 0, 32, AES_ALGORITHM);
            
            // Decrypt
            Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);
            byte[] decryptedData = cipher.doFinal(encrypted);
            
            return new String(decryptedData, StandardCharsets.UTF_8);
        } catch (Exception e) {
            logger.error("Decryption failed for data", e);
            throw new RuntimeException("Decryption failed", e);
        }
    }
    
    /**
     * Encrypts email address for storage in database.
     * @param email the email to encrypt
     * @return encrypted email
     */
    public static String encryptEmail(String email) {
        if (email == null || email.isEmpty()) {
            return email;
        }
        return encryptData(email);
    }
    
    /**
     * Decrypts email address from database.
     * @param encryptedEmail the encrypted email
     * @return decrypted email
     */
    public static String decryptEmail(String encryptedEmail) {
        if (encryptedEmail == null || encryptedEmail.isEmpty()) {
            return encryptedEmail;
        }
        return decryptData(encryptedEmail);
    }
    
    /**
     * Encrypts phone number for storage in database.
     * @param phone the phone number to encrypt
     * @return encrypted phone number
     */
    public static String encryptPhone(String phone) {
        if (phone == null || phone.isEmpty()) {
            return phone;
        }
        return encryptData(phone);
    }
    
    /**
     * Decrypts phone number from database.
     * @param encryptedPhone the encrypted phone number
     * @return decrypted phone number
     */
    public static String decryptPhone(String encryptedPhone) {
        if (encryptedPhone == null || encryptedPhone.isEmpty()) {
            return encryptedPhone;
        }
        return decryptData(encryptedPhone);
    }
    
    /**
     * Encrypts address for storage in database.
     * @param address the address to encrypt
     * @return encrypted address
     */
    public static String encryptAddress(String address) {
        if (address == null || address.isEmpty()) {
            return address;
        }
        return encryptData(address);
    }
    
    /**
     * Decrypts address from database.
     * @param encryptedAddress the encrypted address
     * @return decrypted address
     */
    public static String decryptAddress(String encryptedAddress) {
        if (encryptedAddress == null || encryptedAddress.isEmpty()) {
            return encryptedAddress;
        }
        return decryptData(encryptedAddress);
    }
    
    /**
     * Pads a key to the specified length using zero padding.
     * @param key the key to pad
     * @param length the target length
     * @return padded key
     */
    private static byte[] padKey(byte[] key, int length) {
        byte[] paddedKey = new byte[length];
        System.arraycopy(key, 0, paddedKey, 0, key.length);
        return paddedKey;
    }
    
    /**
     * Generates a random AES-256 encryption key.
     * @return Base64 encoded encryption key
     */
    public static String generateEncryptionKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(AES_ALGORITHM);
            keyGenerator.init(AES_256_KEY_SIZE);
            SecretKey secretKey = keyGenerator.generateKey();
            return Base64.getEncoder().encodeToString(secretKey.getEncoded());
        } catch (NoSuchAlgorithmException e) {
            logger.error("Failed to generate encryption key", e);
            throw new RuntimeException("Failed to generate encryption key", e);
        }
    }
    
    /**
     * Sets the encryption key for the session.
     * Should be called during application initialization with key from secure storage.
     * @param key the encryption key (should be 32 bytes for AES-256)
     */
    public static void setEncryptionKey(String key) {
        if (key == null || key.isEmpty()) {
            logger.warn("Attempted to set null or empty encryption key");
            return;
        }
        encryptionKey = key;
        if (encryptionKey.length() < 32) {
            encryptionKey = String.format("%-32s", encryptionKey).replace(' ', '0');
        } else if (encryptionKey.length() > 32) {
            encryptionKey = encryptionKey.substring(0, 32);
        }
        logger.info("Encryption key updated");
    }
    
    /**
     * Gets the current encryption key (for testing purposes only).
     * @return the current encryption key
     */
    public static String getEncryptionKey() {
        return encryptionKey;
    }
    
    /**
     * Loads encryption key from environment variable.
     * @return true if key was loaded successfully
     */
    public static boolean loadEncryptionKeyFromEnvironment() {
        String envKey = System.getenv("ATTENDANCE_ENCRYPTION_KEY");
        if (envKey != null && !envKey.isEmpty()) {
            setEncryptionKey(envKey);
            logger.info("Encryption key loaded from environment variable");
            return true;
        }
        logger.warn("ATTENDANCE_ENCRYPTION_KEY environment variable not found");
        return false;
    }
    
    /**
     * Generates a secure random token for session management.
     * @param length the length of the token
     * @return the random token
     */
    public static String generateSecureToken(int length) {
        byte[] bytes = new byte[length];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
    
    /**
     * Sanitizes input to prevent injection attacks.
     * @param input the input to sanitize
     * @return sanitized input
     */
    public static String sanitizeInput(String input) {
        if (input == null) {
            return null;
        }
        // Remove potentially dangerous characters
        return input.replaceAll("[<>\"'%;()&+]", "").trim();
    }
    
    /**
     * Checks if a string contains only safe characters.
     * @param input the input to check
     * @return true if input is safe
     */
    public static boolean isSafeInput(String input) {
        if (input == null) {
            return true;
        }
        // Check for potentially dangerous patterns
        String dangerous = "[<>\"'%;()&+]";
        return !input.matches(".*" + dangerous + ".*");
    }
}
    
    /**
     * Convenience method for encrypting data.
     * @param data the data to encrypt
     * @return the encrypted data as Base64 string
     */
    public static String encrypt(String data) {
        return encryptData(data);
    }
    
    /**
     * Convenience method for decrypting data.
     * @param encryptedData the encrypted data as Base64 string
     * @return the decrypted data
     */
    public static String decrypt(String encryptedData) {
        return decryptData(encryptedData);
    }
}