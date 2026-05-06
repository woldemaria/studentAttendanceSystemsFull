package com.attendance.system.exception;

/**
 * Exception thrown for database-related errors.
 */
public class DatabaseException extends AttendanceSystemException {
    private static final long serialVersionUID = 1L;
    
    public DatabaseException(String message) {
        super("DB_ERROR", message, message);
    }
    
    public DatabaseException(String message, Throwable cause) {
        super("DB_ERROR", message, message, cause);
    }
    
    public DatabaseException(String errorCode, String userMessage, String technicalDetails) {
        super(errorCode, userMessage, technicalDetails);
    }
    
    public DatabaseException(String errorCode, String userMessage, String technicalDetails, Throwable cause) {
        super(errorCode, userMessage, technicalDetails, cause);
    }
    
    // Specific database error types
    public static DatabaseException connectionFailed(Throwable cause) {
        return new DatabaseException("DB_CONNECTION_FAILED", 
                "Unable to connect to database", 
                "Database connection failed: " + cause.getMessage(), 
                cause);
    }
    
    public static DatabaseException queryFailed(String query, Throwable cause) {
        return new DatabaseException("DB_QUERY_FAILED", 
                "Database operation failed", 
                "Query failed: " + query + " - " + cause.getMessage(), 
                cause);
    }
    
    public static DatabaseException duplicateEntry(String field) {
        return new DatabaseException("DB_DUPLICATE_ENTRY", 
                "A record with this " + field + " already exists", 
                "Duplicate entry for field: " + field);
    }
    
    public static DatabaseException recordNotFound(String entity, String identifier) {
        return new DatabaseException("DB_RECORD_NOT_FOUND", 
                entity + " not found", 
                entity + " with identifier '" + identifier + "' not found");
    }
    
    public static DatabaseException constraintViolation(String constraint, Throwable cause) {
        return new DatabaseException("DB_CONSTRAINT_VIOLATION", 
                "Data integrity constraint violated", 
                "Constraint violation: " + constraint, 
                cause);
    }
    
    public static DatabaseException transactionFailed(Throwable cause) {
        return new DatabaseException("DB_TRANSACTION_FAILED", 
                "Transaction could not be completed", 
                "Transaction failed: " + cause.getMessage(), 
                cause);
    }
}