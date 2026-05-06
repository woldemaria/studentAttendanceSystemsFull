package com.attendance.system.exception;

/**
 * Exception thrown for RMI communication errors.
 */
public class RemoteServiceException extends AttendanceSystemException {
    private static final long serialVersionUID = 1L;
    
    public RemoteServiceException(String message) {
        super("RMI_ERROR", message, message);
    }
    
    public RemoteServiceException(String message, Throwable cause) {
        super("RMI_ERROR", message, message, cause);
    }
    
    public RemoteServiceException(String errorCode, String userMessage, String technicalDetails) {
        super(errorCode, userMessage, technicalDetails);
    }
    
    public RemoteServiceException(String errorCode, String userMessage, String technicalDetails, Throwable cause) {
        super(errorCode, userMessage, technicalDetails, cause);
    }
    
    // Specific RMI error types
    public static RemoteServiceException connectionLost() {
        return new RemoteServiceException("RMI_CONNECTION_LOST", 
                "Connection to server lost", 
                "RMI connection was interrupted");
    }
    
    public static RemoteServiceException serverUnavailable() {
        return new RemoteServiceException("RMI_SERVER_UNAVAILABLE", 
                "Server is currently unavailable", 
                "RMI server is not responding");
    }
    
    public static RemoteServiceException timeout() {
        return new RemoteServiceException("RMI_TIMEOUT", 
                "Request timed out", 
                "RMI request exceeded timeout limit");
    }
    
    public static RemoteServiceException registryNotFound() {
        return new RemoteServiceException("RMI_REGISTRY_NOT_FOUND", 
                "Cannot connect to server", 
                "RMI registry not found");
    }
    
    public static RemoteServiceException serviceNotBound(String serviceName) {
        return new RemoteServiceException("RMI_SERVICE_NOT_BOUND", 
                "Service is not available", 
                "Service '" + serviceName + "' is not bound in RMI registry");
    }
}