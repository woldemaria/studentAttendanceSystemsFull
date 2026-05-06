package com.attendance.system.dao;

import com.attendance.system.exception.DatabaseException;
import com.attendance.system.model.Notification;
import com.attendance.system.model.NotificationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Notification operations.
 */
public class NotificationDAO {
    private static final Logger logger = LoggerFactory.getLogger(NotificationDAO.class);
    
    private final DatabaseManager databaseManager;
    
    public NotificationDAO() {
        this.databaseManager = DatabaseManager.getInstance();
    }
    
    public NotificationDAO(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }
    
    /**
     * Inserts a new notification.
     * @param notification the notification to insert
     * @return true if notification was inserted successfully
     * @throws DatabaseException if database error occurs
     */
    public boolean insertNotification(Notification notification) throws DatabaseException {
        String sql = """
            INSERT INTO NOTIFICATIONS (user_id, title, message, type, is_read)
            VALUES (?, ?, ?, ?, ?)
            """;
        
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            statement.setInt(1, notification.getUserId());
            statement.setString(2, notification.getTitle());
            statement.setString(3, notification.getMessage());
            statement.setString(4, notification.getType().name());
            statement.setBoolean(5, notification.isRead());
            
            int rowsAffected = statement.executeUpdate();
            
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        notification.setNotificationId(generatedKeys.getInt(1));
                    }
                }
                connection.commit();
                logger.info("Notification inserted: " + notification);
                return true;
            }
            
            return false;
            
        } catch (SQLException e) {
            logger.error("Failed to insert notification", e);
            throw DatabaseException.queryFailed(sql, e);
        }
    }
    
    /**
     * Finds notifications by user ID.
     * @param userId the user ID
     * @param unreadOnly true to get only unread notifications
     * @return list of notifications
     * @throws DatabaseException if database error occurs
     */
    public List<Notification> findByUser(int userId, boolean unreadOnly) throws DatabaseException {
        String sql;
        if (unreadOnly) {
            sql = """
                SELECT * FROM NOTIFICATIONS 
                WHERE user_id = ? AND is_read = FALSE
                ORDER BY created_at DESC
                """;
        } else {
            sql = """
                SELECT * FROM NOTIFICATIONS 
                WHERE user_id = ?
                ORDER BY created_at DESC
                """;
        }
        
        List<Notification> notifications = new ArrayList<>();
        
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setInt(1, userId);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    notifications.add(mapResultSetToNotification(resultSet));
                }
            }
            
            return notifications;
            
        } catch (SQLException e) {
            logger.error("Failed to find notifications by user: " + userId, e);
            throw DatabaseException.queryFailed(sql, e);
        }
    }
    
    /**
     * Finds a notification by ID.
     * @param notificationId the notification ID
     * @return notification or null if not found
     * @throws DatabaseException if database error occurs
     */
    public Notification findById(int notificationId) throws DatabaseException {
        String sql = "SELECT * FROM NOTIFICATIONS WHERE notification_id = ?";
        
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setInt(1, notificationId);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToNotification(resultSet);
                }
            }
            
            return null;
            
        } catch (SQLException e) {
            logger.error("Failed to find notification by ID: " + notificationId, e);
            throw DatabaseException.queryFailed(sql, e);
        }
    }
    
    /**
     * Marks a notification as read.
     * @param notificationId the notification ID
     * @return true if notification was marked as read
     * @throws DatabaseException if database error occurs
     */
    public boolean markAsRead(int notificationId) throws DatabaseException {
        String sql = """
            UPDATE NOTIFICATIONS 
            SET is_read = TRUE, read_at = CURRENT_TIMESTAMP
            WHERE notification_id = ?
            """;
        
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setInt(1, notificationId);
            
            int rowsAffected = statement.executeUpdate();
            connection.commit();
            
            if (rowsAffected > 0) {
                logger.info("Notification marked as read: " + notificationId);
                return true;
            }
            
            return false;
            
        } catch (SQLException e) {
            logger.error("Failed to mark notification as read: " + notificationId, e);
            throw DatabaseException.queryFailed(sql, e);
        }
    }
    
    /**
     * Marks a notification as unread.
     * @param notificationId the notification ID
     * @return true if notification was marked as unread
     * @throws DatabaseException if database error occurs
     */
    public boolean markAsUnread(int notificationId) throws DatabaseException {
        String sql = """
            UPDATE NOTIFICATIONS 
            SET is_read = FALSE, read_at = NULL
            WHERE notification_id = ?
            """;
        
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setInt(1, notificationId);
            
            int rowsAffected = statement.executeUpdate();
            connection.commit();
            
            if (rowsAffected > 0) {
                logger.info("Notification marked as unread: " + notificationId);
                return true;
            }
            
            return false;
            
        } catch (SQLException e) {
            logger.error("Failed to mark notification as unread: " + notificationId, e);
            throw DatabaseException.queryFailed(sql, e);
        }
    }
    
    /**
     * Marks all notifications for a user as read.
     * @param userId the user ID
     * @return number of notifications marked as read
     * @throws DatabaseException if database error occurs
     */
    public int markAllAsRead(int userId) throws DatabaseException {
        String sql = """
            UPDATE NOTIFICATIONS 
            SET is_read = TRUE, read_at = CURRENT_TIMESTAMP
            WHERE user_id = ? AND is_read = FALSE
            """;
        
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setInt(1, userId);
            
            int rowsAffected = statement.executeUpdate();
            connection.commit();
            
            logger.info("Marked " + rowsAffected + " notifications as read for user: " + userId);
            return rowsAffected;
            
        } catch (SQLException e) {
            logger.error("Failed to mark all notifications as read for user: " + userId, e);
            throw DatabaseException.queryFailed(sql, e);
        }
    }
    
    /**
     * Deletes a notification.
     * @param notificationId the notification ID
     * @return true if notification was deleted successfully
     * @throws DatabaseException if database error occurs
     */
    public boolean deleteNotification(int notificationId) throws DatabaseException {
        String sql = "DELETE FROM NOTIFICATIONS WHERE notification_id = ?";
        
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setInt(1, notificationId);
            
            int rowsAffected = statement.executeUpdate();
            connection.commit();
            
            if (rowsAffected > 0) {
                logger.info("Notification deleted: " + notificationId);
                return true;
            }
            
            return false;
            
        } catch (SQLException e) {
            logger.error("Failed to delete notification: " + notificationId, e);
            throw DatabaseException.queryFailed(sql, e);
        }
    }
    
    /**
     * Deletes all notifications for a user.
     * @param userId the user ID
     * @return number of notifications deleted
     * @throws DatabaseException if database error occurs
     */
    public int deleteAllForUser(int userId) throws DatabaseException {
        String sql = "DELETE FROM NOTIFICATIONS WHERE user_id = ?";
        
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setInt(1, userId);
            
            int rowsAffected = statement.executeUpdate();
            connection.commit();
            
            logger.info("Deleted " + rowsAffected + " notifications for user: " + userId);
            return rowsAffected;
            
        } catch (SQLException e) {
            logger.error("Failed to delete notifications for user: " + userId, e);
            throw DatabaseException.queryFailed(sql, e);
        }
    }
    
    /**
     * Gets unread notification count for a user.
     * @param userId the user ID
     * @return count of unread notifications
     * @throws DatabaseException if database error occurs
     */
    public int getUnreadCount(int userId) throws DatabaseException {
        String sql = "SELECT COUNT(*) FROM NOTIFICATIONS WHERE user_id = ? AND is_read = FALSE";
        
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setInt(1, userId);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
            }
            
            return 0;
            
        } catch (SQLException e) {
            logger.error("Failed to get unread notification count for user: " + userId, e);
            throw DatabaseException.queryFailed(sql, e);
        }
    }
    
    /**
     * Finds notifications by type.
     * @param userId the user ID
     * @param type the notification type
     * @return list of notifications of the specified type
     * @throws DatabaseException if database error occurs
     */
    public List<Notification> findByType(int userId, NotificationType type) throws DatabaseException {
        String sql = """
            SELECT * FROM NOTIFICATIONS 
            WHERE user_id = ? AND type = ?
            ORDER BY created_at DESC
            """;
        
        List<Notification> notifications = new ArrayList<>();
        
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setInt(1, userId);
            statement.setString(2, type.name());
            
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    notifications.add(mapResultSetToNotification(resultSet));
                }
            }
            
            return notifications;
            
        } catch (SQLException e) {
            logger.error("Failed to find notifications by type for user: " + userId, e);
            throw DatabaseException.queryFailed(sql, e);
        }
    }
    
    /**
     * Finds recent notifications for a user (created within last N hours).
     * @param userId the user ID
     * @param hoursBack number of hours to look back
     * @return list of recent notifications
     * @throws DatabaseException if database error occurs
     */
    public List<Notification> findRecent(int userId, int hoursBack) throws DatabaseException {
        String sql = """
            SELECT * FROM NOTIFICATIONS 
            WHERE user_id = ? AND created_at >= DATE_SUB(NOW(), INTERVAL ? HOUR)
            ORDER BY created_at DESC
            """;
        
        List<Notification> notifications = new ArrayList<>();
        
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setInt(1, userId);
            statement.setInt(2, hoursBack);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    notifications.add(mapResultSetToNotification(resultSet));
                }
            }
            
            return notifications;
            
        } catch (SQLException e) {
            logger.error("Failed to find recent notifications for user: " + userId, e);
            throw DatabaseException.queryFailed(sql, e);
        }
    }
    
    /**
     * Finds notifications by type for multiple users.
     * @param userIds list of user IDs
     * @param type the notification type
     * @return list of notifications
     * @throws DatabaseException if database error occurs
     */
    public List<Notification> findByTypeForUsers(List<Integer> userIds, NotificationType type) throws DatabaseException {
        if (userIds == null || userIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        StringBuilder placeholders = new StringBuilder();
        for (int i = 0; i < userIds.size(); i++) {
            if (i > 0) placeholders.append(",");
            placeholders.append("?");
        }
        
        String sql = String.format("""
            SELECT * FROM NOTIFICATIONS 
            WHERE user_id IN (%s) AND type = ?
            ORDER BY created_at DESC
            """, placeholders);
        
        List<Notification> notifications = new ArrayList<>();
        
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            for (int i = 0; i < userIds.size(); i++) {
                statement.setInt(i + 1, userIds.get(i));
            }
            statement.setString(userIds.size() + 1, type.name());
            
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    notifications.add(mapResultSetToNotification(resultSet));
                }
            }
            
            return notifications;
            
        } catch (SQLException e) {
            logger.error("Failed to find notifications by type for multiple users", e);
            throw DatabaseException.queryFailed(sql, e);
        }
    }
    
    // Helper method to map ResultSet to Notification
    private Notification mapResultSetToNotification(ResultSet rs) throws SQLException {
        Notification notification = new Notification();
        
        notification.setNotificationId(rs.getInt("notification_id"));
        notification.setUserId(rs.getInt("user_id"));
        notification.setTitle(rs.getString("title"));
        notification.setMessage(rs.getString("message"));
        
        String typeStr = rs.getString("type");
        if (typeStr != null) {
            notification.setType(NotificationType.valueOf(typeStr));
        }
        
        notification.setRead(rs.getBoolean("is_read"));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            notification.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        Timestamp readAt = rs.getTimestamp("read_at");
        if (readAt != null) {
            notification.setReadAt(readAt.toLocalDateTime());
        }
        
        return notification;
    }
}
