package com.attendance.system.client;

import com.attendance.system.model.*;
import com.attendance.system.service.AttendanceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Teacher-specific dashboard interface with class management, attendance marking, and class reports.
 */
public class TeacherDashboard extends JPanel {
    private static final Logger logger = LoggerFactory.getLogger(TeacherDashboard.class);
    
    private final AttendanceGUI parentFrame;
    private final AttendanceService remoteService;
    private final String sessionToken;
    private User currentUser;
    
    // Main components
    private JTabbedPane tabbedPane;
    private JPanel dashboardPanel;
    private JPanel attendancePanel;
    private JPanel reportsPanel;
    
    // Dashboard components
    private JLabel totalCoursesLabel;
    private JLabel totalStudentsLabel;
    private JLabel todayClassesLabel;
    private JComboBox<Course> courseComboBox;
    

    
    public TeacherDashboard(AttendanceGUI parentFrame, AttendanceService remoteService, String sessionToken) {
        this.parentFrame = parentFrame;
        this.remoteService = remoteService;
        this.sessionToken = sessionToken;
        
        // Get current user
        try {
            this.currentUser = remoteService.validateSession(sessionToken);
        } catch (Exception e) {
            logger.error("Failed to validate session", e);
            parentFrame.handleLogout();
            return;
        }
        
        initializeComponents();
        setupLayout();
        setupEventHandlers();
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
        createAttendancePanel();
        createReportsPanel();
        
        // Add tabs
        tabbedPane.addTab("Dashboard", new ImageIcon(), dashboardPanel, "Overview and quick actions");
        tabbedPane.addTab("Mark Attendance", new ImageIcon(), attendancePanel, "Mark student attendance");
        tabbedPane.addTab("Reports", new ImageIcon(), reportsPanel, "Generate class reports");
    }
    
    /**
     * Creates the main dashboard panel.
     */
    private void createDashboardPanel() {
        dashboardPanel = new JPanel(new BorderLayout());
        dashboardPanel.setBackground(Color.WHITE);
        
        // Title panel
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        titlePanel.setBackground(Color.WHITE);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel titleLabel = new JLabel("Teacher Dashboard");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        titleLabel.setForeground(new Color(70, 130, 180));
        titlePanel.add(titleLabel);
        
        dashboardPanel.add(titlePanel, BorderLayout.NORTH);
        
        // Statistics panel
        JPanel statsPanel = createTeacherStatisticsPanel();
        dashboardPanel.add(statsPanel, BorderLayout.CENTER);
        
        // Quick actions panel
        JPanel actionsPanel = createTeacherQuickActionsPanel();
        dashboardPanel.add(actionsPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Creates the teacher statistics panel.
     */
    private JPanel createTeacherStatisticsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 3, 20, 20));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.WHITE);
        
        // Create statistic cards
        totalCoursesLabel = new JLabel("0");
        panel.add(createStatCard("My Courses", totalCoursesLabel, new Color(70, 130, 180)));
        
        totalStudentsLabel = new JLabel("0");
        panel.add(createStatCard("Total Students", totalStudentsLabel, new Color(34, 139, 34)));
        
        todayClassesLabel = new JLabel("0");
        panel.add(createStatCard("Today's Classes", todayClassesLabel, new Color(255, 140, 0)));
        
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
     * Creates the teacher quick actions panel.
     */
    private JPanel createTeacherQuickActionsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder("Quick Actions"));
        
        JButton markTodayButton = new JButton("Mark Today's Attendance");
        markTodayButton.setPreferredSize(new Dimension(180, 35));
        markTodayButton.addActionListener(e -> {
            tabbedPane.setSelectedIndex(1); // Switch to attendance tab
        });
        
        JButton viewReportsButton = new JButton("View Class Reports");
        viewReportsButton.setPreferredSize(new Dimension(150, 35));
        viewReportsButton.addActionListener(e -> {
            tabbedPane.setSelectedIndex(2); // Switch to reports tab
        });
        
        JButton refreshButton = new JButton("Refresh Data");
        refreshButton.setPreferredSize(new Dimension(120, 35));
        refreshButton.addActionListener(e -> loadInitialData());
        
        panel.add(markTodayButton);
        panel.add(viewReportsButton);
        panel.add(refreshButton);
        
        return panel;
    }
    
    /**
     * Creates the attendance marking panel.
     */
    private void createAttendancePanel() {
        attendancePanel = new AttendanceMarkingPanel(parentFrame, remoteService, sessionToken);
    }
    
    /**
     * Creates the reports panel.
     */
    private void createReportsPanel() {
        reportsPanel = new JPanel(new BorderLayout());
        reportsPanel.setBackground(Color.WHITE);
        
        JLabel label = new JLabel("Class Reports - Coming Soon", SwingConstants.CENTER);
        label.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        label.setForeground(Color.GRAY);
        
        reportsPanel.add(label, BorderLayout.CENTER);
    }
    
    /**
     * Sets up the layout.
     */
    private void setupLayout() {
        add(tabbedPane, BorderLayout.CENTER);
        
        // Create header panel with user info and logout button
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(70, 130, 180));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel welcomeLabel = new JLabel("Welcome, " + currentUser.getFullName());
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
     * Sets up event handlers.
     */
    private void setupEventHandlers() {
        // Tab change listener
        tabbedPane.addChangeListener(e -> {
            int selectedIndex = tabbedPane.getSelectedIndex();
            if (selectedIndex == 1) { // Attendance tab
                // AttendanceMarkingPanel handles its own loading
            }
        });
    }
    
    /**
     * Loads initial data.
     */
    private void loadInitialData() {
        loadTeacherStatistics();
    }
    
    /**
     * Loads teacher statistics.
     */
    private void loadTeacherStatistics() {
        CompletableFuture.supplyAsync(() -> {
            try {
                return remoteService.getCoursesByTeacher(sessionToken, currentUser.getUserId());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).thenAccept(courses -> {
            SwingUtilities.invokeLater(() -> {
                updateTeacherStatistics(courses);
            });
        }).exceptionally(throwable -> {
            SwingUtilities.invokeLater(() -> {
                logger.error("Failed to load teacher statistics", throwable);
                parentFrame.showErrorDialog("Error", "Failed to load statistics: " + throwable.getMessage());
            });
            return null;
        });
    }
    
    /**
     * Updates teacher statistics.
     */
    private void updateTeacherStatistics(List<Course> courses) {
        totalCoursesLabel.setText(String.valueOf(courses.size()));
        
        // Calculate total students across all courses
        int totalStudents = courses.stream()
                .mapToInt(Course::getEnrollmentCount)
                .sum();
        totalStudentsLabel.setText(String.valueOf(totalStudents));
        
        // For now, set today's classes to course count
        todayClassesLabel.setText(String.valueOf(courses.size()));
    }
}