package com.attendance.system.model;

import java.util.Arrays;
import java.util.List;

/**
 * Admin user class with full system access permissions.
 */
public class Admin extends User {
    private static final long serialVersionUID = 1L;
    
    // Admin-specific permissions
    private static final List<String> ADMIN_PERMISSIONS = Arrays.asList(
        "USER_CREATE", "USER_READ", "USER_UPDATE", "USER_DELETE",
        "ATTENDANCE_CREATE", "ATTENDANCE_READ", "ATTENDANCE_UPDATE", "ATTENDANCE_DELETE",
        "COURSE_CREATE", "COURSE_READ", "COURSE_UPDATE", "COURSE_DELETE",
        "REPORT_GENERATE", "REPORT_EXPORT",
        "SYSTEM_CONFIG", "SYSTEM_MAINTENANCE", "SYSTEM_BACKUP",
        "AUDIT_LOG_READ", "NOTIFICATION_SEND"
    );
    
    public Admin() {
        super();
        setRole(UserRole.ADMIN);
    }
    
    public Admin(String username, String email, String firstName, String lastName) {
        super(username, email, firstName, lastName, UserRole.ADMIN);
    }
    
    @Override
    public List<String> getPermissions() {
        return ADMIN_PERMISSIONS;
    }
    
    @Override
    public String getDisplayName() {
        return "Administrator: " + getFullName();
    }
    
    /**
     * Checks if admin can perform system maintenance operations.
     * @return true (admins have full access)
     */
    public boolean canPerformMaintenance() {
        return true;
    }
    
    /**
     * Checks if admin can access audit logs.
     * @return true (admins have full access)
     */
    public boolean canAccessAuditLogs() {
        return true;
    }
    
    /**
     * Checks if admin can manage user accounts.
     * @return true (admins have full access)
     */
    public boolean canManageUsers() {
        return true;
    }
}