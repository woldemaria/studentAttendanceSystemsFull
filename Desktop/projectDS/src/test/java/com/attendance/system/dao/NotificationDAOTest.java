package com.attendance.system.dao;

import com.attendance.system.exception.DatabaseException;
import com.attendance.system.model.Notification;
import com.attendance.system.model.NotificationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for NotificationDAO.
 * Tests notification database operations.
 */
@DisplayName("NotificationDAO Tests")
public class NotificationDAOTest {
    
    private NotificationDAO notificationDAO;
    private DatabaseManager databaseManager;
    
    @BeforeEach
    public void setUp() {
        // Initialize with test database
        databaseManager = DatabaseManager.getInstance();
        notificationDAO = new NotificationDAO(databaseManager);
    }
    
    @Test
    @DisplayName("Should insert notification successfully")
    public void testInsertNotification() throws DatabaseException {
        // Arrange
        Notification notification = new Notification(
            1,
            "Test Notification",
            "This is a test notification",
            NotificationType.SYSTEM_NOTIFICATION
        );
        
        // Act
        boolean result = notificationDAO.insertNotification(notification);
        
        // Assert
        assertTrue(result);
        assertNotEquals(0, notification.getNotificationId());
    }
    
    @Test
    @DisplayName("Should find notifications by user ID")
    public void testFindByUser() throws DatabaseException {
        // Arrange
        int userId = 1;
        Notification notification = new Notification(
            userId,
            "Test Notification",
            "This is a test notification",
            NotificationType.SYSTEM_NOTIFICATION
        );
        notificationDAO.insertNotification(notification);
        
        // Act
        List<Notification> result = notificationDAO.findByUser(userId, false);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.size() > 0);
    }
    
    @Test
    @DisplayName("Should find unread notifications only")
    public void testFindByUser_UnreadOnly() throws DatabaseException {
        // Arrange
        int userId = 1;
        Notification notification = new Notification(
            userId,
            "Unread Notification",
            "This is an unread notification",
            NotificationType.SYSTEM_NOTIFICATION
        );
        notificationDAO.insertNotification(notification);
        
        // Act
        List<Notification> result = notificationDAO.findByUser(userId, true);
        
        // Assert
        assertNotNull(result);
        for (Notification n : result) {
            assertFalse(n.isRead());
        }
    }
    
    @Test
    @DisplayName("Should find notification by ID")
    public void testFindById() throws DatabaseException {
        // Arrange
        Notification notification = new Notification(
            1,
            "Test Notification",
            "This is a test notification",
            NotificationType.SYSTEM_NOTIFICATION
        );
        notificationDAO.insertNotification(notification);
        int notificationId = notification.getNotificationId();
        
        // Act
        Notification result = notificationDAO.findById(notificationId);
        
        // Assert
        assertNotNull(result);
        assertEquals(notificationId, result.getNotificationId());
        assertEquals("Test Notification", result.getTitle());
    }
    
    @Test
    @DisplayName("Should mark notification as read")
    public void testMarkAsRead() throws DatabaseException {
        // Arrange
        Notification notification = new Notification(
            1,
            "Test Notification",
            "This is a test notification",
            NotificationType.SYSTEM_NOTIFICATION
        );
        notificationDAO.insertNotification(notification);
        int notificationId = notification.getNotificationId();
        
        // Act
        boolean result = notificationDAO.markAsRead(notificationId);
        
        // Assert
        assertTrue(result);
        Notification updated = notificationDAO.findById(notificationId);
        assertTrue(updated.isRead());
    }
    
    @Test
    @DisplayName("Should mark notification as unread")
    public void testMarkAsUnread() throws DatabaseException {
        // Arrange
        Notification notification = new Notification(
            1,
            "Test Notification",
            "This is a test notification",
            NotificationType.SYSTEM_NOTIFICATION
        );
        notification.setRead(true);
        notificationDAO.insertNotification(notification);
        int notificationId = notification.getNotificationId();
        
        // Act
        boolean result = notificationDAO.markAsUnread(notificationId);
        
        // Assert
        assertTrue(result);
        Notification updated = notificationDAO.findById(notificationId);
        assertFalse(updated.isRead());
    }
    
    @Test
    @DisplayName("Should mark all notifications as read for user")
    public void testMarkAllAsRead() throws DatabaseException {
        // Arrange
        int userId = 1;
        for (int i = 0; i < 3; i++) {
            Notification notification = new Notification(
                userId,
                "Test Notification " + i,
                "This is test notification " + i,
                NotificationType.SYSTEM_NOTIFICATION
            );
            notificationDAO.insertNotification(notification);
        }
        
        // Act
        int result = notificationDAO.markAllAsRead(userId);
        
        // Assert
        assertTrue(result > 0);
        List<Notification> unread = notificationDAO.findByUser(userId, true);
        assertEquals(0, unread.size());
    }
    
    @Test
    @DisplayName("Should delete notification")
    public void testDeleteNotification() throws DatabaseException {
        // Arrange
        Notification notification = new Notification(
            1,
            "Test Notification",
            "This is a test notification",
            NotificationType.SYSTEM_NOTIFICATION
        );
        notificationDAO.insertNotification(notification);
        int notificationId = notification.getNotificationId();
        
        // Act
        boolean result = notificationDAO.deleteNotification(notificationId);
        
        // Assert
        assertTrue(result);
        Notification deleted = notificationDAO.findById(notificationId);
        assertNull(deleted);
    }
    
    @Test
    @DisplayName("Should delete all notifications for user")
    public void testDeleteAllForUser() throws DatabaseException {
        // Arrange
        int userId = 1;
        for (int i = 0; i < 3; i++) {
            Notification notification = new Notification(
                userId,
                "Test Notification " + i,
                "This is test notification " + i,
                NotificationType.SYSTEM_NOTIFICATION
            );
            notificationDAO.insertNotification(notification);
        }
        
        // Act
        int result = notificationDAO.deleteAllForUser(userId);
        
        // Assert
        assertTrue(result > 0);
        List<Notification> remaining = notificationDAO.findByUser(userId, false);
        assertEquals(0, remaining.size());
    }
    
    @Test
    @DisplayName("Should get unread notification count")
    public void testGetUnreadCount() throws DatabaseException {
        // Arrange
        int userId = 1;
        for (int i = 0; i < 3; i++) {
            Notification notification = new Notification(
                userId,
                "Test Notification " + i,
                "This is test notification " + i,
                NotificationType.SYSTEM_NOTIFICATION
            );
            notificationDAO.insertNotification(notification);
        }
        
        // Act
        int result = notificationDAO.getUnreadCount(userId);
        
        // Assert
        assertTrue(result > 0);
    }
    
    @Test
    @DisplayName("Should find notifications by type")
    public void testFindByType() throws DatabaseException {
        // Arrange
        int userId = 1;
        Notification notification = new Notification(
            userId,
            "Attendance Warning",
            "Your attendance is low",
            NotificationType.ATTENDANCE_WARNING
        );
        notificationDAO.insertNotification(notification);
        
        // Act
        List<Notification> result = notificationDAO.findByType(userId, NotificationType.ATTENDANCE_WARNING);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.size() > 0);
        for (Notification n : result) {
            assertEquals(NotificationType.ATTENDANCE_WARNING, n.getType());
        }
    }
    
    @Test
    @DisplayName("Should find recent notifications")
    public void testFindRecent() throws DatabaseException {
        // Arrange
        int userId = 1;
        Notification notification = new Notification(
            userId,
            "Recent Notification",
            "This is a recent notification",
            NotificationType.SYSTEM_NOTIFICATION
        );
        notificationDAO.insertNotification(notification);
        
        // Act
        List<Notification> result = notificationDAO.findRecent(userId, 24);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.size() > 0);
    }
}
