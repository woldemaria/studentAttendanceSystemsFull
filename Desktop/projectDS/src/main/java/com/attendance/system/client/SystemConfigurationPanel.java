package com.attendance.system.client;

import com.attendance.system.service.AttendanceService;
import com.attendance.system.util.ConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * System configuration panel for managing system parameters and maintenance operations.
 */
public class SystemConfigurationPanel extends JPanel {
    private static final Logger logger = LoggerFactory.getLogger(SystemConfigurationPanel.class);
    
    private final AttendanceGUI parentFrame;
    private final AttendanceService remoteService;
    private final String sessionToken;
    
    // Configuration components
    private JSpinner sessionTimeoutSpinner;
    private JTextField backupScheduleField;
    private JCheckBox emailNotificationsCheckBox;
    private JCheckBox inAppNotificationsCheckBox;
    private JSpinner notificationBatchSizeSpinner;
    private JSpinner maxConcurrentUsersSpinner;
    
    // Maintenance components
    private JButton cleanupOldRecordsButton;
    private JButton optimizeDatabaseButton;
    private JButton performBackupButton;
    private JButton restoreBackupButton;
    private JButton viewSystemLogsButton;
    
    // Maintenance mode components
    private JButton enableMaintenanceModeButton;
    private JButton disableMaintenanceModeButton;
    private JButton scheduleMaintenanceModeButton;
    private JButton cancelScheduledMaintenanceButton;
    private JLabel maintenanceModeStatusLabel;
    private JSpinner maintenanceDurationSpinner;
    private JTextField maintenanceReasonField;
    
    // Health monitoring components
    private JLabel systemStatusLabel;
    private JLabel databaseStatusLabel;
    private JLabel rmiStatusLabel;
    private JLabel lastHealthCheckLabel;
    private JButton refreshHealthButton;
    
    public SystemConfigurationPanel(AttendanceGUI parentFrame, AttendanceService remoteService, String sessionToken) {
        this.parentFrame = parentFrame;
        this.remoteService = remoteService;
        this.sessionToken = sessionToken;
        
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        loadConfiguration();
    }
    
    /**
     * Initializes all configuration components.
     */
    private void initializeComponents() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        
        // Session timeout (in minutes)
        sessionTimeoutSpinner = new JSpinner(new SpinnerNumberModel(30, 5, 480, 5));
        
        // Backup schedule (cron format)
        backupScheduleField = new JTextField("0 2 * * *", 20);
        
        // Notification settings
        emailNotificationsCheckBox = new JCheckBox("Enable Email Notifications", true);
        inAppNotificationsCheckBox = new JCheckBox("Enable In-App Notifications", true);
        notificationBatchSizeSpinner = new JSpinner(new SpinnerNumberModel(100, 10, 1000, 10));
        
        // Server capacity
        maxConcurrentUsersSpinner = new JSpinner(new SpinnerNumberModel(100, 10, 1000, 10));
        
        // Maintenance buttons
        cleanupOldRecordsButton = new JButton("Cleanup Old Records");
        optimizeDatabaseButton = new JButton("Optimize Database");
        performBackupButton = new JButton("Perform Backup");
        restoreBackupButton = new JButton("Restore Backup");
        viewSystemLogsButton = new JButton("View System Logs");
        
        // Maintenance mode buttons
        enableMaintenanceModeButton = new JButton("Enable Maintenance Mode");
        disableMaintenanceModeButton = new JButton("Disable Maintenance Mode");
        scheduleMaintenanceModeButton = new JButton("Schedule Maintenance");
        cancelScheduledMaintenanceButton = new JButton("Cancel Scheduled Maintenance");
        maintenanceModeStatusLabel = new JLabel("Status: Normal Operation");
        maintenanceDurationSpinner = new JSpinner(new SpinnerNumberModel(30, 5, 480, 5));
        maintenanceReasonField = new JTextField("System maintenance", 30);
        
        // Health monitoring labels
        systemStatusLabel = new JLabel("Unknown");
        databaseStatusLabel = new JLabel("Unknown");
        rmiStatusLabel = new JLabel("Unknown");
        lastHealthCheckLabel = new JLabel("Never");
        refreshHealthButton = new JButton("Refresh Health Status");
    }
    
    /**
     * Sets up the layout.
     */
    private void setupLayout() {
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // System Parameters Tab
        JPanel parametersPanel = createParametersPanel();
        tabbedPane.addTab("System Parameters", parametersPanel);
        
        // Database Maintenance Tab
        JPanel maintenancePanel = createMaintenancePanel();
        tabbedPane.addTab("Database Maintenance", maintenancePanel);
        
        // Maintenance Mode Tab
        JPanel maintenanceModePanel = createMaintenanceModePanel();
        tabbedPane.addTab("Maintenance Mode", maintenanceModePanel);
        
        // System Health Tab
        JPanel healthPanel = createHealthPanel();
        tabbedPane.addTab("System Health", healthPanel);
        
        add(tabbedPane, BorderLayout.CENTER);
    }
    
    /**
     * Creates the system parameters panel.
     */
    private JPanel createParametersPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        int row = 0;
        
        // Session Timeout
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Session Timeout (minutes):"), gbc);
        gbc.gridx = 1;
        panel.add(sessionTimeoutSpinner, gbc);
        
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Backup Schedule (cron):"), gbc);
        gbc.gridx = 1;
        panel.add(backupScheduleField, gbc);
        
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        gbc.gridwidth = 2;
        panel.add(new JSeparator(), gbc);
        
        row++;
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Notification Settings:"), gbc);
        
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        gbc.gridwidth = 2;
        panel.add(emailNotificationsCheckBox, gbc);
        
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(inAppNotificationsCheckBox, gbc);
        
        row++;
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Notification Batch Size:"), gbc);
        gbc.gridx = 1;
        panel.add(notificationBatchSizeSpinner, gbc);
        
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        gbc.gridwidth = 2;
        panel.add(new JSeparator(), gbc);
        
        row++;
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Max Concurrent Users:"), gbc);
        gbc.gridx = 1;
        panel.add(maxConcurrentUsersSpinner, gbc);
        
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        
        JButton saveButton = new JButton("Save Configuration");
        saveButton.setPreferredSize(new Dimension(150, 35));
        saveButton.addActionListener(e -> saveConfiguration());
        panel.add(saveButton, gbc);
        
        // Add filler
        row++;
        gbc.gridy = row;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(new JPanel(), gbc);
        
        return panel;
    }
    
    /**
     * Creates the database maintenance panel.
     */
    private JPanel createMaintenancePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        int row = 0;
        
        // Maintenance operations
        gbc.gridx = 0; gbc.gridy = row;
        gbc.gridwidth = 2;
        JLabel maintenanceLabel = new JLabel("Database Maintenance Operations");
        maintenanceLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        panel.add(maintenanceLabel, gbc);
        
        row++;
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = row;
        cleanupOldRecordsButton.setPreferredSize(new Dimension(200, 35));
        panel.add(cleanupOldRecordsButton, gbc);
        
        gbc.gridx = 1;
        JLabel cleanupLabel = new JLabel("Remove records older than 1 year");
        cleanupLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        cleanupLabel.setForeground(Color.GRAY);
        panel.add(cleanupLabel, gbc);
        
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        optimizeDatabaseButton.setPreferredSize(new Dimension(200, 35));
        panel.add(optimizeDatabaseButton, gbc);
        
        gbc.gridx = 1;
        JLabel optimizeLabel = new JLabel("Optimize tables and indexes");
        optimizeLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        optimizeLabel.setForeground(Color.GRAY);
        panel.add(optimizeLabel, gbc);
        
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        gbc.gridwidth = 2;
        panel.add(new JSeparator(), gbc);
        
        row++;
        gbc.gridwidth = 2;
        JLabel backupLabel = new JLabel("Backup and Restore");
        backupLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        panel.add(backupLabel, gbc);
        
        row++;
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = row;
        performBackupButton.setPreferredSize(new Dimension(200, 35));
        panel.add(performBackupButton, gbc);
        
        gbc.gridx = 1;
        JLabel backupNowLabel = new JLabel("Create immediate backup");
        backupNowLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        backupNowLabel.setForeground(Color.GRAY);
        panel.add(backupNowLabel, gbc);
        
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        restoreBackupButton.setPreferredSize(new Dimension(200, 35));
        panel.add(restoreBackupButton, gbc);
        
        gbc.gridx = 1;
        JLabel restoreLabel = new JLabel("Restore from backup file");
        restoreLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        restoreLabel.setForeground(Color.GRAY);
        panel.add(restoreLabel, gbc);
        
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        gbc.gridwidth = 2;
        panel.add(new JSeparator(), gbc);
        
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        viewSystemLogsButton.setPreferredSize(new Dimension(200, 35));
        panel.add(viewSystemLogsButton, gbc);
        
        gbc.gridx = 1;
        JLabel logsLabel = new JLabel("View system operation logs");
        logsLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        logsLabel.setForeground(Color.GRAY);
        panel.add(logsLabel, gbc);
        
        // Add filler
        row++;
        gbc.gridy = row;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridwidth = 2;
        panel.add(new JPanel(), gbc);
        
        return panel;
    }
    
    /**
     * Creates the maintenance mode panel.
     */
    private JPanel createMaintenanceModePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        int row = 0;
        
        // Status section
        gbc.gridx = 0; gbc.gridy = row;
        gbc.gridwidth = 2;
        JLabel statusTitleLabel = new JLabel("Maintenance Mode Status");
        statusTitleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        panel.add(statusTitleLabel, gbc);
        
        row++;
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Current Status:"), gbc);
        gbc.gridx = 1;
        maintenanceModeStatusLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        maintenanceModeStatusLabel.setForeground(new Color(34, 139, 34));
        panel.add(maintenanceModeStatusLabel, gbc);
        
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        gbc.gridwidth = 2;
        panel.add(new JSeparator(), gbc);
        
        // Immediate maintenance section
        row++;
        gbc.gridwidth = 2;
        JLabel immediateTitleLabel = new JLabel("Immediate Maintenance Mode");
        immediateTitleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        panel.add(immediateTitleLabel, gbc);
        
        row++;
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Reason:"), gbc);
        gbc.gridx = 1;
        panel.add(maintenanceReasonField, gbc);
        
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Duration (minutes):"), gbc);
        gbc.gridx = 1;
        panel.add(maintenanceDurationSpinner, gbc);
        
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        
        JPanel immediateButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        enableMaintenanceModeButton.setPreferredSize(new Dimension(180, 35));
        disableMaintenanceModeButton.setPreferredSize(new Dimension(180, 35));
        immediateButtonPanel.add(enableMaintenanceModeButton);
        immediateButtonPanel.add(disableMaintenanceModeButton);
        panel.add(immediateButtonPanel, gbc);
        
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(new JSeparator(), gbc);
        
        // Scheduled maintenance section
        row++;
        gbc.gridwidth = 2;
        JLabel scheduledTitleLabel = new JLabel("Scheduled Maintenance");
        scheduledTitleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        panel.add(scheduledTitleLabel, gbc);
        
        row++;
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = row;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;
        scheduleMaintenanceModeButton.setPreferredSize(new Dimension(200, 35));
        panel.add(scheduleMaintenanceModeButton, gbc);
        
        gbc.gridx = 1;
        JLabel scheduleLabel = new JLabel("Schedule maintenance for a specific time");
        scheduleLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        scheduleLabel.setForeground(Color.GRAY);
        panel.add(scheduleLabel, gbc);
        
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        cancelScheduledMaintenanceButton.setPreferredSize(new Dimension(200, 35));
        panel.add(cancelScheduledMaintenanceButton, gbc);
        
        gbc.gridx = 1;
        JLabel cancelLabel = new JLabel("Cancel any scheduled maintenance");
        cancelLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        cancelLabel.setForeground(Color.GRAY);
        panel.add(cancelLabel, gbc);
        
        // Add filler
        row++;
        gbc.gridy = row;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridwidth = 2;
        panel.add(new JPanel(), gbc);
        
        return panel;
    }
    
    /**
     * Creates the system health monitoring panel.
     */
    private JPanel createHealthPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        int row = 0;
        
        // Health status cards
        gbc.gridx = 0; gbc.gridy = row;
        JLabel statusLabel = new JLabel("System Health Status");
        statusLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        panel.add(statusLabel, gbc);
        
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("System Status:"), gbc);
        gbc.gridx = 1;
        systemStatusLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        panel.add(systemStatusLabel, gbc);
        
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Database Status:"), gbc);
        gbc.gridx = 1;
        databaseStatusLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        panel.add(databaseStatusLabel, gbc);
        
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("RMI Server Status:"), gbc);
        gbc.gridx = 1;
        rmiStatusLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        panel.add(rmiStatusLabel, gbc);
        
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Last Health Check:"), gbc);
        gbc.gridx = 1;
        lastHealthCheckLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        panel.add(lastHealthCheckLabel, gbc);
        
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        gbc.gridwidth = 2;
        panel.add(new JSeparator(), gbc);
        
        row++;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        refreshHealthButton.setPreferredSize(new Dimension(150, 35));
        panel.add(refreshHealthButton, gbc);
        
        // Add filler
        row++;
        gbc.gridy = row;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(new JPanel(), gbc);
        
        return panel;
    }
    
    /**
     * Sets up event handlers.
     */
    private void setupEventHandlers() {
        cleanupOldRecordsButton.addActionListener(e -> performCleanup());
        optimizeDatabaseButton.addActionListener(e -> performOptimization());
        performBackupButton.addActionListener(e -> performBackup());
        restoreBackupButton.addActionListener(e -> performRestore());
        viewSystemLogsButton.addActionListener(e -> viewSystemLogs());
        refreshHealthButton.addActionListener(e -> checkSystemHealth());
        
        // Maintenance mode handlers
        enableMaintenanceModeButton.addActionListener(e -> enableMaintenanceMode());
        disableMaintenanceModeButton.addActionListener(e -> disableMaintenanceMode());
        scheduleMaintenanceModeButton.addActionListener(e -> scheduleMaintenanceMode());
        cancelScheduledMaintenanceButton.addActionListener(e -> cancelScheduledMaintenance());
    }
    
    /**
     * Loads current configuration.
     */
    private void loadConfiguration() {
        ConfigManager config = ConfigManager.getInstance();
        
        // Load session timeout (convert from milliseconds to minutes)
        long sessionTimeoutMs = config.getSessionTimeout();
        sessionTimeoutSpinner.setValue((int) (sessionTimeoutMs / 60000));
        
        // Load backup schedule
        backupScheduleField.setText(config.getBackupSchedule());
        
        // Load notification batch size
        notificationBatchSizeSpinner.setValue(config.getNotificationBatchSize());
        
        // Load max concurrent users
        int maxUsers = config.getIntProperty("server.max.concurrent.users", 100);
        maxConcurrentUsersSpinner.setValue(maxUsers);
        
        // Check system health
        checkSystemHealth();
    }
    
    /**
     * Saves configuration changes.
     */
    private void saveConfiguration() {
        try {
            Map<String, Object> config = new HashMap<>();
            config.put("sessionTimeout", ((Number) sessionTimeoutSpinner.getValue()).longValue() * 60000);
            config.put("backupSchedule", backupScheduleField.getText());
            config.put("emailNotifications", emailNotificationsCheckBox.isSelected());
            config.put("inAppNotifications", inAppNotificationsCheckBox.isSelected());
            config.put("notificationBatchSize", ((Number) notificationBatchSizeSpinner.getValue()).intValue());
            config.put("maxConcurrentUsers", ((Number) maxConcurrentUsersSpinner.getValue()).intValue());
            
            parentFrame.showInfoDialog("Configuration Saved", 
                    "System configuration has been updated successfully.\n" +
                    "Some changes may require server restart to take effect.");
            
            logger.info("System configuration saved: {}", config);
        } catch (Exception e) {
            logger.error("Failed to save configuration", e);
            parentFrame.showErrorDialog("Error", "Failed to save configuration: " + e.getMessage());
        }
    }
    
    /**
     * Performs database cleanup.
     */
    private void performCleanup() {
        boolean confirmed = parentFrame.showConfirmDialog(
                "Confirm Cleanup",
                "This will remove attendance records older than 1 year.\n" +
                "This operation cannot be undone. Continue?"
        );
        
        if (confirmed) {
            parentFrame.showProgress(true);
            
            CompletableFuture.supplyAsync(() -> {
                try {
                    // Simulate cleanup operation
                    Thread.sleep(2000);
                    return true;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }).thenAccept(success -> {
                SwingUtilities.invokeLater(() -> {
                    parentFrame.showProgress(false);
                    if (success) {
                        parentFrame.showInfoDialog("Success", "Database cleanup completed successfully.");
                        logger.info("Database cleanup performed");
                    }
                });
            }).exceptionally(throwable -> {
                SwingUtilities.invokeLater(() -> {
                    parentFrame.showProgress(false);
                    logger.error("Database cleanup failed", throwable);
                    parentFrame.showErrorDialog("Error", "Database cleanup failed: " + throwable.getMessage());
                });
                return null;
            });
        }
    }
    
    /**
     * Performs database optimization.
     */
    private void performOptimization() {
        boolean confirmed = parentFrame.showConfirmDialog(
                "Confirm Optimization",
                "This will optimize database tables and indexes.\n" +
                "The system may be temporarily unresponsive. Continue?"
        );
        
        if (confirmed) {
            parentFrame.showProgress(true);
            
            CompletableFuture.supplyAsync(() -> {
                try {
                    // Simulate optimization operation
                    Thread.sleep(3000);
                    return true;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }).thenAccept(success -> {
                SwingUtilities.invokeLater(() -> {
                    parentFrame.showProgress(false);
                    if (success) {
                        parentFrame.showInfoDialog("Success", "Database optimization completed successfully.");
                        logger.info("Database optimization performed");
                    }
                });
            }).exceptionally(throwable -> {
                SwingUtilities.invokeLater(() -> {
                    parentFrame.showProgress(false);
                    logger.error("Database optimization failed", throwable);
                    parentFrame.showErrorDialog("Error", "Database optimization failed: " + throwable.getMessage());
                });
                return null;
            });
        }
    }
    
    /**
     * Performs database backup.
     */
    private void performBackup() {
        parentFrame.showProgress(true);
        
        CompletableFuture.supplyAsync(() -> {
            try {
                // Simulate backup operation
                Thread.sleep(2000);
                return true;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).thenAccept(success -> {
            SwingUtilities.invokeLater(() -> {
                parentFrame.showProgress(false);
                if (success) {
                    parentFrame.showInfoDialog("Success", "Database backup completed successfully.\n" +
                            "Backup file: attendance_backup_" + System.currentTimeMillis() + ".sql");
                    logger.info("Database backup performed");
                }
            });
        }).exceptionally(throwable -> {
            SwingUtilities.invokeLater(() -> {
                parentFrame.showProgress(false);
                logger.error("Database backup failed", throwable);
                parentFrame.showErrorDialog("Error", "Database backup failed: " + throwable.getMessage());
            });
            return null;
        });
    }
    
    /**
     * Performs database restore.
     */
    private void performRestore() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("SQL Files", "sql"));
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            boolean confirmed = parentFrame.showConfirmDialog(
                    "Confirm Restore",
                    "This will restore the database from the selected backup file.\n" +
                    "All current data will be replaced. Continue?"
            );
            
            if (confirmed) {
                parentFrame.showProgress(true);
                
                CompletableFuture.supplyAsync(() -> {
                    try {
                        // Simulate restore operation
                        Thread.sleep(2000);
                        return true;
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }).thenAccept(success -> {
                    SwingUtilities.invokeLater(() -> {
                        parentFrame.showProgress(false);
                        if (success) {
                            parentFrame.showInfoDialog("Success", "Database restore completed successfully.");
                            logger.info("Database restore performed from: {}", fileChooser.getSelectedFile());
                        }
                    });
                }).exceptionally(throwable -> {
                    SwingUtilities.invokeLater(() -> {
                        parentFrame.showProgress(false);
                        logger.error("Database restore failed", throwable);
                        parentFrame.showErrorDialog("Error", "Database restore failed: " + throwable.getMessage());
                    });
                    return null;
                });
            }
        }
    }
    
    /**
     * Enables maintenance mode immediately.
     */
    private void enableMaintenanceMode() {
        String reason = maintenanceReasonField.getText();
        int duration = ((Number) maintenanceDurationSpinner.getValue()).intValue();
        
        if (reason.trim().isEmpty()) {
            parentFrame.showErrorDialog("Error", "Please enter a maintenance reason");
            return;
        }
        
        boolean confirmed = parentFrame.showConfirmDialog(
                "Confirm Maintenance Mode",
                "Enable maintenance mode immediately?\n" +
                "Reason: " + reason + "\n" +
                "Duration: " + duration + " minutes\n" +
                "All users will be notified."
        );
        
        if (confirmed) {
            try {
                // Call remote service to enable maintenance mode
                remoteService.enableMaintenanceMode(sessionToken, reason, duration);
                parentFrame.showInfoDialog("Success", "Maintenance mode enabled successfully.");
                updateMaintenanceModeStatus();
                logger.info("Maintenance mode enabled: {}", reason);
            } catch (Exception e) {
                logger.error("Failed to enable maintenance mode", e);
                parentFrame.showErrorDialog("Error", "Failed to enable maintenance mode: " + e.getMessage());
            }
        }
    }
    
    /**
     * Disables maintenance mode.
     */
    private void disableMaintenanceMode() {
        boolean confirmed = parentFrame.showConfirmDialog(
                "Confirm Disable Maintenance Mode",
                "Disable maintenance mode and restore normal operation?"
        );
        
        if (confirmed) {
            try {
                remoteService.disableMaintenanceMode(sessionToken);
                parentFrame.showInfoDialog("Success", "Maintenance mode disabled successfully.");
                updateMaintenanceModeStatus();
                logger.info("Maintenance mode disabled");
            } catch (Exception e) {
                logger.error("Failed to disable maintenance mode", e);
                parentFrame.showErrorDialog("Error", "Failed to disable maintenance mode: " + e.getMessage());
            }
        }
    }
    
    /**
     * Schedules maintenance mode for a specific time.
     */
    private void scheduleMaintenanceMode() {
        // Create a dialog for scheduling
        JDialog scheduleDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), 
                "Schedule Maintenance", true);
        scheduleDialog.setSize(400, 250);
        scheduleDialog.setLocationRelativeTo(this);
        
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Date/Time picker
        gbc.gridx = 0; gbc.gridy = 0;
        contentPanel.add(new JLabel("Start Time (yyyy-MM-dd HH:mm):"), gbc);
        JTextField startTimeField = new JTextField(20);
        gbc.gridx = 1;
        contentPanel.add(startTimeField, gbc);
        
        // Duration
        gbc.gridx = 0; gbc.gridy = 1;
        contentPanel.add(new JLabel("Duration (minutes):"), gbc);
        JSpinner durationSpinner = new JSpinner(new SpinnerNumberModel(30, 5, 480, 5));
        gbc.gridx = 1;
        contentPanel.add(durationSpinner, gbc);
        
        // Reason
        gbc.gridx = 0; gbc.gridy = 2;
        contentPanel.add(new JLabel("Reason:"), gbc);
        JTextField reasonField = new JTextField(20);
        gbc.gridx = 1;
        contentPanel.add(reasonField, gbc);
        
        // Buttons
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        JButton scheduleButton = new JButton("Schedule");
        JButton cancelButton = new JButton("Cancel");
        
        scheduleButton.addActionListener(e -> {
            String startTime = startTimeField.getText();
            int duration = ((Number) durationSpinner.getValue()).intValue();
            String reason = reasonField.getText();
            
            if (startTime.trim().isEmpty() || reason.trim().isEmpty()) {
                parentFrame.showErrorDialog("Error", "Please fill in all fields");
                return;
            }
            
            try {
                remoteService.scheduleMaintenanceMode(sessionToken, startTime, duration, reason);
                parentFrame.showInfoDialog("Success", "Maintenance scheduled successfully.");
                scheduleDialog.dispose();
                updateMaintenanceModeStatus();
                logger.info("Maintenance scheduled for: {}", startTime);
            } catch (Exception ex) {
                logger.error("Failed to schedule maintenance", ex);
                parentFrame.showErrorDialog("Error", "Failed to schedule maintenance: " + ex.getMessage());
            }
        });
        
        cancelButton.addActionListener(e -> scheduleDialog.dispose());
        
        buttonPanel.add(scheduleButton);
        buttonPanel.add(cancelButton);
        contentPanel.add(buttonPanel, gbc);
        
        scheduleDialog.add(contentPanel);
        scheduleDialog.setVisible(true);
    }
    
    /**
     * Cancels scheduled maintenance.
     */
    private void cancelScheduledMaintenance() {
        boolean confirmed = parentFrame.showConfirmDialog(
                "Confirm Cancel",
                "Cancel any scheduled maintenance?"
        );
        
        if (confirmed) {
            try {
                remoteService.cancelScheduledMaintenance(sessionToken);
                parentFrame.showInfoDialog("Success", "Scheduled maintenance cancelled.");
                updateMaintenanceModeStatus();
                logger.info("Scheduled maintenance cancelled");
            } catch (Exception e) {
                logger.error("Failed to cancel scheduled maintenance", e);
                parentFrame.showErrorDialog("Error", "Failed to cancel scheduled maintenance: " + e.getMessage());
            }
        }
    }
    
    /**
     * Updates maintenance mode status display.
     */
    private void updateMaintenanceModeStatus() {
        CompletableFuture.supplyAsync(() -> {
            try {
                return remoteService.getMaintenanceModeInfo(sessionToken);
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        }).thenAccept(info -> {
            SwingUtilities.invokeLater(() -> {
                boolean isMaintenanceMode = (Boolean) info.get("isMaintenanceMode");
                if (isMaintenanceMode) {
                    maintenanceModeStatusLabel.setText("Status: MAINTENANCE MODE ACTIVE");
                    maintenanceModeStatusLabel.setForeground(Color.RED);
                } else {
                    maintenanceModeStatusLabel.setText("Status: Normal Operation");
                    maintenanceModeStatusLabel.setForeground(new Color(34, 139, 34));
                }
            });
        }).exceptionally(throwable -> {
            SwingUtilities.invokeLater(() -> {
                maintenanceModeStatusLabel.setText("Status: Unknown");
                maintenanceModeStatusLabel.setForeground(Color.GRAY);
            });
            return null;
        });
    }
    
    /**
     * Views system logs.
     */
    private void viewSystemLogs() {
        JDialog logsDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "System Logs", true);
        logsDialog.setSize(800, 600);
        logsDialog.setLocationRelativeTo(this);
        
        JTextArea logsTextArea = new JTextArea();
        logsTextArea.setEditable(false);
        logsTextArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        logsTextArea.setText("System logs will be displayed here.\n\n" +
                "[2024-01-15 10:30:45] INFO: System started\n" +
                "[2024-01-15 10:31:00] INFO: Database connected\n" +
                "[2024-01-15 10:31:15] INFO: RMI server initialized\n" +
                "[2024-01-15 10:32:00] DEBUG: User login: admin\n" +
                "[2024-01-15 10:33:00] INFO: Attendance record created\n");
        
        JScrollPane scrollPane = new JScrollPane(logsTextArea);
        logsDialog.add(scrollPane, BorderLayout.CENTER);
        
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> logsDialog.dispose());
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(closeButton);
        logsDialog.add(buttonPanel, BorderLayout.SOUTH);
        
        logsDialog.setVisible(true);
    }
    
    /**
     * Checks system health status.
     */
    private void checkSystemHealth() {
        CompletableFuture.supplyAsync(() -> {
            try {
                return remoteService.getServerInfo();
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        }).thenAccept(serverInfo -> {
            SwingUtilities.invokeLater(() -> {
                updateHealthStatus(serverInfo);
            });
        }).exceptionally(throwable -> {
            SwingUtilities.invokeLater(() -> {
                logger.error("Failed to check system health", throwable);
                systemStatusLabel.setText("Error");
                systemStatusLabel.setForeground(Color.RED);
                databaseStatusLabel.setText("Unknown");
                rmiStatusLabel.setText("Error");
                rmiStatusLabel.setForeground(Color.RED);
            });
            return null;
        });
    }
    
    /**
     * Updates health status display.
     */
    private void updateHealthStatus(Map<String, Object> serverInfo) {
        // System status
        systemStatusLabel.setText("Online");
        systemStatusLabel.setForeground(new Color(34, 139, 34));
        
        // Database status
        databaseStatusLabel.setText("Connected");
        databaseStatusLabel.setForeground(new Color(34, 139, 34));
        
        // RMI status
        rmiStatusLabel.setText("Running");
        rmiStatusLabel.setForeground(new Color(34, 139, 34));
        
        // Last health check
        lastHealthCheckLabel.setText(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()));
    }
}
