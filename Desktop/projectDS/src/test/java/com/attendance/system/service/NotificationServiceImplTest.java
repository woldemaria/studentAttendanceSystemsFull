package com.attendance.system.service;

import com.attendance.system.dao.AttendanceDAO;
import com.attendance.system.dao.CourseDAO;
import com.attendance.system.dao.NotificationDAO;
import com.attendance.system.dao.UserDAO;
import com.attendance.system.exception.DatabaseException;
import com.attendance.system.exception.ValidationException;
import com.attendance.system.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for NotificationServiceImpl.
 * Tests notification creation, delivery, and preference management.
 */
@DisplayName("NotificationServiceImpl Tests")
public class NotificationServiceImplTest {
    
    private NotificationServiceImpl notificationService;
    
    @Mock
    private NotificationDAO notificationDAO;
    
    @Mock
    private AttendanceDAO attendanceDAO;
    
    @Mock
    private CourseDAO courseDAO;
    
    @Mock
    private UserDAO userDAO;
    
    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        
        notificationService = new NotificationServiceImpl(
            notificationDAO, attendanceDAO, courseDAO, userDAO
        );
    }
    
    @Test
    @DisplayName("Should send low attendance warning when attendance below 75%")
    public void testSendLowAttendanceWarning_BelowThreshold() throws DatabaseException {
        // Arrange
        int studentId = 1;
        int courseId = 1;
        double attendancePercentage = 70.0;
        
        Course course = new Course();
        course.setCourseId(courseId);
        course.setCourseName("Mathematics 101");
        
        when(courseDAO.findById(courseId)).thenReturn(course);
        when(notificationDAO.insertNotification(any(Notification.class))).thenReturn(true);
        
        // Act
        boolean result = notificationService.sendLowAttendanceWarning(studentId, courseId, attendancePercentage);
        
        // Assert
        assertTrue(result);
        verify(notificationDAO, times(1)).insertNotification(any(Notification.class));
    }
    
    @Test
    @DisplayName("Should not send warning when attendance at or above 75%")
    public void testSendLowAttendanceWarning_AboveThreshold() throws DatabaseException {
        // Arrange
        int studentId = 1;
        int courseId = 1;
        double attendancePercentage = 80.0;
        
        // Act
        boolean result = notificationService.sendLowAttendanceWarning(studentId, courseId, attendancePercentage);
        
        // Assert
        assertFalse(result);
        verify(notificationDAO, never()).insertNotification(any(Notification.class));
    }
    
    @Test
    @DisplayName("Should send absent notification when student marked absent")
    public void testSendAbsentNotification() throws DatabaseException {
        // Arrange
        int studentId = 1;
        int courseId = 1;
        LocalDate attendanceDate = LocalDate.now();
        
        Course course = new Course();
        course.setCourseId(courseId);
        course.setCourseName("Physics 101");
        
        when(courseDAO.findById(courseId)).thenReturn(course);
        when(notificationDAO.insertNotification(any(Notification.class))).thenReturn(true);
        
        // Act
        boolean result = notificationService.sendAbsentNotification(studentId, courseId, attendanceDate);
        
        // Assert
        assertTrue(result);
        verify(notificationDAO, times(1)).insertNotification(any(Notification.class));
    }
    
    @Test
    @DisplayName("Should send teacher attendance reminder")
    public void testSendTeacherAttendanceReminder() throws DatabaseException {
        // Arrange
        int teacherId = 1;
        int courseId = 1;
        LocalDate classDate = LocalDate.now();
        
        Course course = new Course();
        course.setCourseId(courseId);
        course.setCourseName("Chemistry 101");
        
        when(courseDAO.findById(courseId)).thenReturn(course);
        when(notificationDAO.insertNotification(any(Notification.class))).thenReturn(true);
        
        // Act
        boolean result = notificationService.sendTeacherAttendanceReminder(teacherId, courseId, classDate);
        
        // Assert
        assertTrue(result);
        verify(notificationDAO, times(1)).insertNotification(any(Notification.class));
    }
    
    @Test
    @DisplayName("Should send generic notification with valid data")
    public void testSendNotification_ValidData() throws DatabaseException, ValidationException {
        // Arrange
        int userId = 1;
        String title = "Test Notification";
        String message = "This is a test notification";
        NotificationType type = NotificationType.SYSTEM_NOTIFICATION;
        
        when(notificationDAO.insertNotification(any(Notification.class))).thenReturn(true);
        
        // Act
        boolean result = notificationService.sendNotification(userId, title, message, type);
        
        // Assert
        assertTrue(result);
        verify(notificationDAO, times(1)).insertNotification(any(Notification.class));
    }
    
    @Test
    @DisplayName("Should throw ValidationException for invalid user ID")
    public void testSendNotification_InvalidUserId() {
        // Arrange
        int userId = -1;
        String title = "Test";
        String message = "Test message";
        NotificationType type = NotificationType.SYSTEM_NOTIFICATION;
        
        // Act & Assert
        assertThrows(ValidationException.class, () -> 
            notificationService.sendNotification(userId, title, message, type)
        );
    }
    
    @Test
    @DisplayName("Should throw ValidationException for empty title")
    public void testSendNotification_EmptyTitle() {
        // Arrange
        int userId = 1;
        String title = "";
        String message = "Test message";
        NotificationType type = NotificationType.SYSTEM_NOTIFICATION;
        
        // Act & Assert
        assertThrows(ValidationException.class, () -> 
            notificationService.sendNotification(userId, title, message, type)
        );
    }
    
    @Test
    @DisplayName("Should throw ValidationException for empty message")
    public void testSendNotification_EmptyMessage() {
        // Arrange
        int userId = 1;
        String title = "Test";
        String message = "";
        NotificationType type = NotificationType.SYSTEM_NOTIFICATION;
        
        // Act & Assert
        assertThrows(ValidationException.class, () -> 
            notificationService.sendNotification(userId, title, message, type)
        );
    }
    
    @Test
    @DisplayName("Should send bulk notifications to multiple users")
    public void testSendBulkNotification() throws DatabaseException, ValidationException {
        // Arrange
        List<Integer> userIds = Arrays.asList(1, 2, 3);
        String title = "Bulk Notification";
        String message = "This is a bulk notification";
        NotificationType type = NotificationType.SYSTEM_NOTIFICATION;
        
        when(notificationDAO.insertNotification(any(Notification.class))).thenReturn(true);
        
        // Act
        int result = notificationService.sendBulkNotification(userIds, title, message, type);
        
        // Assert
        assertEquals(3, result);
        verify(notificationDAO, times(3)).insertNotification(any(Notification.class));
    }
    
    @Test
    @DisplayName("Should get notifications for user")
    public void testGetNotifications() throws DatabaseException {
        // Arrange
        int userId = 1;
        List<Notification> notifications = new ArrayList<>();
        
        Notification notif1 = new Notification(userId, "Title 1", "Message 1", NotificationType.SYSTEM_NOTIFICATION);
        Notification notif2 = new Notification(userId, "Title 2", "Message 2", NotificationType.ATTENDANCE_WARNING);
        notifications.add(notif1);
        notifications.add(notif2);
        
        when(notificationDAO.findByUser(userId, false)).thenReturn(notifications);
        
        // Act
        List<Notification> result = notificationService.getNotifications(userId, false);
        
        // Assert
        assertEquals(2, result.size());
        verify(notificationDAO, times(1)).findByUser(userId, false);
    }
    
    @Test
    @DisplayName("Should get unread notification count")
    public void testGetUnreadCount() throws DatabaseException {
        // Arrange
        int userId = 1;
        when(notificationDAO.getUnreadCount(userId)).thenReturn(5);
        
        // Act
        int result = notificationService.getUnreadCount(userId);
        
        // Assert
        assertEquals(5, result);
        verify(notificationDAO, times(1)).getUnreadCount(userId);
    }
    
    @Test
    @DisplayName("Should mark notification as read")
    public void testMarkNotificationAsRead() throws DatabaseException {
        // Arrange
        int notificationId = 1;
        when(notificationDAO.markAsRead(notificationId)).thenReturn(true);
        
        // Act
        boolean result = notificationService.markNotificationAsRead(notificationId);
        
        // Assert
        assertTrue(result);
        verify(notificationDAO, times(1)).markAsRead(notificationId);
    }
    
    @Test
    @DisplayName("Should mark all notifications as read for user")
    public void testMarkAllNotificationsAsRead() throws DatabaseException {
        // Arrange
        int userId = 1;
        when(notificationDAO.markAllAsRead(userId)).thenReturn(3);
        
        // Act
        int result = notificationService.markAllNotificationsAsRead(userId);
        
        // Assert
        assertEquals(3, result);
        verify(notificationDAO, times(1)).markAllAsRead(userId);
    }
    
    @Test
    @DisplayName("Should delete notification")
    public void testDeleteNotification() throws DatabaseException {
        // Arrange
        int notificationId = 1;
        when(notificationDAO.deleteNotification(notificationId)).thenReturn(true);
        
        // Act
        boolean result = notificationService.deleteNotification(notificationId);
        
        // Assert
        assertTrue(result);
        verify(notificationDAO, times(1)).deleteNotification(notificationId);
    }
    
    @Test
    @DisplayName("Should get notification preferences")
    public void testGetNotificationPreferences() throws DatabaseException {
        // Arrange
        int userId = 1;
        
        // Act
        Map<String, Object> result = notificationService.getNotificationPreferences(userId);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.containsKey("emailNotifications"));
        assertTrue(result.containsKey("inAppNotifications"));
        assertTrue((boolean) result.get("emailNotifications"));
        assertTrue((boolean) result.get("inAppNotifications"));
    }
    
    @Test
    @DisplayName("Should update notification preferences")
    public void testUpdateNotificationPreferences() throws DatabaseException, ValidationException {
        // Arrange
        int userId = 1;
        Map<String, Object> preferences = new HashMap<>();
        preferences.put("emailNotifications", false);
        preferences.put("inAppNotifications", true);
        
        // Act
        boolean result = notificationService.updateNotificationPreferences(userId, preferences);
        
        // Assert
        assertTrue(result);
    }
    
    @Test
    @DisplayName("Should throw ValidationException for invalid preferences")
    public void testUpdateNotificationPreferences_InvalidData() {
        // Arrange
        int userId = 1;
        Map<String, Object> preferences = new HashMap<>();
        preferences.put("emailNotifications", "invalid"); // Should be boolean
        
        // Act & Assert
        assertThrows(ValidationException.class, () -> 
            notificationService.updateNotificationPreferences(userId, preferences)
        );
    }
    
    @Test
    @DisplayName("Should check if email notifications are enabled")
    public void testIsEmailNotificationEnabled() throws DatabaseException {
        // Arrange
        int userId = 1;
        
        // Act
        boolean result = notificationService.isEmailNotificationEnabled(userId);
        
        // Assert
        assertTrue(result);
    }
    
    @Test
    @DisplayName("Should check if in-app notifications are enabled")
    public void testIsInAppNotificationEnabled() throws DatabaseException {
        // Arrange
        int userId = 1;
        
        // Act
        boolean result = notificationService.isInAppNotificationEnabled(userId);
        
        // Assert
        assertTrue(result);
    }
    
    @Test
    @DisplayName("Should get notification history with limit")
    public void testGetNotificationHistory() throws DatabaseException {
        // Arrange
        int userId = 1;
        int limit = 10;
        List<Notification> allNotifications = new ArrayList<>();
        
        for (int i = 0; i < 15; i++) {
            allNotifications.add(new Notification(userId, "Title " + i, "Message " + i, NotificationType.SYSTEM_NOTIFICATION));
        }
        
        when(notificationDAO.findByUser(userId, false)).thenReturn(allNotifications);
        
        // Act
        List<Notification> result = notificationService.getNotificationHistory(userId, limit);
        
        // Assert
        assertEquals(10, result.size());
    }
    
    @Test
    @DisplayName("Should send weekly attendance summary")
    public void testSendWeeklyAttendanceSummary() throws DatabaseException {
        // Arrange
        int studentId = 1;
        Student student = new Student();
        student.setUserId(studentId);
        student.setFirstName("John");
        student.setLastName("Doe");
        
        List<Course> courses = new ArrayList<>();
        Course course = new Course();
        course.setCourseId(1);
        course.setCourseName("Math 101");
        courses.add(course);
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("attendancePercentage", 85.0);
        stats.put("totalClasses", 20);
        stats.put("attendedClasses", 17);
        
        when(userDAO.findById(studentId)).thenReturn(student);
        when(courseDAO.findByStudentId(studentId)).thenReturn(courses);
        when(attendanceDAO.calculateAttendanceStatistics(studentId, 1)).thenReturn(stats);
        when(notificationDAO.insertNotification(any(Notification.class))).thenReturn(true);
        
        // Act
        boolean result = notificationService.sendWeeklyAttendanceSummary(studentId);
        
        // Assert
        assertTrue(result);
        verify(notificationDAO, times(1)).insertNotification(any(Notification.class));
    }
}
