# Notification System Documentation

## Overview

The Notification System is a comprehensive component of the Student Attendance System that handles the creation, delivery, and management of notifications to users. It supports multiple notification types and delivery mechanisms, with preference management for customized notification experiences.

## Components

### 1. NotificationDAO (Data Access Object)

**Location:** `src/main/java/com/attendance/system/dao/NotificationDAO.java`

Handles all database operations for notifications.

#### Key Methods:

- `insertNotification(Notification)` - Creates a new notification
- `findByUser(int userId, boolean unreadOnly)` - Retrieves notifications for a user
- `findById(int notificationId)` - Finds a specific notification
- `markAsRead(int notificationId)` - Marks notification as read
- `markAsUnread(int notificationId)` - Marks notification as unread
- `markAllAsRead(int userId)` - Marks all notifications as read for a user
- `deleteNotification(int notificationId)` - Deletes a notification
- `deleteAllForUser(int userId)` - Deletes all notifications for a user
- `getUnreadCount(int userId)` - Gets count of unread notifications
- `findByType(int userId, NotificationType type)` - Finds notifications by type
- `findRecent(int userId, int hoursBack)` - Finds recent notifications
- `findByTypeForUsers(List<Integer> userIds, NotificationType type)` - Bulk find by type

### 2. NotificationService (Interface)

**Location:** `src/main/java/com/attendance/system/service/NotificationService.java`

Defines the contract for notification operations.

#### Key Methods:

**Notification Sending:**
- `sendLowAttendanceWarning(int studentId, int courseId, double attendancePercentage)` - Sends warning when attendance < 75%
- `sendWeeklyAttendanceSummary(int studentId)` - Sends weekly summary to student
- `sendAbsentNotification(int studentId, int courseId, LocalDate attendanceDate)` - Notifies student of absence
- `sendTeacherAttendanceReminder(int teacherId, int courseId, LocalDate classDate)` - Reminds teacher to mark attendance
- `sendNotification(int userId, String title, String message, NotificationType type)` - Generic notification
- `sendBulkNotification(List<Integer> userIds, String title, String message, NotificationType type)` - Bulk send

**Notification Management:**
- `getNotifications(int userId, boolean unreadOnly)` - Retrieves notifications
- `getUnreadCount(int userId)` - Gets unread count
- `markNotificationAsRead(int notificationId)` - Marks as read
- `markAllNotificationsAsRead(int userId)` - Marks all as read
- `deleteNotification(int notificationId)` - Deletes notification
- `getNotificationHistory(int userId, int limit)` - Gets notification history

**Preference Management:**
- `getNotificationPreferences(int userId)` - Gets user preferences
- `updateNotificationPreferences(int userId, Map<String, Object> preferences)` - Updates preferences
- `isEmailNotificationEnabled(int userId)` - Checks email preference
- `isInAppNotificationEnabled(int userId)` - Checks in-app preference

### 3. NotificationServiceImpl (Implementation)

**Location:** `src/main/java/com/attendance/system/service/NotificationServiceImpl.java`

Implements the NotificationService interface with business logic.

#### Key Features:

- **Low Attendance Warnings:** Automatically detects when student attendance falls below 75% threshold
- **Weekly Summaries:** Generates and sends weekly attendance summaries to all students
- **Absence Notifications:** Notifies students within 1 hour of absence marking
- **Teacher Reminders:** Reminds teachers to mark attendance within 2 hours of class end
- **Bulk Operations:** Supports sending notifications to multiple users
- **Preference Management:** Allows users to customize notification settings

#### Constants:

```java
private static final double LOW_ATTENDANCE_THRESHOLD = 75.0;
private static final int ABSENT_NOTIFICATION_DELAY_HOURS = 1;
private static final int TEACHER_REMINDER_DELAY_HOURS = 2;
```

## Notification Types

Defined in `NotificationType` enum:

- `ATTENDANCE_WARNING` - Low attendance warning
- `SYSTEM_NOTIFICATION` - General system notification
- `COURSE_UPDATE` - Course-related update
- `REMINDER` - Reminder notification

## Notification Model

**Location:** `src/main/java/com/attendance/system/model/Notification.java`

### Fields:

- `notificationId` - Unique identifier
- `userId` - Recipient user ID
- `title` - Notification title
- `message` - Notification message
- `type` - NotificationType
- `isRead` - Read status
- `createdAt` - Creation timestamp
- `readAt` - Read timestamp

### Methods:

- `markAsRead()` - Marks notification as read
- `markAsUnread()` - Marks notification as unread
- `getTypeDisplay()` - Gets type display name
- `isUrgent()` - Checks if urgent (ATTENDANCE_WARNING)
- `getAgeInHours()` - Gets age in hours
- `getMessagePreview()` - Gets first 50 characters of message

## Database Schema

### NOTIFICATIONS Table

```sql
CREATE TABLE NOTIFICATIONS (
    notification_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    title VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    type ENUM('ATTENDANCE_WARNING', 'SYSTEM_NOTIFICATION', 'COURSE_UPDATE', 'REMINDER') NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    read_at TIMESTAMP NULL,
    FOREIGN KEY (user_id) REFERENCES USERS(user_id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_type (type),
    INDEX idx_created_at (created_at),
    INDEX idx_unread (user_id, is_read)
);
```

## Notification Preferences

Default preferences (stored in memory, can be extended to database):

```java
{
    "emailNotifications": true,
    "inAppNotifications": true,
    "lowAttendanceWarnings": true,
    "weeklyAttendanceSummary": true,
    "absentNotifications": true,
    "teacherReminders": true,
    "notificationFrequency": "IMMEDIATE"
}
```

## Usage Examples

### Sending a Low Attendance Warning

```java
NotificationService notificationService = new NotificationServiceImpl();
boolean success = notificationService.sendLowAttendanceWarning(
    studentId,      // Student ID
    courseId,       // Course ID
    70.5            // Attendance percentage
);
```

### Sending a Generic Notification

```java
boolean success = notificationService.sendNotification(
    userId,
    "Important Update",
    "Please review the course materials",
    NotificationType.SYSTEM_NOTIFICATION
);
```

### Getting Unread Notifications

```java
List<Notification> unreadNotifications = notificationService.getNotifications(
    userId,
    true  // unreadOnly
);
```

### Marking All Notifications as Read

```java
int markedCount = notificationService.markAllNotificationsAsRead(userId);
```

### Updating Notification Preferences

```java
Map<String, Object> preferences = new HashMap<>();
preferences.put("emailNotifications", false);
preferences.put("inAppNotifications", true);

boolean success = notificationService.updateNotificationPreferences(
    userId,
    preferences
);
```

## Integration with AttendanceService

The notification system is integrated with the main `AttendanceService` interface through the following remote methods:

```java
// Get notifications
List<Notification> getNotifications(String sessionToken, int userId, boolean unreadOnly)

// Get unread count
int getUnreadNotificationCount(String sessionToken, int userId)

// Mark as read
boolean markNotificationAsRead(String sessionToken, int notificationId)

// Mark all as read
int markAllNotificationsAsRead(String sessionToken, int userId)

// Send notification (admin only)
boolean sendNotification(String sessionToken, Notification notification)

// Get preferences
Map<String, Object> getNotificationPreferences(String sessionToken, int userId)

// Update preferences
boolean updateNotificationPreferences(String sessionToken, int userId, Map<String, Object> preferences)
```

## Automated Notification Tasks

The system includes methods for automated notification processing:

### Check and Send Low Attendance Warnings

```java
public int checkAndSendLowAttendanceWarnings() throws DatabaseException
```

Should be called periodically (e.g., daily) to check all students and send warnings.

### Send Weekly Attendance Summaries

```java
public int sendWeeklyAttendanceSummaries() throws DatabaseException
```

Should be called weekly to send attendance summaries to all students.

## Testing

### Unit Tests

**NotificationServiceImplTest** - Tests notification service operations
- Sending notifications
- Retrieving notifications
- Managing preferences
- Bulk operations

**NotificationDAOTest** - Tests database operations
- CRUD operations
- Filtering and searching
- Read/unread status management

### Running Tests

```bash
mvn test -Dtest=NotificationServiceImplTest
mvn test -Dtest=NotificationDAOTest
```

## Error Handling

The notification system uses custom exceptions:

- `DatabaseException` - Database operation failures
- `ValidationException` - Invalid input data

All methods include proper error logging and exception handling.

## Performance Considerations

1. **Indexing:** The NOTIFICATIONS table includes indexes on:
   - `user_id` - For user-specific queries
   - `type` - For type-based filtering
   - `created_at` - For time-based queries
   - `(user_id, is_read)` - For unread notification queries

2. **Batch Operations:** The `sendBulkNotification` method efficiently sends notifications to multiple users

3. **Connection Pooling:** Uses HikariCP for efficient database connection management

## Future Enhancements

1. **Email Delivery:** Integrate with email service for email notifications
2. **Push Notifications:** Add support for mobile push notifications
3. **Notification Scheduling:** Schedule notifications for specific times
4. **Notification Templates:** Create reusable notification templates
5. **Notification Analytics:** Track notification delivery and read rates
6. **Notification Preferences Database:** Store preferences in database instead of memory

## Security Considerations

1. **Access Control:** Notifications are user-specific and access is controlled through session tokens
2. **Data Validation:** All input is validated before processing
3. **SQL Injection Prevention:** Uses prepared statements for all database queries
4. **Audit Logging:** All notification operations are logged for security monitoring

## Compliance

The notification system complies with:

- **Requirement 4.5:** Low attendance notifications
- **Requirement 10.1:** Low attendance warning notifications
- **Requirement 10.3:** Absent notification delivery within 1 hour
- **Requirement 10.4:** Notification preference management
- **Requirement 10.5:** Teacher reminder notifications
