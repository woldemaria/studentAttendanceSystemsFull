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
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Student-specific dashboard interface with attendance viewing, percentage display, and notifications.
 */
public class StudentDashboard extends JPanel {
    private static final Logger logger = LoggerFactory.getLogger(StudentDashboard.class);
    
    private final AttendanceGUI parentFrame;
    private final AttendanceService remoteService;
    private final String sessionToken;
    private User currentUser;
    
    // Main components
    private JTabbedPane tabbedPane;
    private JPanel dashboardPanel;
    private JPanel attendancePanel;
    private JPanel notificationsPanel;
    
    // Dashboard components
    private JLabel overallPercentageLabel;
    private JLabel totalClassesLabel;
    private JLabel attendedClassesLabel;
    private JLabel enrolledCoursesLabel;
    private JPanel courseStatsPanel;
    
    // Attendance components
    private JTable attendanceTable;
    private DefaultTableModel attendanceTableModel;
    private JComboBox<Course> courseFilterCombo;
    private JComboBox<String> periodFilterCombo;
    private JButton refreshAttendanceButton;
    
    // Notifications components
    private JList<Notification> notificationsList;
    private DefaultListModel<Notification> notificationsListModel;
    private JButton markReadButton;
    private JButton refreshNotificationsButton;
    
    public StudentDashboard(AttendanceGUI parentFrame, AttendanceService remoteService, String sessionToken) {
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
        createNotificationsPanel();
        
        // Add tabs
        tabbedPane.addTab("Dashboard", new ImageIcon(), dashboardPanel, "Overview and statistics");
        tabbedPane.addTab("My Attendance", new ImageIcon(), attendancePanel, "View attendance records");
        tabbedPane.addTab("Notifications", new ImageIcon(), notificationsPanel, "View notifications and alerts");
    }
    
    /**
     * Creates the main dashboard panel.
     */
    private void createDashboardPanel() {
        dashboardPanel = new JPanel(new BorderLayout());
        dashboardPanel.setBackground(Color.WHITE);
        
        // Title and student info panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Title
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        titlePanel.setBackground(Color.WHITE);
        
        JLabel titleLabel = new JLabel("Student Dashboard");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        titleLabel.setForeground(new Color(70, 130, 180));
        titlePanel.add(titleLabel);
        
        headerPanel.add(titlePanel, BorderLayout.NORTH);
        
        // Student info panel (show year level and class section)
        if (currentUser instanceof Student) {
            Student student = (Student) currentUser;
            JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            infoPanel.setBackground(Color.WHITE);
            
            JLabel studentInfoLabel = new JLabel(String.format(
                "Student: %s | Student Number: %s | Year & Class: %s | Program: %s",
                student.getFullName(),
                student.getStudentNumber(),
                student.getFullClassDesignation(),
                student.getProgram()
            ));
            studentInfoLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
            studentInfoLabel.setForeground(new Color(100, 100, 100));
            infoPanel.add(studentInfoLabel);
            
            headerPanel.add(infoPanel, BorderLayout.CENTER);
        }
        
        dashboardPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Main content panel
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(Color.WHITE);
        
        // Overall statistics panel
        JPanel overallStatsPanel = createOverallStatisticsPanel();
        contentPanel.add(overallStatsPanel, BorderLayout.NORTH);
        
        // Course-wise statistics panel
        courseStatsPanel = new JPanel();
        courseStatsPanel.setLayout(new BoxLayout(courseStatsPanel, BoxLayout.Y_AXIS));
        courseStatsPanel.setBackground(Color.WHITE);
        courseStatsPanel.setBorder(BorderFactory.createTitledBorder("Course-wise Attendance"));
        
        JScrollPane courseStatsScrollPane = new JScrollPane(courseStatsPanel);
        courseStatsScrollPane.setPreferredSize(new Dimension(0, 300));
        contentPanel.add(courseStatsScrollPane, BorderLayout.CENTER);
        
        dashboardPanel.add(contentPanel, BorderLayout.CENTER);
        
        // Quick actions panel
        JPanel actionsPanel = createStudentQuickActionsPanel();
        dashboardPanel.add(actionsPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Creates the overall statistics panel.
     */
    private JPanel createOverallStatisticsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 20, 20));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.WHITE);
        
        // Create statistic cards
        overallPercentageLabel = new JLabel("0%");
        panel.add(createStatCard("Overall Attendance", overallPercentageLabel, new Color(70, 130, 180)));
        
        totalClassesLabel = new JLabel("0");
        panel.add(createStatCard("Total Classes", totalClassesLabel, new Color(34, 139, 34)));
        
        attendedClassesLabel = new JLabel("0");
        panel.add(createStatCard("Classes Attended", attendedClassesLabel, new Color(255, 140, 0)));
        
        enrolledCoursesLabel = new JLabel("0");
        panel.add(createStatCard("Enrolled Courses", enrolledCoursesLabel, new Color(220, 20, 60)));
        
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
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        titleLabel.setForeground(color);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        valueLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        valueLabel.setForeground(color);
        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        
        return card;
    }
    
    /**
     * Creates the student quick actions panel.
     */
    private JPanel createStudentQuickActionsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder("Quick Actions"));
        
        JButton viewAttendanceButton = new JButton("View Detailed Attendance");
        viewAttendanceButton.setPreferredSize(new Dimension(180, 35));
        viewAttendanceButton.addActionListener(e -> {
            tabbedPane.setSelectedIndex(1); // Switch to attendance tab
        });
        
        JButton viewNotificationsButton = new JButton("View Notifications");
        viewNotificationsButton.setPreferredSize(new Dimension(150, 35));
        viewNotificationsButton.addActionListener(e -> {
            tabbedPane.setSelectedIndex(2); // Switch to notifications tab
        });
        
        JButton refreshButton = new JButton("Refresh Data");
        refreshButton.setPreferredSize(new Dimension(120, 35));
        refreshButton.addActionListener(e -> loadInitialData());
        
        panel.add(viewAttendanceButton);
        panel.add(viewNotificationsButton);
        panel.add(refreshButton);
        
        return panel;
    }
    
    /**
     * Creates the attendance viewing panel.
     */
    private void createAttendancePanel() {
        attendancePanel = new JPanel(new BorderLayout());
        attendancePanel.setBackground(Color.WHITE);
        
        // Create filter panel
        JPanel filterPanel = createAttendanceFilterPanel();
        attendancePanel.add(filterPanel, BorderLayout.NORTH);
        
        // Create attendance table
        createAttendanceTable();
        JScrollPane scrollPane = new JScrollPane(attendanceTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Attendance Records"));
        attendancePanel.add(scrollPane, BorderLayout.CENTER);
        
        // Create summary panel
        JPanel summaryPanel = createAttendanceSummaryPanel();
        attendancePanel.add(summaryPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Creates the attendance filter panel.
     */
    private JPanel createAttendanceFilterPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Course filter
        JLabel courseLabel = new JLabel("Course:");
        courseFilterCombo = new JComboBox<>();
        courseFilterCombo.setPreferredSize(new Dimension(200, 30));
        courseFilterCombo.addActionListener(e -> filterAttendanceRecords());
        
        // Period filter
        JLabel periodLabel = new JLabel("Period:");
        periodFilterCombo = new JComboBox<>();
        periodFilterCombo.addItem("All Time");
        periodFilterCombo.addItem("This Month");
        periodFilterCombo.addItem("Last 30 Days");
        periodFilterCombo.addItem("This Semester");
        periodFilterCombo.setPreferredSize(new Dimension(120, 30));
        periodFilterCombo.addActionListener(e -> filterAttendanceRecords());
        
        // Refresh button
        refreshAttendanceButton = new JButton("Refresh");
        refreshAttendanceButton.addActionListener(e -> loadAttendanceRecords());
        
        panel.add(courseLabel);
        panel.add(courseFilterCombo);
        panel.add(Box.createHorizontalStrut(20));
        panel.add(periodLabel);
        panel.add(periodFilterCombo);
        panel.add(Box.createHorizontalStrut(20));
        panel.add(refreshAttendanceButton);
        
        return panel;
    }
    
    /**
     * Creates the attendance table.
     */
    private void createAttendanceTable() {
        String[] columnNames = {"Date", "Course", "Class Time", "Status", "Remarks"};
        attendanceTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        attendanceTable = new JTable(attendanceTableModel);
        attendanceTable.setRowHeight(25);
        attendanceTable.getTableHeader().setReorderingAllowed(false);
        
        // Set up status column with custom renderer
        attendanceTable.getColumnModel().getColumn(3).setCellRenderer(new AttendanceStatusRenderer());
        
        // Set column widths
        attendanceTable.getColumnModel().getColumn(0).setPreferredWidth(100);
        attendanceTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        attendanceTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        attendanceTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        attendanceTable.getColumnModel().getColumn(4).setPreferredWidth(200);
    }
    
    /**
     * Creates the attendance summary panel.
     */
    private JPanel createAttendanceSummaryPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder("Summary"));
        
        // Summary will be updated when records are loaded
        
        return panel;
    }
    
    /**
     * Creates the notifications panel.
     */
    private void createNotificationsPanel() {
        notificationsPanel = new JPanel(new BorderLayout());
        notificationsPanel.setBackground(Color.WHITE);
        
        // Create notifications list
        notificationsListModel = new DefaultListModel<>();
        notificationsList = new JList<>(notificationsListModel);
        notificationsList.setCellRenderer(new NotificationListCellRenderer());
        notificationsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JScrollPane scrollPane = new JScrollPane(notificationsList);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Notifications"));
        notificationsPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Create button panel
        JPanel buttonPanel = createNotificationsButtonPanel();
        notificationsPanel.add(buttonPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Creates the notifications button panel.
     */
    private JPanel createNotificationsButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        panel.setBackground(Color.WHITE);
        
        markReadButton = new JButton("Mark as Read");
        markReadButton.setPreferredSize(new Dimension(120, 35));
        markReadButton.setEnabled(false);
        markReadButton.addActionListener(e -> markNotificationAsRead());
        
        refreshNotificationsButton = new JButton("Refresh");
        refreshNotificationsButton.setPreferredSize(new Dimension(100, 35));
        refreshNotificationsButton.addActionListener(e -> loadNotifications());
        
        JButton markAllReadButton = new JButton("Mark All Read");
        markAllReadButton.setPreferredSize(new Dimension(120, 35));
        markAllReadButton.addActionListener(e -> markAllNotificationsAsRead());
        
        panel.add(markReadButton);
        panel.add(refreshNotificationsButton);
        panel.add(markAllReadButton);
        
        return panel;
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
        // Notifications list selection listener
        notificationsList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                boolean hasSelection = notificationsList.getSelectedIndex() != -1;
                markReadButton.setEnabled(hasSelection);
            }
        });
        
        // Tab change listener
        tabbedPane.addChangeListener(e -> {
            int selectedIndex = tabbedPane.getSelectedIndex();
            if (selectedIndex == 1) { // Attendance tab
                loadStudentCourses();
            } else if (selectedIndex == 2) { // Notifications tab
                loadNotifications();
            }
        });
    }
    
    /**
     * Loads initial data.
     */
    private void loadInitialData() {
        loadStudentStatistics();
        loadStudentCourses();
        loadNotifications();
    }
    
    /**
     * Loads student statistics.
     */
    private void loadStudentStatistics() {
        CompletableFuture.supplyAsync(() -> {
            try {
                // Get student's courses
                List<Course> courses = remoteService.getCoursesByStudent(sessionToken, currentUser.getUserId());
                
                // Get attendance statistics for each course
                int totalClasses = 0;
                int attendedClasses = 0;
                
                for (Course course : courses) {
                    Map<String, Object> stats = remoteService.getAttendanceStatistics(
                            sessionToken, currentUser.getUserId(), course.getCourseId());
                    
                    totalClasses += (Integer) stats.getOrDefault("totalClasses", 0);
                    attendedClasses += (Integer) stats.getOrDefault("attendedClasses", 0);
                }
                
                return new StudentStats(courses.size(), totalClasses, attendedClasses, courses);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).thenAccept(stats -> {
            SwingUtilities.invokeLater(() -> {
                updateStudentStatistics(stats);
            });
        }).exceptionally(throwable -> {
            SwingUtilities.invokeLater(() -> {
                logger.error("Failed to load student statistics", throwable);
                parentFrame.showErrorDialog("Error", "Failed to load statistics: " + throwable.getMessage());
            });
            return null;
        });
    }
    
    /**
     * Updates student statistics display.
     */
    private void updateStudentStatistics(StudentStats stats) {
        enrolledCoursesLabel.setText(String.valueOf(stats.enrolledCourses));
        totalClassesLabel.setText(String.valueOf(stats.totalClasses));
        attendedClassesLabel.setText(String.valueOf(stats.attendedClasses));
        
        double percentage = stats.totalClasses > 0 ? 
                (double) stats.attendedClasses / stats.totalClasses * 100 : 0;
        overallPercentageLabel.setText(String.format("%.1f%%", percentage));
        
        // Update course-wise statistics
        updateCourseStatistics(stats.courses);
    }
    
    /**
     * Updates course-wise statistics panel.
     */
    private void updateCourseStatistics(List<Course> courses) {
        courseStatsPanel.removeAll();
        
        for (Course course : courses) {
            JPanel coursePanel = createCourseStatPanel(course);
            courseStatsPanel.add(coursePanel);
            courseStatsPanel.add(Box.createVerticalStrut(10));
        }
        
        courseStatsPanel.revalidate();
        courseStatsPanel.repaint();
    }
    
    /**
     * Creates a course statistics panel.
     */
    private JPanel createCourseStatPanel(Course course) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        panel.setBackground(Color.WHITE);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        
        JLabel courseLabel = new JLabel(course.getFullName());
        courseLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        courseLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        
        JLabel statsLabel = new JLabel("Loading...");
        statsLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        statsLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        
        panel.add(courseLabel, BorderLayout.NORTH);
        panel.add(statsLabel, BorderLayout.CENTER);
        
        // Load course statistics asynchronously
        CompletableFuture.supplyAsync(() -> {
            try {
                return remoteService.getAttendanceStatistics(sessionToken, currentUser.getUserId(), course.getCourseId());
            } catch (Exception e) {
                return null;
            }
        }).thenAccept(stats -> {
            SwingUtilities.invokeLater(() -> {
                if (stats != null) {
                    int total = (Integer) stats.getOrDefault("totalClasses", 0);
                    int attended = (Integer) stats.getOrDefault("attendedClasses", 0);
                    double percentage = total > 0 ? (double) attended / total * 100 : 0;
                    
                    statsLabel.setText(String.format("Attendance: %d/%d (%.1f%%)", attended, total, percentage));
                    
                    // Color code based on percentage
                    if (percentage >= 75) {
                        statsLabel.setForeground(new Color(0, 128, 0));
                    } else if (percentage >= 60) {
                        statsLabel.setForeground(new Color(255, 140, 0));
                    } else {
                        statsLabel.setForeground(Color.RED);
                    }
                } else {
                    statsLabel.setText("Unable to load statistics");
                    statsLabel.setForeground(Color.GRAY);
                }
            });
        });
        
        return panel;
    }
    
    /**
     * Loads student courses for filtering.
     */
    private void loadStudentCourses() {
        CompletableFuture.supplyAsync(() -> {
            try {
                return remoteService.getCoursesByStudent(sessionToken, currentUser.getUserId());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).thenAccept(courses -> {
            SwingUtilities.invokeLater(() -> {
                updateCourseFilterComboBox(courses);
                loadAttendanceRecords();
            });
        }).exceptionally(throwable -> {
            SwingUtilities.invokeLater(() -> {
                logger.error("Failed to load student courses", throwable);
                parentFrame.showErrorDialog("Error", "Failed to load courses: " + throwable.getMessage());
            });
            return null;
        });
    }
    
    /**
     * Updates the course filter combo box.
     */
    private void updateCourseFilterComboBox(List<Course> courses) {
        courseFilterCombo.removeAllItems();
        courseFilterCombo.addItem(null); // All courses option
        
        for (Course course : courses) {
            courseFilterCombo.addItem(course);
        }
    }
    
    /**
     * Loads attendance records.
     */
    private void loadAttendanceRecords() {
        parentFrame.showProgress(true);
        
        CompletableFuture.supplyAsync(() -> {
            try {
                return remoteService.getAttendanceRecords(sessionToken, currentUser.getUserId(), null, null);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).thenAccept(records -> {
            SwingUtilities.invokeLater(() -> {
                updateAttendanceTable(records);
                parentFrame.showProgress(false);
            });
        }).exceptionally(throwable -> {
            SwingUtilities.invokeLater(() -> {
                parentFrame.showProgress(false);
                logger.error("Failed to load attendance records", throwable);
                parentFrame.showErrorDialog("Error", "Failed to load attendance records: " + throwable.getMessage());
            });
            return null;
        });
    }
    
    /**
     * Updates the attendance table.
     */
    private void updateAttendanceTable(List<AttendanceRecord> records) {
        attendanceTableModel.setRowCount(0);
        
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        
        for (AttendanceRecord record : records) {
            Object[] row = {
                record.getAttendanceDate().format(dateFormatter),
                record.getCourse() != null ? record.getCourse().getFullName() : "Unknown Course",
                record.getClassTime() != null ? record.getClassTime().format(timeFormatter) : "",
                record.getStatus(),
                record.getRemarks() != null ? record.getRemarks() : ""
            };
            attendanceTableModel.addRow(row);
        }
    }
    
    /**
     * Filters attendance records based on selected criteria.
     */
    private void filterAttendanceRecords() {
        // Implementation for filtering would go here
        // For now, just reload all records
        loadAttendanceRecords();
    }
    
    /**
     * Loads notifications.
     */
    private void loadNotifications() {
        CompletableFuture.supplyAsync(() -> {
            try {
                return remoteService.getNotifications(sessionToken, currentUser.getUserId(), false);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).thenAccept(notifications -> {
            SwingUtilities.invokeLater(() -> {
                updateNotificationsList(notifications);
            });
        }).exceptionally(throwable -> {
            SwingUtilities.invokeLater(() -> {
                logger.error("Failed to load notifications", throwable);
                parentFrame.showErrorDialog("Error", "Failed to load notifications: " + throwable.getMessage());
            });
            return null;
        });
    }
    
    /**
     * Updates the notifications list.
     */
    private void updateNotificationsList(List<Notification> notifications) {
        notificationsListModel.clear();
        for (Notification notification : notifications) {
            notificationsListModel.addElement(notification);
        }
    }
    
    /**
     * Marks selected notification as read.
     */
    private void markNotificationAsRead() {
        Notification selected = notificationsList.getSelectedValue();
        if (selected != null) {
            CompletableFuture.runAsync(() -> {
                try {
                    remoteService.markNotificationAsRead(sessionToken, selected.getNotificationId());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }).thenRun(() -> {
                SwingUtilities.invokeLater(() -> {
                    loadNotifications(); // Refresh the list
                });
            }).exceptionally(throwable -> {
                SwingUtilities.invokeLater(() -> {
                    logger.error("Failed to mark notification as read", throwable);
                    parentFrame.showErrorDialog("Error", "Failed to mark notification as read: " + throwable.getMessage());
                });
                return null;
            });
        }
    }
    
    /**
     * Marks all notifications as read.
     */
    private void markAllNotificationsAsRead() {
        boolean confirmed = parentFrame.showConfirmDialog(
            "Confirm Mark All as Read",
            "Are you sure you want to mark all notifications as read?"
        );
        
        if (confirmed) {
            parentFrame.showProgress(true);
            
            CompletableFuture.supplyAsync(() -> {
                try {
                    return remoteService.markAllNotificationsAsRead(sessionToken, currentUser.getUserId());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }).thenAccept(count -> {
                SwingUtilities.invokeLater(() -> {
                    parentFrame.showProgress(false);
                    parentFrame.showInfoDialog("Success", count + " notification(s) marked as read");
                    loadNotifications();
                });
            }).exceptionally(throwable -> {
                SwingUtilities.invokeLater(() -> {
                    parentFrame.showProgress(false);
                    logger.error("Failed to mark all notifications as read", throwable);
                    parentFrame.showErrorDialog("Error", "Failed to mark all notifications as read: " + throwable.getMessage());
                });
                return null;
            });
        }
    }
    
    /**
     * Custom renderer for attendance status in table.
     */
    private static class AttendanceStatusRenderer extends DefaultTableCellRenderer implements TableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            if (value instanceof AttendanceStatus) {
                AttendanceStatus status = (AttendanceStatus) value;
                setText(status.getDisplayName());
                
                if (!isSelected) {
                    switch (status) {
                        case PRESENT:
                            setBackground(new Color(220, 255, 220));
                            break;
                        case ABSENT:
                            setBackground(new Color(255, 220, 220));
                            break;
                        case LATE:
                            setBackground(new Color(255, 255, 220));
                            break;
                        case EXCUSED:
                            setBackground(new Color(220, 220, 255));
                            break;
                        default:
                            setBackground(Color.WHITE);
                    }
                }
            }
            
            return c;
        }
    }
    
    /**
     * Custom renderer for notifications list.
     */
    private static class NotificationListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            
            if (value instanceof Notification) {
                Notification notification = (Notification) value;
                
                String text = String.format("<html><b>%s</b><br/>%s<br/><small>%s</small></html>",
                        notification.getTitle(),
                        notification.getMessage(),
                        notification.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
                
                setText(text);
                
                if (!notification.isRead() && !isSelected) {
                    setBackground(new Color(240, 248, 255));
                    setFont(getFont().deriveFont(Font.BOLD));
                }
            }
            
            return this;
        }
    }
    
    /**
     * Helper class for student statistics.
     */
    private static class StudentStats {
        final int enrolledCourses;
        final int totalClasses;
        final int attendedClasses;
        final List<Course> courses;
        
        StudentStats(int enrolledCourses, int totalClasses, int attendedClasses, List<Course> courses) {
            this.enrolledCourses = enrolledCourses;
            this.totalClasses = totalClasses;
            this.attendedClasses = attendedClasses;
            this.courses = courses;
        }
    }
}