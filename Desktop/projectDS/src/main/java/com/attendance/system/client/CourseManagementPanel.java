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
 * Course Management Panel for administrators to manage courses.
 * Provides CRUD operations for courses and teacher assignments.
 */
public class CourseManagementPanel extends JPanel {
    private static final Logger logger = LoggerFactory.getLogger(CourseManagementPanel.class);
    
    private final AttendanceGUI parentFrame;
    private final AttendanceService remoteService;
    private final String sessionToken;
    
    // Table components
    private JTable courseTable;
    private DefaultTableModel courseTableModel;
    
    // Control buttons
    private JButton addCourseButton;
    private JButton editCourseButton;
    private JButton deleteCourseButton;
    private JButton assignTeacherButton;
    private JButton refreshButton;
    
    // Filter components
    private JComboBox<String> semesterFilter;
    private JComboBox<String> yearFilter;
    private JCheckBox activeOnlyCheckBox;
    
    public CourseManagementPanel(AttendanceGUI parentFrame, AttendanceService remoteService, String sessionToken) {
        this.parentFrame = parentFrame;
        this.remoteService = remoteService;
        this.sessionToken = sessionToken;
        
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        loadCourses();
    }
    
    /**
     * Initializes all GUI components.
     */
    private void initializeComponents() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        
        // Create course table
        String[] columnNames = {"Course ID", "Course Code", "Course Name", "Credits", "Teacher", "Semester", "Year", "Active"};
        courseTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table read-only
            }
        };
        
        courseTable = new JTable(courseTableModel);
        courseTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        courseTable.setRowHeight(25);
        courseTable.getTableHeader().setReorderingAllowed(false);
        
        // Set column widths
        courseTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        courseTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        courseTable.getColumnModel().getColumn(2).setPreferredWidth(200);
        courseTable.getColumnModel().getColumn(3).setPreferredWidth(80);
        courseTable.getColumnModel().getColumn(4).setPreferredWidth(150);
        courseTable.getColumnModel().getColumn(5).setPreferredWidth(100);
        courseTable.getColumnModel().getColumn(6).setPreferredWidth(80);
        courseTable.getColumnModel().getColumn(7).setPreferredWidth(80);
        
        // Create buttons
        addCourseButton = new JButton("Add Course");
        addCourseButton.setPreferredSize(new Dimension(120, 35));
        addCourseButton.setBackground(new Color(34, 139, 34));
        addCourseButton.setForeground(Color.WHITE);
        
        editCourseButton = new JButton("Edit Course");
        editCourseButton.setPreferredSize(new Dimension(120, 35));
        editCourseButton.setBackground(new Color(70, 130, 180));
        editCourseButton.setForeground(Color.WHITE);
        editCourseButton.setEnabled(false);
        
        deleteCourseButton = new JButton("Delete Course");
        deleteCourseButton.setPreferredSize(new Dimension(120, 35));
        deleteCourseButton.setBackground(new Color(220, 20, 60));
        deleteCourseButton.setForeground(Color.WHITE);
        deleteCourseButton.setEnabled(false);
        
        assignTeacherButton = new JButton("Assign Teacher");
        assignTeacherButton.setPreferredSize(new Dimension(130, 35));
        assignTeacherButton.setBackground(new Color(255, 140, 0));
        assignTeacherButton.setForeground(Color.WHITE);
        assignTeacherButton.setEnabled(false);
        
        refreshButton = new JButton("Refresh");
        refreshButton.setPreferredSize(new Dimension(100, 35));
        refreshButton.setBackground(new Color(128, 128, 128));
        refreshButton.setForeground(Color.WHITE);
        
        // Create filter components
        semesterFilter = new JComboBox<>(new String[]{"All Semesters", "Fall", "Spring", "Summer"});
        yearFilter = new JComboBox<>(new String[]{"All Years", "2023-2024", "2024-2025", "2025-2026"});
        activeOnlyCheckBox = new JCheckBox("Active Courses Only", true);
        activeOnlyCheckBox.setBackground(Color.WHITE);
    }
    
    /**
     * Sets up the layout of components.
     */
    private void setupLayout() {
        // Title panel
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        titlePanel.setBackground(Color.WHITE);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        
        JLabel titleLabel = new JLabel("Course Management");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        titleLabel.setForeground(new Color(70, 130, 180));
        titlePanel.add(titleLabel);
        
        // Filter panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.setBackground(Color.WHITE);
        filterPanel.setBorder(BorderFactory.createTitledBorder("Filters"));
        
        filterPanel.add(new JLabel("Semester:"));
        filterPanel.add(semesterFilter);
        filterPanel.add(Box.createHorizontalStrut(10));
        filterPanel.add(new JLabel("Academic Year:"));
        filterPanel.add(yearFilter);
        filterPanel.add(Box.createHorizontalStrut(10));
        filterPanel.add(activeOnlyCheckBox);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createTitledBorder("Actions"));
        
        buttonPanel.add(addCourseButton);
        buttonPanel.add(editCourseButton);
        buttonPanel.add(deleteCourseButton);
        buttonPanel.add(assignTeacherButton);
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
        add(new JScrollPane(courseTable), BorderLayout.CENTER);
    }
    
    /**
     * Sets up event handlers.
     */
    private void setupEventHandlers() {
        // Table selection listener
        courseTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                boolean hasSelection = courseTable.getSelectedRow() != -1;
                editCourseButton.setEnabled(hasSelection);
                deleteCourseButton.setEnabled(hasSelection);
                assignTeacherButton.setEnabled(hasSelection);
            }
        });
        
        // Button listeners
        addCourseButton.addActionListener(e -> showAddCourseDialog());
        editCourseButton.addActionListener(e -> showEditCourseDialog());
        deleteCourseButton.addActionListener(e -> deleteSelectedCourse());
        assignTeacherButton.addActionListener(e -> showAssignTeacherDialog());
        refreshButton.addActionListener(e -> loadCourses());
        
        // Filter listeners
        semesterFilter.addActionListener(e -> loadCourses());
        yearFilter.addActionListener(e -> loadCourses());
        activeOnlyCheckBox.addActionListener(e -> loadCourses());
    }
    
    /**
     * Loads courses from the server.
     */
    private void loadCourses() {
        parentFrame.showProgress(true);
        
        CompletableFuture.supplyAsync(() -> {
            try {
                return remoteService.getAllActiveCourses(sessionToken);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).thenAccept(courses -> {
            SwingUtilities.invokeLater(() -> {
                updateCourseTable(courses);
                parentFrame.showProgress(false);
            });
        }).exceptionally(throwable -> {
            SwingUtilities.invokeLater(() -> {
                parentFrame.showProgress(false);
                logger.error("Failed to load courses", throwable);
                parentFrame.showErrorDialog("Error", "Failed to load courses: " + throwable.getMessage());
            });
            return null;
        });
    }
    
    /**
     * Updates the course table with the provided courses.
     */
    private void updateCourseTable(List<Course> courses) {
        courseTableModel.setRowCount(0);
        
        for (Course course : courses) {
            // Apply filters
            if (!activeOnlyCheckBox.isSelected() || course.isActive()) {
                String semester = (String) semesterFilter.getSelectedItem();
                String year = (String) yearFilter.getSelectedItem();
                
                if (("All Semesters".equals(semester) || course.getSemester().equals(semester)) &&
                    ("All Years".equals(year) || course.getAcademicYear().equals(year))) {
                    
                    // Get teacher name
                    String teacherName = "Unassigned";
                    try {
                        if (course.getTeacherId() > 0) {
                            User teacher = remoteService.getUserById(sessionToken, course.getTeacherId());
                            if (teacher != null) {
                                teacherName = teacher.getFirstName() + " " + teacher.getLastName();
                            }
                        }
                    } catch (Exception e) {
                        logger.warn("Failed to get teacher name for course: " + course.getCourseCode(), e);
                    }
                    
                    Object[] row = {
                        course.getCourseId(),
                        course.getCourseCode(),
                        course.getCourseName(),
                        course.getCredits(),
                        teacherName,
                        course.getSemester(),
                        course.getAcademicYear(),
                        course.isActive() ? "Yes" : "No"
                    };
                    courseTableModel.addRow(row);
                }
            }
        }
    }
    
    /**
     * Shows the add course dialog.
     */
    private void showAddCourseDialog() {
        CourseEditDialog dialog = new CourseEditDialog(parentFrame, remoteService, sessionToken, null);
        dialog.setVisible(true);
        
        if (dialog.isConfirmed()) {
            loadCourses(); // Refresh the table
        }
    }
    
    /**
     * Shows the edit course dialog.
     */
    private void showEditCourseDialog() {
        int selectedRow = courseTable.getSelectedRow();
        if (selectedRow == -1) return;
        
        int courseId = (Integer) courseTableModel.getValueAt(selectedRow, 0);
        
        // Get full course object from server
        parentFrame.showProgress(true);
        
        CompletableFuture.supplyAsync(() -> {
            try {
                return remoteService.getCourseById(sessionToken, courseId);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).thenAccept(course -> {
            SwingUtilities.invokeLater(() -> {
                parentFrame.showProgress(false);
                if (course != null) {
                    CourseEditDialog dialog = new CourseEditDialog(parentFrame, remoteService, sessionToken, course);
                    dialog.setVisible(true);
                    
                    if (dialog.isConfirmed()) {
                        loadCourses(); // Refresh the table
                    }
                } else {
                    parentFrame.showErrorDialog("Error", "Course not found");
                }
            });
        }).exceptionally(throwable -> {
            SwingUtilities.invokeLater(() -> {
                parentFrame.showProgress(false);
                logger.error("Failed to load course", throwable);
                parentFrame.showErrorDialog("Error", "Failed to load course: " + throwable.getMessage());
            });
            return null;
        });
    }
    
    /**
     * Deletes the selected course.
     */
    private void deleteSelectedCourse() {
        int selectedRow = courseTable.getSelectedRow();
        if (selectedRow == -1) return;
        
        String courseCode = (String) courseTableModel.getValueAt(selectedRow, 1);
        String courseName = (String) courseTableModel.getValueAt(selectedRow, 2);
        
        boolean confirmed = parentFrame.showConfirmDialog(
            "Confirm Delete",
            "Are you sure you want to delete course '" + courseCode + " - " + courseName + "'?\n" +
            "This action cannot be undone and will affect all related enrollments and attendance records."
        );
        
        if (confirmed) {
            int courseId = (Integer) courseTableModel.getValueAt(selectedRow, 0);
            
            parentFrame.showProgress(true);
            
            CompletableFuture.supplyAsync(() -> {
                try {
                    return remoteService.deleteCourse(sessionToken, courseId);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }).thenAccept(success -> {
                SwingUtilities.invokeLater(() -> {
                    parentFrame.showProgress(false);
                    if (success) {
                        parentFrame.showInfoDialog("Success", "Course deleted successfully");
                        loadCourses();
                    } else {
                        parentFrame.showErrorDialog("Error", "Failed to delete course");
                    }
                });
            }).exceptionally(throwable -> {
                SwingUtilities.invokeLater(() -> {
                    parentFrame.showProgress(false);
                    logger.error("Failed to delete course", throwable);
                    parentFrame.showErrorDialog("Error", "Failed to delete course: " + throwable.getMessage());
                });
                return null;
            });
        }
    }
    
    /**
     * Shows the assign teacher dialog.
     */
    private void showAssignTeacherDialog() {
        int selectedRow = courseTable.getSelectedRow();
        if (selectedRow == -1) return;
        
        int courseId = (Integer) courseTableModel.getValueAt(selectedRow, 0);
        String courseCode = (String) courseTableModel.getValueAt(selectedRow, 1);
        
        TeacherAssignmentDialog dialog = new TeacherAssignmentDialog(parentFrame, remoteService, sessionToken, courseId, courseCode);
        dialog.setVisible(true);
        
        if (dialog.isConfirmed()) {
            loadCourses(); // Refresh the table
        }
    }
    
    /**
     * Course edit dialog for adding/editing courses.
     */
    private static class CourseEditDialog extends JDialog {
        private boolean confirmed = false;
        private final AttendanceService remoteService;
        private final String sessionToken;
        private final Course course;
        
        private JTextField courseCodeField;
        private JTextField courseNameField;
        private JTextField creditsField;
        private JComboBox<String> semesterCombo;
        private JTextField yearField;
        private JComboBox<User> teacherCombo;
        private JCheckBox activeCheckBox;
        
        public CourseEditDialog(Frame parent, AttendanceService service, String token, Course course) {
            super(parent, course == null ? "Add Course" : "Edit Course", true);
            this.remoteService = service;
            this.sessionToken = token;
            this.course = course;
            
            initializeComponents();
            setupLayout();
            setupEventHandlers();
            loadTeachers();
            
            if (course != null) {
                populateFields();
            }
            
            setSize(500, 400);
            setLocationRelativeTo(parent);
        }
        
        private void initializeComponents() {
            courseCodeField = new JTextField(20);
            courseNameField = new JTextField(30);
            creditsField = new JTextField(10);
            semesterCombo = new JComboBox<>(new String[]{"Fall", "Spring", "Summer"});
            yearField = new JTextField(10);
            teacherCombo = new JComboBox<>();
            activeCheckBox = new JCheckBox("Active", true);
        }
        
        private void setupLayout() {
            setLayout(new BorderLayout());
            
            JPanel formPanel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.anchor = GridBagConstraints.WEST;
            
            gbc.gridx = 0; gbc.gridy = 0;
            formPanel.add(new JLabel("Course Code:"), gbc);
            gbc.gridx = 1;
            formPanel.add(courseCodeField, gbc);
            
            gbc.gridx = 0; gbc.gridy = 1;
            formPanel.add(new JLabel("Course Name:"), gbc);
            gbc.gridx = 1;
            formPanel.add(courseNameField, gbc);
            
            gbc.gridx = 0; gbc.gridy = 2;
            formPanel.add(new JLabel("Credits:"), gbc);
            gbc.gridx = 1;
            formPanel.add(creditsField, gbc);
            
            gbc.gridx = 0; gbc.gridy = 3;
            formPanel.add(new JLabel("Semester:"), gbc);
            gbc.gridx = 1;
            formPanel.add(semesterCombo, gbc);
            
            gbc.gridx = 0; gbc.gridy = 4;
            formPanel.add(new JLabel("Academic Year:"), gbc);
            gbc.gridx = 1;
            formPanel.add(yearField, gbc);
            
            gbc.gridx = 0; gbc.gridy = 5;
            formPanel.add(new JLabel("Teacher:"), gbc);
            gbc.gridx = 1;
            formPanel.add(teacherCombo, gbc);
            
            gbc.gridx = 0; gbc.gridy = 6;
            gbc.gridwidth = 2;
            formPanel.add(activeCheckBox, gbc);
            
            add(formPanel, BorderLayout.CENTER);
            
            JPanel buttonPanel = new JPanel(new FlowLayout());
            JButton saveButton = new JButton(course == null ? "Create" : "Update");
            JButton cancelButton = new JButton("Cancel");
            
            saveButton.addActionListener(e -> saveCourse());
            cancelButton.addActionListener(e -> dispose());
            
            buttonPanel.add(saveButton);
            buttonPanel.add(cancelButton);
            add(buttonPanel, BorderLayout.SOUTH);
        }
        
        private void setupEventHandlers() {
            // Add validation listeners if needed
        }
        
        private void loadTeachers() {
            try {
                List<User> teachers = remoteService.getUsersByRole(sessionToken, UserRole.TEACHER);
                teacherCombo.removeAllItems();
                for (User teacher : teachers) {
                    teacherCombo.addItem(teacher);
                }
            } catch (Exception e) {
                // Handle error
            }
        }
        
        private void populateFields() {
            if (course != null) {
                courseCodeField.setText(course.getCourseCode());
                courseNameField.setText(course.getCourseName());
                creditsField.setText(String.valueOf(course.getCredits()));
                semesterCombo.setSelectedItem(course.getSemester());
                yearField.setText(course.getAcademicYear());
                activeCheckBox.setSelected(course.isActive());
                
                // Select the correct teacher
                for (int i = 0; i < teacherCombo.getItemCount(); i++) {
                    User teacher = teacherCombo.getItemAt(i);
                    if (teacher.getUserId() == course.getTeacherId()) {
                        teacherCombo.setSelectedIndex(i);
                        break;
                    }
                }
            }
        }
        
        private void saveCourse() {
            try {
                // Validate input
                String courseCode = courseCodeField.getText().trim();
                String courseName = courseNameField.getText().trim();
                String creditsText = creditsField.getText().trim();
                String year = yearField.getText().trim();
                User selectedTeacher = (User) teacherCombo.getSelectedItem();
                
                if (courseCode.isEmpty() || courseName.isEmpty() || creditsText.isEmpty() || 
                    year.isEmpty() || selectedTeacher == null) {
                    JOptionPane.showMessageDialog(this, "Please fill in all fields", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                int credits = Integer.parseInt(creditsText);
                
                Course courseToSave;
                if (course == null) {
                    courseToSave = new Course();
                } else {
                    courseToSave = course;
                }
                
                courseToSave.setCourseCode(courseCode);
                courseToSave.setCourseName(courseName);
                courseToSave.setCredits(credits);
                courseToSave.setSemester((String) semesterCombo.getSelectedItem());
                courseToSave.setAcademicYear(year);
                courseToSave.setTeacherId(selectedTeacher.getUserId());
                courseToSave.setActive(activeCheckBox.isSelected());
                
                boolean success;
                if (course == null) {
                    success = remoteService.createCourse(sessionToken, courseToSave);
                } else {
                    success = remoteService.updateCourse(sessionToken, courseToSave);
                }
                
                if (success) {
                    confirmed = true;
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to save course", "Error", JOptionPane.ERROR_MESSAGE);
                }
                
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Credits must be a valid number", "Validation Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error saving course: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        
        public boolean isConfirmed() {
            return confirmed;
        }
    }
    
    /**
     * Teacher assignment dialog.
     */
    private static class TeacherAssignmentDialog extends JDialog {
        private boolean confirmed = false;
        private final AttendanceService remoteService;
        private final String sessionToken;
        private final int courseId;
        
        private JComboBox<User> teacherCombo;
        
        public TeacherAssignmentDialog(Frame parent, AttendanceService service, String token, int courseId, String courseCode) {
            super(parent, "Assign Teacher to " + courseCode, true);
            this.remoteService = service;
            this.sessionToken = token;
            this.courseId = courseId;
            
            initializeComponents();
            setupLayout();
            loadTeachers();
            
            setSize(400, 200);
            setLocationRelativeTo(parent);
        }
        
        private void initializeComponents() {
            teacherCombo = new JComboBox<>();
        }
        
        private void setupLayout() {
            setLayout(new BorderLayout());
            
            JPanel formPanel = new JPanel(new FlowLayout());
            formPanel.add(new JLabel("Select Teacher:"));
            formPanel.add(teacherCombo);
            
            add(formPanel, BorderLayout.CENTER);
            
            JPanel buttonPanel = new JPanel(new FlowLayout());
            JButton assignButton = new JButton("Assign");
            JButton cancelButton = new JButton("Cancel");
            
            assignButton.addActionListener(e -> assignTeacher());
            cancelButton.addActionListener(e -> dispose());
            
            buttonPanel.add(assignButton);
            buttonPanel.add(cancelButton);
            add(buttonPanel, BorderLayout.SOUTH);
        }
        
        private void loadTeachers() {
            try {
                List<User> teachers = remoteService.getUsersByRole(sessionToken, UserRole.TEACHER);
                teacherCombo.removeAllItems();
                for (User teacher : teachers) {
                    teacherCombo.addItem(teacher);
                }
            } catch (Exception e) {
                // Handle error
            }
        }
        
        private void assignTeacher() {
            try {
                User selectedTeacher = (User) teacherCombo.getSelectedItem();
                if (selectedTeacher == null) {
                    JOptionPane.showMessageDialog(this, "Please select a teacher", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                boolean success = remoteService.assignTeacherToCourse(sessionToken, courseId, selectedTeacher.getUserId());
                
                if (success) {
                    confirmed = true;
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to assign teacher", "Error", JOptionPane.ERROR_MESSAGE);
                }
                
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error assigning teacher: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        
        public boolean isConfirmed() {
            return confirmed;
        }
    }
}