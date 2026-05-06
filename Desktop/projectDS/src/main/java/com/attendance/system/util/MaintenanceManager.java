package com.attendance.system.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;

/**
 * Manages system maintenance mode, scheduled maintenance, and system updates.
 * Handles maintenance state, user notifications, and graceful shutdown procedures.
 */
public class MaintenanceManager {
    private static final Logger logger = LoggerFactory.getLogger(MaintenanceManager.class);
    private static MaintenanceManager instance;
    
    private volatile boolean maintenanceMode = false;
    private volatile LocalDateTime maintenanceStartTime;
    private volatile LocalDateTime maintenanceEndTime;
    private volatile String maintenanceReason = "";
    private volatile String systemVersion = "1.0.0";
    private volatile String nextVersion = "";
    
    private final Set<MaintenanceListener> listeners = ConcurrentHashMap.newKeySet();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private final Map<String, BackupInfo> backups = new ConcurrentHashMap<>();
    private final List<MaintenanceEvent> maintenanceHistory = Collections.synchronizedList(new ArrayList<>());
    
    private ScheduledFuture<?> maintenanceSchedule;
    private ScheduledFuture<?> maintenanceEndSchedule;
    
    private MaintenanceManager() {
        loadMaintenanceHistory();
    }
    
    public static synchronized MaintenanceManager getInstance() {
        if (instance == null) {
            instance = new MaintenanceManager();
        }
        return instance;
    }
    
    /**
     * Enables maintenance mode immediately.
     * @param reason the reason for maintenance
     * @param estimatedDurationMinutes estimated duration in minutes
     */
    public void enableMaintenanceMode(String reason, int estimatedDurationMinutes) {
        synchronized (this) {
            if (maintenanceMode) {
                logger.warn("Maintenance mode already enabled");
                return;
            }
            
            maintenanceMode = true;
            maintenanceStartTime = LocalDateTime.now();
            maintenanceEndTime = maintenanceStartTime.plusMinutes(estimatedDurationMinutes);
            maintenanceReason = reason;
            
            logger.info("Maintenance mode enabled: {} (estimated duration: {} minutes)", 
                    reason, estimatedDurationMinutes);
            
            // Record maintenance event
            recordMaintenanceEvent("MAINTENANCE_STARTED", reason);
            
            // Notify all listeners
            notifyMaintenanceStarted();
            
            // Schedule automatic maintenance end
            if (maintenanceEndSchedule != null) {
                maintenanceEndSchedule.cancel(false);
            }
            maintenanceEndSchedule = scheduler.schedule(
                    this::disableMaintenanceMode,
                    estimatedDurationMinutes,
                    TimeUnit.MINUTES
            );
        }
    }
    
    /**
     * Disables maintenance mode.
     */
    public void disableMaintenanceMode() {
        synchronized (this) {
            if (!maintenanceMode) {
                logger.warn("Maintenance mode not enabled");
                return;
            }
            
            maintenanceMode = false;
            LocalDateTime endTime = LocalDateTime.now();
            
            logger.info("Maintenance mode disabled. Duration: {} minutes",
                    java.time.temporal.ChronoUnit.MINUTES.between(maintenanceStartTime, endTime));
            
            // Record maintenance event
            recordMaintenanceEvent("MAINTENANCE_ENDED", "Maintenance completed");
            
            // Notify all listeners
            notifyMaintenanceEnded();
            
            if (maintenanceEndSchedule != null) {
                maintenanceEndSchedule.cancel(false);
                maintenanceEndSchedule = null;
            }
        }
    }
    
    /**
     * Schedules maintenance mode for a specific time.
     * @param startTime the start time for maintenance
     * @param estimatedDurationMinutes estimated duration in minutes
     * @param reason the reason for maintenance
     */
    public void scheduleMaintenanceMode(LocalDateTime startTime, int estimatedDurationMinutes, String reason) {
        long delayMillis = java.time.temporal.ChronoUnit.MILLIS.between(LocalDateTime.now(), startTime);
        
        if (delayMillis < 0) {
            logger.error("Cannot schedule maintenance in the past");
            throw new IllegalArgumentException("Maintenance start time must be in the future");
        }
        
        if (maintenanceSchedule != null) {
            maintenanceSchedule.cancel(false);
        }
        
        maintenanceSchedule = scheduler.schedule(
                () -> enableMaintenanceMode(reason, estimatedDurationMinutes),
                delayMillis,
                TimeUnit.MILLISECONDS
        );
        
        logger.info("Maintenance scheduled for {} with reason: {}", startTime, reason);
        recordMaintenanceEvent("MAINTENANCE_SCHEDULED", reason + " at " + startTime);
    }
    
    /**
     * Cancels scheduled maintenance.
     */
    public void cancelScheduledMaintenance() {
        if (maintenanceSchedule != null) {
            maintenanceSchedule.cancel(false);
            maintenanceSchedule = null;
            logger.info("Scheduled maintenance cancelled");
            recordMaintenanceEvent("MAINTENANCE_CANCELLED", "Scheduled maintenance was cancelled");
        }
    }
    
    /**
     * Checks if the system is in maintenance mode.
     * @return true if maintenance mode is enabled
     */
    public boolean isMaintenanceMode() {
        return maintenanceMode;
    }
    
    /**
     * Gets maintenance mode information.
     * @return map containing maintenance details
     */
    public Map<String, Object> getMaintenanceModeInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("isMaintenanceMode", maintenanceMode);
        info.put("startTime", maintenanceStartTime);
        info.put("endTime", maintenanceEndTime);
        info.put("reason", maintenanceReason);
        info.put("estimatedDuration", maintenanceEndTime != null ? 
                java.time.temporal.ChronoUnit.MINUTES.between(maintenanceStartTime, maintenanceEndTime) : 0);
        return info;
    }
    
    /**
     * Creates a backup before system update.
     * @param backupName the name of the backup
     * @return backup ID
     */
    public String createBackup(String backupName) {
        String backupId = UUID.randomUUID().toString();
        BackupInfo backup = new BackupInfo(
                backupId,
                backupName,
                LocalDateTime.now(),
                systemVersion,
                "PENDING"
        );
        
        backups.put(backupId, backup);
        logger.info("Backup created: {} (ID: {})", backupName, backupId);
        recordMaintenanceEvent("BACKUP_CREATED", backupName);
        
        // Simulate backup completion
        scheduler.schedule(() -> {
            backup.setStatus("COMPLETED");
            backup.setCompletedTime(LocalDateTime.now());
            logger.info("Backup completed: {}", backupId);
        }, 2, TimeUnit.SECONDS);
        
        return backupId;
    }
    
    /**
     * Restores from a backup.
     * @param backupId the backup ID to restore from
     * @return true if restore was successful
     */
    public boolean restoreFromBackup(String backupId) {
        BackupInfo backup = backups.get(backupId);
        if (backup == null) {
            logger.error("Backup not found: {}", backupId);
            return false;
        }
        
        if (!"COMPLETED".equals(backup.getStatus())) {
            logger.error("Backup is not ready for restore: {}", backupId);
            return false;
        }
        
        logger.info("Restoring from backup: {} (version: {})", backupId, backup.getVersion());
        recordMaintenanceEvent("BACKUP_RESTORED", "Restored from backup: " + backup.getName());
        
        // Simulate restore operation
        scheduler.schedule(() -> {
            logger.info("Restore completed from backup: {}", backupId);
        }, 2, TimeUnit.SECONDS);
        
        return true;
    }
    
    /**
     * Gets all available backups.
     * @return list of backup information
     */
    public List<BackupInfo> getAvailableBackups() {
        return new ArrayList<>(backups.values());
    }
    
    /**
     * Gets backup information.
     * @param backupId the backup ID
     * @return backup information or null if not found
     */
    public BackupInfo getBackupInfo(String backupId) {
        return backups.get(backupId);
    }
    
    /**
     * Deletes a backup.
     * @param backupId the backup ID to delete
     * @return true if deletion was successful
     */
    public boolean deleteBackup(String backupId) {
        BackupInfo removed = backups.remove(backupId);
        if (removed != null) {
            logger.info("Backup deleted: {}", backupId);
            recordMaintenanceEvent("BACKUP_DELETED", "Deleted backup: " + removed.getName());
            return true;
        }
        return false;
    }
    
    /**
     * Initiates a system update.
     * @param newVersion the new version to update to
     * @param updateDescription description of the update
     * @return true if update was initiated
     */
    public boolean initiateSystemUpdate(String newVersion, String updateDescription) {
        if (maintenanceMode) {
            logger.error("Cannot initiate update while in maintenance mode");
            return false;
        }
        
        // Create backup before update
        String backupId = createBackup("Pre-update backup for v" + newVersion);
        
        nextVersion = newVersion;
        logger.info("System update initiated: {} -> {}", systemVersion, newVersion);
        recordMaintenanceEvent("UPDATE_INITIATED", updateDescription);
        
        // Notify listeners about update
        notifyUpdateInitiated(newVersion, updateDescription);
        
        return true;
    }
    
    /**
     * Completes a system update.
     * @param newVersion the new version
     */
    public void completeSystemUpdate(String newVersion) {
        systemVersion = newVersion;
        nextVersion = "";
        logger.info("System update completed. New version: {}", systemVersion);
        recordMaintenanceEvent("UPDATE_COMPLETED", "System updated to version: " + newVersion);
        notifyUpdateCompleted(newVersion);
    }
    
    /**
     * Gets current system version.
     * @return the current system version
     */
    public String getSystemVersion() {
        return systemVersion;
    }
    
    /**
     * Gets next scheduled version.
     * @return the next version or empty string if none scheduled
     */
    public String getNextVersion() {
        return nextVersion;
    }
    
    /**
     * Registers a maintenance listener.
     * @param listener the listener to register
     */
    public void addMaintenanceListener(MaintenanceListener listener) {
        listeners.add(listener);
    }
    
    /**
     * Unregisters a maintenance listener.
     * @param listener the listener to unregister
     */
    public void removeMaintenanceListener(MaintenanceListener listener) {
        listeners.remove(listener);
    }
    
    /**
     * Gets maintenance history.
     * @return list of maintenance events
     */
    public List<MaintenanceEvent> getMaintenanceHistory() {
        return new ArrayList<>(maintenanceHistory);
    }
    
    /**
     * Clears maintenance history.
     */
    public void clearMaintenanceHistory() {
        maintenanceHistory.clear();
        logger.info("Maintenance history cleared");
    }
    
    /**
     * Gracefully shuts down the maintenance manager.
     */
    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(10, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        logger.info("MaintenanceManager shutdown complete");
    }
    
    // Private helper methods
    
    private void notifyMaintenanceStarted() {
        for (MaintenanceListener listener : listeners) {
            try {
                listener.onMaintenanceStarted(maintenanceReason, maintenanceEndTime);
            } catch (Exception e) {
                logger.error("Error notifying listener of maintenance start", e);
            }
        }
    }
    
    private void notifyMaintenanceEnded() {
        for (MaintenanceListener listener : listeners) {
            try {
                listener.onMaintenanceEnded();
            } catch (Exception e) {
                logger.error("Error notifying listener of maintenance end", e);
            }
        }
    }
    
    private void notifyUpdateInitiated(String newVersion, String description) {
        for (MaintenanceListener listener : listeners) {
            try {
                listener.onUpdateInitiated(newVersion, description);
            } catch (Exception e) {
                logger.error("Error notifying listener of update initiation", e);
            }
        }
    }
    
    private void notifyUpdateCompleted(String newVersion) {
        for (MaintenanceListener listener : listeners) {
            try {
                listener.onUpdateCompleted(newVersion);
            } catch (Exception e) {
                logger.error("Error notifying listener of update completion", e);
            }
        }
    }
    
    private void recordMaintenanceEvent(String eventType, String details) {
        MaintenanceEvent event = new MaintenanceEvent(
                UUID.randomUUID().toString(),
                eventType,
                details,
                LocalDateTime.now()
        );
        maintenanceHistory.add(event);
        logger.debug("Maintenance event recorded: {} - {}", eventType, details);
    }
    
    private void loadMaintenanceHistory() {
        // Load maintenance history from persistent storage if needed
        // For now, we'll keep it in memory
    }
    
    /**
     * Backup information class.
     */
    public static class BackupInfo implements Serializable {
        private static final long serialVersionUID = 1L;
        
        private final String backupId;
        private final String name;
        private final LocalDateTime createdTime;
        private LocalDateTime completedTime;
        private final String version;
        private String status; // PENDING, COMPLETED, FAILED
        private long sizeBytes;
        
        public BackupInfo(String backupId, String name, LocalDateTime createdTime, 
                         String version, String status) {
            this.backupId = backupId;
            this.name = name;
            this.createdTime = createdTime;
            this.version = version;
            this.status = status;
            this.sizeBytes = 0;
        }
        
        public String getBackupId() { return backupId; }
        public String getName() { return name; }
        public LocalDateTime getCreatedTime() { return createdTime; }
        public LocalDateTime getCompletedTime() { return completedTime; }
        public void setCompletedTime(LocalDateTime completedTime) { this.completedTime = completedTime; }
        public String getVersion() { return version; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public long getSizeBytes() { return sizeBytes; }
        public void setSizeBytes(long sizeBytes) { this.sizeBytes = sizeBytes; }
    }
    
    /**
     * Maintenance event class.
     */
    public static class MaintenanceEvent implements Serializable {
        private static final long serialVersionUID = 1L;
        
        private final String eventId;
        private final String eventType;
        private final String details;
        private final LocalDateTime timestamp;
        
        public MaintenanceEvent(String eventId, String eventType, String details, LocalDateTime timestamp) {
            this.eventId = eventId;
            this.eventType = eventType;
            this.details = details;
            this.timestamp = timestamp;
        }
        
        public String getEventId() { return eventId; }
        public String getEventType() { return eventType; }
        public String getDetails() { return details; }
        public LocalDateTime getTimestamp() { return timestamp; }
    }
    
    /**
     * Listener interface for maintenance events.
     */
    public interface MaintenanceListener {
        void onMaintenanceStarted(String reason, LocalDateTime estimatedEndTime);
        void onMaintenanceEnded();
        void onUpdateInitiated(String newVersion, String description);
        void onUpdateCompleted(String newVersion);
    }
}
