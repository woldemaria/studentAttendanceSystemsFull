package com.attendance.system.util;

import com.attendance.system.exception.*;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Comprehensive error handling utility providing retry mechanisms, circuit breaker,
 * and fallback operations for resilient system operation.
 */
public class ErrorHandler {
    
    private static final int DEFAULT_MAX_RETRIES = 3;
    private static final long DEFAULT_RETRY_DELAY_MS = 1000;
    private static final double DEFAULT_BACKOFF_MULTIPLIER = 2.0;
    private static final int DEFAULT_CIRCUIT_BREAKER_THRESHOLD = 5;
    private static final long DEFAULT_CIRCUIT_BREAKER_TIMEOUT_MS = 60000;
    
    /**
     * Retry configuration for automatic retry logic.
     */
    public static class RetryConfig {
        private final int maxRetries;
        private final long initialDelayMs;
        private final double backoffMultiplier;
        private final long maxDelayMs;
        
        public RetryConfig(int maxRetries, long initialDelayMs, double backoffMultiplier, long maxDelayMs) {
            this.maxRetries = maxRetries;
            this.initialDelayMs = initialDelayMs;
            this.backoffMultiplier = backoffMultiplier;
            this.maxDelayMs = maxDelayMs;
        }
        
        public static RetryConfig defaultConfig() {
            return new RetryConfig(DEFAULT_MAX_RETRIES, DEFAULT_RETRY_DELAY_MS, 
                    DEFAULT_BACKOFF_MULTIPLIER, 30000);
        }
        
        public static RetryConfig aggressive() {
            return new RetryConfig(5, 500, 1.5, 10000);
        }
        
        public static RetryConfig conservative() {
            return new RetryConfig(2, 2000, 2.0, 30000);
        }
    }
    
    /**
     * Circuit breaker for preventing cascading failures.
     */
    public static class CircuitBreaker {
        private final String name;
        private final int failureThreshold;
        private final long timeoutMs;
        
        private int failureCount = 0;
        private long lastFailureTime = 0;
        private State state = State.CLOSED;
        
        public enum State {
            CLOSED, OPEN, HALF_OPEN
        }
        
        public CircuitBreaker(String name, int failureThreshold, long timeoutMs) {
            this.name = name;
            this.failureThreshold = failureThreshold;
            this.timeoutMs = timeoutMs;
        }
        
        public static CircuitBreaker create(String name) {
            return new CircuitBreaker(name, DEFAULT_CIRCUIT_BREAKER_THRESHOLD, 
                    DEFAULT_CIRCUIT_BREAKER_TIMEOUT_MS);
        }
        
        public synchronized void recordSuccess() {
            failureCount = 0;
            state = State.CLOSED;
            SystemLogger.log(SystemLogger.LogCategory.SYSTEM, SystemLogger.LogLevel.DEBUG,
                    "Circuit breaker '" + name + "' closed - operation successful");
        }
        
        public synchronized void recordFailure() {
            failureCount++;
            lastFailureTime = System.currentTimeMillis();
            
            if (failureCount >= failureThreshold) {
                state = State.OPEN;
                SystemLogger.logSecurityEvent("Circuit breaker opened", 
                        "Circuit breaker '" + name + "' opened after " + failureCount + " failures",
                        SystemLogger.SecurityLevel.HIGH);
            }
        }
        
        public synchronized boolean isOpen() {
            if (state == State.OPEN) {
                long timeSinceLastFailure = System.currentTimeMillis() - lastFailureTime;
                if (timeSinceLastFailure > timeoutMs) {
                    state = State.HALF_OPEN;
                    failureCount = 0;
                    SystemLogger.log(SystemLogger.LogCategory.SYSTEM, SystemLogger.LogLevel.INFO,
                            "Circuit breaker '" + name + "' half-open - attempting recovery");
                    return false;
                }
                return true;
            }
            return false;
        }
        
        public String getName() {
            return name;
        }
        
        public State getState() {
            return state;
        }
    }
    
    /**
     * Executes an operation with automatic retry using exponential backoff.
     * 
     * @param operation Operation to execute
     * @param config Retry configuration
     * @param <T> Return type
     * @return Result of the operation
     * @throws Exception If all retries fail
     */
    public static <T> T executeWithRetry(Callable<T> operation, RetryConfig config) throws Exception {
        Exception lastException = null;
        long delayMs = config.initialDelayMs;
        
        for (int attempt = 0; attempt <= config.maxRetries; attempt++) {
            try {
                T result = operation.call();
                if (attempt > 0) {
                    SystemLogger.log(SystemLogger.LogCategory.SYSTEM, SystemLogger.LogLevel.INFO,
                            "Operation succeeded after " + attempt + " retries");
                }
                return result;
            } catch (Exception e) {
                lastException = e;
                
                if (attempt < config.maxRetries) {
                    SystemLogger.log(SystemLogger.LogCategory.SYSTEM, SystemLogger.LogLevel.WARN,
                            "Operation failed (attempt " + (attempt + 1) + "/" + (config.maxRetries + 1) + 
                            "), retrying in " + delayMs + "ms", e);
                    
                    Thread.sleep(delayMs);
                    delayMs = Math.min((long)(delayMs * config.backoffMultiplier), config.maxDelayMs);
                } else {
                    SystemLogger.log(SystemLogger.LogCategory.SYSTEM, SystemLogger.LogLevel.ERROR,
                            "Operation failed after " + (attempt + 1) + " attempts", e);
                }
            }
        }
        
        throw lastException;
    }
    
    /**
     * Executes an operation with automatic retry using default configuration.
     * 
     * @param operation Operation to execute
     * @param <T> Return type
     * @return Result of the operation
     * @throws Exception If all retries fail
     */
    public static <T> T executeWithRetry(Callable<T> operation) throws Exception {
        return executeWithRetry(operation, RetryConfig.defaultConfig());
    }
    
    /**
     * Executes an operation with circuit breaker protection.
     * 
     * @param breaker Circuit breaker instance
     * @param operation Operation to execute
     * @param <T> Return type
     * @return Result of the operation
     * @throws RemoteServiceException If circuit breaker is open
     * @throws Exception If operation fails
     */
    public static <T> T executeWithCircuitBreaker(CircuitBreaker breaker, Callable<T> operation) 
            throws Exception {
        if (breaker.isOpen()) {
            throw RemoteServiceException.serverUnavailable();
        }
        
        try {
            T result = operation.call();
            breaker.recordSuccess();
            return result;
        } catch (Exception e) {
            breaker.recordFailure();
            throw e;
        }
    }
    
    /**
     * Executes an operation with fallback handling.
     * 
     * @param operation Primary operation to execute
     * @param fallback Fallback operation if primary fails
     * @param <T> Return type
     * @return Result of primary operation or fallback
     */
    public static <T> T executeWithFallback(Callable<T> operation, Callable<T> fallback) {
        try {
            return operation.call();
        } catch (Exception e) {
            SystemLogger.log(SystemLogger.LogCategory.SYSTEM, SystemLogger.LogLevel.WARN,
                    "Primary operation failed, executing fallback", e);
            try {
                return fallback.call();
            } catch (Exception fallbackException) {
                SystemLogger.log(SystemLogger.LogCategory.SYSTEM, SystemLogger.LogLevel.ERROR,
                        "Fallback operation also failed", fallbackException);
                throw new RuntimeException("Both primary and fallback operations failed", fallbackException);
            }
        }
    }
    
    /**
     * Executes an operation with timeout.
     * 
     * @param operation Operation to execute
     * @param timeout Timeout duration
     * @param unit Timeout unit
     * @param <T> Return type
     * @return Result of the operation
     * @throws Exception If operation times out or fails
     */
    public static <T> T executeWithTimeout(Callable<T> operation, long timeout, TimeUnit unit) 
            throws Exception {
        long startTime = System.currentTimeMillis();
        long timeoutMs = unit.toMillis(timeout);
        
        try {
            return operation.call();
        } catch (Exception e) {
            long elapsedTime = System.currentTimeMillis() - startTime;
            if (elapsedTime >= timeoutMs) {
                throw RemoteServiceException.timeout();
            }
            throw e;
        }
    }
    
    /**
     * Handles database exceptions with appropriate recovery strategies.
     * 
     * @param exception Database exception
     * @param operation Operation that failed
     * @param recoveryAction Action to take for recovery
     */
    public static void handleDatabaseException(DatabaseException exception, String operation, 
                                               Consumer<String> recoveryAction) {
        SystemLogger.logSystemError(exception, "Database Operation: " + operation, 
                exception.getTechnicalDetails());
        
        String errorCode = exception.getErrorCode();
        
        switch (errorCode) {
            case "DB_CONNECTION_FAILED":
                SystemLogger.logSecurityEvent("Database connection failure", 
                        "Failed to connect to database", SystemLogger.SecurityLevel.HIGH);
                recoveryAction.accept("Attempting to reconnect to database");
                break;
                
            case "DB_QUERY_FAILED":
                SystemLogger.log(SystemLogger.LogCategory.DATABASE, SystemLogger.LogLevel.ERROR,
                        "Query execution failed: " + exception.getTechnicalDetails());
                break;
                
            case "DB_DUPLICATE_ENTRY":
                SystemLogger.log(SystemLogger.LogCategory.DATABASE, SystemLogger.LogLevel.WARN,
                        "Duplicate entry detected: " + exception.getUserMessage());
                break;
                
            case "DB_RECORD_NOT_FOUND":
                SystemLogger.log(SystemLogger.LogCategory.DATABASE, SystemLogger.LogLevel.DEBUG,
                        "Record not found: " + exception.getUserMessage());
                break;
                
            case "DB_CONSTRAINT_VIOLATION":
                SystemLogger.logSecurityEvent("Data integrity constraint violation", 
                        exception.getTechnicalDetails(), SystemLogger.SecurityLevel.MEDIUM);
                break;
                
            case "DB_TRANSACTION_FAILED":
                SystemLogger.logSecurityEvent("Transaction failure", 
                        "Database transaction could not be completed", SystemLogger.SecurityLevel.HIGH);
                recoveryAction.accept("Rolling back transaction and retrying");
                break;
                
            default:
                SystemLogger.log(SystemLogger.LogCategory.DATABASE, SystemLogger.LogLevel.ERROR,
                        "Unknown database error: " + exception.getUserMessage());
        }
    }
    
    /**
     * Handles authentication exceptions with appropriate security responses.
     * 
     * @param exception Authentication exception
     * @param userId User ID attempting authentication
     * @param securityAction Action to take for security
     */
    public static void handleAuthenticationException(AuthenticationException exception, String userId, 
                                                     Consumer<String> securityAction) {
        String errorCode = exception.getErrorCode();
        
        switch (errorCode) {
            case "INVALID_CREDENTIALS":
                SystemLogger.logSecurityEvent("Invalid credentials", 
                        "User " + userId + " provided invalid credentials", 
                        SystemLogger.SecurityLevel.MEDIUM, userId, null);
                break;
                
            case "ACCOUNT_LOCKED":
                SystemLogger.logSecurityEvent("Account locked", 
                        "Account for user " + userId + " is locked", 
                        SystemLogger.SecurityLevel.HIGH, userId, null);
                securityAction.accept("Account locked - notifying administrators");
                break;
                
            case "ACCOUNT_DISABLED":
                SystemLogger.logSecurityEvent("Account disabled", 
                        "Account for user " + userId + " is disabled", 
                        SystemLogger.SecurityLevel.MEDIUM, userId, null);
                break;
                
            case "SESSION_EXPIRED":
                SystemLogger.log(SystemLogger.LogCategory.AUTHENTICATION, SystemLogger.LogLevel.INFO,
                        "Session expired for user " + userId);
                break;
                
            case "INSUFFICIENT_PERMISSIONS":
                SystemLogger.logSecurityEvent("Insufficient permissions", 
                        "User " + userId + " attempted unauthorized operation", 
                        SystemLogger.SecurityLevel.MEDIUM, userId, null);
                break;
                
            default:
                SystemLogger.logSecurityEvent("Authentication error", 
                        "Unknown authentication error for user " + userId, 
                        SystemLogger.SecurityLevel.MEDIUM, userId, null);
        }
    }
    
    /**
     * Handles RMI communication exceptions with recovery strategies.
     * 
     * @param exception Remote service exception
     * @param operation Operation that failed
     * @param recoveryAction Action to take for recovery
     */
    public static void handleRemoteServiceException(RemoteServiceException exception, String operation, 
                                                    Consumer<String> recoveryAction) {
        String errorCode = exception.getErrorCode();
        
        switch (errorCode) {
            case "RMI_CONNECTION_LOST":
                SystemLogger.logSecurityEvent("RMI connection lost", 
                        "Connection to RMI server lost during operation: " + operation, 
                        SystemLogger.SecurityLevel.HIGH);
                recoveryAction.accept("Attempting to reconnect to server");
                break;
                
            case "RMI_SERVER_UNAVAILABLE":
                SystemLogger.logSecurityEvent("RMI server unavailable", 
                        "RMI server is not responding", SystemLogger.SecurityLevel.HIGH);
                recoveryAction.accept("Server is unavailable - retrying later");
                break;
                
            case "RMI_TIMEOUT":
                SystemLogger.log(SystemLogger.LogCategory.RMI, SystemLogger.LogLevel.WARN,
                        "RMI request timeout for operation: " + operation);
                recoveryAction.accept("Request timed out - retrying with longer timeout");
                break;
                
            case "RMI_REGISTRY_NOT_FOUND":
                SystemLogger.logSecurityEvent("RMI registry not found", 
                        "Cannot locate RMI registry", SystemLogger.SecurityLevel.CRITICAL);
                recoveryAction.accept("RMI registry not found - check server configuration");
                break;
                
            case "RMI_SERVICE_NOT_BOUND":
                SystemLogger.logSecurityEvent("RMI service not bound", 
                        "Service not bound in RMI registry", SystemLogger.SecurityLevel.HIGH);
                recoveryAction.accept("Service not available - check server status");
                break;
                
            default:
                SystemLogger.log(SystemLogger.LogCategory.RMI, SystemLogger.LogLevel.ERROR,
                        "Unknown RMI error during operation: " + operation, exception);
        }
    }
    
    /**
     * Handles validation exceptions with user-friendly error messages.
     * 
     * @param exception Validation exception
     * @param context Context where validation failed
     */
    public static void handleValidationException(ValidationException exception, String context) {
        SystemLogger.log(SystemLogger.LogCategory.BUSINESS_LOGIC, SystemLogger.LogLevel.WARN,
                "Validation failed in " + context + ": " + exception.getUserMessage());
        
        if (exception.getValidationErrors() != null && !exception.getValidationErrors().isEmpty()) {
            for (ValidationException.ValidationError error : exception.getValidationErrors()) {
                SystemLogger.log(SystemLogger.LogCategory.BUSINESS_LOGIC, SystemLogger.LogLevel.DEBUG,
                        "Validation error - Field: " + error.getField() + ", Message: " + error.getMessage());
            }
        }
    }
}
