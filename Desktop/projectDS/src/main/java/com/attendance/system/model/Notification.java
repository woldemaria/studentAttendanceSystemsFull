package com.attendance.system.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Notification entity representing system notifications to users.
 */
public class Notification implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int notificationId;
    private int userId;
    private String title;
    private String message;
    private NotificationType type;
    private boolean isRead;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
    
    // Transient field for relationship
    private transient User user;
    
    public Notification() {
        this.isRead = false;
        this.createdAt = LocalDateTime.now();
    }
    
    public Notification(int userId, String title, String message, NotificationType type) {
        this();
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.type = type;
    }
    
    // Getters and Setters
    public int getNotificationId() {
        return notificationId;
    }
    
    public void setNotificationId(int notificationId) {
        this.notificationId = notificationId;
    }
    
    public int getUserId() {
        return userId;
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public NotificationType getType() {
        return type;
    }
    
    public void setType(NotificationType type) {
        this.type = type;
    }
    
    public boolean isRead() {
        return isRead;
    }
    
    public void setRead(boolean read) {
        this.isRead = read;
        if (read && readAt == null) {
            this.readAt = LocalDateTime.now();
        } else if (!read) {
            this.readAt = null;
        }
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getReadAt() {
        return readAt;
    }
    
    public void setReadAt(LocalDateTime readAt) {
        this.readAt = readAt;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
        if (user != null) {
            this.userId = user.getUserId();
        }
    }
    
    /**
     * Marks the notification as read.
     */
    public void markAsRead() {
        setRead(true);
    }
    
    /**
     * Marks the notification as unread.
     */
    public void markAsUnread() {
        setRead(false);
    }
    
    /**
     * Gets the type display name.
     * @return type display name
     */
    public String getTypeDisplay() {
        return type != null ? type.getDisplayName() : "Unknown";
    }
    
    /**
     * Checks if the notification is urgent (attendance warning).
     * @return true if type is ATTENDANCE_WARNING
     */
    public boolean isUrgent() {
        return type == NotificationType.ATTENDANCE_WARNING;
    }
    
    /**
     * Gets the age of the notification in hours.
     * @return hours since creation
     */
    public long getAgeInHours() {
        if (createdAt == null) {
            return 0;
        }
        return java.time.Duration.between(createdAt, LocalDateTime.now()).toHours();
    }
    
    /**
     * Gets a short preview of the message (first 50 characters).
     * @return message preview
     */
    public String getMessagePreview() {
        if (message == null || message.length() <= 50) {
            return message;
        }
        return message.substring(0, 47) + "...";
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Notification that = (Notification) o;
        return notificationId == that.notificationId;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(notificationId);
    }
    
    @Override
    public String toString() {
        return String.format("Notification{id=%d, userId=%d, title='%s', type=%s, read=%s, createdAt=%s}",
                notificationId, userId, title, type, isRead, createdAt);
    }
}