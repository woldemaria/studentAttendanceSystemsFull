package com.attendance.system.service;

import com.attendance.system.exception.DatabaseException;
import com.attendance.system.exception.ValidationException;
import com.attendance.system.model.Notification;
import com.attendance.system.model.NotificationType;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Service interface for notification operations.
 * Handles creation, delivery, and management of notifications.
 */
public interface NotificationService {
    
    /**
     * Sends a low attendance warning notification to a student.
     * Triggered when student's attendance falls below 75% in any course.
     * 
     * @param studentId the student ID
     * @param courseId the course ID
     * @param attendancePercentage the current attendance percentage
     * @return true if notification was sent successfully
     * @throws DatabaseException if database operation fails
     */
    boolean sendLowAttendanceWarning(int studentId, int courseId, double attendancePercentage) 
            throws DatabaseException;
    
    /**
     * Sends a weekly attendance summary notification to a student.
     * Contains summary of attendance for all enrolled courses.
     * 
     * @param studentId the student ID
     * @return true if notification was sent successfully
     * @throws DatabaseException if database operation fails
     */
    boolean sendWeeklyAttendanceSummary(int studentId) throws DatabaseException;
    
    /**
     * Sends an absent notification to a student.
     * Triggered when attendance is marked as absent.
     * Must be delivered within 1 hour of marking.
     * 
     * @param studentId the student ID
     * @param courseId the course ID
     * @param attendanceDate the date of absence
     * @return true if notification was sent successfully
     * @throws DatabaseException if database operation fails
     */
    boolean sendAbsentNotification(int studentId, int courseId, LocalDate attendanceDate) 
            throws DatabaseException;
    
    /**
     * Sends a teacher reminder notification for unmarked attendance.
     * Triggered when attendance has not been marked within 2 hours of class end time.
     * 
     * @param teacherId the teacher ID
     * @param courseId the course ID
     * @param classDate the date of the class
     * @return true if notification was sent successfully
     * @throws DatabaseException if database operation fails
     */
    boolean sendTeacherAttendanceReminder(int teacherId, int courseId, LocalDate classDate) 
            throws DatabaseException;
    
    /**
     * Sends a generic notification to a user.
     * 
     * @param userId the user ID
     * @param title the notification title
     * @param message the notification message
     * @param type the notification type
     * @return true if notification was sent successfully
     * @throws DatabaseException if database operation fails
     * @throws ValidationException if notification data is invalid
     */
    boolean sendNotification(int userId, String title, String message, NotificationType type) 
            throws DatabaseException, ValidationException;
    
    /**
     * Sends notifications to multiple users.
     * 
     * @param userIds list of user IDs
     * @param title the notification title
     * @param message the notification message
     * @param type the notification type
     * @return number of notifications sent successfully
     * @throws DatabaseException if database operation fails
     */
    int sendBulkNotification(List<Integer> userIds, String title, String message, NotificationType type) 
            throws DatabaseException;
    
    /**
     * Gets notifications for a user.
     * 
     * @param userId the user ID
     * @param unreadOnly true to get only unread notifications
     * @return list of notifications
     * @throws DatabaseException if database operation fails
     */
    List<Notification> getNotifications(int userId, boolean unreadOnly) throws DatabaseException;
    
    /**
     * Gets unread notification count for a user.
     * 
     * @param userId the user ID
     * @return count of unread notifications
     * @throws DatabaseException if database operation fails
     */
    int getUnreadCount(int userId) throws DatabaseException;
    
    /**
     * Marks a notification as read.
     * 
     * @param notificationId the notification ID
     * @return true if notification was marked as read
     * @throws DatabaseException if database operation fails
     */
    boolean markNotificationAsRead(int notificationId) throws DatabaseException;
    
    /**
     * Marks all notifications for a user as read.
     * 
     * @param userId the user ID
     * @return number of notifications marked as read
     * @throws DatabaseException if database operation fails
     */
    int markAllNotificationsAsRead(int userId) throws DatabaseException;
    
    /**
     * Deletes a notification.
     * 
     * @param notificationId the notification ID
     * @return true if notification was deleted
     * @throws DatabaseException if database operation fails
     */
    boolean deleteNotification(int notificationId) throws DatabaseException;
    
    /**
     * Gets notification preferences for a user.
     * 
     * @param userId the user ID
     * @return map of notification preferences
     * @throws DatabaseException if database operation fails
     */
    Map<String, Object> getNotificationPreferences(int userId) throws DatabaseException;
    
    /**
     * Updates notification preferences for a user.
     * 
     * @param userId the user ID
     * @param preferences map of preferences to update
     * @return true if preferences were updated successfully
     * @throws DatabaseException if database operation fails
     * @throws ValidationException if preferences are invalid
     */
    boolean updateNotificationPreferences(int userId, Map<String, Object> preferences) 
            throws DatabaseException, ValidationException;
    
    /**
     * Checks if a user has email notifications enabled.
     * 
     * @param userId the user ID
     * @return true if email notifications are enabled
     * @throws DatabaseException if database operation fails
     */
    boolean isEmailNotificationEnabled(int userId) throws DatabaseException;
    
    /**
     * Checks if a user has in-app notifications enabled.
     * 
     * @param userId the user ID
     * @return true if in-app notifications are enabled
     * @throws DatabaseException if database operation fails
     */
    boolean isInAppNotificationEnabled(int userId) throws DatabaseException;
    
    /**
     * Gets notification history for a user.
     * 
     * @param userId the user ID
     * @param limit maximum number of notifications to return
     * @return list of notifications
     * @throws DatabaseException if database operation fails
     */
    List<Notification> getNotificationHistory(int userId, int limit) throws DatabaseException;
    
    /**
     * Processes pending notifications.
     * Called periodically to send notifications that are due.
     * 
     * @return number of notifications processed
     * @throws DatabaseException if database operation fails
     */
    int processPendingNotifications() throws DatabaseException;
}
