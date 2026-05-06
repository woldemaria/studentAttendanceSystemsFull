package com.attendance.system.service;

import com.attendance.system.exception.AuthenticationException;
import com.attendance.system.exception.DatabaseException;
import com.attendance.system.exception.ValidationException;
import com.attendance.system.model.*;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

/**
 * Remote service interface for the Student Attendance System.
 * This interface defines all remote methods available to clients via RMI.
 */
public interface AttendanceService extends Remote {
    
    // Authentication methods
    
    /**
     * Authenticates a user with username and password.
     * @param username the username
     * @param password the password
     * @return authenticated user with session token
     * @throws RemoteException if RMI communication fails
     * @throws AuthenticationException if authentication fails
     */
    AuthenticationService.AuthenticatedUser authenticateUser(String username, String password) 
            throws RemoteException, AuthenticationException;
    
    /**
     * Validates a session token.
     * @param sessionToken the session token
     * @return user associated with the session
     * @throws RemoteException if RMI communication fails
     * @throws AuthenticationException if session is invalid
     */
    User validateSession(String sessionToken) 
            throws RemoteException, AuthenticationException;
    
    /**
     * Logs out a user.
     * @param sessionToken the session token
     * @throws RemoteException if RMI communication fails
     */
    void logout(String sessionToken) throws RemoteException;
    
    /**
     * Changes a user's password.
     * @param sessionToken the session token
     * @param currentPassword the current password
     * @param newPassword the new password
     * @throws RemoteException if RMI communication fails
     * @throws AuthenticationException if current password is incorrect
     * @throws ValidationException if new password is invalid
     * @throws DatabaseException if database operation fails
     */
    void changePassword(String sessionToken, String currentPassword, String newPassword) 
            throws RemoteException, AuthenticationException, ValidationException, DatabaseException;
    
    /**
     * Registers a new user account (self-registration for students and teachers).
     * @param username the username
     * @param email the email address
     * @param firstName the first name
     * @param lastName the last name
     * @param password the password
     * @param role the user role (STUDENT or TEACHER)
     * @return true if registration was successful
     * @throws RemoteException if RMI communication fails
     * @throws ValidationException if user data is invalid or username/email already exists
     * @throws DatabaseException if database operation fails
     */
    boolean registerUser(String username, String email, String firstName, String lastName, 
                        String password, UserRole role) 
            throws RemoteException, ValidationException, DatabaseException;
    
    // User management methods (Admin only)
    
    /**
     * Creates a new user account.
     * @param sessionToken the session token (admin required)
     * @param user the user to create
     * @return true if user was created successfully
     * @throws RemoteException if RMI communication fails
     * @throws AuthenticationException if session is invalid or insufficient permissions
     * @throws ValidationException if user data is invalid
     * @throws DatabaseException if database operation fails
     */
    boolean createUser(String sessionToken, User user) 
            throws RemoteException, AuthenticationException, ValidationException, DatabaseException;
    
    /**
     * Updates an existing user account.
     * @param sessionToken the session token (admin required)
     * @param user the user to update
     * @return true if user was updated successfully
     * @throws RemoteException if RMI communication fails
     * @throws AuthenticationException if session is invalid or insufficient permissions
     * @throws ValidationException if user data is invalid
     * @throws DatabaseException if database operation fails
     */
    boolean updateUser(String sessionToken, User user) 
            throws RemoteException, AuthenticationException, ValidationException, DatabaseException;
    
    /**
     * Deletes a user account.
     * @param sessionToken the session token (admin required)
     * @param userId the user ID to delete
     * @return true if user was deleted successfully
     * @throws RemoteException if RMI communication fails
     * @throws AuthenticationException if session is invalid or insufficient permissions
     * @throws DatabaseException if database operation fails
     */
    boolean deleteUser(String sessionToken, int userId) 
            throws RemoteException, AuthenticationException, DatabaseException;
    
    /**
     * Gets all users.
     * @param sessionToken the session token (admin required)
     * @return list of all users
     * @throws RemoteException if RMI communication fails
     * @throws AuthenticationException if session is invalid or insufficient permissions
     * @throws DatabaseException if database operation fails
     */
    List<User> getAllUsers(String sessionToken) 
            throws RemoteException, AuthenticationException, DatabaseException;
    
    /**
     * Gets users by role.
     * @param sessionToken the session token (admin required)
     * @param role the user role
     * @return list of users with the specified role
     * @throws RemoteException if RMI communication fails
     * @throws AuthenticationException if session is invalid or insufficient permissions
     * @throws DatabaseException if database operation fails
     */
    List<User> getUsersByRole(String sessionToken, UserRole role) 
            throws RemoteException, AuthenticationException, DatabaseException;
    
    // Course management methods
    
    /**
     * Creates a new course.
     * @param sessionToken the session token (admin required)
     * @param course the course to create
     * @return true if course was created successfully
     * @throws RemoteException if RMI communication fails
     * @throws AuthenticationException if session is invalid or insufficient permissions
     * @throws ValidationException if course data is invalid
     * @throws DatabaseException if database operation fails
     */
    boolean createCourse(String sessionToken, Course course) 
            throws RemoteException, AuthenticationException, ValidationException, DatabaseException;
    
    /**
     * Updates an existing course.
     * @param sessionToken the session token (admin required)
     * @param course the course to update
     * @return true if course was updated successfully
     * @throws RemoteException if RMI communication fails
     * @throws AuthenticationException if session is invalid or insufficient permissions
     * @throws ValidationException if course data is invalid
     * @throws DatabaseException if database operation fails
     */
    boolean updateCourse(String sessionToken, Course course) 
            throws RemoteException, AuthenticationException, ValidationException, DatabaseException;
    
    /**
     * Gets all active courses.
     * @param sessionToken the session token
     * @return list of all active courses
     * @throws RemoteException if RMI communication fails
     * @throws AuthenticationException if session is invalid
     * @throws DatabaseException if database operation fails
     */
    List<Course> getAllActiveCourses(String sessionToken) 
            throws RemoteException, AuthenticationException, DatabaseException;
    
    /**
     * Gets courses by teacher ID.
     * @param sessionToken the session token
     * @param teacherId the teacher ID
     * @return list of courses taught by the teacher
     * @throws RemoteException if RMI communication fails
     * @throws AuthenticationException if session is invalid
     * @throws DatabaseException if database operation fails
     */
    List<Course> getCoursesByTeacher(String sessionToken, int teacherId) 
            throws RemoteException, AuthenticationException, DatabaseException;
    
    /**
     * Gets courses by student ID (enrolled courses).
     * @param sessionToken the session token
     * @param studentId the student ID
     * @return list of courses the student is enrolled in
     * @throws RemoteException if RMI communication fails
     * @throws AuthenticationException if session is invalid
     * @throws DatabaseException if database operation fails
     */
    List<Course> getCoursesByStudent(String sessionToken, int studentId) 
            throws RemoteException, AuthenticationException, DatabaseException;
    
    /**
     * Enrolls a student in a course.
     * @param sessionToken the session token (admin required)
     * @param studentId the student ID
     * @param courseId the course ID
     * @return true if enrollment was successful
     * @throws RemoteException if RMI communication fails
     * @throws AuthenticationException if session is invalid or insufficient permissions
     * @throws DatabaseException if database operation fails
     */
    boolean enrollStudent(String sessionToken, int studentId, int courseId) 
            throws RemoteException, AuthenticationException, DatabaseException;
    
    /**
     * Gets enrolled students for a course.
     * @param sessionToken the session token
     * @param courseId the course ID
     * @return list of enrolled students
     * @throws RemoteException if RMI communication fails
     * @throws AuthenticationException if session is invalid
     * @throws DatabaseException if database operation fails
     */
    List<Student> getEnrolledStudents(String sessionToken, int courseId) 
            throws RemoteException, AuthenticationException, DatabaseException;
    
    // Attendance management methods
    
    /**
     * Marks attendance for a student.
     * @param sessionToken the session token (teacher required)
     * @param record the attendance record to create
     * @return true if attendance was marked successfully
     * @throws RemoteException if RMI communication fails
     * @throws AuthenticationException if session is invalid or insufficient permissions
     * @throws ValidationException if attendance data is invalid
     * @throws DatabaseException if database operation fails
     */
    boolean markAttendance(String sessionToken, AttendanceRecord record) 
            throws RemoteException, AuthenticationException, ValidationException, DatabaseException;
    
    /**
     * Updates an existing attendance record.
     * @param sessionToken the session token (teacher required)
     * @param record the attendance record to update
     * @return true if attendance was updated successfully
     * @throws RemoteException if RMI communication fails
     * @throws AuthenticationException if session is invalid or insufficient permissions
     * @throws ValidationException if attendance data is invalid
     * @throws DatabaseException if database operation fails
     */
    boolean updateAttendance(String sessionToken, AttendanceRecord record) 
            throws RemoteException, AuthenticationException, ValidationException, DatabaseException;
    
    /**
     * Gets attendance records for a student.
     * @param sessionToken the session token
     * @param studentId the student ID
     * @param startDate the start date (optional)
     * @param endDate the end date (optional)
     * @return list of attendance records
     * @throws RemoteException if RMI communication fails
     * @throws AuthenticationException if session is invalid
     * @throws DatabaseException if database operation fails
     */
    List<AttendanceRecord> getAttendanceRecords(String sessionToken, int studentId, 
                                               LocalDate startDate, LocalDate endDate) 
            throws RemoteException, AuthenticationException, DatabaseException;
    
    /**
     * Gets attendance records for a course and date.
     * @param sessionToken the session token (teacher required)
     * @param courseId the course ID
     * @param date the attendance date
     * @return list of attendance records
     * @throws RemoteException if RMI communication fails
     * @throws AuthenticationException if session is invalid or insufficient permissions
     * @throws DatabaseException if database operation fails
     */
    List<AttendanceRecord> getAttendanceByClassDate(String sessionToken, int courseId, LocalDate date) 
            throws RemoteException, AuthenticationException, DatabaseException;
    
    /**
     * Gets attendance statistics for a student and course.
     * @param sessionToken the session token
     * @param studentId the student ID
     * @param courseId the course ID
     * @return attendance statistics map
     * @throws RemoteException if RMI communication fails
     * @throws AuthenticationException if session is invalid
     * @throws DatabaseException if database operation fails
     */
    Map<String, Object> getAttendanceStatistics(String sessionToken, int studentId, int courseId) 
            throws RemoteException, AuthenticationException, DatabaseException;
    
    // Notification methods
    
    /**
     * Gets notifications for a user.
     * @param sessionToken the session token
     * @param userId the user ID
     * @param unreadOnly true to get only unread notifications
     * @return list of notifications
     * @throws RemoteException if RMI communication fails
     * @throws AuthenticationException if session is invalid
     * @throws DatabaseException if database operation fails
     */
    List<Notification> getNotifications(String sessionToken, int userId, boolean unreadOnly) 
            throws RemoteException, AuthenticationException, DatabaseException;
    
    /**
     * Gets unread notification count for a user.
     * @param sessionToken the session token
     * @param userId the user ID
     * @return count of unread notifications
     * @throws RemoteException if RMI communication fails
     * @throws AuthenticationException if session is invalid
     * @throws DatabaseException if database operation fails
     */
    int getUnreadNotificationCount(String sessionToken, int userId) 
            throws RemoteException, AuthenticationException, DatabaseException;
    
    /**
     * Marks a notification as read.
     * @param sessionToken the session token
     * @param notificationId the notification ID
     * @return true if notification was marked as read
     * @throws RemoteException if RMI communication fails
     * @throws AuthenticationException if session is invalid
     * @throws DatabaseException if database operation fails
     */
    boolean markNotificationAsRead(String sessionToken, int notificationId) 
            throws RemoteException, AuthenticationException, DatabaseException;
    
    /**
     * Marks all notifications for a user as read.
     * @param sessionToken the session token
     * @param userId the user ID
     * @return number of notifications marked as read
     * @throws RemoteException if RMI communication fails
     * @throws AuthenticationException if session is invalid
     * @throws DatabaseException if database operation fails
     */
    int markAllNotificationsAsRead(String sessionToken, int userId) 
            throws RemoteException, AuthenticationException, DatabaseException;
    
    /**
     * Sends a notification to a user.
     * @param sessionToken the session token (admin required)
     * @param notification the notification to send
     * @return true if notification was sent successfully
     * @throws RemoteException if RMI communication fails
     * @throws AuthenticationException if session is invalid or insufficient permissions
     * @throws DatabaseException if database operation fails
     */
    boolean sendNotification(String sessionToken, Notification notification) 
            throws RemoteException, AuthenticationException, DatabaseException;
    
    /**
     * Gets notification preferences for a user.
     * @param sessionToken the session token
     * @param userId the user ID
     * @return map of notification preferences
     * @throws RemoteException if RMI communication fails
     * @throws AuthenticationException if session is invalid
     * @throws DatabaseException if database operation fails
     */
    Map<String, Object> getNotificationPreferences(String sessionToken, int userId) 
            throws RemoteException, AuthenticationException, DatabaseException;
    
    /**
     * Updates notification preferences for a user.
     * @param sessionToken the session token
     * @param userId the user ID
     * @param preferences map of preferences to update
     * @return true if preferences were updated successfully
     * @throws RemoteException if RMI communication fails
     * @throws AuthenticationException if session is invalid
     * @throws ValidationException if preferences are invalid
     * @throws DatabaseException if database operation fails
     */
    boolean updateNotificationPreferences(String sessionToken, int userId, Map<String, Object> preferences) 
            throws RemoteException, AuthenticationException, ValidationException, DatabaseException;
    
    // Report generation methods
    
    /**
     * Generates an attendance report.
     * @param sessionToken the session token
     * @param criteria the report criteria
     * @return report data as byte array
     * @throws RemoteException if RMI communication fails
     * @throws AuthenticationException if session is invalid
     * @throws DatabaseException if database operation fails
     */
    byte[] generateAttendanceReport(String sessionToken, ReportCriteria criteria) 
            throws RemoteException, AuthenticationException, DatabaseException;
    
    /**
     * Gets system statistics (admin only).
     * @param sessionToken the session token (admin required)
     * @return system statistics map
     * @throws RemoteException if RMI communication fails
     * @throws AuthenticationException if session is invalid or insufficient permissions
     * @throws DatabaseException if database operation fails
     */
    Map<String, Object> getSystemStatistics(String sessionToken) 
            throws RemoteException, AuthenticationException, DatabaseException;
    
    // System health and monitoring
    
    /**
     * Checks if the service is healthy.
     * @return true if service is healthy
     * @throws RemoteException if RMI communication fails
     */
    boolean isHealthy() throws RemoteException;
    
    /**
     * Gets server information.
     * @return server information map
     * @throws RemoteException if RMI communication fails
     */
    Map<String, Object> getServerInfo() throws RemoteException;
    
    // Maintenance mode methods
    
    /**
     * Enables maintenance mode immediately.
     * @param sessionToken the session token (admin required)
     * @param reason the reason for maintenance
     * @param estimatedDurationMinutes estimated duration in minutes
     * @return true if maintenance mode was enabled
     * @throws RemoteException if RMI communication fails
     * @throws AuthenticationException if session is invalid or insufficient permissions
     */
    boolean enableMaintenanceMode(String sessionToken, String reason, int estimatedDurationMinutes) 
            throws RemoteException, AuthenticationException;
    
    /**
     * Disables maintenance mode.
     * @param sessionToken the session token (admin required)
     * @return true if maintenance mode was disabled
     * @throws RemoteException if RMI communication fails
     * @throws AuthenticationException if session is invalid or insufficient permissions
     */
    boolean disableMaintenanceMode(String sessionToken) 
            throws RemoteException, AuthenticationException;
    
    /**
     * Schedules maintenance mode for a specific time.
     * @param sessionToken the session token (admin required)
     * @param startTime the start time for maintenance (ISO format)
     * @param estimatedDurationMinutes estimated duration in minutes
     * @param reason the reason for maintenance
     * @return true if maintenance was scheduled
     * @throws RemoteException if RMI communication fails
     * @throws AuthenticationException if session is invalid or insufficient permissions
     * @throws ValidationException if parameters are invalid
     */
    boolean scheduleMaintenanceMode(String sessionToken, String startTime, int estimatedDurationMinutes, String reason) 
            throws RemoteException, AuthenticationException, ValidationException;
    
    /**
     * Cancels scheduled maintenance.
     * @param sessionToken the session token (admin required)
     * @return true if scheduled maintenance was cancelled
     * @throws RemoteException if RMI communication fails
     * @throws AuthenticationException if session is invalid or insufficient permissions
     */
    boolean cancelScheduledMaintenance(String sessionToken) 
            throws RemoteException, AuthenticationException;
    
    /**
     * Gets maintenance mode information.
     * @param sessionToken the session token
     * @return map containing maintenance details
     * @throws RemoteException if RMI communication fails
     * @throws AuthenticationException if session is invalid
     */
    Map<String, Object> getMaintenanceModeInfo(String sessionToken) 
            throws RemoteException, AuthenticationException;
    
    /**
     * Creates a backup before system update.
     * @param sessionToken the session token (admin required)
     * @param backupName the name of the backup
     * @return backup ID
     * @throws RemoteException if RMI communication fails
     * @throws AuthenticationException if session is invalid or insufficient permissions
     */
    String createBackup(String sessionToken, String backupName) 
            throws RemoteException, AuthenticationException;
    
    /**
     * Restores from a backup.
     * @param sessionToken the session token (admin required)
     * @param backupId the backup ID to restore from
     * @return true if restore was successful
     * @throws RemoteException if RMI communication fails
     * @throws AuthenticationException if session is invalid or insufficient permissions
     */
    boolean restoreFromBackup(String sessionToken, String backupId) 
            throws RemoteException, AuthenticationException;
    
    /**
     * Gets available backups.
     * @param sessionToken the session token (admin required)
     * @return list of backup information
     * @throws RemoteException if RMI communication fails
     * @throws AuthenticationException if session is invalid or insufficient permissions
     */
    List<Map<String, Object>> getAvailableBackups(String sessionToken) 
            throws RemoteException, AuthenticationException;
    
    /**
     * Deletes a backup.
     * @param sessionToken the session token (admin required)
     * @param backupId the backup ID to delete
     * @return true if deletion was successful
     * @throws RemoteException if RMI communication fails
     * @throws AuthenticationException if session is invalid or insufficient permissions
     */
    boolean deleteBackup(String sessionToken, String backupId) 
            throws RemoteException, AuthenticationException;
    
    /**
     * Initiates a system update.
     * @param sessionToken the session token (admin required)
     * @param newVersion the new version to update to
     * @param updateDescription description of the update
     * @return true if update was initiated
     * @throws RemoteException if RMI communication fails
     * @throws AuthenticationException if session is invalid or insufficient permissions
     */
    boolean initiateSystemUpdate(String sessionToken, String newVersion, String updateDescription) 
            throws RemoteException, AuthenticationException;
    
    /**
     * Completes a system update.
     * @param sessionToken the session token (admin required)
     * @param newVersion the new version
     * @return true if update was completed
     * @throws RemoteException if RMI communication fails
     * @throws AuthenticationException if session is invalid or insufficient permissions
     */
    boolean completeSystemUpdate(String sessionToken, String newVersion) 
            throws RemoteException, AuthenticationException;
    
    /**
     * Gets current system version.
     * @return the current system version
     * @throws RemoteException if RMI communication fails
     */
    String getSystemVersion() throws RemoteException;
    
    /**
     * Gets maintenance history.
     * @param sessionToken the session token (admin required)
     * @return list of maintenance events
     * @throws RemoteException if RMI communication fails
     * @throws AuthenticationException if session is invalid or insufficient permissions
     */
    List<Map<String, Object>> getMaintenanceHistory(String sessionToken) 
            throws RemoteException, AuthenticationException;
    
    /**
     * Inner class for report criteria.
     */
    class ReportCriteria implements java.io.Serializable {
        private static final long serialVersionUID = 1L;
        
        private int studentId;
        private int courseId;
        private int teacherId;
        private LocalDate startDate;
        private LocalDate endDate;
        private String reportType; // "PDF", "EXCEL"
        private boolean includeStatistics;
        
        // Constructors
        public ReportCriteria() {}
        
        public ReportCriteria(LocalDate startDate, LocalDate endDate, String reportType) {
            this.startDate = startDate;
            this.endDate = endDate;
            this.reportType = reportType;
        }
        
        // Getters and Setters
        public int getStudentId() { return studentId; }
        public void setStudentId(int studentId) { this.studentId = studentId; }
        
        public int getCourseId() { return courseId; }
        public void setCourseId(int courseId) { this.courseId = courseId; }
        
        public int getTeacherId() { return teacherId; }
        public void setTeacherId(int teacherId) { this.teacherId = teacherId; }
        
        public LocalDate getStartDate() { return startDate; }
        public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
        
        public LocalDate getEndDate() { return endDate; }
        public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
        
        public String getReportType() { return reportType; }
        public void setReportType(String reportType) { this.reportType = reportType; }
        
        public boolean isIncludeStatistics() { return includeStatistics; }
        public void setIncludeStatistics(boolean includeStatistics) { this.includeStatistics = includeStatistics; }
    }
}