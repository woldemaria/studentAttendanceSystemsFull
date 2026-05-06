package com.attendance.system.client;

import com.attendance.system.model.*;
import com.attendance.system.service.AttendanceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.rmi.RemoteException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Admin-specific dashboard interface with user management, system reports, and configuration.
 */
public class AdminDashboard extends JPanel {
    private static final Logger logger = LoggerFactory.getLogger(AdminDashboard.class);
    
    private final AttendanceGUI parentFrame;
    private final AttendanceService remoteService;
    private final String sessionToken;
    
    // Main components
    private JTabbedPane tabbedPane;
    private JPanel dashboardPanel;
    private JPanel userManagementPanel;
    private JPanel systemReportsPanel;
    private JPanel systemConfigPanel;
    
    // System statistics components
    private JLabel totalUsersLabel;
    private JLabel totalStudentsLabel;
    private JLabel totalTeachersLabel;
    private JLabel totalCoursesLabel;
    private JLabel activeSessionsLabel;
    
    public AdminDashboard(AttendanceGUI parentFrame, AttendanceService remoteService, String sessionToken) {
        this.parentFrame = parentFrame;
        this.remoteService = remoteService;
        this.sessionToken = sessionToken;
        
        initializeComponents();
        setupLayout();
        loadInitialData();
    }
    
    /**
     * Initializes all GUI components.
     */
    private void initializeComponents() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        
        // Create tabbed pane
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        
        // Initialize panels
        createDashboardPanel();
        createUserManagementPanel();
        createSystemReportsPanel();
        createSystemConfigPanel();
        
        // Add tabs
        tabbedPane.addTab("Dashboard", new ImageIcon(), dashboardPanel, "System overview and statistics");
        tabbedPane.addTab("User Management", new ImageIcon(), userManagementPanel, "Manage users and accounts");
        tabbedPane.addTab("Reports", new ImageIcon(), systemReportsPanel, "Generate system reports");
        tabbedPane.addTab("Configuration", new ImageIcon(), systemConfigPanel, "System settings and configuration");
    }
    
    /**
     * Creates the main dashboard panel with system statistics.
     */
    private void createDashboardPanel() {
        dashboardPanel = new JPanel(new BorderLayout());
        dashboardPanel.setBackground(Color.WHITE);
        
        // Title panel
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        titlePanel.setBackground(Color.WHITE);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel titleLabel = new JLabel("Administrator Dashboard");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        titleLabel.setForeground(new Color(70, 130, 180));
        titlePanel.add(titleLabel);
        
        dashboardPanel.add(titlePanel, BorderLayout.NORTH);
        
        // Statistics panel
        JPanel statsPanel = createStatisticsPanel();
        dashboardPanel.add(statsPanel, BorderLayout.CENTER);
        
        // Quick actions panel
        JPanel actionsPanel = createQuickActionsPanel();
        dashboardPanel.add(actionsPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Creates the statistics panel.
     */
    private JPanel createStatisticsPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 3, 20, 20));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.WHITE);
        
        // Create statistic cards
        totalUsersLabel = new JLabel("0");
        panel.add(createStatCard("Total Users", totalUsersLabel, new Color(70, 130, 180)));
        
        totalStudentsLabel = new JLabel("0");
        panel.add(createStatCard("Students", totalStudentsLabel, new Color(34, 139, 34)));
        
        totalTeachersLabel = new JLabel("0");
        panel.add(createStatCard("Teachers", totalTeachersLabel, new Color(255, 140, 0)));
        
        totalCoursesLabel = new JLabel("0");
        panel.add(createStatCard("Courses", totalCoursesLabel, new Color(220, 20, 60)));
        
        activeSessionsLabel = new JLabel("0");
        panel.add(createStatCard("Active Sessions", activeSessionsLabel, new Color(138, 43, 226)));
        
        // System health card
        JLabel healthLabel = new JLabel("Online");
        panel.add(createStatCard("System Status", healthLabel, new Color(0, 128, 0)));
        
        return panel;
    }
    
    /**
     * Creates a statistic card.
     */
    private JPanel createStatCard(String title, JLabel valueLabel, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color, 2),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        titleLabel.setForeground(color);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        valueLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 32));
        valueLabel.setForeground(color);
        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        
        return card;
    }
    
    /**
     * Creates the quick actions panel.
     */
    private JPanel createQuickActionsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder("Quick Actions"));
        
        JButton refreshStatsButton = new JButton("Refresh Statistics");
        refreshStatsButton.setPreferredSize(new Dimension(150, 35));
        refreshStatsButton.addActionListener(e -> loadSystemStatistics());
        
        JButton viewLogsButton = new JButton("View System Logs");
        viewLogsButton.setPreferredSize(new Dimension(150, 35));
        viewLogsButton.addActionListener(e -> showSystemLogs());
        
        JButton backupButton = new JButton("Backup Database");
        backupButton.setPreferredSize(new Dimension(150, 35));
        backupButton.addActionListener(e -> performDatabaseBackup());
        
        panel.add(refreshStatsButton);
        panel.add(viewLogsButton);
        panel.add(backupButton);
        
        return panel;
    }
    
    /**
     * Creates the user management panel.
     */
    private void createUserManagementPanel() {
        userManagementPanel = new UserManagementPanel(parentFrame, remoteService, sessionToken);
    }
    
    /**
     * Creates the system reports panel.
     */
    private void createSystemReportsPanel() {
        systemReportsPanel = new JPanel(new BorderLayout());
        systemReportsPanel.setBackground(Color.WHITE);
        
        JLabel label = new JLabel("System Reports - Coming Soon", SwingConstants.CENTER);
        label.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        label.setForeground(Color.GRAY);
        
        systemReportsPanel.add(label, BorderLayout.CENTER);
    }
    
    /**
     * Creates the system configuration panel.
     */
    private void createSystemConfigPanel() {
        systemConfigPanel = new SystemConfigurationPanel(parentFrame, remoteService, sessionToken);
    }
    
    /**
     * Sets up the layout.
     */
    private void setupLayout() {
        add(tabbedPane, BorderLayout.CENTER);
        
        // Create header panel with logout button
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(70, 130, 180));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel welcomeLabel = new JLabel("Welcome, Administrator");
        welcomeLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        welcomeLabel.setForeground(Color.WHITE);
        
        JButton logoutButton = new JButton("Logout");
        logoutButton.setBackground(Color.WHITE);
        logoutButton.setForeground(new Color(70, 130, 180));
        logoutButton.setFocusPainted(false);
        logoutButton.addActionListener(e -> parentFrame.handleLogout());
        
        headerPanel.add(welcomeLabel, BorderLayout.WEST);
        headerPanel.add(logoutButton, BorderLayout.EAST);
        
        add(headerPanel, BorderLayout.NORTH);
    }
    
    /**
     * Loads initial data.
     */
    private void loadInitialData() {
        loadSystemStatistics();
    }
    
    /**
     * Loads system statistics.
     */
    private void loadSystemStatistics() {
        CompletableFuture.supplyAsync(() -> {
            try {
                return remoteService.getSystemStatistics(sessionToken);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).thenAccept(stats -> {
            SwingUtilities.invokeLater(() -> {
                updateStatistics(stats);
            });
        }).exceptionally(throwable -> {
            SwingUtilities.invokeLater(() -> {
                logger.error("Failed to load system statistics", throwable);
                parentFrame.showErrorDialog("Error", "Failed to load system statistics: " + throwable.getMessage());
            });
            return null;
        });
    }
    
    /**
     * Updates the statistics display.
     */
    private void updateStatistics(Map<String, Object> stats) {
        totalUsersLabel.setText(String.valueOf(stats.getOrDefault("totalUsers", 0)));
        totalStudentsLabel.setText(String.valueOf(stats.getOrDefault("totalStudents", 0)));
        totalTeachersLabel.setText(String.valueOf(stats.getOrDefault("totalTeachers", 0)));
        totalCoursesLabel.setText(String.valueOf(stats.getOrDefault("totalCourses", 0)));
        activeSessionsLabel.setText(String.valueOf(stats.getOrDefault("activeSessions", 0)));
    }
    
    /**
     * Shows system logs.
     */
    private void showSystemLogs() {
        parentFrame.showInfoDialog("System Logs", "System logs functionality will be implemented");
    }
    
    /**
     * Performs database backup.
     */
    private void performDatabaseBackup() {
        parentFrame.showInfoDialog("Database Backup", "Database backup functionality will be implemented");
    }
}
