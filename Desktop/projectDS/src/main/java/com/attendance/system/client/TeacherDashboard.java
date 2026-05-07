package com.attendance.system.client;

import com.attendance.system.model.*;
import com.attendance.system.service.AttendanceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
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
        createCourseManagementPanel();
        createReportsPanel();
        
        // Add tabs
        tabbedPane.addTab("Dashboard", new ImageIcon(), dashboardPanel, "Overview and quick actions");
        tabbedPane.addTab("Mark Attendance", new ImageIcon(), attendancePanel, "Mark student attendance");
        tabbedPane.addTab("My Courses", new ImageIcon(), createCourseTabPanel(), "Manage my courses and view enrollments");
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
     * Creates the course management panel for teachers.
     */
    private void createCourseManagementPanel() {
        // This will be used in createCourseTabPanel()
    }
    
    /**
     * Creates the course tab panel with course info and notifications.
     */
    private JPanel createCourseTabPanel() {
        JTabbedPane courseTabPane = new JTabbedPane();
        
        // My Courses tab - shows courses assigned to this teacher
        JPanel myCoursesPanel = createMyCoursesPanel();
        courseTabPane.addTab("My Courses", myCoursesPanel);
        
        // Notifications tab
        NotificationPanel notificationPanel = new NotificationPanel(parentFrame, remoteService, sessionToken, currentUser);
        courseTabPane.addTab("Notifications", notificationPanel);
        
        JPanel container = new JPanel(new BorderLayout());
        container.add(courseTabPane, BorderLayout.CENTER);
        
        return container;
    }
    
    /**
     * Creates the my courses panel for teachers.
     */
    private JPanel createMyCoursesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        
        // Title
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        titlePanel.setBackground(Color.WHITE);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        
        JLabel titleLabel = new JLabel("My Courses");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        titleLabel.setForeground(new Color(70, 130, 180));
        titlePanel.add(titleLabel);
        
        panel.add(titlePanel, BorderLayout.NORTH);
        
        // Course list
        DefaultListModel<Course> listModel = new DefaultListModel<>();
        JList<Course> courseList = new JList<>(listModel);
        courseList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        courseList.setCellRenderer(new CourseCellRenderer());
        
        // Load teacher's courses
        CompletableFuture.supplyAsync(() -> {
            try {
                return remoteService.getCoursesByTeacher(sessionToken, currentUser.getUserId());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).thenAccept(courses -> {
            SwingUtilities.invokeLater(() -> {
                listModel.clear();
                for (Course course : courses) {
                    listModel.addElement(course);
                }
            });
        }).exceptionally(throwable -> {
            SwingUtilities.invokeLater(() -> {
                logger.error("Failed to load teacher courses", throwable);
            });
            return null;
        });
        
        JScrollPane scrollPane = new JScrollPane(courseList);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Course details panel
        JPanel detailsPanel = new JPanel(new BorderLayout());
        detailsPanel.setBorder(BorderFactory.createTitledBorder("Course Details"));
        detailsPanel.setPreferredSize(new Dimension(300, 0));
        
        JTextArea detailsArea = new JTextArea();
        detailsArea.setEditable(false);
        detailsArea.setBackground(Color.WHITE);
        detailsArea.setText("Select a course to view details");
        
        courseList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Course selectedCourse = courseList.getSelectedValue();
                if (selectedCourse != null) {
                    StringBuilder details = new StringBuilder();
                    details.append("Course Code: ").append(selectedCourse.getCourseCode()).append("\n");
                    details.append("Course Name: ").append(selectedCourse.getCourseName()).append("\n");
                    details.append("Credits: ").append(selectedCourse.getCredits()).append("\n");
                    details.append("Semester: ").append(selectedCourse.getSemester()).append("\n");
                    details.append("Academic Year: ").append(selectedCourse.getAcademicYear()).append("\n");
                    details.append("Status: ").append(selectedCourse.isActive() ? "Active" : "Inactive").append("\n");
                    
                    // Load enrollment count
                    CompletableFuture.supplyAsync(() -> {
                        try {
                            return remoteService.getEnrolledStudents(sessionToken, selectedCourse.getCourseId());
                        } catch (Exception ex) {
                            return List.<Student>of();
                        }
                    }).thenAccept(students -> {
                        SwingUtilities.invokeLater(() -> {
                            details.append("Enrolled Students: ").append(students.size());
                            detailsArea.setText(details.toString());
                        });
                    });
                } else {
                    detailsArea.setText("Select a course to view details");
                }
            }
        });
        
        detailsPanel.add(new JScrollPane(detailsArea), BorderLayout.CENTER);
        panel.add(detailsPanel, BorderLayout.EAST);
        
        return panel;
    }
    
    /**
     * Creates the reports panel.
     */
    private void createReportsPanel() {
        reportsPanel = new JPanel(new BorderLayout());
        reportsPanel.setBackground(Color.WHITE);
        
        // Title panel
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        titlePanel.setBackground(Color.WHITE);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel titleLabel = new JLabel("Class Reports");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        titleLabel.setForeground(new Color(70, 130, 180));
        titlePanel.add(titleLabel);
        
        reportsPanel.add(titlePanel, BorderLayout.NORTH);
        
        // Main content panel
        JPanel contentPanel = new JPanel(new GridLayout(2, 2, 20, 20));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        contentPanel.setBackground(Color.WHITE);
        
        // Course Attendance Summary
        JPanel attendanceSummaryCard = createReportCard(
            "Course Attendance Summary",
            "View attendance statistics for all your courses",
            new Color(70, 130, 180),
            e -> generateCourseAttendanceSummary()
        );
        contentPanel.add(attendanceSummaryCard);
        
        // Student Attendance Report
        JPanel studentReportCard = createReportCard(
            "Student Attendance Report",
            "Detailed attendance report for individual students",
            new Color(34, 139, 34),
            e -> generateStudentAttendanceReport()
        );
        contentPanel.add(studentReportCard);
        
        // Class Performance Report
        JPanel performanceCard = createReportCard(
            "Class Performance",
            "Overall class attendance performance and trends",
            new Color(255, 140, 0),
            e -> generateClassPerformanceReport()
        );
        contentPanel.add(performanceCard);
        
        // My Courses Report
        JPanel myCoursesCard = createReportCard(
            "My Courses Report",
            "Summary of all courses you are teaching",
            new Color(220, 20, 60),
            e -> generateMyCoursesReport()
        );
        contentPanel.add(myCoursesCard);
        
        reportsPanel.add(contentPanel, BorderLayout.CENTER);
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
     * Generates course attendance summary report.
     */
    private void generateCourseAttendanceSummary() {
        try {
            // Get teacher's courses
            java.util.List<Course> courses = remoteService.getCoursesByTeacher(
                sessionToken, parentFrame.getCurrentUser().getUser().getUserId());
            
            StringBuilder report = new StringBuilder();
            report.append("COURSE ATTENDANCE SUMMARY\n");
            report.append("========================\n\n");
            report.append("Teacher: ").append(parentFrame.getCurrentUser().getUser().getFullName()).append("\n");
            report.append("Total Courses: ").append(courses.size()).append("\n\n");
            
            for (Course course : courses) {
                report.append("Course: ").append(course.getCourseName()).append(" (").append(course.getCourseCode()).append(")\n");
                report.append("Semester: ").append(course.getSemester()).append("\n");
                report.append("Academic Year: ").append(course.getAcademicYear()).append("\n");
                report.append("Status: ").append(course.isActive() ? "Active" : "Inactive").append("\n");
                report.append("---\n");
            }
            
            showReportDialog("Course Attendance Summary", report.toString());
        } catch (Exception e) {
            logger.error("Failed to generate course attendance summary", e);
            JOptionPane.showMessageDialog(this,
                "Failed to generate report: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Generates student attendance report.
     */
    private void generateStudentAttendanceReport() {
        try {
            // Get teacher's courses
            java.util.List<Course> courses = remoteService.getCoursesByTeacher(
                sessionToken, parentFrame.getCurrentUser().getUser().getUserId());
            
            if (courses.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "You are not assigned to any courses yet.",
                    "No Courses", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            
            // Let teacher select a course
            Course selectedCourse = (Course) JOptionPane.showInputDialog(
                this,
                "Select a course:",
                "Student Attendance Report",
                JOptionPane.QUESTION_MESSAGE,
                null,
                courses.toArray(),
                courses.get(0)
            );
            
            if (selectedCourse != null) {
                // Get enrolled students
                java.util.List<Student> students = remoteService.getEnrolledStudents(
                    sessionToken, selectedCourse.getCourseId());
                
                StringBuilder report = new StringBuilder();
                report.append("STUDENT ATTENDANCE REPORT\n");
                report.append("========================\n\n");
                report.append("Course: ").append(selectedCourse.getCourseName()).append("\n");
                report.append("Course Code: ").append(selectedCourse.getCourseCode()).append("\n");
                report.append("Total Students: ").append(students.size()).append("\n\n");
                
                for (Student student : students) {
                    report.append("Student: ").append(student.getFullName()).append("\n");
                    report.append("Student Number: ").append(student.getStudentNumber()).append("\n");
                    report.append("Year: ").append(student.getYearLevel()).append("\n");
                    report.append("Class: ").append(student.getClassSection()).append("\n");
                    report.append("---\n");
                }
                
                showReportDialog("Student Attendance Report", report.toString());
            }
        } catch (Exception e) {
            logger.error("Failed to generate student attendance report", e);
            JOptionPane.showMessageDialog(this,
                "Failed to generate report: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Generates class performance report.
     */
    private void generateClassPerformanceReport() {
        try {
            // Get teacher's courses
            java.util.List<Course> courses = remoteService.getCoursesByTeacher(
                sessionToken, parentFrame.getCurrentUser().getUser().getUserId());
            
            StringBuilder report = new StringBuilder();
            report.append("CLASS PERFORMANCE REPORT\n");
            report.append("=======================\n\n");
            report.append("Teacher: ").append(parentFrame.getCurrentUser().getUser().getFullName()).append("\n");
            report.append("Total Courses: ").append(courses.size()).append("\n\n");
            
            int totalStudents = 0;
            for (Course course : courses) {
                java.util.List<Student> students = remoteService.getEnrolledStudents(
                    sessionToken, course.getCourseId());
                totalStudents += students.size();
                
                report.append("Course: ").append(course.getCourseName()).append("\n");
                report.append("Enrolled Students: ").append(students.size()).append("\n");
                report.append("Status: ").append(course.isActive() ? "Active" : "Inactive").append("\n");
                report.append("---\n");
            }
            
            report.append("\nTotal Students Across All Courses: ").append(totalStudents).append("\n");
            
            showReportDialog("Class Performance Report", report.toString());
        } catch (Exception e) {
            logger.error("Failed to generate class performance report", e);
            JOptionPane.showMessageDialog(this,
                "Failed to generate report: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Generates my courses report.
     */
    private void generateMyCoursesReport() {
        try {
            // Get teacher's courses
            java.util.List<Course> courses = remoteService.getCoursesByTeacher(
                sessionToken, parentFrame.getCurrentUser().getUser().getUserId());
            
            StringBuilder report = new StringBuilder();
            report.append("MY COURSES REPORT\n");
            report.append("=================\n\n");
            report.append("Teacher: ").append(parentFrame.getCurrentUser().getUser().getFullName()).append("\n");
            report.append("Total Courses: ").append(courses.size()).append("\n\n");
            
            for (Course course : courses) {
                report.append("Course Name: ").append(course.getCourseName()).append("\n");
                report.append("Course Code: ").append(course.getCourseCode()).append("\n");
                report.append("Credits: ").append(course.getCredits()).append("\n");
                report.append("Semester: ").append(course.getSemester()).append("\n");
                report.append("Academic Year: ").append(course.getAcademicYear()).append("\n");
                report.append("Status: ").append(course.isActive() ? "Active" : "Inactive").append("\n");
                
                // Get enrolled students count
                java.util.List<Student> students = remoteService.getEnrolledStudents(
                    sessionToken, course.getCourseId());
                report.append("Enrolled Students: ").append(students.size()).append("\n");
                report.append("---\n\n");
            }
            
            showReportDialog("My Courses Report", report.toString());
        } catch (Exception e) {
            logger.error("Failed to generate my courses report", e);
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
    
    /**
     * Custom cell renderer for course list.
     */
    private static class CourseCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                    boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            
            if (value instanceof Course) {
                Course course = (Course) value;
                setText(course.getCourseCode() + " - " + course.getCourseName());
                
                if (!course.isActive() && !isSelected) {
                    setForeground(Color.GRAY);
                }
            }
            
            return this;
        }
    }
}