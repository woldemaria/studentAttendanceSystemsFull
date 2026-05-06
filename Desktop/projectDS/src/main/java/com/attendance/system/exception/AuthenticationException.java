package com.attendance.system.exception;

/**
 * Exception thrown for authentication-related errors.
 */
public class AuthenticationException extends AttendanceSystemException {
    private static final long serialVersionUID = 1L;
    
    public AuthenticationException(String message) {
        super("AUTH_ERROR", message, message);
    }
    
    public AuthenticationException(String message, Throwable cause) {
        super("AUTH_ERROR", message, message, cause);
    }
    
    public AuthenticationException(String errorCode, String message) {
        super(errorCode, message, message);
    }
    
    // Specific authentication error types
    public static AuthenticationException invalidCredentials() {
        return new AuthenticationException("INVALID_CREDENTIALS", "Invalid username or password");
    }
    
    public static AuthenticationException accountLocked() {
        return new AuthenticationException("ACCOUNT_LOCKED", "Account has been locked due to multiple failed login attempts");
    }
    
    public static AuthenticationException accountDisabled() {
        return new AuthenticationException("ACCOUNT_DISABLED", "Account has been disabled");
    }
    
    public static AuthenticationException sessionExpired() {
        return new AuthenticationException("SESSION_EXPIRED", "Your session has expired. Please log in again");
    }
    
    public static AuthenticationException insufficientPermissions() {
        return new AuthenticationException("INSUFFICIENT_PERMISSIONS", "You do not have permission to perform this action");
    }
}