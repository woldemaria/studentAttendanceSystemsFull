package com.attendance.system.model;

/**
 * Enumeration representing different attendance status values.
 */
public enum AttendanceStatus {
    PRESENT("Present"),
    ABSENT("Absent"),
    LATE("Late"),
    EXCUSED("Excused");
    
    private final String displayName;
    
    AttendanceStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Checks if this status counts as attended for percentage calculations.
     * @return true if PRESENT or LATE, false otherwise
     */
    public boolean countsAsAttended() {
        return this == PRESENT || this == LATE;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}