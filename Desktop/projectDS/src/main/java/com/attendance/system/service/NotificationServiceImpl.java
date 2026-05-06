package com.attendance.system.service;

import com.attendance.system.dao.AttendanceDAO;
import com.attendance.system.dao.CourseDAO;
import com.attendance.system.dao.NotificationDAO;
import com.attendance.system.dao.UserDAO;
import com.attendance.system.exception.DatabaseException;
import com.attendance.system.exception.ValidationException;
import com.attendance.system.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

/**
 * Implementation of notification service.
 * Handles creation, delivery, and management of notifications.
 */
public class NotificationServiceImpl implements NotificationService {
    private static final Logger logger = LoggerFactory.getLogger(NotificationServiceImpl.class);
    
    private static final double LOW_ATTENDANCE_THRESHOLD = 75.0;
    private static final int ABSENT_NOTIFICATION_DELAY_HOURS = 1;
    private static final int TEACHER_REMINDER_DELAY_HOURS = 2;
    
    private final NotificationDAO notificationDAO;
    private final AttendanceDAO attendanceDAO;
    private final CourseDAO courseDAO;
    private final UserDAO userDAO;
    
    public NotificationServiceImpl() {
        this.notificationDAO = new NotificationDAO();
        this.attendanceDAO = new AttendanceDAO();
        this.courseDAO = new CourseDAO();
        this.userDAO = new UserDAO();
    }
    
    public NotificationServiceImpl(NotificationDAO notificationDAO, AttendanceDAO attendanceDAO,
                                  CourseDAO courseDAO, UserDAO userDAO) {
        this.notificationDAO = notificationDAO;
        this.attendanceDAO = attendanceDAO;
        this.courseDAO = courseDAO;
        this.userDAO = userDAO;
    }
    
    @Override
    public boolean sendLowAttendanceWarning(int studentId, int courseId, double attendancePercentage) 
            throws DatabaseException {
        
        if (attendancePercentage >= LOW_ATTENDANCE_THRESHOLD) {
            logger.debug("Attendance percentage {} is above threshold, no warning needed", attendancePercentage);
            return false;
        }
        
        try {
            Course course = courseDAO.findById(courseId);
            if (course == null) {
                logger.warn("Course not found: {}", courseId);
                return false;
            }
            
            String title = "Low Attendance Warning";
            String message = String.format(
                "Your attendance in %s is %.1f%%, which is below the 75%% threshold. " +
                "Please attend more classes to maintain good academic standing.",
                course.getCourseName(), attendancePercentage
            );
            
            Notification notification = new Notification(
                studentId,
                title,
                message,
                NotificationType.ATTENDANCE_WARNING
            );
            
            boolean success = notificationDAO.insertNotification(notification);
            
            if (success) {
                logger.info("Low attendance warning sent to student {} for course {}", studentId, courseId);
            }
            
            return success;
            
        } catch (Exception e) {
            logger.error("Failed to send low attendance warning", e);
            throw new DatabaseException("Failed to send low attendance warning", e);
        }
    }
    
    @Override
    public boolean sendWeeklyAttendanceSummary(int studentId) throws DatabaseException {
        try {
            Student student = (Student) userDAO.findById(studentId);
            if (student == null) {
                logger.warn("Student not found: {}", studentId);
                return false;
            }
            
            // Get all enrolled courses
            List<Course> enrolledCourses = courseDAO.findByStudentId(studentId);
            
            if (enrolledCourses.isEmpty()) {
                logger.debug("Student {} has no enrolled courses", studentId);
                return false;
            }
            
            // Build summary message
            StringBuilder messageBuilder = new StringBuilder();
            messageBuilder.append("Weekly Attendance Summary:\n\n");
            
            for (Course course : enrolledCourses) {
                Map<String, Object> stats = attendanceDAO.calculateAttendanceStatistics(studentId, course.getCourseId());
                double percentage = (double) stats.getOrDefault("attendancePercentage", 0.0);
                int total = (int) stats.getOrDefault("totalClasses", 0);
                int attended = (int) stats.getOrDefault("attendedClasses", 0);
                
                messageBuilder.append(String.format(
                    "%s: %.1f%% (%d/%d classes)\n",
                    course.getCourseName(), percentage, attended, total
                ));
            }
            
            Notification notification = new Notification(
                studentId,
                "Weekly Attendance Summary",
                messageBuilder.toString(),
                NotificationType.SYSTEM_NOTIFICATION
            );
            
            boolean success = notificationDAO.insertNotification(notification);
            
            if (success) {
                logger.info("Weekly attendance summary sent to student {}", studentId);
            }
            
            return success;
            
        } catch (Exception e) {
            logger.error("Failed to send weekly attendance summary", e);
            throw new DatabaseException("Failed to send weekly attendance summary", e);
        }
    }
    
    @Override
    public boolean sendAbsentNotification(int studentId, int courseId, LocalDate attendanceDate) 
            throws DatabaseException {
        try {
            Course course = courseDAO.findById(courseId);
            if (course == null) {
                logger.warn("Course not found: {}", courseId);
                return false;
            }
            
            String title = "Absence Recorded";
            String message = String.format(
                "Your absence in %s on %s has been recorded. " +
                "Please contact your instructor if this is an error.",
                course.getCourseName(), attendanceDate
            );
            
            Notification notification = new Notification(
                studentId,
                title,
                message,
                NotificationType.SYSTEM_NOTIFICATION
            );
            
            boolean success = notificationDAO.insertNotification(notification);
            
            if (success) {
                logger.info("Absent notification sent to student {} for course {} on {}", 
                    studentId, courseId, attendanceDate);
            }
            
            return success;
            
        } catch (Exception e) {
            logger.error("Failed to send absent notification", e);
            throw new DatabaseException("Failed to send absent notification", e);
        }
    }
    
    @Override
    public boolean sendTeacherAttendanceReminder(int teacherId, int courseId, LocalDate classDate) 
            throws DatabaseException {
        try {
            Course course = courseDAO.findById(courseId);
            if (course == null) {
                logger.warn("Course not found: {}", courseId);
                return false;
            }
            
            String title = "Attendance Marking Reminder";
            String message = String.format(
                "Please mark attendance for %s on %s. " +
                "Attendance should be marked within 2 hours of class end time.",
                course.getCourseName(), classDate
            );
            
            Notification notification = new Notification(
                teacherId,
                title,
                message,
                NotificationType.REMINDER
            );
            
            boolean success = notificationDAO.insertNotification(notification);
            
            if (success) {
                logger.info("Attendance reminder sent to teacher {} for course {} on {}", 
                    teacherId, courseId, classDate);
            }
            
            return success;
            
        } catch (Exception e) {
            logger.error("Failed to send teacher attendance reminder", e);
            throw new DatabaseException("Failed to send teacher attendance reminder", e);
        }
    }
    
    @Override
    public boolean sendNotification(int userId, String title, String message, NotificationType type) 
            throws DatabaseException, ValidationException {
        
        // Validate input
        if (userId <= 0) {
            throw new ValidationException("Invalid user ID");
        }
        if (title == null || title.trim().isEmpty()) {
            throw new ValidationException("Notification title cannot be empty");
        }
        if (message == null || message.trim().isEmpty()) {
            throw new ValidationException("Notification message cannot be empty");
        }
        if (type == null) {
            throw new ValidationException("Notification type cannot be null");
        }
        
        try {
            Notification notification = new Notification(userId, title, message, type);
            boolean success = notificationDAO.insertNotification(notification);
            
            if (success) {
                logger.info("Notification sent to user {}: {}", userId, title);
            }
            
            return success;
            
        } catch (Exception e) {
            logger.error("Failed to send notification", e);
            throw new DatabaseException("Failed to send notification", e);
        }
    }
    
    @Override
    public int sendBulkNotification(List<Integer> userIds, String title, String message, NotificationType type) 
            throws DatabaseException {
        
        if (userIds == null || userIds.isEmpty()) {
            return 0;
        }
        
        int successCount = 0;
        
        for (int userId : userIds) {
            try {
                if (sendNotification(userId, title, message, type)) {
                    successCount++;
                }
            } catch (ValidationException e) {
                logger.warn("Failed to send notification to user {}: {}", userId, e.getMessage());
            }
        }
        
        logger.info("Bulk notification sent to {} out of {} users", successCount, userIds.size());
        return successCount;
    }
    
    @Override
    public List<Notification> getNotifications(int userId, boolean unreadOnly) throws DatabaseException {
        try {
            return notificationDAO.findByUser(userId, unreadOnly);
        } catch (Exception e) {
            logger.error("Failed to get notifications for user {}", userId, e);
            throw new DatabaseException("Failed to get notifications", e);
        }
    }
    
    @Override
    public int getUnreadCount(int userId) throws DatabaseException {
        try {
            return notificationDAO.getUnreadCount(userId);
        } catch (Exception e) {
            logger.error("Failed to get unread count for user {}", userId, e);
            throw new DatabaseException("Failed to get unread count", e);
        }
    }
    
    @Override
    public boolean markNotificationAsRead(int notificationId) throws DatabaseException {
        try {
            boolean success = notificationDAO.markAsRead(notificationId);
            if (success) {
                logger.info("Notification marked as read: {}", notificationId);
            }
            return success;
        } catch (Exception e) {
            logger.error("Failed to mark notification as read: {}", notificationId, e);
            throw new DatabaseException("Failed to mark notification as read", e);
        }
    }
    
    @Override
    public int markAllNotificationsAsRead(int userId) throws DatabaseException {
        try {
            int count = notificationDAO.markAllAsRead(userId);
            logger.info("Marked {} notifications as read for user {}", count, userId);
            return count;
        } catch (Exception e) {
            logger.error("Failed to mark all notifications as read for user {}", userId, e);
            throw new DatabaseException("Failed to mark all notifications as read", e);
        }
    }
    
    @Override
    public boolean deleteNotification(int notificationId) throws DatabaseException {
        try {
            boolean success = notificationDAO.deleteNotification(notificationId);
            if (success) {
                logger.info("Notification deleted: {}", notificationId);
            }
            return success;
        } catch (Exception e) {
            logger.error("Failed to delete notification: {}", notificationId, e);
            throw new DatabaseException("Failed to delete notification", e);
        }
    }
    
    @Override
    public Map<String, Object> getNotificationPreferences(int userId) throws DatabaseException {
        try {
            Map<String, Object> preferences = new HashMap<>();
            
            // Default preferences
            preferences.put("emailNotifications", true);
            preferences.put("inAppNotifications", true);
            preferences.put("lowAttendanceWarnings", true);
            preferences.put("weeklyAttendanceSummary", true);
            preferences.put("absentNotifications", true);
            preferences.put("teacherReminders", true);
            preferences.put("notificationFrequency", "IMMEDIATE");
            
            // In a full implementation, these would be retrieved from a NOTIFICATION_PREFERENCES table
            // For now, we return default preferences
            
            logger.debug("Retrieved notification preferences for user {}", userId);
            return preferences;
            
        } catch (Exception e) {
            logger.error("Failed to get notification preferences for user {}", userId, e);
            throw new DatabaseException("Failed to get notification preferences", e);
        }
    }
    
    @Override
    public boolean updateNotificationPreferences(int userId, Map<String, Object> preferences) 
            throws DatabaseException, ValidationException {
        
        if (userId <= 0) {
            throw new ValidationException("Invalid user ID");
        }
        if (preferences == null || preferences.isEmpty()) {
            throw new ValidationException("Preferences cannot be empty");
        }
        
        try {
            // Validate preference values
            for (Map.Entry<String, Object> entry : preferences.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                
                if (key.endsWith("Notifications") || key.endsWith("Warnings") || key.endsWith("Summary") || key.endsWith("Reminders")) {
                    if (!(value instanceof Boolean)) {
                        throw new ValidationException("Preference " + key + " must be a boolean");
                    }
                }
            }
            
            // In a full implementation, these would be stored in a NOTIFICATION_PREFERENCES table
            // For now, we just validate and log
            
            logger.info("Updated notification preferences for user {}", userId);
            return true;
            
        } catch (Exception e) {
            logger.error("Failed to update notification preferences for user {}", userId, e);
            throw new DatabaseException("Failed to update notification preferences", e);
        }
    }
    
    @Override
    public boolean isEmailNotificationEnabled(int userId) throws DatabaseException {
        try {
            Map<String, Object> preferences = getNotificationPreferences(userId);
            return (boolean) preferences.getOrDefault("emailNotifications", true);
        } catch (Exception e) {
            logger.error("Failed to check email notification status for user {}", userId, e);
            throw new DatabaseException("Failed to check email notification status", e);
        }
    }
    
    @Override
    public boolean isInAppNotificationEnabled(int userId) throws DatabaseException {
        try {
            Map<String, Object> preferences = getNotificationPreferences(userId);
            return (boolean) preferences.getOrDefault("inAppNotifications", true);
        } catch (Exception e) {
            logger.error("Failed to check in-app notification status for user {}", userId, e);
            throw new DatabaseException("Failed to check in-app notification status", e);
        }
    }
    
    @Override
    public List<Notification> getNotificationHistory(int userId, int limit) throws DatabaseException {
        try {
            List<Notification> allNotifications = notificationDAO.findByUser(userId, false);
            
            // Return only the most recent 'limit' notifications
            if (allNotifications.size() > limit) {
                return new ArrayList<>(allNotifications.subList(0, limit));
            }
            
            return allNotifications;
            
        } catch (Exception e) {
            logger.error("Failed to get notification history for user {}", userId, e);
            throw new DatabaseException("Failed to get notification history", e);
        }
    }
    
    @Override
    public int processPendingNotifications() throws DatabaseException {
        try {
            // This method would process notifications that are scheduled to be sent
            // For now, it's a placeholder for future implementation
            logger.debug("Processing pending notifications");
            return 0;
        } catch (Exception e) {
            logger.error("Failed to process pending notifications", e);
            throw new DatabaseException("Failed to process pending notifications", e);
        }
    }
    
    /**
     * Checks all students for low attendance and sends warnings.
     * Should be called periodically (e.g., daily).
     * 
     * @return number of warnings sent
     * @throws DatabaseException if database operation fails
     */
    public int checkAndSendLowAttendanceWarnings() throws DatabaseException {
        try {
            int warningsSent = 0;
            
            // Get all students
            List<User> allUsers = userDAO.findByRole(UserRole.STUDENT);
            
            for (User user : allUsers) {
                Student student = (Student) user;
                
                // Get all enrolled courses
                List<Course> enrolledCourses = courseDAO.findByStudentId(student.getUserId());
                
                for (Course course : enrolledCourses) {
                    Map<String, Object> stats = attendanceDAO.calculateAttendanceStatistics(
                        student.getUserId(), course.getCourseId()
                    );
                    
                    double percentage = (double) stats.getOrDefault("attendancePercentage", 0.0);
                    
                    if (percentage < LOW_ATTENDANCE_THRESHOLD) {
                        if (sendLowAttendanceWarning(student.getUserId(), course.getCourseId(), percentage)) {
                            warningsSent++;
                        }
                    }
                }
            }
            
            logger.info("Sent {} low attendance warnings", warningsSent);
            return warningsSent;
            
        } catch (Exception e) {
            logger.error("Failed to check and send low attendance warnings", e);
            throw new DatabaseException("Failed to check and send low attendance warnings", e);
        }
    }
    
    /**
     * Sends weekly attendance summaries to all students.
     * Should be called periodically (e.g., weekly).
     * 
     * @return number of summaries sent
     * @throws DatabaseException if database operation fails
     */
    public int sendWeeklyAttendanceSummaries() throws DatabaseException {
        try {
            int summariesSent = 0;
            
            // Get all students
            List<User> allUsers = userDAO.findByRole(UserRole.STUDENT);
            
            for (User user : allUsers) {
                if (sendWeeklyAttendanceSummary(user.getUserId())) {
                    summariesSent++;
                }
            }
            
            logger.info("Sent {} weekly attendance summaries", summariesSent);
            return summariesSent;
            
        } catch (Exception e) {
            logger.error("Failed to send weekly attendance summaries", e);
            throw new DatabaseException("Failed to send weekly attendance summaries", e);
        }
    }
}
