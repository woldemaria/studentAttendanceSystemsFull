package com.attendance.system.model;

/**
 * Enumeration representing different user roles in the attendance system.
 * Each role has specific permissions and access levels.
 */
public enum UserRole {
    ADMIN("Administrator"),
    TEACHER("Teacher"),
    STUDENT("Student");
    
    private final String displayName;
    
    UserRole(String displayName) {
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