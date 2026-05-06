package com.attendance.system.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Comprehensive logging system for the Student Attendance System.
 * Provides different log levels and categories for user activity, security events,
 * performance metrics, and audit trails.
 */
public class SystemLogger {
    private static final Logger logger = LoggerFactory.getLogger(SystemLogger.class);
    private static final Logger auditLogger = LoggerFactory.getLogger("AUDIT");
    private static final Logger securityLogger = LoggerFactory.getLogger("SECURITY");
    private static final Logger performanceLogger = LoggerFactory.getLogger("PERFORMANCE");
    private static final Logger activityLogger = LoggerFactory.getLogger("ACTIVITY");
    
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    
    /**
     * Log levels for categorizing log messages
     */
    public enum LogLevel {
        DEBUG, INFO, WARN, ERROR, CRITICAL
    }
    
    /**
     * Security levels for security events
     */
    public enum SecurityLevel {
        LOW, MEDIUM, HIGH, CRITICAL
    }
    
    /**
     * Log categories for organizing logs
     */
    public enum LogCategory {
        AUTHENTICATION, DATABASE, RMI, BUSINESS_LOGIC, USER_INTERFACE, SYSTEM, NOTIFICATION
    }
    
    /**
     * Logs user activity with context information.
     * 
     * @param userId User ID performing the action
     * @param action Action being performed
     * @param details Additional details about the action
     */
    public static void logUserActivity(String userId, String action, String details) {
        try {
            MDC.put("userId", userId);
            MDC.put("action", action);
            MDC.put("timestamp", LocalDateTime.now().format(TIMESTAMP_FORMAT));
            
            String message = String.format("User Activity - User: %s, Action: %s, Details: %s", 
                    userId, action, details);
            activityLogger.info(message);
        } finally {
            MDC.clear();
        }
    }
    
    /**
     * Logs user activity with additional context map.
     * 
     * @param userId User ID performing the action
     * @param action Action being performed
     * @param details Additional details about the action
     * @param context Additional context information
     */
    public static void logUserActivity(String userId, String action, String details, Map<String, String> context) {
        try {
            MDC.put("userId", userId);
            MDC.put("action", action);
            MDC.put("timestamp", LocalDateTime.now().format(TIMESTAMP_FORMAT));
            
            if (context != null) {
                context.forEach(MDC::put);
            }
            
            String message = String.format("User Activity - User: %s, Action: %s, Details: %s, Context: %s", 
                    userId, action, details, context != null ? context : "{}");
            activityLogger.info(message);
        } finally {
            MDC.clear();
        }
    }
    
    /**
     * Logs security events with severity level.
     * 
     * @param event Security event description
     * @param details Event details
     * @param level Security level (LOW, MEDIUM, HIGH, CRITICAL)
     */
    public static void logSecurityEvent(String event, String details, SecurityLevel level) {
        try {
            MDC.put("event", event);
            MDC.put("level", level.toString());
            MDC.put("timestamp", LocalDateTime.now().format(TIMESTAMP_FORMAT));
            
            String message = String.format("Security Event [%s] - Event: %s, Details: %s", 
                    level, event, details);
            
            switch (level) {
                case CRITICAL:
                    securityLogger.error(message);
                    break;
                case HIGH:
                    securityLogger.warn(message);
                    break;
                case MEDIUM:
                case LOW:
                    securityLogger.info(message);
                    break;
            }
        } finally {
            MDC.clear();
        }
    }
    
    /**
     * Logs security events with additional context.
     * 
     * @param event Security event description
     * @param details Event details
     * @param level Security level
     * @param userId User ID associated with the event
     * @param context Additional context information
     */
    public static void logSecurityEvent(String event, String details, SecurityLevel level, 
                                       String userId, Map<String, String> context) {
        try {
            MDC.put("event", event);
            MDC.put("level", level.toString());
            MDC.put("userId", userId);
            MDC.put("timestamp", LocalDateTime.now().format(TIMESTAMP_FORMAT));
            
            if (context != null) {
                context.forEach(MDC::put);
            }
            
            String message = String.format("Security Event [%s] - Event: %s, User: %s, Details: %s, Context: %s", 
                    level, event, userId, details, context != null ? context : "{}");
            
            switch (level) {
                case CRITICAL:
                    securityLogger.error(message);
                    break;
                case HIGH:
                    securityLogger.warn(message);
                    break;
                case MEDIUM:
                case LOW:
                    securityLogger.info(message);
                    break;
            }
        } finally {
            MDC.clear();
        }
    }
    
    /**
     * Logs system errors with context information.
     * 
     * @param error Exception that occurred
     * @param context Context where the error occurred
     */
    public static void logSystemError(Exception error, String context) {
        try {
            MDC.put("context", context);
            MDC.put("errorType", error.getClass().getSimpleName());
            MDC.put("timestamp", LocalDateTime.now().format(TIMESTAMP_FORMAT));
            
            String message = String.format("System Error in %s - %s: %s", 
                    context, error.getClass().getSimpleName(), error.getMessage());
            logger.error(message, error);
        } finally {
            MDC.clear();
        }
    }
    
    /**
     * Logs system errors with additional details.
     * 
     * @param error Exception that occurred
     * @param context Context where the error occurred
     * @param details Additional error details
     */
    public static void logSystemError(Exception error, String context, String details) {
        try {
            MDC.put("context", context);
            MDC.put("errorType", error.getClass().getSimpleName());
            MDC.put("timestamp", LocalDateTime.now().format(TIMESTAMP_FORMAT));
            
            String message = String.format("System Error in %s - %s: %s, Details: %s", 
                    context, error.getClass().getSimpleName(), error.getMessage(), details);
            logger.error(message, error);
        } finally {
            MDC.clear();
        }
    }
    
    /**
     * Logs performance metrics for operations.
     * 
     * @param operation Operation name
     * @param duration Duration in milliseconds
     */
    public static void logPerformanceMetric(String operation, long duration) {
        try {
            MDC.put("operation", operation);
            MDC.put("duration", String.valueOf(duration));
            MDC.put("timestamp", LocalDateTime.now().format(TIMESTAMP_FORMAT));
            
            String message = String.format("Performance Metric - Operation: %s, Duration: %d ms", 
                    operation, duration);
            
            if (duration > 5000) {
                performanceLogger.warn(message + " (SLOW)");
            } else {
                performanceLogger.info(message);
            }
        } finally {
            MDC.clear();
        }
    }
    
    /**
     * Logs performance metrics with additional context.
     * 
     * @param operation Operation name
     * @param duration Duration in milliseconds
     * @param context Additional context information
     */
    public static void logPerformanceMetric(String operation, long duration, Map<String, String> context) {
        try {
            MDC.put("operation", operation);
            MDC.put("duration", String.valueOf(duration));
            MDC.put("timestamp", LocalDateTime.now().format(TIMESTAMP_FORMAT));
            
            if (context != null) {
                context.forEach(MDC::put);
            }
            
            String message = String.format("Performance Metric - Operation: %s, Duration: %d ms, Context: %s", 
                    operation, duration, context != null ? context : "{}");
            
            if (duration > 5000) {
                performanceLogger.warn(message + " (SLOW)");
            } else {
                performanceLogger.info(message);
            }
        } finally {
            MDC.clear();
        }
    }
    
    /**
     * Logs database operations with execution details.
     * 
     * @param query SQL query executed
     * @param executionTime Execution time in milliseconds
     */
    public static void logDatabaseOperation(String query, long executionTime) {
        try {
            MDC.put("query", query);
            MDC.put("executionTime", String.valueOf(executionTime));
            MDC.put("timestamp", LocalDateTime.now().format(TIMESTAMP_FORMAT));
            
            String message = String.format("Database Operation - Query: %s, Execution Time: %d ms", 
                    query, executionTime);
            
            if (executionTime > 1000) {
                logger.warn(message + " (SLOW QUERY)");
            } else {
                logger.debug(message);
            }
        } finally {
            MDC.clear();
        }
    }
    
    /**
     * Logs database operations with additional context.
     * 
     * @param query SQL query executed
     * @param executionTime Execution time in milliseconds
     * @param context Additional context information
     */
    public static void logDatabaseOperation(String query, long executionTime, Map<String, String> context) {
        try {
            MDC.put("query", query);
            MDC.put("executionTime", String.valueOf(executionTime));
            MDC.put("timestamp", LocalDateTime.now().format(TIMESTAMP_FORMAT));
            
            if (context != null) {
                context.forEach(MDC::put);
            }
            
            String message = String.format("Database Operation - Query: %s, Execution Time: %d ms, Context: %s", 
                    query, executionTime, context != null ? context : "{}");
            
            if (executionTime > 1000) {
                logger.warn(message + " (SLOW QUERY)");
            } else {
                logger.debug(message);
            }
        } finally {
            MDC.clear();
        }
    }
    
    /**
     * Logs audit trail entries for compliance and security monitoring.
     * 
     * @param userId User ID performing the action
     * @param action Action being audited
     * @param resourceType Type of resource being accessed
     * @param resourceId ID of the resource
     * @param result Result of the action (SUCCESS, FAILURE, etc.)
     */
    public static void logAuditTrail(String userId, String action, String resourceType, 
                                     String resourceId, String result) {
        try {
            MDC.put("userId", userId);
            MDC.put("action", action);
            MDC.put("resourceType", resourceType);
            MDC.put("resourceId", resourceId);
            MDC.put("result", result);
            MDC.put("timestamp", LocalDateTime.now().format(TIMESTAMP_FORMAT));
            
            String message = String.format("Audit Trail - User: %s, Action: %s, Resource: %s/%s, Result: %s", 
                    userId, action, resourceType, resourceId, result);
            auditLogger.info(message);
        } finally {
            MDC.clear();
        }
    }
    
    /**
     * Logs audit trail entries with additional details.
     * 
     * @param userId User ID performing the action
     * @param action Action being audited
     * @param resourceType Type of resource being accessed
     * @param resourceId ID of the resource
     * @param result Result of the action
     * @param details Additional details about the action
     */
    public static void logAuditTrail(String userId, String action, String resourceType, 
                                     String resourceId, String result, String details) {
        try {
            MDC.put("userId", userId);
            MDC.put("action", action);
            MDC.put("resourceType", resourceType);
            MDC.put("resourceId", resourceId);
            MDC.put("result", result);
            MDC.put("timestamp", LocalDateTime.now().format(TIMESTAMP_FORMAT));
            
            String message = String.format("Audit Trail - User: %s, Action: %s, Resource: %s/%s, Result: %s, Details: %s", 
                    userId, action, resourceType, resourceId, result, details);
            auditLogger.info(message);
        } finally {
            MDC.clear();
        }
    }
    
    /**
     * Logs general system messages with category.
     * 
     * @param category Log category
     * @param level Log level
     * @param message Message to log
     */
    public static void log(LogCategory category, LogLevel level, String message) {
        try {
            MDC.put("category", category.toString());
            MDC.put("level", level.toString());
            MDC.put("timestamp", LocalDateTime.now().format(TIMESTAMP_FORMAT));
            
            String formattedMessage = String.format("[%s] %s", category, message);
            
            switch (level) {
                case DEBUG:
                    logger.debug(formattedMessage);
                    break;
                case INFO:
                    logger.info(formattedMessage);
                    break;
                case WARN:
                    logger.warn(formattedMessage);
                    break;
                case ERROR:
                    logger.error(formattedMessage);
                    break;
                case CRITICAL:
                    logger.error("CRITICAL: " + formattedMessage);
                    break;
            }
        } finally {
            MDC.clear();
        }
    }
    
    /**
     * Logs general system messages with category and exception.
     * 
     * @param category Log category
     * @param level Log level
     * @param message Message to log
     * @param exception Exception to log
     */
    public static void log(LogCategory category, LogLevel level, String message, Exception exception) {
        try {
            MDC.put("category", category.toString());
            MDC.put("level", level.toString());
            MDC.put("timestamp", LocalDateTime.now().format(TIMESTAMP_FORMAT));
            
            String formattedMessage = String.format("[%s] %s", category, message);
            
            switch (level) {
                case DEBUG:
                    logger.debug(formattedMessage, exception);
                    break;
                case INFO:
                    logger.info(formattedMessage, exception);
                    break;
                case WARN:
                    logger.warn(formattedMessage, exception);
                    break;
                case ERROR:
                    logger.error(formattedMessage, exception);
                    break;
                case CRITICAL:
                    logger.error("CRITICAL: " + formattedMessage, exception);
                    break;
            }
        } finally {
            MDC.clear();
        }
    }
    
    /**
     * Logs RMI communication events.
     * 
     * @param methodName RMI method name
     * @param clientId Client ID
     * @param status Status of the call (SUCCESS, FAILURE, etc.)
     */
    public static void logRMICall(String methodName, String clientId, String status) {
        try {
            MDC.put("method", methodName);
            MDC.put("clientId", clientId);
            MDC.put("status", status);
            MDC.put("timestamp", LocalDateTime.now().format(TIMESTAMP_FORMAT));
            
            String message = String.format("RMI Call - Method: %s, Client: %s, Status: %s", 
                    methodName, clientId, status);
            logger.info(message);
        } finally {
            MDC.clear();
        }
    }
    
    /**
     * Logs RMI communication events with duration.
     * 
     * @param methodName RMI method name
     * @param clientId Client ID
     * @param status Status of the call
     * @param duration Duration in milliseconds
     */
    public static void logRMICall(String methodName, String clientId, String status, long duration) {
        try {
            MDC.put("method", methodName);
            MDC.put("clientId", clientId);
            MDC.put("status", status);
            MDC.put("duration", String.valueOf(duration));
            MDC.put("timestamp", LocalDateTime.now().format(TIMESTAMP_FORMAT));
            
            String message = String.format("RMI Call - Method: %s, Client: %s, Status: %s, Duration: %d ms", 
                    methodName, clientId, status, duration);
            logger.info(message);
        } finally {
            MDC.clear();
        }
    }
    
    /**
     * Logs database connection pool events.
     * 
     * @param event Event description
     * @param activeConnections Number of active connections
     * @param idleConnections Number of idle connections
     */
    public static void logConnectionPoolEvent(String event, int activeConnections, int idleConnections) {
        try {
            MDC.put("event", event);
            MDC.put("activeConnections", String.valueOf(activeConnections));
            MDC.put("idleConnections", String.valueOf(idleConnections));
            MDC.put("timestamp", LocalDateTime.now().format(TIMESTAMP_FORMAT));
            
            String message = String.format("Connection Pool Event - Event: %s, Active: %d, Idle: %d", 
                    event, activeConnections, idleConnections);
            logger.info(message);
        } finally {
            MDC.clear();
        }
    }
    
    /**
     * Logs notification events.
     * 
     * @param notificationType Type of notification
     * @param recipientId Recipient user ID
     * @param status Status of notification delivery
     */
    public static void logNotificationEvent(String notificationType, String recipientId, String status) {
        try {
            MDC.put("type", notificationType);
            MDC.put("recipient", recipientId);
            MDC.put("status", status);
            MDC.put("timestamp", LocalDateTime.now().format(TIMESTAMP_FORMAT));
            
            String message = String.format("Notification Event - Type: %s, Recipient: %s, Status: %s", 
                    notificationType, recipientId, status);
            logger.info(message);
        } finally {
            MDC.clear();
        }
    }
}
