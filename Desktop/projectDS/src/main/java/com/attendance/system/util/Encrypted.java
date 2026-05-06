package com.attendance.system.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark fields that should be encrypted when stored in the database.
 * This annotation is used to identify sensitive fields that require encryption/decryption.
 * 
 * Usage:
 * @Encrypted
 * private String email;
 * 
 * @Encrypted(type = EncryptionType.PHONE)
 * private String phoneNumber;
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Encrypted {
    /**
     * The type of encryption to apply to this field.
     * Defaults to GENERAL for generic data encryption.
     */
    EncryptionType type() default EncryptionType.GENERAL;
    
    /**
     * Whether this field is required to be encrypted.
     * If true, null or empty values will not be encrypted.
     */
    boolean required() default false;
}
