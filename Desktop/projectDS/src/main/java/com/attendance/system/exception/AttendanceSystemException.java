package com.attendance.system.exception;

/**
 * Base exception class for all attendance system exceptions.
 */
public class AttendanceSystemException extends Exception {
    private static final long serialVersionUID = 1L;
    
    private final String errorCode;
    private final String userMessage;
    private final String technicalDetails;
    
    public AttendanceSystemException(String message) {
        super(message);
        this.errorCode = "SYSTEM_ERROR";
        this.userMessage = message;
        this.technicalDetails = message;
    }
    
    public AttendanceSystemException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "SYSTEM_ERROR";
        this.userMessage = message;
        this.technicalDetails = message;
    }
    
    public AttendanceSystemException(String errorCode, String userMessage, String technicalDetails) {
        super(userMessage);
        this.errorCode = errorCode;
        this.userMessage = userMessage;
        this.technicalDetails = technicalDetails;
    }
    
    public AttendanceSystemException(String errorCode, String userMessage, String technicalDetails, Throwable cause) {
        super(userMessage, cause);
        this.errorCode = errorCode;
        this.userMessage = userMessage;
        this.technicalDetails = technicalDetails;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public String getUserMessage() {
        return userMessage;
    }
    
    public String getTechnicalDetails() {
        return technicalDetails;
    }
    
    @Override
    public String toString() {
        return String.format("AttendanceSystemException{errorCode='%s', userMessage='%s', technicalDetails='%s'}",
                errorCode, userMessage, technicalDetails);
    }
}