package com.attendance.system.service;

import com.attendance.system.model.Notification;
import com.attendance.system.model.NotificationType;
import com.attendance.system.model.User;
import com.attendance.system.util.MaintenanceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;

/**
 * Service for sending maintenance-related notifications to users.
 * Handles notifications about scheduled maintenance, system updates, and maintenance completion.
 */
public class MaintenanceNotificationService implements MaintenanceManager.MaintenanceListener {
    private static final Logger logger = LoggerFactory.getLogger(MaintenanceNotificationService.class);
    
    private final NotificationService notificationService;
    private final List<User> allUsers;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    public MaintenanceNotificationService(NotificationService notificationService, List<User> allUsers) {
        this.notificationService = notificationService;
        this.allUsers = allUsers;
        
        // Register as listener to MaintenanceManager
        MaintenanceManager.getInstance().addMaintenanceListener(this);
    }
    
    /**
     * Sends maintenance notification to all users.
     * @param title notification title
     * @param message notification message
     */
    public void notifyAllUsers(String title, String message) {
        for (User user : allUsers) {
            try {
                notificationService.sendNotification(
                        user.getUserId(),
                        title,
                        message,
                        NotificationType.SYSTEM_NOTIFICATION
                );
                logger.debug("Maintenance notification sent to user: {}", user.getUserId());
            } catch (Exception e) {
                logger.error("Failed to send maintenance notification to user: {}", user.getUserId(), e);
            }
        }
    }
    
    /**
     * Sends scheduled maintenance notification.
     * @param startTime the maintenance start time
     * @param estimatedDurationMinutes estimated duration
     * @param reason the reason for maintenance
     */
    public void notifyScheduledMaintenance(LocalDateTime startTime, int estimatedDurationMinutes, String reason) {
        String title = "Scheduled System Maintenance";
        String message = String.format(
                "The system will undergo scheduled maintenance on %s for approximately %d minutes.\n" +
                "Reason: %s\n" +
                "During this time, the system will be unavailable. Please plan accordingly.",
                startTime.format(formatter),
                estimatedDurationMinutes,
                reason
        );
        
        notifyAllUsers(title, message);
        logger.info("Scheduled maintenance notification sent to all users");
    }
    
    /**
     * Sends maintenance started notification.
     * @param reason the reason for maintenance
     * @param estimatedEndTime estimated end time
     */
    public void notifyMaintenanceStarted(String reason, LocalDateTime estimatedEndTime) {
        String title = "System Maintenance In Progress";
        String message = String.format(
                "The system is currently undergoing maintenance.\n" +
                "Reason: %s\n" +
                "Estimated completion time: %s\n" +
                "We apologize for any inconvenience.",
                reason,
                estimatedEndTime != null ? estimatedEndTime.format(formatter) : "Unknown"
        );
        
        notifyAllUsers(title, message);
        logger.info("Maintenance started notification sent to all users");
    }
    
    /**
     * Sends maintenance completed notification.
     */
    public void notifyMaintenanceCompleted() {
        String title = "System Maintenance Completed";
        String message = "The scheduled system maintenance has been completed successfully.\n" +
                "The system is now back online and fully operational.";
        
        notifyAllUsers(title, message);
        logger.info("Maintenance completed notification sent to all users");
    }
    
    /**
     * Sends system update notification.
     * @param newVersion the new version
     * @param description update description
     */
    public void notifySystemUpdate(String newVersion, String description) {
        String title = "System Update Available";
        String message = String.format(
                "A system update is available: Version %s\n" +
                "Description: %s\n" +
                "The system will be updated during the next scheduled maintenance window.",
                newVersion,
                description
        );
        
        notifyAllUsers(title, message);
        logger.info("System update notification sent to all users");
    }
    
    /**
     * Sends update completed notification.
     * @param newVersion the new version
     */
    public void notifyUpdateCompleted(String newVersion) {
        String title = "System Update Completed";
        String message = String.format(
                "The system has been successfully updated to version %s.\n" +
                "Please refresh your application to use the latest features.",
                newVersion
        );
        
        notifyAllUsers(title, message);
        logger.info("Update completed notification sent to all users");
    }
    
    /**
     * Sends maintenance reminder notification (sent before scheduled maintenance).
     * @param minutesUntilMaintenance minutes until maintenance starts
     * @param reason the reason for maintenance
     */
    public void notifyMaintenanceReminder(int minutesUntilMaintenance, String reason) {
        String title = "Reminder: System Maintenance Starting Soon";
        String message = String.format(
                "System maintenance will begin in %d minutes.\n" +
                "Reason: %s\n" +
                "Please save your work and log out before the maintenance window.",
                minutesUntilMaintenance,
                reason
        );
        
        notifyAllUsers(title, message);
        logger.info("Maintenance reminder notification sent to all users");
    }
    
    /**
     * Schedules a reminder notification before maintenance.
     * @param minutesBeforeMaintenance minutes before maintenance to send reminder
     * @param maintenanceStartTime the maintenance start time
     * @param reason the reason for maintenance
     */
    public void scheduleMaintenanceReminder(int minutesBeforeMaintenance, 
                                           LocalDateTime maintenanceStartTime, 
                                           String reason) {
        long delayMillis = java.time.temporal.ChronoUnit.MILLIS.between(
                LocalDateTime.now(),
                maintenanceStartTime.minusMinutes(minutesBeforeMaintenance)
        );
        
        if (delayMillis > 0) {
            scheduler.schedule(
                    () -> notifyMaintenanceReminder(minutesBeforeMaintenance, reason),
                    delayMillis,
                    TimeUnit.MILLISECONDS
            );
            logger.info("Maintenance reminder scheduled for {} minutes before maintenance", minutesBeforeMaintenance);
        }
    }
    
    /**
     * Shuts down the notification service.
     */
    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        MaintenanceManager.getInstance().removeMaintenanceListener(this);
        logger.info("MaintenanceNotificationService shutdown complete");
    }
    
    // MaintenanceListener implementation
    
    @Override
    public void onMaintenanceStarted(String reason, LocalDateTime estimatedEndTime) {
        notifyMaintenanceStarted(reason, estimatedEndTime);
    }
    
    @Override
    public void onMaintenanceEnded() {
        notifyMaintenanceCompleted();
    }
    
    @Override
    public void onUpdateInitiated(String newVersion, String description) {
        notifySystemUpdate(newVersion, description);
    }
    
    @Override
    public void onUpdateCompleted(String newVersion) {
        notifyUpdateCompleted(newVersion);
    }
}
