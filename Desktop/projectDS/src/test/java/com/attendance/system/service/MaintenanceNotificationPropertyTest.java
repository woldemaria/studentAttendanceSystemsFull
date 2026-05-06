package com.attendance.system.service;

import com.attendance.system.model.Notification;
import com.attendance.system.model.NotificationType;
import com.attendance.system.model.User;
import com.attendance.system.model.UserRole;
import com.attendance.system.util.MaintenanceManager;
import net.java.quickcheck.Generator;
import net.java.quickcheck.QuickCheck;
import net.java.quickcheck.characteristic.Classification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static net.java.quickcheck.generator.CombinedGenerators.*;
import static net.java.quickcheck.generator.PrimitiveGenerators.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based tests for maintenance mode user notifications.
 * **Validates: Requirements 12.5**
 * 
 * Property 42: Maintenance Mode User Notification
 * For any scheduled system maintenance period, the system should activate maintenance mode
 * and notify all users with appropriate messages about the maintenance window and expected duration.
 */
@DisplayName("Maintenance Mode User Notification Property Tests")
public class MaintenanceNotificationPropertyTest {
    
    private MaintenanceManager maintenanceManager;
    private MockNotificationService mockNotificationService;
    private MaintenanceNotificationService maintenanceNotificationService;
    private List<User> testUsers;
    
    @BeforeEach
    public void setUp() {
        maintenanceManager = MaintenanceManager.getInstance();
        mockNotificationService = new MockNotificationService();
        testUsers = generateTestUsers(5);
        maintenanceNotificationService = new MaintenanceNotificationService(
                mockNotificationService, testUsers);
    }
    
    @Test
    @DisplayName("Property 42: Maintenance mode should notify all users with appropriate messages")
    public void testMaintenanceModeNotifiesAllUsers() {
        QuickCheck.forAll(
                // Generate maintenance reasons
                strings(
                        frequency(
                                pair(5, constant("System update")),
                                pair(3, constant("Database maintenance")),
                                pair(2, constant("Security patch"))
                        ),
                        1, 100
                ),
                // Generate estimated durations (5-120 minutes)
                integers(5, 120),
                // Generate number of users (1-20)
                integers(1, 20)
        ).withConfiguration(
                QuickCheck.defaultConfiguration()
                        .withMinimalTests(100)
                        .withMaxTests(500)
        ).check((reason, estimatedDuration, numUsers) -> {
            // Setup
            List<User> users = generateTestUsers(numUsers);
            MockNotificationService notificationService = new MockNotificationService();
            MaintenanceNotificationService notificationSvc = new MaintenanceNotificationService(
                    notificationService, users);
            
            // Execute
            maintenanceManager.enableMaintenanceMode(reason, estimatedDuration);
            notificationSvc.notifyMaintenanceStarted(reason, 
                    LocalDateTime.now().plusMinutes(estimatedDuration));
            
            // Verify
            // 1. All users should receive notifications
            assertEquals(users.size(), notificationService.getSentNotifications().size(),
                    "All users should receive maintenance notification");
            
            // 2. Each notification should contain maintenance information
            for (Notification notification : notificationService.getSentNotifications()) {
                assertNotNull(notification.getTitle());
                assertNotNull(notification.getMessage());
                assertTrue(notification.getMessage().contains(reason),
                        "Notification should contain maintenance reason");
                assertTrue(notification.getMessage().contains(String.valueOf(estimatedDuration)),
                        "Notification should contain estimated duration");
            }
            
            // 3. Notifications should be of SYSTEM_NOTIFICATION type
            for (Notification notification : notificationService.getSentNotifications()) {
                assertEquals(NotificationType.SYSTEM_NOTIFICATION, notification.getType(),
                        "Maintenance notifications should be system notifications");
            }
            
            // Cleanup
            maintenanceManager.disableMaintenanceMode();
            
            return true;
        });
    }
    
    @Test
    @DisplayName("Property 42: Maintenance notifications should include start time and duration")
    public void testMaintenanceNotificationsIncludeTimeInfo() {
        QuickCheck.forAll(
                // Generate maintenance reasons
                strings(
                        frequency(
                                pair(5, constant("System update")),
                                pair(3, constant("Database maintenance")),
                                pair(2, constant("Security patch"))
                        ),
                        1, 100
                ),
                // Generate estimated durations
                integers(5, 120)
        ).withConfiguration(
                QuickCheck.defaultConfiguration()
                        .withMinimalTests(50)
                        .withMaxTests(200)
        ).check((reason, estimatedDuration) -> {
            // Setup
            LocalDateTime maintenanceStart = LocalDateTime.now().plusMinutes(10);
            LocalDateTime maintenanceEnd = maintenanceStart.plusMinutes(estimatedDuration);
            
            // Execute
            maintenanceManager.scheduleMaintenanceMode(maintenanceStart, estimatedDuration, reason);
            maintenanceNotificationService.notifyScheduledMaintenance(maintenanceStart, estimatedDuration, reason);
            
            // Verify
            List<Notification> notifications = mockNotificationService.getSentNotifications();
            assertTrue(notifications.size() > 0, "Notifications should be sent");
            
            for (Notification notification : notifications) {
                String message = notification.getMessage();
                
                // Message should contain time information
                assertTrue(message.contains(String.valueOf(estimatedDuration)),
                        "Notification should contain duration");
                assertTrue(message.contains(reason),
                        "Notification should contain reason");
                
                // Message should indicate it's a scheduled maintenance
                assertTrue(message.toLowerCase().contains("maintenance") ||
                          message.toLowerCase().contains("scheduled"),
                        "Notification should indicate maintenance");
            }
            
            // Cleanup
            maintenanceManager.cancelScheduledMaintenance();
            
            return true;
        });
    }
    
    @Test
    @DisplayName("Property 42: Maintenance completion should notify all users")
    public void testMaintenanceCompletionNotifiesAllUsers() {
        QuickCheck.forAll(
                // Generate number of users
                integers(1, 20)
        ).withConfiguration(
                QuickCheck.defaultConfiguration()
                        .withMinimalTests(50)
                        .withMaxTests(200)
        ).check((numUsers) -> {
            // Setup
            List<User> users = generateTestUsers(numUsers);
            MockNotificationService notificationService = new MockNotificationService();
            MaintenanceNotificationService notificationSvc = new MaintenanceNotificationService(
                    notificationService, users);
            
            // Execute
            maintenanceManager.enableMaintenanceMode("Test maintenance", 30);
            notificationSvc.notifyMaintenanceCompleted();
            
            // Verify
            List<Notification> notifications = notificationService.getSentNotifications();
            assertEquals(users.size(), notifications.size(),
                    "All users should be notified of maintenance completion");
            
            for (Notification notification : notifications) {
                assertTrue(notification.getMessage().toLowerCase().contains("completed") ||
                          notification.getMessage().toLowerCase().contains("online"),
                        "Completion notification should indicate system is back online");
            }
            
            // Cleanup
            maintenanceManager.disableMaintenanceMode();
            
            return true;
        });
    }
    
    @Test
    @DisplayName("Property 42: System update notifications should include version information")
    public void testSystemUpdateNotificationsIncludeVersion() {
        QuickCheck.forAll(
                // Generate version strings
                strings(
                        frequency(
                                pair(3, constant("2.0.0")),
                                pair(3, constant("1.5.0")),
                                pair(2, constant("3.0.0-beta"))
                        ),
                        1, 20
                ),
                // Generate descriptions
                strings(
                        frequency(
                                pair(3, constant("Major feature release")),
                                pair(3, constant("Security updates")),
                                pair(2, constant("Bug fixes and improvements"))
                        ),
                        1, 100
                )
        ).withConfiguration(
                QuickCheck.defaultConfiguration()
                        .withMinimalTests(50)
                        .withMaxTests(200)
        ).check((version, description) -> {
            // Setup
            MockNotificationService notificationService = new MockNotificationService();
            MaintenanceNotificationService notificationSvc = new MaintenanceNotificationService(
                    notificationService, testUsers);
            
            // Execute
            notificationSvc.notifySystemUpdate(version, description);
            
            // Verify
            List<Notification> notifications = notificationService.getSentNotifications();
            assertTrue(notifications.size() > 0, "Update notifications should be sent");
            
            for (Notification notification : notifications) {
                String message = notification.getMessage();
                assertTrue(message.contains(version),
                        "Notification should contain version number");
                assertTrue(message.contains(description),
                        "Notification should contain update description");
            }
            
            return true;
        });
    }
    
    @Test
    @DisplayName("Property 42: Maintenance reminders should be sent before maintenance")
    public void testMaintenanceRemindersAreSentBeforeMaintenance() {
        QuickCheck.forAll(
                // Generate minutes before maintenance
                integers(5, 60),
                // Generate maintenance reasons
                strings(
                        frequency(
                                pair(5, constant("System update")),
                                pair(3, constant("Database maintenance"))
                        ),
                        1, 100
                )
        ).withConfiguration(
                QuickCheck.defaultConfiguration()
                        .withMinimalTests(50)
                        .withMaxTests(200)
        ).check((minutesBefore, reason) -> {
            // Setup
            LocalDateTime maintenanceStart = LocalDateTime.now().plusMinutes(minutesBefore + 5);
            
            // Execute
            maintenanceNotificationService.scheduleMaintenanceReminder(minutesBefore, maintenanceStart, reason);
            maintenanceNotificationService.notifyMaintenanceReminder(minutesBefore, reason);
            
            // Verify
            List<Notification> notifications = mockNotificationService.getSentNotifications();
            assertTrue(notifications.size() > 0, "Reminder notifications should be sent");
            
            for (Notification notification : notifications) {
                String message = notification.getMessage();
                assertTrue(message.toLowerCase().contains("reminder") ||
                          message.toLowerCase().contains("starting soon"),
                        "Notification should indicate it's a reminder");
                assertTrue(message.contains(String.valueOf(minutesBefore)),
                        "Notification should contain minutes until maintenance");
            }
            
            return true;
        });
    }
    
    @Test
    @DisplayName("Property 42: Maintenance notifications should be delivered to all user types")
    public void testMaintenanceNotificationsDeliveredToAllUserTypes() {
        QuickCheck.forAll(
                // Generate maintenance reason
                strings(
                        frequency(
                                pair(5, constant("System update")),
                                pair(3, constant("Database maintenance"))
                        ),
                        1, 100
                )
        ).withConfiguration(
                QuickCheck.defaultConfiguration()
                        .withMinimalTests(50)
                        .withMaxTests(200)
        ).check((reason) -> {
            // Setup - Create users of different roles
            List<User> mixedUsers = new ArrayList<>();
            User admin = createTestUser(1, UserRole.ADMIN);
            User teacher = createTestUser(2, UserRole.TEACHER);
            User student = createTestUser(3, UserRole.STUDENT);
            mixedUsers.add(admin);
            mixedUsers.add(teacher);
            mixedUsers.add(student);
            
            MockNotificationService notificationService = new MockNotificationService();
            MaintenanceNotificationService notificationSvc = new MaintenanceNotificationService(
                    notificationService, mixedUsers);
            
            // Execute
            maintenanceManager.enableMaintenanceMode(reason, 30);
            notificationSvc.notifyMaintenanceStarted(reason, LocalDateTime.now().plusMinutes(30));
            
            // Verify
            List<Notification> notifications = notificationService.getSentNotifications();
            assertEquals(3, notifications.size(),
                    "All user types should receive maintenance notification");
            
            Set<Integer> notifiedUserIds = new HashSet<>();
            for (Notification notification : notifications) {
                notifiedUserIds.add(notification.getUserId());
            }
            
            assertTrue(notifiedUserIds.contains(admin.getUserId()));
            assertTrue(notifiedUserIds.contains(teacher.getUserId()));
            assertTrue(notifiedUserIds.contains(student.getUserId()));
            
            // Cleanup
            maintenanceManager.disableMaintenanceMode();
            
            return true;
        });
    }
    
    // Helper methods
    
    private List<User> generateTestUsers(int count) {
        List<User> users = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            users.add(createTestUser(i + 1, UserRole.STUDENT));
        }
        return users;
    }
    
    private User createTestUser(int userId, UserRole role) {
        User user = new User();
        user.setUserId(userId);
        user.setUsername("user" + userId);
        user.setEmail("user" + userId + "@test.com");
        user.setFirstName("Test");
        user.setLastName("User" + userId);
        user.setRole(role);
        user.setActive(true);
        return user;
    }
    
    /**
     * Mock notification service for testing.
     */
    private static class MockNotificationService implements NotificationService {
        private final List<Notification> sentNotifications = new CopyOnWriteArrayList<>();
        
        @Override
        public void sendNotification(Notification notification) {
            sentNotifications.add(notification);
        }
        
        @Override
        public List<Notification> getNotifications(int userId, boolean unreadOnly) {
            return new ArrayList<>();
        }
        
        @Override
        public int getUnreadCount(int userId) {
            return 0;
        }
        
        @Override
        public void markAsRead(int notificationId) {
        }
        
        @Override
        public void markAllAsRead(int userId) {
        }
        
        @Override
        public Map<String, Object> getPreferences(int userId) {
            return new HashMap<>();
        }
        
        @Override
        public void updatePreferences(int userId, Map<String, Object> preferences) {
        }
        
        public List<Notification> getSentNotifications() {
            return new ArrayList<>(sentNotifications);
        }
    }
}
