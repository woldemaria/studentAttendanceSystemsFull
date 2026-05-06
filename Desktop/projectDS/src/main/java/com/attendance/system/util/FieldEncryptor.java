package com.attendance.system.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for handling encryption and decryption of fields marked with @Encrypted annotation.
 * Provides methods to automatically encrypt/decrypt object fields based on annotations.
 */
public class FieldEncryptor {
    private static final Logger logger = LoggerFactory.getLogger(FieldEncryptor.class);
    
    /**
     * Encrypts all fields marked with @Encrypted annotation in the given object.
     * @param object the object to encrypt
     * @return true if encryption was successful
     */
    public static boolean encryptFields(Object object) {
        if (object == null) {
            return false;
        }
        
        try {
            Field[] fields = object.getClass().getDeclaredFields();
            for (Field field : fields) {
                if (field.isAnnotationPresent(Encrypted.class)) {
                    field.setAccessible(true);
                    Object value = field.get(object);
                    
                    if (value != null && value instanceof String) {
                        String stringValue = (String) value;
                        if (!stringValue.isEmpty()) {
                            Encrypted encrypted = field.getAnnotation(Encrypted.class);
                            String encryptedValue = encryptFieldValue(stringValue, encrypted.type());
                            field.set(object, encryptedValue);
                        }
                    }
                }
            }
            return true;
        } catch (IllegalAccessException e) {
            logger.error("Failed to encrypt fields", e);
            return false;
        }
    }
    
    /**
     * Decrypts all fields marked with @Encrypted annotation in the given object.
     * @param object the object to decrypt
     * @return true if decryption was successful
     */
    public static boolean decryptFields(Object object) {
        if (object == null) {
            return false;
        }
        
        try {
            Field[] fields = object.getClass().getDeclaredFields();
            for (Field field : fields) {
                if (field.isAnnotationPresent(Encrypted.class)) {
                    field.setAccessible(true);
                    Object value = field.get(object);
                    
                    if (value != null && value instanceof String) {
                        String stringValue = (String) value;
                        if (!stringValue.isEmpty()) {
                            Encrypted encrypted = field.getAnnotation(Encrypted.class);
                            String decryptedValue = decryptFieldValue(stringValue, encrypted.type());
                            field.set(object, decryptedValue);
                        }
                    }
                }
            }
            return true;
        } catch (IllegalAccessException e) {
            logger.error("Failed to decrypt fields", e);
            return false;
        }
    }
    
    /**
     * Gets all fields marked with @Encrypted annotation in the given class.
     * @param clazz the class to inspect
     * @return list of encrypted fields
     */
    public static List<Field> getEncryptedFields(Class<?> clazz) {
        List<Field> encryptedFields = new ArrayList<>();
        Field[] fields = clazz.getDeclaredFields();
        
        for (Field field : fields) {
            if (field.isAnnotationPresent(Encrypted.class)) {
                encryptedFields.add(field);
            }
        }
        
        return encryptedFields;
    }
    
    /**
     * Checks if a class has any encrypted fields.
     * @param clazz the class to check
     * @return true if the class has encrypted fields
     */
    public static boolean hasEncryptedFields(Class<?> clazz) {
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Encrypted.class)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Encrypts a field value based on its encryption type.
     * @param value the value to encrypt
     * @param type the encryption type
     * @return encrypted value
     */
    private static String encryptFieldValue(String value, EncryptionType type) {
        switch (type) {
            case EMAIL:
                return SecurityUtil.encryptEmail(value);
            case PHONE:
                return SecurityUtil.encryptPhone(value);
            case ADDRESS:
                return SecurityUtil.encryptAddress(value);
            case GENERAL:
            default:
                return SecurityUtil.encryptData(value);
        }
    }
    
    /**
     * Decrypts a field value based on its encryption type.
     * @param value the value to decrypt
     * @param type the encryption type
     * @return decrypted value
     */
    private static String decryptFieldValue(String value, EncryptionType type) {
        switch (type) {
            case EMAIL:
                return SecurityUtil.decryptEmail(value);
            case PHONE:
                return SecurityUtil.decryptPhone(value);
            case ADDRESS:
                return SecurityUtil.decryptAddress(value);
            case GENERAL:
            default:
                return SecurityUtil.decryptData(value);
        }
    }
}
