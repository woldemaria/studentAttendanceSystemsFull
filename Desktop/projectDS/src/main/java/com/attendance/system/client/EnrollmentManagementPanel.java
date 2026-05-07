package com.attendance.system.client;

import com.attendance.system.model.*;
import com.attendance.system.service.AttendanceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Enrollment Management Panel for administrators to manage student enrollments.
 * Provides functionality to enroll/drop students from courses.
 */
public class EnrollmentManagementPanel extends JPanel {
    private static final Logger logger = LoggerFactory.getLogger(EnrollmentManagementPanel.class);
    
    private final AttendanceGUI parentFrame;
    private final AttendanceService remoteService;
    private final String sessionToken;
    
    // Table components
    private JTable enrollmentTable;
    private DefaultTableModel enrollmentTableModel;
    
    // Control buttons
    private JButton enrollStudentButton;
    private JButton dropStudentButton;
    private JButton viewEnrollmentsButton;
    private JButton refreshButton;
    
    // Filter components
    private JComboBox<Course> courseFilter;
    private JComboBox<String> statusFilter;
    private JTextField studentSearchField;
    
    public EnrollmentManagementPanel(AttendanceGUI parentFrame, AttendanceService remoteService, String sessionToken) {
        this.parentFrame = parentFrame;
        this.remoteService = remoteService;
        this.sessionToken = sessionToken;
        
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
        
        // Create enrollment table
        String[] columnNames = {"Enrollment ID", "Student Name", "Student Number", "Course Code", "Course Name", "Status", "Enrollment Date", "Grade"};
        enrollmentTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table read-only
            }
        };
        
        enrollmentTable = new JTable(enrollmentTableModel);
        enrollmentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        enrollmentTable.setRowHeight(25);
        enrollmentTable.getTableHeader().setReorderingAllowed(false);
        
        // Set column widths
        enrollmentTable.getColumnModel().getColumn(0).setPreferredWidth(100);
        enrollmentTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        enrollmentTable.getColumnModel().getColumn(2).setPreferredWidth(120);
        enrollmentTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        enrollmentTable.getColumnModel().getColumn(4).setPreferredWidth(200);
        enrollmentTable.getColumnModel().getColumn(5).setPreferredWidth(100);
        enrollmentTable.getColumnModel().getColumn(6).setPreferredWidth(120);
        enrollmentTable.getColumnModel().getColumn(7).setPreferredWidth(80);
        
        // Create buttons
        enrollStudentButton = new JButton("Enroll Student");
        enrollStudentButton.setPreferredSize(new Dimension(130, 35));
        enrollStudentButton.setBackground(new Color(34, 139, 34));
        enrollStudentButton.setForeground(Color.WHITE);
        
        dropStudentButton = new JButton("Drop Student");
        dropStudentButton.setPreferredSize(new Dimension(120, 35));
        dropStudentButton.setBackground(new Color(220, 20, 60));
        dropStudentButton.setForeground(Color.WHITE);
        dropStudentButton.setEnabled(false);
        
        viewEnrollmentsButton = new JButton("View Course Enrollments");
        viewEnrollmentsButton.setPreferredSize(new Dimension(180, 35));
        viewEnrollmentsButton.setBackground(new Color(70, 130, 180));
        viewEnrollmentsButton.setForeground(Color.WHITE);
        
        refreshButton = new JButton("Refresh");
        refreshButton.setPreferredSize(new Dimension(100, 35));
        refreshButton.setBackground(new Color(128, 128, 128));
        refreshButton.setForeground(Color.WHITE);
        
        // Create filter components
        courseFilter = new JComboBox<>();
        courseFilter.setPreferredSize(new Dimension(200, 25));
        
        statusFilter = new JComboBox<>(new String[]{"All Status", "ENROLLED", "DROPPED", "COMPLETED"});
        statusFilter.setPreferredSize(new Dimension(120, 25));
        
        studentSearchField = new JTextField(15);
        studentSearchField.setToolTipText("Search by student name or number");
    }
    
    /**
     * Sets up the layout of components.
     */
    private void setupLayout() {
        // Title panel
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        titlePanel.setBackground(Color.WHITE);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        
        JLabel titleLabel = new JLabel("Enrollment Management");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        titleLabel.setForeground(new Color(70, 130, 180));
        titlePanel.add(titleLabel);
        
        // Filter panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.setBackground(Color.WHITE);
        filterPanel.setBorder(BorderFactory.createTitledBorder("Filters"));
        
        filterPanel.add(new JLabel("Course:"));
        filterPanel.add(courseFilter);
        filterPanel.add(Box.createHorizontalStrut(10));
        filterPanel.add(new JLabel("Status:"));
        filterPanel.add(statusFilter);
        filterPanel.add(Box.createHorizontalStrut(10));
        filterPanel.add(new JLabel("Search Student:"));
        filterPanel.add(studentSearchField);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createTitledBorder("Actions"));
        
        buttonPanel.add(enrollStudentButton);
        buttonPanel.add(dropStudentButton);
        buttonPanel.add(viewEnrollmentsButton);
        buttonPanel.add(Box.createHorizontalStrut(20));
        buttonPanel.add(refreshButton);
        
        // Control panel (filters + buttons)
        JPanel controlPanel = new JPanel(new BorderLayout());
        controlPanel.setBackground(Color.WHITE);
        controlPanel.add(filterPanel, BorderLayout.NORTH);
        controlPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Main layout
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(titlePanel, BorderLayout.NORTH);
        topPanel.add(controlPanel, BorderLayout.CENTER);
        
        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(enrollmentTable), BorderLayout.CENTER);
    }
    
    /**
     * Sets up event handlers.
     */
    private void setupEventHandlers() {
        // Table selection listener
        enrollmentTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                boolean hasSelection = enrollmentTable.getSelectedRow() != -1;
                dropStudentButton.setEnabled(hasSelection);
            }
        });
        
        // Button listeners
        enrollStudentButton.addActionListener(e -> showEnrollStudentDialog());
        dropStudentButton.addActionListener(e -> dropSelectedStudent());
        viewEnrollmentsButton.addActionListener(e -> showCourseEnrollmentsDialog());
        refreshButton.addActionListener(e -> loadEnrollments());
        
        // Filter listeners
        courseFilter.addActionListener(e -> loadEnrollments());
        statusFilter.addActionListener(e -> loadEnrollments());
        
        // Search field listener
        studentSearchField.addActionListener(e -> loadEnrollments());
    }
    
    /**
     * Loads initial data.
     */
    private void loadInitialData() {
        loadCourses();
        loadEnrollments();
    }
    
    /**
     * Loads courses for the filter dropdown.
     */
    private void loadCourses() {
        CompletableFuture.supplyAsync(() -> {
            try {
                return remoteService.getAllActiveCourses(sessionToken);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).thenAccept(courses -> {
            SwingUtilities.invokeLater(() -> {
                courseFilter.removeAllItems();
                courseFilter.addItem(null); // "All Courses" option
                for (Course course : courses) {
                    courseFilter.addItem(course);
                }
            });
        }).exceptionally(throwable -> {
            SwingUtilities.invokeLater(() -> {
                logger.error("Failed to load courses", throwable);
            });
            return null;
        });
    }
    
    /**
     * Loads enrollments from the server.
     */
    private void loadEnrollments() {
        parentFrame.showProgress(true);
        
        CompletableFuture.supplyAsync(() -> {
            try {
                return remoteService.getAllEnrollments(sessionToken);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).thenAccept(enrollments -> {
            SwingUtilities.invokeLater(() -> {
                updateEnrollmentTable(enrollments);
                parentFrame.showProgress(false);
            });
        }).exceptionally(throwable -> {
            SwingUtilities.invokeLater(() -> {
                parentFrame.showProgress(false);
                logger.error("Failed to load enrollments", throwable);
                parentFrame.showErrorDialog("Error", "Failed to load enrollments: " + throwable.getMessage());
            });
            return null;
        });
    }
    
    /**
     * Updates the enrollment table.
     */
    private void updateEnrollmentTable(List<Enrollment> enrollments) {
        enrollmentTableModel.setRowCount(0);
        
        for (Enrollment enrollment : enrollments) {
            // Apply filters
            Course selectedCourse = (Course) courseFilter.getSelectedItem();
            String selectedStatus = (String) statusFilter.getSelectedItem();
            String searchText = studentSearchField.getText().trim().toLowerCase();
            
            boolean matchesCourse = selectedCourse == null || enrollment.getCourseId() == selectedCourse.getCourseId();
            boolean matchesStatus = "All Status".equals(selectedStatus) || enrollment.getStatus().name().equals(selectedStatus);
            boolean matchesSearch = searchText.isEmpty() || 
                                  enrollment.getStudent().getFirstName().toLowerCase().contains(searchText) ||
                                  enrollment.getStudent().getLastName().toLowerCase().contains(searchText) ||
                                  enrollment.getStudent().getStudentNumber().toLowerCase().contains(searchText);
            
            if (matchesCourse && matchesStatus && matchesSearch) {
                Object[] row = {
                    enrollment.getEnrollmentId(),
                    enrollment.getStudent().getFirstName() + " " + enrollment.getStudent().getLastName(),
                    enrollment.getStudent().getStudentNumber(),
                    enrollment.getCourse().getCourseCode(),
                    enrollment.getCourse().getCourseName(),
                    enrollment.getStatus().name(),
                    enrollment.getEnrollmentDate(),
                    enrollment.getGrade() != null ? enrollment.getGrade() : "N/A"
                };
                enrollmentTableModel.addRow(row);
            }
        }
    }
    
    /**
     * Shows the enroll student dialog.
     */
    private void showEnrollStudentDialog() {
        EnrollStudentDialog dialog = new EnrollStudentDialog(parentFrame, remoteService, sessionToken);
        dialog.setVisible(true);
        
        if (dialog.isConfirmed()) {
            loadEnrollments(); // Refresh the table
        }
    }
    
    /**
     * Drops the selected student from course.
     */
    private void dropSelectedStudent() {
        int selectedRow = enrollmentTable.getSelectedRow();
        if (selectedRow == -1) return;
        
        String studentName = (String) enrollmentTableModel.getValueAt(selectedRow, 1);
        String courseCode = (String) enrollmentTableModel.getValueAt(selectedRow, 3);
        int enrollmentId = (Integer) enrollmentTableModel.getValueAt(selectedRow, 0);
        
        boolean confirmed = parentFrame.showConfirmDialog(
            "Confirm Drop",
            "Are you sure you want to drop " + studentName + " from " + courseCode + "?\n" +
            "This will change their enrollment status to DROPPED."
        );
        
        if (confirmed) {
            parentFrame.showProgress(true);
            
            CompletableFuture.supplyAsync(() -> {
                try {
                    return remoteService.dropStudentFromCourse(sessionToken, enrollmentId);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }).thenAccept(success -> {
                SwingUtilities.invokeLater(() -> {
                    parentFrame.showProgress(false);
                    if (success) {
                        parentFrame.showInfoDialog("Success", "Student dropped from course successfully");
                        loadEnrollments();
                    } else {
                        parentFrame.showErrorDialog("Error", "Failed to drop student from course");
                    }
                });
            }).exceptionally(throwable -> {
                SwingUtilities.invokeLater(() -> {
                    parentFrame.showProgress(false);
                    logger.error("Failed to drop student", throwable);
                    parentFrame.showErrorDialog("Error", "Failed to drop student: " + throwable.getMessage());
                });
                return null;
            });
        }
    }
    
    /**
     * Shows course enrollments dialog.
     */
    private void showCourseEnrollmentsDialog() {
        Course selectedCourse = (Course) courseFilter.getSelectedItem();
        if (selectedCourse == null) {
            parentFrame.showErrorDialog("Error", "Please select a course first");
            return;
        }
        
        CourseEnrollmentsDialog dialog = new CourseEnrollmentsDialog(parentFrame, remoteService, sessionToken, selectedCourse);
        dialog.setVisible(true);
    }
    
    /**
     * Enroll student dialog.
     */
    private static class EnrollStudentDialog extends JDialog {
        private boolean confirmed = false;
        private final AttendanceService remoteService;
        private final String sessionToken;
        
        private JComboBox<Student> studentCombo;
        private JComboBox<Course> courseCombo;
        
        public EnrollStudentDialog(Frame parent, AttendanceService service, String token) {
            super(parent, "Enroll Student", true);
            this.remoteService = service;
            this.sessionToken = token;
            
            initializeComponents();
            setupLayout();
            loadData();
            
            setSize(500, 300);
            setLocationRelativeTo(parent);
        }
        
        private void initializeComponents() {
            studentCombo = new JComboBox<>();
            courseCombo = new JComboBox<>();
        }
        
        private void setupLayout() {
            setLayout(new BorderLayout());
            
            JPanel formPanel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(10, 10, 10, 10);
            gbc.anchor = GridBagConstraints.WEST;
            
            gbc.gridx = 0; gbc.gridy = 0;
            formPanel.add(new JLabel("Select Student:"), gbc);
            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            formPanel.add(studentCombo, gbc);
            
            gbc.gridx = 0; gbc.gridy = 1;
            gbc.fill = GridBagConstraints.NONE;
            formPanel.add(new JLabel("Select Course:"), gbc);
            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            formPanel.add(courseCombo, gbc);
            
            add(formPanel, BorderLayout.CENTER);
            
            JPanel buttonPanel = new JPanel(new FlowLayout());
            JButton enrollButton = new JButton("Enroll");
            JButton cancelButton = new JButton("Cancel");
            
            enrollButton.addActionListener(e -> enrollStudent());
            cancelButton.addActionListener(e -> dispose());
            
            buttonPanel.add(enrollButton);
            buttonPanel.add(cancelButton);
            add(buttonPanel, BorderLayout.SOUTH);
        }
        
        private void loadData() {
            try {
                // Load students
                List<User> students = remoteService.getUsersByRole(sessionToken, UserRole.STUDENT);
                studentCombo.removeAllItems();
                for (User user : students) {
                    if (user instanceof Student) {
                        studentCombo.addItem((Student) user);
                    }
                }
                
                // Load courses
                List<Course> courses = remoteService.getAllActiveCourses(sessionToken);
                courseCombo.removeAllItems();
                for (Course course : courses) {
                    courseCombo.addItem(course);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error loading data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        
        private void enrollStudent() {
            try {
                Student selectedStudent = (Student) studentCombo.getSelectedItem();
                Course selectedCourse = (Course) courseCombo.getSelectedItem();
                
                if (selectedStudent == null || selectedCourse == null) {
                    JOptionPane.showMessageDialog(this, "Please select both student and course", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                boolean success = remoteService.enrollStudent(sessionToken, selectedStudent.getUserId(), selectedCourse.getCourseId());
                
                if (success) {
                    confirmed = true;
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to enroll student", "Error", JOptionPane.ERROR_MESSAGE);
                }
                
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error enrolling student: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        
        public boolean isConfirmed() {
            return confirmed;
        }
    }
    
    /**
     * Course enrollments dialog.
     */
    private static class CourseEnrollmentsDialog extends JDialog {
        private final AttendanceService remoteService;
        private final String sessionToken;
        private final Course course;
        
        private JTable enrollmentTable;
        private DefaultTableModel enrollmentTableModel;
        
        public CourseEnrollmentsDialog(Frame parent, AttendanceService service, String token, Course course) {
            super(parent, "Enrollments for " + course.getCourseCode(), true);
            this.remoteService = service;
            this.sessionToken = token;
            this.course = course;
            
            initializeComponents();
            setupLayout();
            loadEnrollments();
            
            setSize(700, 500);
            setLocationRelativeTo(parent);
        }
        
        private void initializeComponents() {
            String[] columnNames = {"Student Name", "Student Number", "Status", "Enrollment Date", "Grade"};
            enrollmentTableModel = new DefaultTableModel(columnNames, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            
            enrollmentTable = new JTable(enrollmentTableModel);
            enrollmentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            enrollmentTable.setRowHeight(25);
        }
        
        private void setupLayout() {
            setLayout(new BorderLayout());
            
            JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JLabel titleLabel = new JLabel("Enrollments for " + course.getCourseName());
            titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
            titlePanel.add(titleLabel);
            
            add(titlePanel, BorderLayout.NORTH);
            add(new JScrollPane(enrollmentTable), BorderLayout.CENTER);
            
            JButton closeButton = new JButton("Close");
            closeButton.addActionListener(e -> dispose());
            
            JPanel buttonPanel = new JPanel(new FlowLayout());
            buttonPanel.add(closeButton);
            add(buttonPanel, BorderLayout.SOUTH);
        }
        
        private void loadEnrollments() {
            try {
                List<Student> enrolledStudents = remoteService.getEnrolledStudents(sessionToken, course.getCourseId());
                enrollmentTableModel.setRowCount(0);
                
                for (Student student : enrolledStudents) {
                    Object[] row = {
                        student.getFirstName() + " " + student.getLastName(),
                        student.getStudentNumber(),
                        "ENROLLED", // Default status
                        student.getEnrollmentDate(),
                        "N/A" // Grade not available in this context
                    };
                    enrollmentTableModel.addRow(row);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error loading enrollments: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}