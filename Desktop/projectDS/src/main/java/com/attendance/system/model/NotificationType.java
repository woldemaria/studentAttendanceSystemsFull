package com.attendance.system.model;

/**
 * Enumeration representing different types of notifications in the system.
 */
public enum NotificationType {
    ATTENDANCE_WARNING("Attendance Warning"),
    SYSTEM_NOTIFICATION("System Notification"),
    COURSE_UPDATE("Course Update"),
    REMINDER("Reminder");
    
    private final String displayName;
    
    NotificationType(String displayName) {
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