# Data Security and Encryption System Documentation

## Overview

The Student Attendance System implements comprehensive data encryption to protect sensitive information both at rest (in the database) and in transit (during RMI communications). This document describes the encryption architecture, implementation details, and usage guidelines.

## Architecture

### Components

1. **SecurityUtil** - Core encryption/decryption utilities
2. **FieldEncryptor** - Annotation-based field encryption
3. **KeyManager** - Secure key management and storage
4. **RMISSLConfiguration** - SSL/TLS configuration for RMI
5. **Encrypted Annotation** - Marks fields for automatic encryption
6. **EncryptionType Enum** - Specifies encryption types for different data

### Encryption Standards

- **Algorithm**: AES-256 (Advanced Encryption Standard with 256-bit key)
- **Mode**: CBC (Cipher Block Chaining) with random IV (Initialization Vector)
- **Padding**: PKCS5
- **Key Size**: 256 bits (32 bytes)
- **IV Size**: 128 bits (16 bytes)
- **Encoding**: Base64 for storage and transmission

## Key Management

### Key Storage

Encryption keys are managed through multiple layers of security:

1. **Environment Variable** (Highest Priority)
   - Set `ATTENDANCE_ENCRYPTION_KEY` environment variable
   - Recommended for production environments
   - Example: `export ATTENDANCE_ENCRYPTION_KEY="your-32-byte-key-here"`

2. **Key File** (Medium Priority)
   - Default location: `.attendance_key` in application directory
   - Custom location: Set `ATTENDANCE_KEY_FILE` environment variable
   - File permissions: Restricted to owner read/write (600)
   - Example: `export ATTENDANCE_KEY_FILE="/secure/path/to/key"`

3. **Default Key** (Development Only)
   - Used if no environment variable or key file is found
   - Should NOT be used in production
   - Logged as warning when used

### Key Generation

Generate a new encryption key:

```java
String newKey = SecurityUtil.generateEncryptionKey();
```

### Key Rotation

To rotate the encryption key:

```java
String newKey = KeyManager.rotateEncryptionKey();
```

**Important**: Key rotation requires re-encrypting all existing data with the new key.

## Field-Level Encryption

### Using the @Encrypted Annotation

Mark sensitive fields for automatic encryption/decryption:

```java
public class User {
    @Encrypted(type = EncryptionType.EMAIL)
    private String email;
    
    @Encrypted(type = EncryptionType.PHONE)
    private String phoneNumber;
    
    @Encrypted(type = EncryptionType.ADDRESS)
    private String address;
    
    @Encrypted(type = EncryptionType.GENERAL)
    private String sensitiveData;
}
```

### Encryption Types

- **EMAIL**: Optimized for email addresses
- **PHONE**: Optimized for phone numbers
- **ADDRESS**: Optimized for addresses
- **ID_NUMBER**: Optimized for identification numbers
- **FINANCIAL**: Optimized for financial information
- **GENERAL**: Generic encryption for any data

### Automatic Encryption/Decryption

Encrypt all marked fields in an object:

```java
User user = new User();
user.setEmail("test@example.com");
user.setPhone("+1234567890");

// Encrypt fields before storing in database
FieldEncryptor.encryptFields(user);
userDAO.save(user);

// Decrypt fields after retrieving from database
User retrievedUser = userDAO.findById(1);
FieldEncryptor.decryptFields(retrievedUser);
```

### Checking for Encrypted Fields

```java
// Check if a class has encrypted fields
if (FieldEncryptor.hasEncryptedFields(User.class)) {
    // Handle encryption
}

// Get list of encrypted fields
List<Field> encryptedFields = FieldEncryptor.getEncryptedFields(User.class);
```

## Direct Encryption/Decryption

### Generic Data Encryption

```java
// Encrypt data
String sensitiveData = "confidential information";
String encrypted = SecurityUtil.encryptData(sensitiveData);

// Decrypt data
String decrypted = SecurityUtil.decryptData(encrypted);
```

### Specific Field Encryption

```java
// Email encryption
String encryptedEmail = SecurityUtil.encryptEmail("user@example.com");
String decryptedEmail = SecurityUtil.decryptEmail(encryptedEmail);

// Phone encryption
String encryptedPhone = SecurityUtil.encryptPhone("+1234567890");
String decryptedPhone = SecurityUtil.decryptPhone(encryptedPhone);

// Address encryption
String encryptedAddress = SecurityUtil.encryptAddress("123 Main St");
String decryptedAddress = SecurityUtil.decryptAddress(encryptedAddress);
```

### Custom Key Encryption

```java
String customKey = "CustomKey123456789012345678901234";
String encrypted = SecurityUtil.encryptData(data, customKey);
String decrypted = SecurityUtil.decryptData(encrypted, customKey);
```

## RMI SSL/TLS Configuration

### Setup

1. **Generate Keystore and Truststore**

```bash
# Generate server keystore
keytool -genkey -alias attendance-server -keyalg RSA -keysize 2048 \
  -keystore server.keystore -validity 365

# Generate client truststore
keytool -export -alias attendance-server -keystore server.keystore \
  -file server.cer
keytool -import -alias attendance-server -file server.cer \
  -keystore client.truststore
```

2. **Configure Environment Variables**

```bash
export ATTENDANCE_KEYSTORE_PATH="/path/to/server.keystore"
export ATTENDANCE_KEYSTORE_PASSWORD="keystore-password"
export ATTENDANCE_TRUSTSTORE_PATH="/path/to/client.truststore"
export ATTENDANCE_TRUSTSTORE_PASSWORD="truststore-password"
```

3. **Initialize SSL Configuration**

```java
// In server startup code
RMISSLConfiguration.configureSSL();
RMISSLConfiguration.configureRMISocketFactories();
```

### Verification

Enable SSL debugging for troubleshooting:

```java
RMISSLConfiguration.enableSSLDebug();
// ... perform operations ...
RMISSLConfiguration.disableSSLDebug();
```

## Database Integration

### Storing Encrypted Data

When storing user data with encrypted fields:

```java
User user = new User();
user.setUsername("john_doe");
user.setEmail("john@example.com");
user.setPhone("+1234567890");

// Encrypt sensitive fields
FieldEncryptor.encryptFields(user);

// Store in database (email and phone are now encrypted)
userDAO.createUser(user);
```

### Retrieving Encrypted Data

When retrieving user data from the database:

```java
User user = userDAO.findById(1);

// Decrypt sensitive fields
FieldEncryptor.decryptFields(user);

// Now user.getEmail() returns decrypted email
System.out.println(user.getEmail()); // john@example.com
```

## Security Best Practices

### 1. Key Management

- **Never hardcode encryption keys** in source code
- **Use environment variables** for production keys
- **Rotate keys regularly** (at least annually)
- **Backup keys securely** in a separate location
- **Restrict file permissions** on key files (600)

### 2. Encryption Usage

- **Encrypt sensitive data** before storing in database
- **Decrypt only when needed** for display or processing
- **Use appropriate encryption types** for different data
- **Validate encrypted data** before decryption

### 3. RMI Communications

- **Always enable SSL/TLS** for RMI in production
- **Use strong certificates** (2048-bit RSA minimum)
- **Validate server certificates** on client side
- **Monitor SSL/TLS connections** for security issues

### 4. Password Security

- **Use BCrypt** for password hashing (already implemented)
- **Never encrypt passwords** - only hash them
- **Enforce password policies** (minimum 8 characters, mixed case, numbers, symbols)
- **Implement account locking** after failed attempts

### 5. Audit and Monitoring

- **Log all encryption operations** for audit trails
- **Monitor key access** and usage
- **Alert on encryption failures** or anomalies
- **Review encryption logs** regularly

## Performance Considerations

### Encryption Overhead

- **Encryption**: ~1-5ms per field (depends on data size)
- **Decryption**: ~1-5ms per field (depends on data size)
- **Key generation**: ~100-500ms (one-time operation)

### Optimization Tips

1. **Batch operations**: Encrypt/decrypt multiple fields together
2. **Lazy decryption**: Only decrypt fields when needed
3. **Caching**: Cache decrypted values temporarily (with caution)
4. **Connection pooling**: Use connection pooling for database operations

## Troubleshooting

### Common Issues

**Issue**: "Decryption failed" error
- **Cause**: Wrong encryption key or corrupted encrypted data
- **Solution**: Verify encryption key matches the one used for encryption

**Issue**: "Encryption key not found" warning
- **Cause**: Environment variable or key file not configured
- **Solution**: Set `ATTENDANCE_ENCRYPTION_KEY` or `ATTENDANCE_KEY_FILE`

**Issue**: RMI SSL connection fails
- **Cause**: Keystore/truststore not configured or invalid
- **Solution**: Verify keystore paths and passwords in environment variables

**Issue**: Performance degradation
- **Cause**: Excessive encryption/decryption operations
- **Solution**: Review encryption usage and optimize as needed

## Testing

### Unit Tests

Run encryption unit tests:

```bash
mvn test -Dtest=SecurityUtilTest
mvn test -Dtest=FieldEncryptorTest
mvn test -Dtest=KeyManagerTest
```

### Property-Based Tests

Run property-based encryption tests:

```bash
mvn test -Dtest=EncryptionPropertyTest
```

These tests validate:
- Property 34: Sensitive Data Encryption Storage
- Property 38: Role-Based Database Access Control

## Compliance

The encryption system helps meet the following requirements:

- **Requirement 11.1**: Encrypt all sensitive data using AES-256
- **Requirement 11.6**: Implement role-based database access controls
- **Requirement 6.6**: Encrypt data transmitted between client and server
- **Requirement 11.2**: Enforce password policies and secure hashing

## Future Enhancements

1. **Hardware Security Module (HSM)** integration for key storage
2. **Key versioning** for seamless key rotation
3. **Encryption at rest** for database backups
4. **Field-level access control** based on user roles
5. **Encryption performance monitoring** and optimization

## References

- [NIST SP 800-38A: Recommendation for Block Cipher Modes of Operation](https://nvlpubs.nist.gov/nistpubs/Legacy/SP/nistspecialpublication800-38a.pdf)
- [OWASP: Cryptographic Storage Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Cryptographic_Storage_Cheat_Sheet.html)
- [Java Cryptography Architecture (JCA)](https://docs.oracle.com/javase/8/docs/technotes/guides/security/crypto/CryptoSpec.html)
- [Java RMI SSL/TLS Configuration](https://docs.oracle.com/javase/8/docs/technotes/guides/rmi/socketfactory.html)

## Support

For questions or issues related to the encryption system, please contact the development team or refer to the system documentation.
