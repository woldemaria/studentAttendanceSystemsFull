package com.attendance.system.util;

/**
 * Enumeration of encryption types for different sensitive data fields.
 * Each type may have specific encryption/decryption logic.
 */
public enum EncryptionType {
    /**
     * General purpose encryption for any sensitive data
     */
    GENERAL("general"),
    
    /**
     * Encryption for email addresses
     */
    EMAIL("email"),
    
    /**
     * Encryption for phone numbers
     */
    PHONE("phone"),
    
    /**
     * Encryption for addresses
     */
    ADDRESS("address"),
    
    /**
     * Encryption for personal identification numbers
     */
    ID_NUMBER("id_number"),
    
    /**
     * Encryption for financial information
     */
    FINANCIAL("financial");
    
    private final String type;
    
    EncryptionType(String type) {
        this.type = type;
    }
    
    public String getType() {
        return type;
    }
}
