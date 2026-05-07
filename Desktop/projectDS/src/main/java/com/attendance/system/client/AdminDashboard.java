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
    private JPanel courseManagementContainer;
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
        createCourseManagementPanel();
        createSystemReportsPanel();
        createSystemConfigPanel();
        
        // Add tabs in order
        tabbedPane.addTab("Dashboard", new ImageIcon(), dashboardPanel, "System overview and statistics");
        tabbedPane.addTab("User Management", new ImageIcon(), userManagementPanel, "Manage users and accounts");
        tabbedPane.addTab("Course & Enrollment", new ImageIcon(), courseManagementContainer, "Manage courses and enrollments");
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
     * Creates the course management panel.
     */
    private void createCourseManagementPanel() {
        JTabbedPane courseTabPane = new JTabbedPane();
        
        // Course Management tab
        CourseManagementPanel coursePanel = new CourseManagementPanel(parentFrame, remoteService, sessionToken);
        courseTabPane.addTab("Courses", coursePanel);
        
        // Enrollment Management tab
        EnrollmentManagementPanel enrollmentPanel = new EnrollmentManagementPanel(parentFrame, remoteService, sessionToken);
        courseTabPane.addTab("Enrollments", enrollmentPanel);
        
        // Notification Management tab
        NotificationPanel notificationPanel = new NotificationPanel(parentFrame, remoteService, sessionToken, 
                parentFrame.getCurrentUser().getUser());
        courseTabPane.addTab("Notifications", notificationPanel);
        
        // Add the tabbed pane as the course management container
        courseManagementContainer = new JPanel(new BorderLayout());
        courseManagementContainer.add(courseTabPane, BorderLayout.CENTER);
    }
    
    /**
     * Creates the system reports panel.
     */
    private void createSystemReportsPanel() {
        systemReportsPanel = new JPanel(new BorderLayout());
        systemReportsPanel.setBackground(Color.WHITE);
        
        // Title panel
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        titlePanel.setBackground(Color.WHITE);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel titleLabel = new JLabel("System Reports");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        titleLabel.setForeground(new Color(70, 130, 180));
        titlePanel.add(titleLabel);
        
        systemReportsPanel.add(titlePanel, BorderLayout.NORTH);
        
        // Main content panel
        JPanel contentPanel = new JPanel(new GridLayout(3, 2, 20, 20));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        contentPanel.setBackground(Color.WHITE);
        
        // User Activity Report
        JPanel userActivityCard = createReportCard(
            "User Activity Report",
            "View login history and user activity statistics",
            new Color(70, 130, 180),
            e -> generateUserActivityReport()
        );
        contentPanel.add(userActivityCard);
        
        // Attendance Summary Report
        JPanel attendanceSummaryCard = createReportCard(
            "Attendance Summary",
            "Overall attendance statistics across all courses",
            new Color(34, 139, 34),
            e -> generateAttendanceSummaryReport()
        );
        contentPanel.add(attendanceSummaryCard);
        
        // Course Enrollment Report
        JPanel enrollmentCard = createReportCard(
            "Enrollment Report",
            "Student enrollment statistics by course",
            new Color(255, 140, 0),
            e -> generateEnrollmentReport()
        );
        contentPanel.add(enrollmentCard);
        
        // Teacher Performance Report
        JPanel teacherCard = createReportCard(
            "Teacher Report",
            "Teacher course assignments and statistics",
            new Color(220, 20, 60),
            e -> generateTeacherReport()
        );
        contentPanel.add(teacherCard);
        
        // System Usage Report
        JPanel systemUsageCard = createReportCard(
            "System Usage",
            "System performance and usage statistics",
            new Color(138, 43, 226),
            e -> generateSystemUsageReport()
        );
        contentPanel.add(systemUsageCard);
        
        // Database Statistics
        JPanel databaseCard = createReportCard(
            "Database Statistics",
            "Database size and table statistics",
            new Color(0, 128, 128),
            e -> generateDatabaseReport()
        );
        contentPanel.add(databaseCard);
        
        systemReportsPanel.add(contentPanel, BorderLayout.CENTER);
    }
    
    /**
     * Creates a report card with title, description, and action button.
     */
    private JPanel createReportCard(String title, String description, Color color, java.awt.event.ActionListener action) {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color, 2),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        // Title
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        titleLabel.setForeground(color);
        
        // Description
        JLabel descLabel = new JLabel("<html>" + description + "</html>");
        descLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        descLabel.setForeground(Color.DARK_GRAY);
        
        // Button
        JButton generateButton = new JButton("Generate Report");
        generateButton.setBackground(color);
        generateButton.setForeground(Color.WHITE);
        generateButton.setFocusPainted(false);
        generateButton.addActionListener(action);
        
        // Layout
        JPanel textPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        textPanel.setBackground(Color.WHITE);
        textPanel.add(titleLabel);
        textPanel.add(descLabel);
        
        card.add(textPanel, BorderLayout.CENTER);
        card.add(generateButton, BorderLayout.SOUTH);
        
        return card;
    }
    
    /**
     * Generates user activity report.
     */
    private void generateUserActivityReport() {
        try {
            Map<String, Object> stats = remoteService.getSystemStatistics(sessionToken);
            
            StringBuilder report = new StringBuilder();
            report.append("USER ACTIVITY REPORT\n");
            report.append("===================\n\n");
            report.append("Total Users: ").append(stats.getOrDefault("totalUsers", 0)).append("\n");
            report.append("Active Users: ").append(stats.getOrDefault("activeUsers", 0)).append("\n");
            report.append("Total Students: ").append(stats.getOrDefault("totalStudents", 0)).append("\n");
            report.append("Total Teachers: ").append(stats.getOrDefault("totalTeachers", 0)).append("\n");
            report.append("Total Admins: ").append(stats.getOrDefault("totalAdmins", 0)).append("\n");
            report.append("\nActive Sessions: ").append(stats.getOrDefault("activeSessions", 0)).append("\n");
            
            showReportDialog("User Activity Report", report.toString());
        } catch (Exception e) {
            logger.error("Failed to generate user activity report", e);
            JOptionPane.showMessageDialog(this, 
                "Failed to generate report: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Generates attendance summary report.
     */
    private void generateAttendanceSummaryReport() {
        try {
            Map<String, Object> stats = remoteService.getSystemStatistics(sessionToken);
            
            StringBuilder report = new StringBuilder();
            report.append("ATTENDANCE SUMMARY REPORT\n");
            report.append("========================\n\n");
            report.append("Total Attendance Records: ").append(stats.getOrDefault("totalAttendanceRecords", 0)).append("\n");
            report.append("Total Courses: ").append(stats.getOrDefault("totalCourses", 0)).append("\n");
            report.append("Active Courses: ").append(stats.getOrDefault("activeCourses", 0)).append("\n");
            report.append("\nThis report shows overall attendance statistics.\n");
            report.append("For detailed course-specific reports, please use the Teacher dashboard.\n");
            
            showReportDialog("Attendance Summary Report", report.toString());
        } catch (Exception e) {
            logger.error("Failed to generate attendance summary report", e);
            JOptionPane.showMessageDialog(this,
                "Failed to generate report: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Generates enrollment report.
     */
    private void generateEnrollmentReport() {
        try {
            Map<String, Object> stats = remoteService.getSystemStatistics(sessionToken);
            
            StringBuilder report = new StringBuilder();
            report.append("ENROLLMENT REPORT\n");
            report.append("=================\n\n");
            report.append("Total Students: ").append(stats.getOrDefault("totalStudents", 0)).append("\n");
            report.append("Total Courses: ").append(stats.getOrDefault("totalCourses", 0)).append("\n");
            report.append("Total Enrollments: ").append(stats.getOrDefault("totalEnrollments", 0)).append("\n");
            report.append("\nAverage Students per Course: ");
            int totalCourses = (int) stats.getOrDefault("totalCourses", 1);
            int totalEnrollments = (int) stats.getOrDefault("totalEnrollments", 0);
            report.append(totalCourses > 0 ? (totalEnrollments / totalCourses) : 0).append("\n");
            
            showReportDialog("Enrollment Report", report.toString());
        } catch (Exception e) {
            logger.error("Failed to generate enrollment report", e);
            JOptionPane.showMessageDialog(this,
                "Failed to generate report: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Generates teacher report.
     */
    private void generateTeacherReport() {
        try {
            Map<String, Object> stats = remoteService.getSystemStatistics(sessionToken);
            
            StringBuilder report = new StringBuilder();
            report.append("TEACHER REPORT\n");
            report.append("==============\n\n");
            report.append("Total Teachers: ").append(stats.getOrDefault("totalTeachers", 0)).append("\n");
            report.append("Total Courses: ").append(stats.getOrDefault("totalCourses", 0)).append("\n");
            report.append("Active Courses: ").append(stats.getOrDefault("activeCourses", 0)).append("\n");
            report.append("\nAverage Courses per Teacher: ");
            int totalTeachers = (int) stats.getOrDefault("totalTeachers", 1);
            int totalCourses = (int) stats.getOrDefault("totalCourses", 0);
            report.append(totalTeachers > 0 ? (totalCourses / totalTeachers) : 0).append("\n");
            
            showReportDialog("Teacher Report", report.toString());
        } catch (Exception e) {
            logger.error("Failed to generate teacher report", e);
            JOptionPane.showMessageDialog(this,
                "Failed to generate report: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Generates system usage report.
     */
    private void generateSystemUsageReport() {
        try {
            Map<String, Object> stats = remoteService.getSystemStatistics(sessionToken);
            
            StringBuilder report = new StringBuilder();
            report.append("SYSTEM USAGE REPORT\n");
            report.append("===================\n\n");
            report.append("Active Sessions: ").append(stats.getOrDefault("activeSessions", 0)).append("\n");
            report.append("Total Users: ").append(stats.getOrDefault("totalUsers", 0)).append("\n");
            report.append("Active Users: ").append(stats.getOrDefault("activeUsers", 0)).append("\n");
            report.append("\nSystem Status: Online\n");
            report.append("Database Status: Connected\n");
            report.append("Server Status: Running\n");
            
            showReportDialog("System Usage Report", report.toString());
        } catch (Exception e) {
            logger.error("Failed to generate system usage report", e);
            JOptionPane.showMessageDialog(this,
                "Failed to generate report: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Generates database statistics report.
     */
    private void generateDatabaseReport() {
        try {
            Map<String, Object> stats = remoteService.getSystemStatistics(sessionToken);
            
            StringBuilder report = new StringBuilder();
            report.append("DATABASE STATISTICS\n");
            report.append("===================\n\n");
            report.append("Total Users: ").append(stats.getOrDefault("totalUsers", 0)).append("\n");
            report.append("Total Students: ").append(stats.getOrDefault("totalStudents", 0)).append("\n");
            report.append("Total Teachers: ").append(stats.getOrDefault("totalTeachers", 0)).append("\n");
            report.append("Total Courses: ").append(stats.getOrDefault("totalCourses", 0)).append("\n");
            report.append("Total Enrollments: ").append(stats.getOrDefault("totalEnrollments", 0)).append("\n");
            report.append("Total Attendance Records: ").append(stats.getOrDefault("totalAttendanceRecords", 0)).append("\n");
            report.append("\nDatabase: Wolde\n");
            report.append("Tables: 7\n");
            report.append("Status: Healthy\n");
            
            showReportDialog("Database Statistics", report.toString());
        } catch (Exception e) {
            logger.error("Failed to generate database report", e);
            JOptionPane.showMessageDialog(this,
                "Failed to generate report: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Shows a report in a dialog.
     */
    private void showReportDialog(String title, String content) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), title, true);
        dialog.setLayout(new BorderLayout());
        
        JTextArea textArea = new JTextArea(content);
        textArea.setEditable(false);
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        textArea.setMargin(new Insets(10, 10, 10, 10));
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(500, 400));
        
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dialog.dispose());
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(closeButton);
        
        dialog.add(scrollPane, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
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
