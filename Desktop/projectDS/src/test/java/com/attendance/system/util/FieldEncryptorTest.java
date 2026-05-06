package com.attendance.system.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for FieldEncryptor annotation-based encryption.
 */
public class FieldEncryptorTest {
    
    @BeforeEach
    public void setUp() {
        SecurityUtil.setEncryptionKey("MySecretKey123456789012345678901");
    }
    
    @Test
    public void testEncryptFields() {
        TestEntity entity = new TestEntity();
        entity.setEmail("test@example.com");
        entity.setPhone("+1234567890");
        entity.setAddress("123 Main St");
        entity.setName("John Doe"); // Not encrypted
        
        String originalEmail = entity.getEmail();
        String originalPhone = entity.getPhone();
        String originalAddress = entity.getAddress();
        
        // Encrypt fields
        boolean result = FieldEncryptor.encryptFields(entity);
        assertTrue(result);
        
        // Encrypted values should be different
        assertNotEquals(originalEmail, entity.getEmail());
        assertNotEquals(originalPhone, entity.getPhone());
        assertNotEquals(originalAddress, entity.getAddress());
        
        // Name should remain unchanged (not encrypted)
        assertEquals("John Doe", entity.getName());
    }
    
    @Test
    public void testDecryptFields() {
        TestEntity entity = new TestEntity();
        entity.setEmail("test@example.com");
        entity.setPhone("+1234567890");
        entity.setAddress("123 Main St");
        
        String originalEmail = entity.getEmail();
        String originalPhone = entity.getPhone();
        String originalAddress = entity.getAddress();
        
        // Encrypt then decrypt
        FieldEncryptor.encryptFields(entity);
        FieldEncryptor.decryptFields(entity);
        
        // Values should be restored
        assertEquals(originalEmail, entity.getEmail());
        assertEquals(originalPhone, entity.getPhone());
        assertEquals(originalAddress, entity.getAddress());
    }
    
    @Test
    public void testGetEncryptedFields() {
        List<Field> encryptedFields = FieldEncryptor.getEncryptedFields(TestEntity.class);
        
        assertNotNull(encryptedFields);
        assertEquals(3, encryptedFields.size());
        
        // Check field names
        boolean hasEmail = encryptedFields.stream().anyMatch(f -> f.getName().equals("email"));
        boolean hasPhone = encryptedFields.stream().anyMatch(f -> f.getName().equals("phone"));
        boolean hasAddress = encryptedFields.stream().anyMatch(f -> f.getName().equals("address"));
        
        assertTrue(hasEmail);
        assertTrue(hasPhone);
        assertTrue(hasAddress);
    }
    
    @Test
    public void testHasEncryptedFields() {
        assertTrue(FieldEncryptor.hasEncryptedFields(TestEntity.class));
        assertFalse(FieldEncryptor.hasEncryptedFields(String.class));
    }
    
    @Test
    public void testEncryptFieldsWithNullObject() {
        boolean result = FieldEncryptor.encryptFields(null);
        assertFalse(result);
    }
    
    @Test
    public void testDecryptFieldsWithNullObject() {
        boolean result = FieldEncryptor.decryptFields(null);
        assertFalse(result);
    }
    
    @Test
    public void testEncryptFieldsWithNullValues() {
        TestEntity entity = new TestEntity();
        entity.setEmail(null);
        entity.setPhone(null);
        entity.setAddress(null);
        
        boolean result = FieldEncryptor.encryptFields(entity);
        assertTrue(result);
        
        // Null values should remain null
        assertNull(entity.getEmail());
        assertNull(entity.getPhone());
        assertNull(entity.getAddress());
    }
    
    @Test
    public void testEncryptFieldsWithEmptyValues() {
        TestEntity entity = new TestEntity();
        entity.setEmail("");
        entity.setPhone("");
        entity.setAddress("");
        
        boolean result = FieldEncryptor.encryptFields(entity);
        assertTrue(result);
        
        // Empty values should remain empty
        assertEquals("", entity.getEmail());
        assertEquals("", entity.getPhone());
        assertEquals("", entity.getAddress());
    }
    
    @Test
    public void testEncryptionTypeAnnotation() {
        TestEntity entity = new TestEntity();
        entity.setEmail("test@example.com");
        
        FieldEncryptor.encryptFields(entity);
        
        // Email should be encrypted
        assertNotEquals("test@example.com", entity.getEmail());
        
        // Decrypt and verify
        FieldEncryptor.decryptFields(entity);
        assertEquals("test@example.com", entity.getEmail());
    }
    
    /**
     * Test entity with encrypted fields for testing.
     */
    public static class TestEntity {
        @Encrypted(type = EncryptionType.EMAIL)
        private String email;
        
        @Encrypted(type = EncryptionType.PHONE)
        private String phone;
        
        @Encrypted(type = EncryptionType.ADDRESS)
        private String address;
        
        private String name; // Not encrypted
        
        public String getEmail() {
            return email;
        }
        
        public void setEmail(String email) {
            this.email = email;
        }
        
        public String getPhone() {
            return phone;
        }
        
        public void setPhone(String phone) {
            this.phone = phone;
        }
        
        public String getAddress() {
            return address;
        }
        
        public void setAddress(String address) {
            this.address = address;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
    }
}
