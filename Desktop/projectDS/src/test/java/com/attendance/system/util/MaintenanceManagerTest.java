package com.attendance.system.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for MaintenanceManager.
 * Tests maintenance mode control, scheduling, backups, and system updates.
 */
@DisplayName("MaintenanceManager Tests")
public class MaintenanceManagerTest {
    
    private MaintenanceManager maintenanceManager;
    private TestMaintenanceListener testListener;
    
    @BeforeEach
    public void setUp() {
        // Get fresh instance for each test
        maintenanceManager = MaintenanceManager.getInstance();
        testListener = new TestMaintenanceListener();
        maintenanceManager.addMaintenanceListener(testListener);
    }
    
    @Test
    @DisplayName("Should enable maintenance mode immediately")
    public void testEnableMaintenanceMode() {
        String reason = "System update";
        int duration = 30;
        
        maintenanceManager.enableMaintenanceMode(reason, duration);
        
        assertTrue(maintenanceManager.isMaintenanceMode());
        Map<String, Object> info = maintenanceManager.getMaintenanceModeInfo();
        assertEquals(reason, info.get("reason"));
        assertEquals(duration, info.get("estimatedDuration"));
    }
    
    @Test
    @DisplayName("Should disable maintenance mode")
    public void testDisableMaintenanceMode() {
        maintenanceManager.enableMaintenanceMode("Test maintenance", 30);
        assertTrue(maintenanceManager.isMaintenanceMode());
        
        maintenanceManager.disableMaintenanceMode();
        
        assertFalse(maintenanceManager.isMaintenanceMode());
    }
    
    @Test
    @DisplayName("Should not enable maintenance mode twice")
    public void testEnableMaintenanceModeTwice() {
        maintenanceManager.enableMaintenanceMode("First maintenance", 30);
        maintenanceManager.enableMaintenanceMode("Second maintenance", 30);
        
        // Should still be in maintenance mode with first reason
        assertTrue(maintenanceManager.isMaintenanceMode());
        Map<String, Object> info = maintenanceManager.getMaintenanceModeInfo();
        assertEquals("First maintenance", info.get("reason"));
    }
    
    @Test
    @DisplayName("Should schedule maintenance mode for future time")
    public void testScheduleMaintenanceMode() {
        LocalDateTime futureTime = LocalDateTime.now().plusMinutes(5);
        String reason = "Scheduled maintenance";
        
        maintenanceManager.scheduleMaintenanceMode(futureTime, 30, reason);
        
        // Should not be in maintenance mode yet
        assertFalse(maintenanceManager.isMaintenanceMode());
    }
    
    @Test
    @DisplayName("Should reject scheduling maintenance in the past")
    public void testScheduleMaintenanceModeInPast() {
        LocalDateTime pastTime = LocalDateTime.now().minusMinutes(5);
        
        assertThrows(IllegalArgumentException.class, () -> {
            maintenanceManager.scheduleMaintenanceMode(pastTime, 30, "Past maintenance");
        });
    }
    
    @Test
    @DisplayName("Should cancel scheduled maintenance")
    public void testCancelScheduledMaintenance() {
        LocalDateTime futureTime = LocalDateTime.now().plusMinutes(5);
        maintenanceManager.scheduleMaintenanceMode(futureTime, 30, "Scheduled maintenance");
        
        maintenanceManager.cancelScheduledMaintenance();
        
        // Maintenance should not be enabled
        assertFalse(maintenanceManager.isMaintenanceMode());
    }
    
    @Test
    @DisplayName("Should create backup with unique ID")
    public void testCreateBackup() {
        String backupName = "Test backup";
        
        String backupId = maintenanceManager.createBackup(backupName);
        
        assertNotNull(backupId);
        assertFalse(backupId.isEmpty());
        
        MaintenanceManager.BackupInfo backup = maintenanceManager.getBackupInfo(backupId);
        assertNotNull(backup);
        assertEquals(backupName, backup.getName());
    }
    
    @Test
    @DisplayName("Should get available backups")
    public void testGetAvailableBackups() {
        maintenanceManager.createBackup("Backup 1");
        maintenanceManager.createBackup("Backup 2");
        
        List<MaintenanceManager.BackupInfo> backups = maintenanceManager.getAvailableBackups();
        
        assertTrue(backups.size() >= 2);
    }
    
    @Test
    @DisplayName("Should delete backup")
    public void testDeleteBackup() {
        String backupId = maintenanceManager.createBackup("Test backup");
        
        boolean deleted = maintenanceManager.deleteBackup(backupId);
        
        assertTrue(deleted);
        assertNull(maintenanceManager.getBackupInfo(backupId));
    }
    
    @Test
    @DisplayName("Should not delete non-existent backup")
    public void testDeleteNonExistentBackup() {
        boolean deleted = maintenanceManager.deleteBackup("non-existent-id");
        
        assertFalse(deleted);
    }
    
    @Test
    @DisplayName("Should initiate system update")
    public void testInitiateSystemUpdate() {
        String newVersion = "2.0.0";
        String description = "Major update";
        
        boolean success = maintenanceManager.initiateSystemUpdate(newVersion, description);
        
        assertTrue(success);
        assertEquals(newVersion, maintenanceManager.getNextVersion());
    }
    
    @Test
    @DisplayName("Should not initiate update during maintenance")
    public void testInitiateUpdateDuringMaintenance() {
        maintenanceManager.enableMaintenanceMode("Maintenance", 30);
        
        boolean success = maintenanceManager.initiateSystemUpdate("2.0.0", "Update");
        
        assertFalse(success);
    }
    
    @Test
    @DisplayName("Should complete system update")
    public void testCompleteSystemUpdate() {
        String oldVersion = maintenanceManager.getSystemVersion();
        String newVersion = "2.0.0";
        
        maintenanceManager.completeSystemUpdate(newVersion);
        
        assertEquals(newVersion, maintenanceManager.getSystemVersion());
        assertEquals("", maintenanceManager.getNextVersion());
    }
    
    @Test
    @DisplayName("Should record maintenance events")
    public void testRecordMaintenanceEvents() {
        maintenanceManager.enableMaintenanceMode("Test", 30);
        maintenanceManager.disableMaintenanceMode();
        
        List<MaintenanceManager.MaintenanceEvent> history = maintenanceManager.getMaintenanceHistory();
        
        assertTrue(history.size() >= 2);
        assertTrue(history.stream().anyMatch(e -> "MAINTENANCE_STARTED".equals(e.getEventType())));
        assertTrue(history.stream().anyMatch(e -> "MAINTENANCE_ENDED".equals(e.getEventType())));
    }
    
    @Test
    @DisplayName("Should notify listeners on maintenance start")
    public void testNotifyListenersOnMaintenanceStart() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        TestMaintenanceListener listener = new TestMaintenanceListener() {
            @Override
            public void onMaintenanceStarted(String reason, LocalDateTime estimatedEndTime) {
                super.onMaintenanceStarted(reason, estimatedEndTime);
                latch.countDown();
            }
        };
        
        maintenanceManager.addMaintenanceListener(listener);
        maintenanceManager.enableMaintenanceMode("Test", 30);
        
        assertTrue(latch.await(2, TimeUnit.SECONDS));
        assertTrue(listener.maintenanceStartedCalled);
    }
    
    @Test
    @DisplayName("Should notify listeners on maintenance end")
    public void testNotifyListenersOnMaintenanceEnd() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        TestMaintenanceListener listener = new TestMaintenanceListener() {
            @Override
            public void onMaintenanceEnded() {
                super.onMaintenanceEnded();
                latch.countDown();
            }
        };
        
        maintenanceManager.addMaintenanceListener(listener);
        maintenanceManager.enableMaintenanceMode("Test", 30);
        maintenanceManager.disableMaintenanceMode();
        
        assertTrue(latch.await(2, TimeUnit.SECONDS));
        assertTrue(listener.maintenanceEndedCalled);
    }
    
    @Test
    @DisplayName("Should get maintenance mode info")
    public void testGetMaintenanceModeInfo() {
        maintenanceManager.enableMaintenanceMode("Test maintenance", 45);
        
        Map<String, Object> info = maintenanceManager.getMaintenanceModeInfo();
        
        assertTrue((Boolean) info.get("isMaintenanceMode"));
        assertEquals("Test maintenance", info.get("reason"));
        assertEquals(45, info.get("estimatedDuration"));
        assertNotNull(info.get("startTime"));
        assertNotNull(info.get("endTime"));
    }
    
    @Test
    @DisplayName("Should get system version")
    public void testGetSystemVersion() {
        String version = maintenanceManager.getSystemVersion();
        
        assertNotNull(version);
        assertFalse(version.isEmpty());
    }
    
    @Test
    @DisplayName("Should clear maintenance history")
    public void testClearMaintenanceHistory() {
        maintenanceManager.enableMaintenanceMode("Test", 30);
        maintenanceManager.disableMaintenanceMode();
        
        List<MaintenanceManager.MaintenanceEvent> historyBefore = maintenanceManager.getMaintenanceHistory();
        assertTrue(historyBefore.size() > 0);
        
        maintenanceManager.clearMaintenanceHistory();
        
        List<MaintenanceManager.MaintenanceEvent> historyAfter = maintenanceManager.getMaintenanceHistory();
        assertEquals(0, historyAfter.size());
    }
    
    @Test
    @DisplayName("Should handle backup info correctly")
    public void testBackupInfo() {
        String backupId = maintenanceManager.createBackup("Test backup");
        MaintenanceManager.BackupInfo backup = maintenanceManager.getBackupInfo(backupId);
        
        assertNotNull(backup);
        assertEquals(backupId, backup.getBackupId());
        assertEquals("Test backup", backup.getName());
        assertNotNull(backup.getCreatedTime());
        assertNotNull(backup.getVersion());
    }
    
    @Test
    @DisplayName("Should handle multiple backups independently")
    public void testMultipleBackups() {
        String id1 = maintenanceManager.createBackup("Backup 1");
        String id2 = maintenanceManager.createBackup("Backup 2");
        String id3 = maintenanceManager.createBackup("Backup 3");
        
        List<MaintenanceManager.BackupInfo> backups = maintenanceManager.getAvailableBackups();
        
        assertTrue(backups.stream().anyMatch(b -> b.getBackupId().equals(id1)));
        assertTrue(backups.stream().anyMatch(b -> b.getBackupId().equals(id2)));
        assertTrue(backups.stream().anyMatch(b -> b.getBackupId().equals(id3)));
    }
    
    /**
     * Test listener for tracking maintenance events.
     */
    private static class TestMaintenanceListener implements MaintenanceManager.MaintenanceListener {
        boolean maintenanceStartedCalled = false;
        boolean maintenanceEndedCalled = false;
        boolean updateInitiatedCalled = false;
        boolean updateCompletedCalled = false;
        
        @Override
        public void onMaintenanceStarted(String reason, LocalDateTime estimatedEndTime) {
            maintenanceStartedCalled = true;
        }
        
        @Override
        public void onMaintenanceEnded() {
            maintenanceEndedCalled = true;
        }
        
        @Override
        public void onUpdateInitiated(String newVersion, String description) {
            updateInitiatedCalled = true;
        }
        
        @Override
        public void onUpdateCompleted(String newVersion) {
            updateCompletedCalled = true;
        }
    }
}
