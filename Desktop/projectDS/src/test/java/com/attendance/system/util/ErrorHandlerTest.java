package com.attendance.system.util;

import com.attendance.system.exception.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ErrorHandler functionality.
 */
public class ErrorHandlerTest {
    
    private AtomicInteger callCount;
    
    @BeforeEach
    public void setUp() {
        callCount = new AtomicInteger(0);
    }
    
    @Test
    public void testRetryConfigDefault() {
        ErrorHandler.RetryConfig config = ErrorHandler.RetryConfig.defaultConfig();
        assertNotNull(config);
    }
    
    @Test
    public void testRetryConfigAggressive() {
        ErrorHandler.RetryConfig config = ErrorHandler.RetryConfig.aggressive();
        assertNotNull(config);
    }
    
    @Test
    public void testRetryConfigConservative() {
        ErrorHandler.RetryConfig config = ErrorHandler.RetryConfig.conservative();
        assertNotNull(config);
    }
    
    @Test
    public void testExecuteWithRetrySuccess() throws Exception {
        Callable<String> operation = () -> {
            callCount.incrementAndGet();
            return "Success";
        };
        
        String result = ErrorHandler.executeWithRetry(operation);
        assertEquals("Success", result);
        assertEquals(1, callCount.get());
    }
    
    @Test
    public void testExecuteWithRetryEventualSuccess() throws Exception {
        Callable<String> operation = () -> {
            int count = callCount.incrementAndGet();
            if (count < 3) {
                throw new RuntimeException("Temporary failure");
            }
            return "Success";
        };
        
        String result = ErrorHandler.executeWithRetry(operation);
        assertEquals("Success", result);
        assertEquals(3, callCount.get());
    }
    
    @Test
    public void testExecuteWithRetryFailure() {
        Callable<String> operation = () -> {
            callCount.incrementAndGet();
            throw new RuntimeException("Permanent failure");
        };
        
        assertThrows(Exception.class, () -> ErrorHandler.executeWithRetry(operation));
        assertTrue(callCount.get() > 1);
    }
    
    @Test
    public void testExecuteWithRetryCustomConfig() throws Exception {
        ErrorHandler.RetryConfig config = new ErrorHandler.RetryConfig(2, 100, 1.5, 500);
        
        Callable<String> operation = () -> {
            callCount.incrementAndGet();
            if (callCount.get() < 2) {
                throw new RuntimeException("Temporary failure");
            }
            return "Success";
        };
        
        String result = ErrorHandler.executeWithRetry(operation, config);
        assertEquals("Success", result);
        assertEquals(2, callCount.get());
    }
    
    @Test
    public void testCircuitBreakerCreate() {
        ErrorHandler.CircuitBreaker breaker = ErrorHandler.CircuitBreaker.create("TestBreaker");
        assertNotNull(breaker);
        assertEquals("TestBreaker", breaker.getName());
        assertEquals(ErrorHandler.CircuitBreaker.State.CLOSED, breaker.getState());
    }
    
    @Test
    public void testCircuitBreakerRecordSuccess() {
        ErrorHandler.CircuitBreaker breaker = ErrorHandler.CircuitBreaker.create("TestBreaker");
        breaker.recordSuccess();
        assertEquals(ErrorHandler.CircuitBreaker.State.CLOSED, breaker.getState());
        assertFalse(breaker.isOpen());
    }
    
    @Test
    public void testCircuitBreakerRecordFailure() {
        ErrorHandler.CircuitBreaker breaker = new ErrorHandler.CircuitBreaker("TestBreaker", 3, 60000);
        
        breaker.recordFailure();
        assertFalse(breaker.isOpen());
        
        breaker.recordFailure();
        assertFalse(breaker.isOpen());
        
        breaker.recordFailure();
        assertTrue(breaker.isOpen());
    }
    
    @Test
    public void testCircuitBreakerHalfOpen() throws InterruptedException {
        ErrorHandler.CircuitBreaker breaker = new ErrorHandler.CircuitBreaker("TestBreaker", 1, 100);
        
        breaker.recordFailure();
        assertTrue(breaker.isOpen());
        
        Thread.sleep(150);
        assertFalse(breaker.isOpen());
        assertEquals(ErrorHandler.CircuitBreaker.State.HALF_OPEN, breaker.getState());
    }
    
    @Test
    public void testExecuteWithCircuitBreakerSuccess() throws Exception {
        ErrorHandler.CircuitBreaker breaker = ErrorHandler.CircuitBreaker.create("TestBreaker");
        
        Callable<String> operation = () -> "Success";
        
        String result = ErrorHandler.executeWithCircuitBreaker(breaker, operation);
        assertEquals("Success", result);
        assertEquals(ErrorHandler.CircuitBreaker.State.CLOSED, breaker.getState());
    }
    
    @Test
    public void testExecuteWithCircuitBreakerOpen() throws Exception {
        ErrorHandler.CircuitBreaker breaker = new ErrorHandler.CircuitBreaker("TestBreaker", 1, 60000);
        
        Callable<String> operation = () -> {
            throw new RuntimeException("Operation failed");
        };
        
        assertThrows(Exception.class, () -> ErrorHandler.executeWithCircuitBreaker(breaker, operation));
        assertTrue(breaker.isOpen());
        
        assertThrows(RemoteServiceException.class, () -> 
            ErrorHandler.executeWithCircuitBreaker(breaker, () -> "Success"));
    }
    
    @Test
    public void testExecuteWithFallbackPrimarySuccess() {
        Callable<String> primary = () -> "Primary";
        Callable<String> fallback = () -> "Fallback";
        
        String result = ErrorHandler.executeWithFallback(primary, fallback);
        assertEquals("Primary", result);
    }
    
    @Test
    public void testExecuteWithFallbackPrimaryFailure() {
        Callable<String> primary = () -> {
            throw new RuntimeException("Primary failed");
        };
        Callable<String> fallback = () -> "Fallback";
        
        String result = ErrorHandler.executeWithFallback(primary, fallback);
        assertEquals("Fallback", result);
    }
    
    @Test
    public void testExecuteWithFallbackBothFail() {
        Callable<String> primary = () -> {
            throw new RuntimeException("Primary failed");
        };
        Callable<String> fallback = () -> {
            throw new RuntimeException("Fallback failed");
        };
        
        assertThrows(RuntimeException.class, () -> ErrorHandler.executeWithFallback(primary, fallback));
    }
    
    @Test
    public void testExecuteWithTimeout() throws Exception {
        Callable<String> operation = () -> "Success";
        
        String result = ErrorHandler.executeWithTimeout(operation, 5, TimeUnit.SECONDS);
        assertEquals("Success", result);
    }
    
    @Test
    public void testHandleDatabaseException() {
        DatabaseException exception = DatabaseException.connectionFailed(new RuntimeException("Connection failed"));
        
        assertDoesNotThrow(() -> {
            ErrorHandler.handleDatabaseException(exception, "TestOperation", message -> {
                // Recovery action
            });
        });
    }
    
    @Test
    public void testHandleAuthenticationException() {
        AuthenticationException exception = AuthenticationException.invalidCredentials();
        
        assertDoesNotThrow(() -> {
            ErrorHandler.handleAuthenticationException(exception, "user123", message -> {
                // Security action
            });
        });
    }
    
    @Test
    public void testHandleRemoteServiceException() {
        RemoteServiceException exception = RemoteServiceException.serverUnavailable();
        
        assertDoesNotThrow(() -> {
            ErrorHandler.handleRemoteServiceException(exception, "TestOperation", message -> {
                // Recovery action
            });
        });
    }
    
    @Test
    public void testHandleValidationException() {
        ValidationException exception = ValidationException.required("email");
        
        assertDoesNotThrow(() -> {
            ErrorHandler.handleValidationException(exception, "UserCreation");
        });
    }
    
    @Test
    public void testHandleDatabaseExceptionDuplicateEntry() {
        DatabaseException exception = DatabaseException.duplicateEntry("email");
        
        assertDoesNotThrow(() -> {
            ErrorHandler.handleDatabaseException(exception, "CreateUser", message -> {
                // Recovery action
            });
        });
    }
    
    @Test
    public void testHandleDatabaseExceptionRecordNotFound() {
        DatabaseException exception = DatabaseException.recordNotFound("User", "123");
        
        assertDoesNotThrow(() -> {
            ErrorHandler.handleDatabaseException(exception, "GetUser", message -> {
                // Recovery action
            });
        });
    }
    
    @Test
    public void testHandleAuthenticationExceptionAccountLocked() {
        AuthenticationException exception = AuthenticationException.accountLocked();
        
        assertDoesNotThrow(() -> {
            ErrorHandler.handleAuthenticationException(exception, "user123", message -> {
                // Security action
            });
        });
    }
    
    @Test
    public void testHandleRemoteServiceExceptionConnectionLost() {
        RemoteServiceException exception = RemoteServiceException.connectionLost();
        
        assertDoesNotThrow(() -> {
            ErrorHandler.handleRemoteServiceException(exception, "MarkAttendance", message -> {
                // Recovery action
            });
        });
    }
}
