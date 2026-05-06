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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * AttendanceMarkingPanel provides a comprehensive interface for marking student attendance.
 * Features include:
 * - Class selection and student list display
 * - Attendance status selection (Present, Absent, Late, Excused)
 * - Bulk attendance marking capabilities
 * - Attendance modification interface with time window validation
 * 
 * Validates Requirements: 3.1, 3.2, 3.4
 */
public class AttendanceMarkingPanel extends JPanel {
    private static final Logger logger = LoggerFactory.getLogger(AttendanceMarkingPanel.class);
    private static final int MODIFICATION_WINDOW_HOURS = 24;
    
    private final AttendanceGUI parentFrame;
    private final AttendanceService remoteService;
    private final String sessionToken;
    private User currentUser;
    
    // Control components
    private JComboBox<Course> courseComboBox;
    private JDateChooser dateChooser;
    private JButton loadStudentsButton;
    private JButton refreshButton;
    
    // Attendance table components
    private JTable attendanceTable;
    private DefaultTableModel attendanceTableModel;
    private List<Student> currentStudents;
    private List<AttendanceRecord> existingRecords;
    private Course selectedCourse;
    
    // Bulk action components
    private JButton markAllPresentButton;
    private JButton markAllAbsentButton;
    private JButton markAllLateButton;
    private JButton markAllExcusedButton;
    private JButton clearAllButton;
    
    // Save and modification components
    private JButton saveAttendanceButton;
    private JButton modifyAttendanceButton;
    private JLabel statusLabel;
    private JProgressBar progressBar;
    
    // Modification mode
    private boolean modificationMode = false;
    private LocalDate modificationDate;
    
    public AttendanceMarkingPanel(AttendanceGUI parentFrame, AttendanceService remoteService, String sessionToken) {
        this.parentFrame = parentFrame;
        this.remoteService = remoteService;
        this.sessionToken = sessionToken;
        this.currentStudents = new ArrayList<>();
        this.existingRecords = new ArrayList<>();
        
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
    }
    
    /**
     * Initializes all GUI components.
     */
    private void initializeComponents() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        
        // Create control panel
        JPanel controlPanel = createControlPanel();
        add(controlPanel, BorderLayout.NORTH);
        
        // Create attendance table
        createAttendanceTable();
        JScrollPane scrollPane = new JScrollPane(attendanceTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Student Attendance Records"));
        add(scrollPane, BorderLayout.CENTER);
        
        // Create button panel
        JPanel buttonPanel = createButtonPanel();
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Creates the control panel with course and date selection.
     */
    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Top row: Course and Date selection
        JPanel selectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        selectionPanel.setBackground(Color.WHITE);
        
        JLabel courseLabel = new JLabel("Course:");
        courseLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        courseComboBox = new JComboBox<>();
        courseComboBox.setPreferredSize(new Dimension(250, 30));
        courseComboBox.addActionListener(e -> onCourseSelected());
        
        JLabel dateLabel = new JLabel("Date:");
        dateLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        dateChooser = new JDateChooser();
        dateChooser.setDate(java.sql.Date.valueOf(LocalDate.now()));
        dateChooser.setPreferredSize(new Dimension(120, 30));
        
        loadStudentsButton = new JButton("Load Students");
        loadStudentsButton.setPreferredSize(new Dimension(130, 30));
        loadStudentsButton.setEnabled(false);
        loadStudentsButton.addActionListener(e -> loadStudentsForAttendance());
        
        refreshButton = new JButton("Refresh");
        refreshButton.setPreferredSize(new Dimension(100, 30));
        refreshButton.addActionListener(e -> loadTeacherCourses());
        
        selectionPanel.add(courseLabel);
        selectionPanel.add(courseComboBox);
        selectionPanel.add(Box.createHorizontalStrut(20));
        selectionPanel.add(dateLabel);
        selectionPanel.add(dateChooser);
        selectionPanel.add(Box.createHorizontalStrut(20));
        selectionPanel.add(loadStudentsButton);
        selectionPanel.add(refreshButton);
        
        // Status and progress panel
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        statusPanel.setBackground(Color.WHITE);
        
        statusLabel = new JLabel("Ready");
        statusLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        statusLabel.setForeground(new Color(70, 130, 180));
        
        progressBar = new JProgressBar();
        progressBar.setPreferredSize(new Dimension(200, 20));
        progressBar.setVisible(false);
        
        statusPanel.add(statusLabel);
        statusPanel.add(progressBar);
        
        panel.add(selectionPanel, BorderLayout.NORTH);
        panel.add(statusPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * Creates the attendance table.
     */
    private void createAttendanceTable() {
        String[] columnNames = {"Student ID", "Student Name", "Student Number", "Status", "Remarks", "Last Modified"};
        attendanceTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Status and Remarks columns are editable
                if (column == 3 || column == 4) {
                    return true;
                }
                // Last Modified column is not editable
                return false;
            }
            
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 3) {
                    return AttendanceStatus.class;
                }
                return String.class;
            }
        };
        
        attendanceTable = new JTable(attendanceTableModel);
        attendanceTable.setRowHeight(30);
        attendanceTable.getTableHeader().setReorderingAllowed(false);
        attendanceTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        
        // Set up status column with combo box
        JComboBox<AttendanceStatus> statusCombo = new JComboBox<>(AttendanceStatus.values());
        attendanceTable.getColumnModel().getColumn(3).setCellEditor(new DefaultCellEditor(statusCombo));
        attendanceTable.getColumnModel().getColumn(3).setCellRenderer(new AttendanceStatusRenderer());
        
        // Set column widths
        attendanceTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        attendanceTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        attendanceTable.getColumnModel().getColumn(2).setPreferredWidth(120);
        attendanceTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        attendanceTable.getColumnModel().getColumn(4).setPreferredWidth(200);
        attendanceTable.getColumnModel().getColumn(5).setPreferredWidth(150);
    }
    
    /**
     * Creates the button panel with bulk actions and save options.
     */
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Bulk action buttons
        JPanel bulkPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        bulkPanel.setBackground(Color.WHITE);
        bulkPanel.setBorder(BorderFactory.createTitledBorder("Bulk Actions"));
        
        markAllPresentButton = createBulkButton("Mark All Present", new Color(34, 139, 34), 
                e -> markAllStatus(AttendanceStatus.PRESENT));
        markAllAbsentButton = createBulkButton("Mark All Absent", new Color(220, 20, 60), 
                e -> markAllStatus(AttendanceStatus.ABSENT));
        markAllLateButton = createBulkButton("Mark All Late", new Color(255, 140, 0), 
                e -> markAllStatus(AttendanceStatus.LATE));
        markAllExcusedButton = createBulkButton("Mark All Excused", new Color(70, 130, 180), 
                e -> markAllStatus(AttendanceStatus.EXCUSED));
        clearAllButton = createBulkButton("Clear All", new Color(128, 128, 128), 
                e -> clearAllAttendance());
        
        bulkPanel.add(markAllPresentButton);
        bulkPanel.add(markAllAbsentButton);
        bulkPanel.add(markAllLateButton);
        bulkPanel.add(markAllExcusedButton);
        bulkPanel.add(Box.createHorizontalStrut(10));
        bulkPanel.add(clearAllButton);
        
        // Save and modification buttons
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        actionPanel.setBackground(Color.WHITE);
        
        saveAttendanceButton = new JButton("Save Attendance");
        saveAttendanceButton.setPreferredSize(new Dimension(150, 35));
        saveAttendanceButton.setBackground(new Color(70, 130, 180));
        saveAttendanceButton.setForeground(Color.WHITE);
        saveAttendanceButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        saveAttendanceButton.setEnabled(false);
        saveAttendanceButton.addActionListener(e -> saveAttendance());
        
        modifyAttendanceButton = new JButton("Modify Existing");
        modifyAttendanceButton.setPreferredSize(new Dimension(150, 35));
        modifyAttendanceButton.setBackground(new Color(255, 140, 0));
        modifyAttendanceButton.setForeground(Color.WHITE);
        modifyAttendanceButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        modifyAttendanceButton.setEnabled(false);
        modifyAttendanceButton.addActionListener(e -> enterModificationMode());
        
        actionPanel.add(saveAttendanceButton);
        actionPanel.add(modifyAttendanceButton);
        
        panel.add(bulkPanel, BorderLayout.NORTH);
        panel.add(actionPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * Creates a bulk action button with styling.
     */
    private JButton createBulkButton(String text, Color color, ActionListener listener) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(130, 30));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        button.setEnabled(false);
        button.addActionListener(listener);
        return button;
    }
    
    /**
     * Sets up event handlers.
     */
    private void setupEventHandlers() {
        // Load courses when panel is shown
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentShown(java.awt.event.ComponentEvent e) {
                if (courseComboBox.getItemCount() == 0) {
                    loadTeacherCourses();
                }
            }
        });
    }
    
    /**
     * Sets up the layout.
     */
    private void setupLayout() {
        // Layout is already set in initializeComponents
    }
    
    /**
     * Called when a course is selected.
     */
    private void onCourseSelected() {
        selectedCourse = (Course) courseComboBox.getSelectedItem();
        loadStudentsButton.setEnabled(selectedCourse != null);
        modificationMode = false;
        updateButtonStates();
    }
    
    /**
     * Loads teacher courses into the combo box.
     */
    private void loadTeacherCourses() {
        updateStatus("Loading courses...");
        
        CompletableFuture.supplyAsync(() -> {
            try {
                return remoteService.getCoursesByTeacher(sessionToken, currentUser.getUserId());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).thenAccept(courses -> {
            SwingUtilities.invokeLater(() -> {
                updateCourseComboBox(courses);
                updateStatus("Courses loaded");
            });
        }).exceptionally(throwable -> {
            SwingUtilities.invokeLater(() -> {
                logger.error("Failed to load teacher courses", throwable);
                updateStatus("Error loading courses");
                parentFrame.showErrorDialog("Error", "Failed to load courses: " + throwable.getMessage());
            });
            return null;
        });
    }
    
    /**
     * Updates the course combo box.
     */
    private void updateCourseComboBox(List<Course> courses) {
        courseComboBox.removeAllItems();
        for (Course course : courses) {
            courseComboBox.addItem(course);
        }
        
        if (!courses.isEmpty()) {
            courseComboBox.setSelectedIndex(0);
            selectedCourse = courses.get(0);
            loadStudentsButton.setEnabled(true);
        }
    }
    
    /**
     * Loads students for attendance marking.
     */
    private void loadStudentsForAttendance() {
        if (selectedCourse == null) {
            parentFrame.showErrorDialog("Error", "Please select a course first");
            return;
        }
        
        java.util.Date selectedDate = dateChooser.getDate();
        if (selectedDate == null) {
            parentFrame.showErrorDialog("Error", "Please select a date");
            return;
        }
        
        LocalDate attendanceDate = selectedDate.toInstant()
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDate();
        
        modificationDate = attendanceDate;
        updateStatus("Loading students...");
        showProgress(true);
        
        CompletableFuture.supplyAsync(() -> {
            try {
                List<Student> students = remoteService.getEnrolledStudents(sessionToken, selectedCourse.getCourseId());
                List<AttendanceRecord> records = remoteService.getAttendanceByClassDate(sessionToken, 
                        selectedCourse.getCourseId(), attendanceDate);
                return new Object[]{students, records};
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).thenAccept(result -> {
            SwingUtilities.invokeLater(() -> {
                @SuppressWarnings("unchecked")
                List<Student> students = (List<Student>) ((Object[]) result)[0];
                @SuppressWarnings("unchecked")
                List<AttendanceRecord> records = (List<AttendanceRecord>) ((Object[]) result)[1];
                
                currentStudents = students;
                existingRecords = records;
                updateAttendanceTable(students, records);
                modificationMode = false;
                updateButtonStates();
                showProgress(false);
                updateStatus("Students loaded: " + students.size());
            });
        }).exceptionally(throwable -> {
            SwingUtilities.invokeLater(() -> {
                showProgress(false);
                logger.error("Failed to load students", throwable);
                updateStatus("Error loading students");
                parentFrame.showErrorDialog("Error", "Failed to load students: " + throwable.getMessage());
            });
            return null;
        });
    }
    
    /**
     * Updates the attendance table with students and existing records.
     */
    private void updateAttendanceTable(List<Student> students, List<AttendanceRecord> records) {
        attendanceTableModel.setRowCount(0);
        
        for (Student student : students) {
            // Find existing record for this student
            AttendanceRecord existingRecord = records.stream()
                    .filter(r -> r.getStudentId() == student.getUserId())
                    .findFirst()
                    .orElse(null);
            
            AttendanceStatus status = existingRecord != null ? existingRecord.getStatus() : AttendanceStatus.PRESENT;
            String remarks = existingRecord != null ? existingRecord.getRemarks() : "";
            String lastModified = existingRecord != null ? 
                    formatDateTime(existingRecord.getMarkedAt()) : "Not marked";
            
            Object[] row = {
                student.getUserId(),
                student.getFullName(),
                student.getStudentNumber(),
                status,
                remarks,
                lastModified
            };
            attendanceTableModel.addRow(row);
        }
    }
    
    /**
     * Marks all students with the specified status.
     */
    private void markAllStatus(AttendanceStatus status) {
        for (int i = 0; i < attendanceTableModel.getRowCount(); i++) {
            attendanceTableModel.setValueAt(status, i, 3);
        }
        updateStatus("Marked all as " + status.getDisplayName());
    }
    
    /**
     * Clears all attendance markings.
     */
    private void clearAllAttendance() {
        for (int i = 0; i < attendanceTableModel.getRowCount(); i++) {
            attendanceTableModel.setValueAt(AttendanceStatus.PRESENT, i, 3);
            attendanceTableModel.setValueAt("", i, 4);
        }
        updateStatus("Attendance cleared");
    }
    
    /**
     * Saves the attendance records.
     */
    private void saveAttendance() {
        if (selectedCourse == null || modificationDate == null) {
            parentFrame.showErrorDialog("Error", "Please load students first");
            return;
        }
        
        // Collect attendance records
        List<AttendanceRecord> records = new ArrayList<>();
        
        for (int i = 0; i < attendanceTableModel.getRowCount(); i++) {
            int studentId = (Integer) attendanceTableModel.getValueAt(i, 0);
            AttendanceStatus status = (AttendanceStatus) attendanceTableModel.getValueAt(i, 3);
            String remarks = (String) attendanceTableModel.getValueAt(i, 4);
            
            AttendanceRecord record = new AttendanceRecord();
            record.setStudentId(studentId);
            record.setCourseId(selectedCourse.getCourseId());
            record.setAttendanceDate(modificationDate);
            record.setClassTime(LocalTime.now());
            record.setStatus(status);
            record.setRemarks(remarks != null ? remarks : "");
            record.setMarkedBy(currentUser.getUserId());
            
            records.add(record);
        }
        
        // Save records
        updateStatus("Saving attendance...");
        showProgress(true);
        
        CompletableFuture.runAsync(() -> {
            try {
                for (AttendanceRecord record : records) {
                    remoteService.markAttendance(sessionToken, record);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).thenRun(() -> {
            SwingUtilities.invokeLater(() -> {
                showProgress(false);
                updateStatus("Attendance saved successfully");
                parentFrame.showInfoDialog("Success", 
                        "Attendance saved successfully for " + records.size() + " students");
                clearAllAttendance();
            });
        }).exceptionally(throwable -> {
            SwingUtilities.invokeLater(() -> {
                showProgress(false);
                logger.error("Failed to save attendance", throwable);
                updateStatus("Error saving attendance");
                parentFrame.showErrorDialog("Error", "Failed to save attendance: " + throwable.getMessage());
            });
            return null;
        });
    }
    
    /**
     * Enters modification mode to modify existing attendance records.
     */
    private void enterModificationMode() {
        if (existingRecords.isEmpty()) {
            parentFrame.showErrorDialog("Info", "No existing records to modify for this date");
            return;
        }
        
        modificationMode = true;
        updateStatus("Modification mode: You can modify records within 24 hours of original entry");
        
        // Validate modification window for each record
        List<String> invalidRecords = new ArrayList<>();
        for (AttendanceRecord record : existingRecords) {
            if (!record.canBeModified()) {
                invalidRecords.add("Student ID " + record.getStudentId() + 
                        " (marked at " + formatDateTime(record.getMarkedAt()) + ")");
            }
        }
        
        if (!invalidRecords.isEmpty()) {
            StringBuilder message = new StringBuilder("The following records cannot be modified (outside 24-hour window):\n");
            for (String invalid : invalidRecords) {
                message.append("- ").append(invalid).append("\n");
            }
            parentFrame.showWarningDialog("Modification Restrictions", message.toString());
        }
        
        updateButtonStates();
    }
    
    /**
     * Updates button states based on current mode and data availability.
     */
    private void updateButtonStates() {
        boolean hasStudents = attendanceTableModel.getRowCount() > 0;
        
        markAllPresentButton.setEnabled(hasStudents && !modificationMode);
        markAllAbsentButton.setEnabled(hasStudents && !modificationMode);
        markAllLateButton.setEnabled(hasStudents && !modificationMode);
        markAllExcusedButton.setEnabled(hasStudents && !modificationMode);
        clearAllButton.setEnabled(hasStudents);
        
        saveAttendanceButton.setEnabled(hasStudents && !modificationMode);
        modifyAttendanceButton.setEnabled(hasStudents && !existingRecords.isEmpty() && !modificationMode);
    }
    
    /**
     * Updates the status label.
     */
    private void updateStatus(String message) {
        statusLabel.setText(message);
    }
    
    /**
     * Shows or hides the progress bar.
     */
    private void showProgress(boolean show) {
        progressBar.setVisible(show);
        if (show) {
            progressBar.setIndeterminate(true);
        }
    }
    
    /**
     * Formats a LocalDateTime for display.
     */
    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "N/A";
        }
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
    
    /**
     * Custom renderer for attendance status with color coding.
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
                            setForeground(new Color(34, 139, 34));
                            break;
                        case ABSENT:
                            setBackground(new Color(255, 220, 220));
                            setForeground(new Color(220, 20, 60));
                            break;
                        case LATE:
                            setBackground(new Color(255, 245, 220));
                            setForeground(new Color(255, 140, 0));
                            break;
                        case EXCUSED:
                            setBackground(new Color(220, 235, 255));
                            setForeground(new Color(70, 130, 180));
                            break;
                        default:
                            setBackground(Color.WHITE);
                            setForeground(Color.BLACK);
                    }
                }
            }
            
            return c;
        }
    }
    
    /**
     * Simple date chooser implementation.
     */
    private static class JDateChooser extends JPanel {
        private java.util.Date date;
        private JTextField dateField;
        private JButton calendarButton;
        
        public JDateChooser() {
            setLayout(new BorderLayout());
            setBackground(Color.WHITE);
            
            dateField = new JTextField();
            dateField.setEditable(false);
            dateField.setBackground(Color.WHITE);
            
            calendarButton = new JButton("...");
            calendarButton.setPreferredSize(new Dimension(30, 30));
            calendarButton.addActionListener(e -> showDatePicker());
            
            add(dateField, BorderLayout.CENTER);
            add(calendarButton, BorderLayout.EAST);
        }
        
        public void setDate(java.util.Date date) {
            this.date = date;
            if (date != null) {
                dateField.setText(new java.text.SimpleDateFormat("yyyy-MM-dd").format(date));
            } else {
                dateField.setText("");
            }
        }
        
        public java.util.Date getDate() {
            return date;
        }
        
        private void showDatePicker() {
            String dateStr = JOptionPane.showInputDialog(this, "Enter date (yyyy-MM-dd):", 
                    dateField.getText());
            if (dateStr != null && !dateStr.trim().isEmpty()) {
                try {
                    java.util.Date newDate = new java.text.SimpleDateFormat("yyyy-MM-dd").parse(dateStr);
                    setDate(newDate);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, "Invalid date format. Please use yyyy-MM-dd");
                }
            }
        }
    }
}
