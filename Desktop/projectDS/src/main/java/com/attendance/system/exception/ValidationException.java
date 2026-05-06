package com.attendance.system.exception;

import java.util.ArrayList;
import java.util.List;

/**
 * Exception thrown for input validation errors.
 */
public class ValidationException extends AttendanceSystemException {
    private static final long serialVersionUID = 1L;
    
    private final List<ValidationError> validationErrors;
    
    public ValidationException(String message) {
        super("VALIDATION_ERROR", message, message);
        this.validationErrors = new ArrayList<>();
    }
    
    public ValidationException(List<ValidationError> validationErrors) {
        super("VALIDATION_ERROR", buildMessage(validationErrors), buildTechnicalDetails(validationErrors));
        this.validationErrors = validationErrors != null ? validationErrors : new ArrayList<>();
    }
    
    public ValidationException(String field, String message) {
        super("VALIDATION_ERROR", message, message);
        this.validationErrors = new ArrayList<>();
        this.validationErrors.add(new ValidationError(field, message));
    }
    
    public List<ValidationError> getValidationErrors() {
        return validationErrors;
    }
    
    public void addValidationError(String field, String message) {
        validationErrors.add(new ValidationError(field, message));
    }
    
    public boolean hasErrors() {
        return !validationErrors.isEmpty();
    }
    
    private static String buildMessage(List<ValidationError> errors) {
        if (errors == null || errors.isEmpty()) {
            return "Validation failed";
        }
        if (errors.size() == 1) {
            return errors.get(0).getMessage();
        }
        return "Multiple validation errors occurred";
    }
    
    private static String buildTechnicalDetails(List<ValidationError> errors) {
        if (errors == null || errors.isEmpty()) {
            return "No validation errors";
        }
        StringBuilder sb = new StringBuilder();
        for (ValidationError error : errors) {
            if (sb.length() > 0) {
                sb.append("; ");
            }
            sb.append(error.getField()).append(": ").append(error.getMessage());
        }
        return sb.toString();
    }
    
    // Common validation error factory methods
    public static ValidationException required(String field) {
        return new ValidationException(field, field + " is required");
    }
    
    public static ValidationException invalidFormat(String field, String expectedFormat) {
        return new ValidationException(field, field + " must be in format: " + expectedFormat);
    }
    
    public static ValidationException tooShort(String field, int minLength) {
        return new ValidationException(field, field + " must be at least " + minLength + " characters");
    }
    
    public static ValidationException tooLong(String field, int maxLength) {
        return new ValidationException(field, field + " must not exceed " + maxLength + " characters");
    }
    
    public static ValidationException invalidRange(String field, Object min, Object max) {
        return new ValidationException(field, field + " must be between " + min + " and " + max);
    }
    
    public static ValidationException futureDate(String field) {
        return new ValidationException(field, field + " cannot be in the future");
    }
    
    public static ValidationException duplicateValue(String field) {
        return new ValidationException(field, field + " already exists");
    }
    
    /**
     * Inner class representing a single validation error.
     */
    public static class ValidationError {
        private final String field;
        private final String message;
        
        public ValidationError(String field, String message) {
            this.field = field;
            this.message = message;
        }
        
        public String getField() {
            return field;
        }
        
        public String getMessage() {
            return message;
        }
        
        @Override
        public String toString() {
            return field + ": " + message;
        }
    }
}