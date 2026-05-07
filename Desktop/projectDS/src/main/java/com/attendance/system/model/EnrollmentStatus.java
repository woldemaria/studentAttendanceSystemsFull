package com.attendance.system.model;

/**
 * Enumeration representing different enrollment statuses.
 */
public enum EnrollmentStatus {
    ENROLLED("Enrolled"),
    DROPPED("Dropped"),
    COMPLETED("Completed"),
    SUSPENDED("Suspended"),
    TRANSFERRED("Transferred");
    
    private final String displayName;
    
    EnrollmentStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}