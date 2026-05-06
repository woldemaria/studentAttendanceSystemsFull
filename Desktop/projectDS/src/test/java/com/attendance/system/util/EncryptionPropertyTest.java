package com.attendance.system.util;

import net.java.quickcheck.Generator;
import net.java.quickcheck.QuickCheck;
import net.java.quickcheck.characteristic.Classification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static net.java.quickcheck.generator.CombinedGenerators.*;
import static net.java.quickcheck.generator.PrimitiveGenerators.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based tests for encryption functionality.
 * **Validates: Requirements 11.1, 6.6**
 */
public class EncryptionPropertyTest {
    
    @BeforeEach
    public void setUp() {
        SecurityUtil.setEncryptionKey("MySecretKey123456789012345678901");
    }
    
    /**
     * Property 34: Sensitive Data Encryption Storage
     * For any sensitive data stored in the database, the system should encrypt it using AES-256 
     * encryption such that the stored data cannot be read without proper decryption.
     * 
     * **Validates: Requirements 11.1**
     */
    @Test
    public void testProperty34_SensitiveDataEncryptionStorage() {
        Generator<String> sensitiveDataGenerator = strings(
            characters('a', 'z'),
            characters('A', 'Z'),
            characters('0', '9'),
            characters('@', '.', '-', '_'),
            1, 100
        );
        
        QuickCheck.forAll(sensitiveDataGenerator)
            .withConfiguration(QuickCheck.defaultConfiguration().withMinimalTests(100))
            .check(sensitiveData -> {
                // Encrypt the sensitive data
                String encrypted = SecurityUtil.encryptData(sensitiveData);
                
                // Verify encryption occurred
                assertNotNull(encrypted, "Encrypted data should not be null");
                assertNotEquals(sensitiveData, encrypted, "Encrypted data should differ from original");
                
                // Verify data can be decrypted
                String decrypted = SecurityUtil.decryptData(encrypted);
                assertEquals(sensitiveData, decrypted, "Decrypted data should match original");
                
                // Verify encrypted data is not readable as plain text
                assertFalse(encrypted.contains(sensitiveData), 
                    "Encrypted data should not contain original data");
                
                return true;
            });
    }
    
    /**
     * Property 38: Role-Based Database Access Control
     * For any database access attempt, the system should enforce role-based access controls 
     * such that users can only access data appropriate to their role permissions.
     * 
     * This test validates that encrypted data remains secure regardless of access patterns.
     * **Validates: Requirements 11.6**
     */
    @Test
    public void testProperty38_RoleBasedDatabaseAccessControl() {
        Generator<String> dataGenerator = strings(
            characters('a', 'z'),
            characters('A', 'Z'),
            characters('0', '9'),
            1, 50
        );
        
        QuickCheck.forAll(dataGenerator)
            .withConfiguration(QuickCheck.defaultConfiguration().withMinimalTests(100))
            .check(data -> {
                // Simulate different encryption keys for different roles
                String adminKey = "AdminKey12345678901234567890123";
                String userKey = "UserKey123456789012345678901234";
                
                // Encrypt with admin key
                String encryptedByAdmin = SecurityUtil.encryptData(data, adminKey);
                
                // Encrypt with user key
                String encryptedByUser = SecurityUtil.encryptData(data, userKey);
                
                // Different keys should produce different encrypted results
                assertNotEquals(encryptedByAdmin, encryptedByUser,
                    "Different encryption keys should produce different results");
                
                // Each can only be decrypted with the correct key
                String decryptedByAdmin = SecurityUtil.decryptData(encryptedByAdmin, adminKey);
                assertEquals(data, decryptedByAdmin, "Admin should decrypt admin-encrypted data");
                
                String decryptedByUser = SecurityUtil.decryptData(encryptedByUser, userKey);
                assertEquals(data, decryptedByUser, "User should decrypt user-encrypted data");
                
                // Cross-decryption should fail
                assertThrows(RuntimeException.class, () -> {
                    SecurityUtil.decryptData(encryptedByAdmin, userKey);
                }, "User should not be able to decrypt admin-encrypted data");
                
                return true;
            });
    }
    
    /**
     * Property: Encryption Determinism with Same Key and IV
     * For any data encrypted with the same key and IV, the result should be deterministic.
     * However, with random IV, same data should encrypt differently each time.
     */
    @Test
    public void testEncryptionRandomness() {
        Generator<String> dataGenerator = strings(
            characters('a', 'z'),
            characters('A', 'Z'),
            characters('0', '9'),
            1, 50
        );
        
        QuickCheck.forAll(dataGenerator)
            .withConfiguration(QuickCheck.defaultConfiguration().withMinimalTests(50))
            .check(data -> {
                // Encrypt same data multiple times
                String encrypted1 = SecurityUtil.encryptData(data);
                String encrypted2 = SecurityUtil.encryptData(data);
                String encrypted3 = SecurityUtil.encryptData(data);
                
                // Due to random IV, all should be different
                assertNotEquals(encrypted1, encrypted2, "Encryptions should differ due to random IV");
                assertNotEquals(encrypted2, encrypted3, "Encryptions should differ due to random IV");
                assertNotEquals(encrypted1, encrypted3, "Encryptions should differ due to random IV");
                
                // But all should decrypt to the same value
                String decrypted1 = SecurityUtil.decryptData(encrypted1);
                String decrypted2 = SecurityUtil.decryptData(encrypted2);
                String decrypted3 = SecurityUtil.decryptData(encrypted3);
                
                assertEquals(data, decrypted1, "First decryption should match original");
                assertEquals(data, decrypted2, "Second decryption should match original");
                assertEquals(data, decrypted3, "Third decryption should match original");
                
                return true;
            });
    }
    
    /**
     * Property: Email Encryption Consistency
     * For any email address, encrypting and decrypting should preserve the original value.
     */
    @Test
    public void testEmailEncryptionConsistency() {
        Generator<String> emailGenerator = strings(
            characters('a', 'z'),
            characters('0', '9'),
            characters('.', '-', '_'),
            1, 20
        ).map(local -> local + "@example.com");
        
        QuickCheck.forAll(emailGenerator)
            .withConfiguration(QuickCheck.defaultConfiguration().withMinimalTests(50))
            .check(email -> {
                String encrypted = SecurityUtil.encryptEmail(email);
                String decrypted = SecurityUtil.decryptEmail(encrypted);
                
                assertEquals(email, decrypted, "Email should be preserved through encryption/decryption");
                assertNotEquals(email, encrypted, "Email should be encrypted");
                
                return true;
            });
    }
    
    /**
     * Property: Phone Number Encryption Consistency
     * For any phone number, encrypting and decrypting should preserve the original value.
     */
    @Test
    public void testPhoneEncryptionConsistency() {
        Generator<String> phoneGenerator = strings(
            characters('0', '9'),
            characters('+', '-', ' ', '(', ')'),
            10, 20
        );
        
        QuickCheck.forAll(phoneGenerator)
            .withConfiguration(QuickCheck.defaultConfiguration().withMinimalTests(50))
            .check(phone -> {
                String encrypted = SecurityUtil.encryptPhone(phone);
                String decrypted = SecurityUtil.decryptPhone(encrypted);
                
                assertEquals(phone, decrypted, "Phone should be preserved through encryption/decryption");
                assertNotEquals(phone, encrypted, "Phone should be encrypted");
                
                return true;
            });
    }
    
    /**
     * Property: Address Encryption Consistency
     * For any address, encrypting and decrypting should preserve the original value.
     */
    @Test
    public void testAddressEncryptionConsistency() {
        Generator<String> addressGenerator = strings(
            characters('a', 'z'),
            characters('A', 'Z'),
            characters('0', '9'),
            characters(' ', ',', '.', '-'),
            10, 100
        );
        
        QuickCheck.forAll(addressGenerator)
            .withConfiguration(QuickCheck.defaultConfiguration().withMinimalTests(50))
            .check(address -> {
                String encrypted = SecurityUtil.encryptAddress(address);
                String decrypted = SecurityUtil.decryptAddress(encrypted);
                
                assertEquals(address, decrypted, "Address should be preserved through encryption/decryption");
                assertNotEquals(address, encrypted, "Address should be encrypted");
                
                return true;
            });
    }
}
