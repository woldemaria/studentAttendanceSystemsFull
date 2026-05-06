package com.attendance.system.util;

import com.attendance.system.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SecurityUtil encryption and security functions.
 */
public class SecurityUtilTest {
    
    private static final String TEST_DATA = "sensitive_data_123";
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PHONE = "+1234567890";
    private static final String TEST_ADDRESS = "123 Main St, City, State 12345";
    
    @BeforeEach
    public void setUp() {
        // Reset encryption key to default for testing
        SecurityUtil.setEncryptionKey("MySecretKey123456789012345678901");
    }
    
    @Test
    public void testEncryptDecryptData() {
        // Encrypt data
        String encrypted = SecurityUtil.encryptData(TEST_DATA);
        assertNotNull(encrypted);
        assertNotEquals(TEST_DATA, encrypted);
        
        // Decrypt data
        String decrypted = SecurityUtil.decryptData(encrypted);
        assertEquals(TEST_DATA, decrypted);
    }
    
    @Test
    public void testEncryptDecryptEmail() {
        // Encrypt email
        String encrypted = SecurityUtil.encryptEmail(TEST_EMAIL);
        assertNotNull(encrypted);
        assertNotEquals(TEST_EMAIL, encrypted);
        
        // Decrypt email
        String decrypted = SecurityUtil.decryptEmail(encrypted);
        assertEquals(TEST_EMAIL, decrypted);
    }
    
    @Test
    public void testEncryptDecryptPhone() {
        // Encrypt phone
        String encrypted = SecurityUtil.encryptPhone(TEST_PHONE);
        assertNotNull(encrypted);
        assertNotEquals(TEST_PHONE, encrypted);
        
        // Decrypt phone
        String decrypted = SecurityUtil.decryptPhone(encrypted);
        assertEquals(TEST_PHONE, decrypted);
    }
    
    @Test
    public void testEncryptDecryptAddress() {
        // Encrypt address
        String encrypted = SecurityUtil.encryptAddress(TEST_ADDRESS);
        assertNotNull(encrypted);
        assertNotEquals(TEST_ADDRESS, encrypted);
        
        // Decrypt address
        String decrypted = SecurityUtil.decryptAddress(encrypted);
        assertEquals(TEST_ADDRESS, decrypted);
    }
    
    @Test
    public void testEncryptNullData() {
        String result = SecurityUtil.encryptData(null);
        assertNull(result);
    }
    
    @Test
    public void testEncryptEmptyData() {
        String result = SecurityUtil.encryptData("");
        assertEquals("", result);
    }
    
    @Test
    public void testDecryptNullData() {
        String result = SecurityUtil.decryptData(null);
        assertNull(result);
    }
    
    @Test
    public void testDecryptEmptyData() {
        String result = SecurityUtil.decryptData("");
        assertEquals("", result);
    }
    
    @Test
    public void testEncryptionWithDifferentKeys() {
        String encrypted1 = SecurityUtil.encryptData(TEST_DATA, "key1_32_bytes_long_for_aes256");
        String encrypted2 = SecurityUtil.encryptData(TEST_DATA, "key2_32_bytes_long_for_aes256");
        
        // Same data encrypted with different keys should produce different results
        assertNotEquals(encrypted1, encrypted2);
    }
    
    @Test
    public void testDecryptionWithWrongKey() {
        String encrypted = SecurityUtil.encryptData(TEST_DATA, "key1_32_bytes_long_for_aes256");
        
        // Decrypting with wrong key should fail or produce garbage
        assertThrows(RuntimeException.class, () -> {
            SecurityUtil.decryptData(encrypted, "key2_32_bytes_long_for_aes256");
        });
    }
    
    @Test
    public void testHashPassword() {
        String password = "SecurePass123!@#";
        String hash = SecurityUtil.hashPassword(password);
        
        assertNotNull(hash);
        assertNotEquals(password, hash);
        assertTrue(hash.length() > 0);
    }
    
    @Test
    public void testVerifyPassword() {
        String password = "SecurePass123!@#";
        String hash = SecurityUtil.hashPassword(password);
        
        assertTrue(SecurityUtil.verifyPassword(password, hash));
        assertFalse(SecurityUtil.verifyPassword("WrongPassword", hash));
    }
    
    @Test
    public void testPasswordHashingWithSalt() {
        String password = "SecurePass123!@#";
        String hash1 = SecurityUtil.hashPassword(password);
        String hash2 = SecurityUtil.hashPassword(password);
        
        // Same password should produce different hashes due to salt
        assertNotEquals(hash1, hash2);
        
        // But both should verify correctly
        assertTrue(SecurityUtil.verifyPassword(password, hash1));
        assertTrue(SecurityUtil.verifyPassword(password, hash2));
    }
    
    @Test
    public void testValidatePasswordValid() {
        String validPassword = "SecurePass123!@#";
        assertDoesNotThrow(() -> SecurityUtil.validatePassword(validPassword));
    }
    
    @Test
    public void testValidatePasswordTooShort() {
        String shortPassword = "Short1!";
        assertThrows(ValidationException.class, () -> SecurityUtil.validatePassword(shortPassword));
    }
    
    @Test
    public void testValidatePasswordNoUppercase() {
        String noUppercase = "securepass123!@#";
        assertThrows(ValidationException.class, () -> SecurityUtil.validatePassword(noUppercase));
    }
    
    @Test
    public void testValidatePasswordNoLowercase() {
        String noLowercase = "SECUREPASS123!@#";
        assertThrows(ValidationException.class, () -> SecurityUtil.validatePassword(noLowercase));
    }
    
    @Test
    public void testValidatePasswordNoNumber() {
        String noNumber = "SecurePass!@#";
        assertThrows(ValidationException.class, () -> SecurityUtil.validatePassword(noNumber));
    }
    
    @Test
    public void testValidatePasswordNoSpecialChar() {
        String noSpecial = "SecurePass123";
        assertThrows(ValidationException.class, () -> SecurityUtil.validatePassword(noSpecial));
    }
    
    @Test
    public void testValidateEmail() {
        String validEmail = "test@example.com";
        assertDoesNotThrow(() -> SecurityUtil.validateEmail(validEmail));
    }
    
    @Test
    public void testValidateEmailInvalid() {
        String invalidEmail = "invalid.email";
        assertThrows(ValidationException.class, () -> SecurityUtil.validateEmail(invalidEmail));
    }
    
    @Test
    public void testValidateUsername() {
        String validUsername = "user_123";
        assertDoesNotThrow(() -> SecurityUtil.validateUsername(validUsername));
    }
    
    @Test
    public void testValidateUsernameTooShort() {
        String shortUsername = "ab";
        assertThrows(ValidationException.class, () -> SecurityUtil.validateUsername(shortUsername));
    }
    
    @Test
    public void testGenerateEncryptionKey() {
        String key1 = SecurityUtil.generateEncryptionKey();
        String key2 = SecurityUtil.generateEncryptionKey();
        
        assertNotNull(key1);
        assertNotNull(key2);
        assertNotEquals(key1, key2);
        assertTrue(key1.length() > 0);
    }
    
    @Test
    public void testGenerateSecureToken() {
        String token1 = SecurityUtil.generateSecureToken(32);
        String token2 = SecurityUtil.generateSecureToken(32);
        
        assertNotNull(token1);
        assertNotNull(token2);
        assertNotEquals(token1, token2);
        assertTrue(token1.length() > 0);
    }
    
    @Test
    public void testSanitizeInput() {
        String dangerous = "test<script>alert('xss')</script>";
        String sanitized = SecurityUtil.sanitizeInput(dangerous);
        
        assertFalse(sanitized.contains("<"));
        assertFalse(sanitized.contains(">"));
        assertFalse(sanitized.contains("'"));
    }
    
    @Test
    public void testIsSafeInput() {
        assertTrue(SecurityUtil.isSafeInput("safe_input_123"));
        assertFalse(SecurityUtil.isSafeInput("unsafe<input>"));
        assertFalse(SecurityUtil.isSafeInput("input'with'quotes"));
    }
    
    @Test
    public void testSetEncryptionKey() {
        String newKey = "NewKey123456789012345678901234";
        SecurityUtil.setEncryptionKey(newKey);
        
        String encrypted = SecurityUtil.encryptData(TEST_DATA);
        String decrypted = SecurityUtil.decryptData(encrypted);
        
        assertEquals(TEST_DATA, decrypted);
    }
    
    @Test
    public void testEncryptionKeyPadding() {
        // Test with short key
        String shortKey = "short";
        SecurityUtil.setEncryptionKey(shortKey);
        
        String encrypted = SecurityUtil.encryptData(TEST_DATA);
        String decrypted = SecurityUtil.decryptData(encrypted);
        
        assertEquals(TEST_DATA, decrypted);
    }
    
    @Test
    public void testMultipleEncryptionsProduceDifferentResults() {
        String encrypted1 = SecurityUtil.encryptData(TEST_DATA);
        String encrypted2 = SecurityUtil.encryptData(TEST_DATA);
        
        // Due to random IV, same data should encrypt differently each time
        assertNotEquals(encrypted1, encrypted2);
        
        // But both should decrypt to the same value
        assertEquals(TEST_DATA, SecurityUtil.decryptData(encrypted1));
        assertEquals(TEST_DATA, SecurityUtil.decryptData(encrypted2));
    }
}
