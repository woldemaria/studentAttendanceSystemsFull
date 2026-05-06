package com.attendance.system.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SystemLogger functionality.
 */
public class SystemLoggerTest {
    
    @BeforeEach
    public void setUp() {
        // Setup for each test
    }
    
    @Test
    public void testLogUserActivity() {
        // Should not throw exception
        assertDoesNotThrow(() -> {
            SystemLogger.logUserActivity("user123", "LOGIN", "User logged in successfully");
        });
    }
    
    @Test
    public void testLogUserActivityWithContext() {
        Map<String, String> context = new HashMap<>();
        context.put("ipAddress", "192.168.1.1");
        context.put("browser", "Chrome");
        
        assertDoesNotThrow(() -> {
            SystemLogger.logUserActivity("user123", "LOGIN", "User logged in", context);
        });
    }
    
    @Test
    public void testLogSecurityEvent() {
        assertDoesNotThrow(() -> {
            SystemLogger.logSecurityEvent("Unauthorized access attempt", 
                    "User attempted to access restricted resource", 
                    SystemLogger.SecurityLevel.HIGH);
        });
    }
    
    @Test
    public void testLogSecurityEventWithContext() {
        Map<String, String> context = new HashMap<>();
        context.put("resource", "/admin/users");
        context.put("method", "DELETE");
        
        assertDoesNotThrow(() -> {
            SystemLogger.logSecurityEvent("Unauthorized access attempt", 
                    "User attempted to access restricted resource", 
                    SystemLogger.SecurityLevel.HIGH, "user123", context);
        });
    }
    
    @Test
    public void testLogSystemError() {
        Exception testException = new RuntimeException("Test error");
        
        assertDoesNotThrow(() -> {
            SystemLogger.logSystemError(testException, "TestContext");
        });
    }
    
    @Test
    public void testLogSystemErrorWithDetails() {
        Exception testException = new RuntimeException("Test error");
        
        assertDoesNotThrow(() -> {
            SystemLogger.logSystemError(testException, "TestContext", "Additional error details");
        });
    }
    
    @Test
    public void testLogPerformanceMetric() {
        assertDoesNotThrow(() -> {
            SystemLogger.logPerformanceMetric("DatabaseQuery", 250);
        });
    }
    
    @Test
    public void testLogPerformanceMetricWithContext() {
        Map<String, String> context = new HashMap<>();
        context.put("query", "SELECT * FROM users");
        context.put("rows", "1000");
        
        assertDoesNotThrow(() -> {
            SystemLogger.logPerformanceMetric("DatabaseQuery", 250, context);
        });
    }
    
    @Test
    public void testLogDatabaseOperation() {
        assertDoesNotThrow(() -> {
            SystemLogger.logDatabaseOperation("SELECT * FROM users WHERE id = ?", 150);
        });
    }
    
    @Test
    public void testLogDatabaseOperationWithContext() {
        Map<String, String> context = new HashMap<>();
        context.put("table", "users");
        context.put("operation", "SELECT");
        
        assertDoesNotThrow(() -> {
            SystemLogger.logDatabaseOperation("SELECT * FROM users", 150, context);
        });
    }
    
    @Test
    public void testLogAuditTrail() {
        assertDoesNotThrow(() -> {
            SystemLogger.logAuditTrail("admin123", "CREATE_USER", "User", "user456", "SUCCESS");
        });
    }
    
    @Test
    public void testLogAuditTrailWithDetails() {
        assertDoesNotThrow(() -> {
            SystemLogger.logAuditTrail("admin123", "CREATE_USER", "User", "user456", "SUCCESS", 
                    "Created new teacher account");
        });
    }
    
    @Test
    public void testLogWithCategory() {
        assertDoesNotThrow(() -> {
            SystemLogger.log(SystemLogger.LogCategory.AUTHENTICATION, SystemLogger.LogLevel.INFO, 
                    "User authentication successful");
        });
    }
    
    @Test
    public void testLogWithCategoryAndException() {
        Exception testException = new RuntimeException("Test error");
        
        assertDoesNotThrow(() -> {
            SystemLogger.log(SystemLogger.LogCategory.DATABASE, SystemLogger.LogLevel.ERROR, 
                    "Database operation failed", testException);
        });
    }
    
    @Test
    public void testLogRMICall() {
        assertDoesNotThrow(() -> {
            SystemLogger.logRMICall("authenticateUser", "client123", "SUCCESS");
        });
    }
    
    @Test
    public void testLogRMICallWithDuration() {
        assertDoesNotThrow(() -> {
            SystemLogger.logRMICall("authenticateUser", "client123", "SUCCESS", 250);
        });
    }
    
    @Test
    public void testLogConnectionPoolEvent() {
        assertDoesNotThrow(() -> {
            SystemLogger.logConnectionPoolEvent("Connection acquired", 5, 3);
        });
    }
    
    @Test
    public void testLogNotificationEvent() {
        assertDoesNotThrow(() -> {
            SystemLogger.logNotificationEvent("LOW_ATTENDANCE_WARNING", "student123", "SENT");
        });
    }
    
    @Test
    public void testAllLogLevels() {
        assertDoesNotThrow(() -> {
            SystemLogger.log(SystemLogger.LogCategory.SYSTEM, SystemLogger.LogLevel.DEBUG, "Debug message");
            SystemLogger.log(SystemLogger.LogCategory.SYSTEM, SystemLogger.LogLevel.INFO, "Info message");
            SystemLogger.log(SystemLogger.LogCategory.SYSTEM, SystemLogger.LogLevel.WARN, "Warn message");
            SystemLogger.log(SystemLogger.LogCategory.SYSTEM, SystemLogger.LogLevel.ERROR, "Error message");
            SystemLogger.log(SystemLogger.LogCategory.SYSTEM, SystemLogger.LogLevel.CRITICAL, "Critical message");
        });
    }
    
    @Test
    public void testAllSecurityLevels() {
        assertDoesNotThrow(() -> {
            SystemLogger.logSecurityEvent("Test event", "Low severity", SystemLogger.SecurityLevel.LOW);
            SystemLogger.logSecurityEvent("Test event", "Medium severity", SystemLogger.SecurityLevel.MEDIUM);
            SystemLogger.logSecurityEvent("Test event", "High severity", SystemLogger.SecurityLevel.HIGH);
            SystemLogger.logSecurityEvent("Test event", "Critical severity", SystemLogger.SecurityLevel.CRITICAL);
        });
    }
    
    @Test
    public void testAllLogCategories() {
        assertDoesNotThrow(() -> {
            SystemLogger.log(SystemLogger.LogCategory.AUTHENTICATION, SystemLogger.LogLevel.INFO, "Auth message");
            SystemLogger.log(SystemLogger.LogCategory.DATABASE, SystemLogger.LogLevel.INFO, "DB message");
            SystemLogger.log(SystemLogger.LogCategory.RMI, SystemLogger.LogLevel.INFO, "RMI message");
            SystemLogger.log(SystemLogger.LogCategory.BUSINESS_LOGIC, SystemLogger.LogLevel.INFO, "BL message");
            SystemLogger.log(SystemLogger.LogCategory.USER_INTERFACE, SystemLogger.LogLevel.INFO, "UI message");
            SystemLogger.log(SystemLogger.LogCategory.SYSTEM, SystemLogger.LogLevel.INFO, "System message");
            SystemLogger.log(SystemLogger.LogCategory.NOTIFICATION, SystemLogger.LogLevel.INFO, "Notification message");
        });
    }
}
