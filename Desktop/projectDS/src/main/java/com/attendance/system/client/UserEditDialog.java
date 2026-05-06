package com.attendance.system.client;

import com.attendance.system.model.*;
import com.attendance.system.service.AttendanceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.CompletableFuture;

/**
 * Dialog for creating and editing user accounts.
 */
public class UserEditDialog extends JDialog {
    private static final Logger logger = LoggerFactory.getLogger(UserEditDialog.class);
    
    private final AttendanceGUI parentFrame;
    private final User editingUser;
    private final AttendanceService remoteService;
    private final String sessionToken;
    private boolean userSaved = false;
    
    // Form components
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JTextField emailField;
    private JTextField firstNameField;
    private JTextField lastNameField;
    private JComboBox<UserRole> roleComboBox;
    private JCheckBox activeCheckBox;
    
    // Role-specific fields
    private JTextField studentNumberField;
    private JTextField programField;
    private JSpinner yearLevelSpinner;
    private JTextField employeeIdField;
    private JTextField departmentField;
    private JTextField specializationField;
    
    // Panels for role-specific fields
    private JPanel studentFieldsPanel;
    private JPanel teacherFieldsPanel;
    
    // Buttons
    private JButton saveButton;
    private JButton cancelButton;
    
    public UserEditDialog(AttendanceGUI parentFrame, User editingUser, 
                         AttendanceService remoteService, String sessionToken) {
        super(parentFrame, editingUser == null ? "Add User" : "Edit User", true);
        
        this.parentFrame = parentFrame;
        this.editingUser = editingUser;
        this.remoteService = remoteService;
        this.sessionToken = sessionToken;
        
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        
        if (editingUser != null) {
            populateFields();
        }
        
        pack();
        setLocationRelativeTo(parentFrame);
    }
    
    /**
     * Initializes all form components.
     */
    private void initializeComponents() {
        // Basic user fields
        usernameField = new JTextField(20);
        passwordField = new JPasswordField(20);
        confirmPasswordField = new JPasswordField(20);
        emailField = new JTextField(20);
        firstNameField = new JTextField(20);
        lastNameField = new JTextField(20);
        
        roleComboBox = new JComboBox<>(UserRole.values());
        roleComboBox.addActionListener(e -> updateRoleSpecificFields());
        
        activeCheckBox = new JCheckBox("Active", true);
        
        // Student-specific fields
        studentNumberField = new JTextField(15);
        programField = new JTextField(15);
        yearLevelSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 4, 1));
        
        // Teacher-specific fields
        employeeIdField = new JTextField(15);
        departmentField = new JTextField(15);
        specializationField = new JTextField(15);
        
        // Buttons
        saveButton = new JButton("Save");
        cancelButton = new JButton("Cancel");
        
        // Create role-specific panels
        createRoleSpecificPanels();
    }
    
    /**
     * Creates panels for role-specific fields.
     */
    private void createRoleSpecificPanels() {
        // Student fields panel
        studentFieldsPanel = new JPanel(new GridBagLayout());
        studentFieldsPanel.setBorder(BorderFactory.createTitledBorder("Student Information"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        gbc.gridx = 0; gbc.gridy = 0;
        studentFieldsPanel.add(new JLabel("Student Number:"), gbc);
        gbc.gridx = 1;
        studentFieldsPanel.add(studentNumberField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        studentFieldsPanel.add(new JLabel("Program:"), gbc);
        gbc.gridx = 1;
        studentFieldsPanel.add(programField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        studentFieldsPanel.add(new JLabel("Year Level:"), gbc);
        gbc.gridx = 1;
        studentFieldsPanel.add(yearLevelSpinner, gbc);
        
        // Teacher fields panel
        teacherFieldsPanel = new JPanel(new GridBagLayout());
        teacherFieldsPanel.setBorder(BorderFactory.createTitledBorder("Teacher Information"));
        
        gbc.gridx = 0; gbc.gridy = 0;
        teacherFieldsPanel.add(new JLabel("Employee ID:"), gbc);
        gbc.gridx = 1;
        teacherFieldsPanel.add(employeeIdField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        teacherFieldsPanel.add(new JLabel("Department:"), gbc);
        gbc.gridx = 1;
        teacherFieldsPanel.add(departmentField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        teacherFieldsPanel.add(new JLabel("Specialization:"), gbc);
        gbc.gridx = 1;
        teacherFieldsPanel.add(specializationField, gbc);
        
        // Initially hide both panels
        studentFieldsPanel.setVisible(false);
        teacherFieldsPanel.setVisible(false);
    }
    
    /**
     * Sets up the dialog layout.
     */
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Main form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Basic user information
        int row = 0;
        
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Username:*"), gbc);
        gbc.gridx = 1;
        formPanel.add(usernameField, gbc);
        
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Password:*"), gbc);
        gbc.gridx = 1;
        formPanel.add(passwordField, gbc);
        
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Confirm Password:*"), gbc);
        gbc.gridx = 1;
        formPanel.add(confirmPasswordField, gbc);
        
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Email:*"), gbc);
        gbc.gridx = 1;
        formPanel.add(emailField, gbc);
        
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("First Name:*"), gbc);
        gbc.gridx = 1;
        formPanel.add(firstNameField, gbc);
        
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Last Name:*"), gbc);
        gbc.gridx = 1;
        formPanel.add(lastNameField, gbc);
        
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Role:*"), gbc);
        gbc.gridx = 1;
        formPanel.add(roleComboBox, gbc);
        
        row++;
        gbc.gridx = 1; gbc.gridy = row;
        formPanel.add(activeCheckBox, gbc);
        
        // Role-specific fields container
        JPanel roleFieldsContainer = new JPanel(new CardLayout());
        roleFieldsContainer.add(new JPanel(), "EMPTY"); // Empty panel
        roleFieldsContainer.add(studentFieldsPanel, "STUDENT");
        roleFieldsContainer.add(teacherFieldsPanel, "TEACHER");
        
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(roleFieldsContainer, gbc);
        
        add(formPanel, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
        
        // Store reference to role fields container for later use
        putClientProperty("roleFieldsContainer", roleFieldsContainer);
    }
    
    /**
     * Sets up event handlers.
     */
    private void setupEventHandlers() {
        saveButton.addActionListener(new SaveActionListener());
        cancelButton.addActionListener(e -> {
            userSaved = false;
            dispose();
        });
        
        // Set default button
        getRootPane().setDefaultButton(saveButton);
        
        // ESC key to cancel
        KeyStroke escapeKeyStroke = KeyStroke.getKeyStroke("ESCAPE");
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escapeKeyStroke, "ESCAPE");
        getRootPane().getActionMap().put("ESCAPE", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cancelButton.doClick();
            }
        });
    }
    
    /**
     * Updates role-specific fields based on selected role.
     */
    private void updateRoleSpecificFields() {
        UserRole selectedRole = (UserRole) roleComboBox.getSelectedItem();
        JPanel container = (JPanel) getClientProperty("roleFieldsContainer");
        CardLayout cardLayout = (CardLayout) container.getLayout();
        
        if (selectedRole == UserRole.STUDENT) {
            cardLayout.show(container, "STUDENT");
        } else if (selectedRole == UserRole.TEACHER) {
            cardLayout.show(container, "TEACHER");
        } else {
            cardLayout.show(container, "EMPTY");
        }
        
        pack();
    }
    
    /**
     * Populates fields when editing an existing user.
     */
    private void populateFields() {
        usernameField.setText(editingUser.getUsername());
        usernameField.setEnabled(false); // Don't allow username changes
        
        // Don't populate password fields for security
        passwordField.setText("");
        confirmPasswordField.setText("");
        
        emailField.setText(editingUser.getEmail());
        firstNameField.setText(editingUser.getFirstName());
        lastNameField.setText(editingUser.getLastName());
        roleComboBox.setSelectedItem(editingUser.getRole());
        activeCheckBox.setSelected(editingUser.isActive());
        
        // Populate role-specific fields
        if (editingUser instanceof Student) {
            Student student = (Student) editingUser;
            studentNumberField.setText(student.getStudentNumber());
            programField.setText(student.getProgram());
            yearLevelSpinner.setValue(student.getYearLevel());
        } else if (editingUser instanceof Teacher) {
            Teacher teacher = (Teacher) editingUser;
            employeeIdField.setText(teacher.getEmployeeId());
            departmentField.setText(teacher.getDepartment());
            specializationField.setText(teacher.getSpecialization());
        }
        
        updateRoleSpecificFields();
    }
    
    /**
     * Validates the form data.
     */
    private boolean validateForm() {
        StringBuilder errors = new StringBuilder();
        
        // Required fields validation
        if (usernameField.getText().trim().isEmpty()) {
            errors.append("Username is required.\n");
        }
        
        if (editingUser == null && passwordField.getPassword().length == 0) {
            errors.append("Password is required.\n");
        }
        
        if (passwordField.getPassword().length > 0) {
            if (passwordField.getPassword().length < 6) {
                errors.append("Password must be at least 6 characters.\n");
            }
            
            if (!java.util.Arrays.equals(passwordField.getPassword(), confirmPasswordField.getPassword())) {
                errors.append("Passwords do not match.\n");
            }
        }
        
        if (emailField.getText().trim().isEmpty()) {
            errors.append("Email is required.\n");
        } else if (!isValidEmail(emailField.getText().trim())) {
            errors.append("Invalid email format.\n");
        }
        
        if (firstNameField.getText().trim().isEmpty()) {
            errors.append("First name is required.\n");
        }
        
        if (lastNameField.getText().trim().isEmpty()) {
            errors.append("Last name is required.\n");
        }
        
        // Role-specific validation
        UserRole selectedRole = (UserRole) roleComboBox.getSelectedItem();
        if (selectedRole == UserRole.STUDENT) {
            if (studentNumberField.getText().trim().isEmpty()) {
                errors.append("Student number is required.\n");
            }
            if (programField.getText().trim().isEmpty()) {
                errors.append("Program is required.\n");
            }
        } else if (selectedRole == UserRole.TEACHER) {
            if (employeeIdField.getText().trim().isEmpty()) {
                errors.append("Employee ID is required.\n");
            }
            if (departmentField.getText().trim().isEmpty()) {
                errors.append("Department is required.\n");
            }
        }
        
        if (errors.length() > 0) {
            JOptionPane.showMessageDialog(this, errors.toString(), "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        return true;
    }
    
    /**
     * Simple email validation.
     */
    private boolean isValidEmail(String email) {
        return email.contains("@") && email.contains(".");
    }
    
    /**
     * Creates a user object from form data.
     */
    private User createUserFromForm() {
        UserRole selectedRole = (UserRole) roleComboBox.getSelectedItem();
        User user;
        
        switch (selectedRole) {
            case STUDENT:
                Student student = new Student();
                student.setStudentNumber(studentNumberField.getText().trim());
                student.setProgram(programField.getText().trim());
                student.setYearLevel((Integer) yearLevelSpinner.getValue());
                user = student;
                break;
                
            case TEACHER:
                Teacher teacher = new Teacher();
                teacher.setEmployeeId(employeeIdField.getText().trim());
                teacher.setDepartment(departmentField.getText().trim());
                teacher.setSpecialization(specializationField.getText().trim());
                user = teacher;
                break;
                
            case ADMIN:
                user = new Admin();
                break;
                
            default:
                throw new IllegalArgumentException("Unknown role: " + selectedRole);
        }
        
        // Set common fields
        if (editingUser != null) {
            user.setUserId(editingUser.getUserId());
            user.setCreatedAt(editingUser.getCreatedAt());
        }
        
        user.setUsername(usernameField.getText().trim());
        user.setEmail(emailField.getText().trim());
        user.setFirstName(firstNameField.getText().trim());
        user.setLastName(lastNameField.getText().trim());
        user.setRole(selectedRole);
        user.setActive(activeCheckBox.isSelected());
        
        return user;
    }
    
    /**
     * Saves the user.
     */
    private void saveUser() {
        if (!validateForm()) {
            return;
        }
        
        User user = createUserFromForm();
        String password = new String(passwordField.getPassword());
        
        // Disable form during save
        setFormEnabled(false);
        
        CompletableFuture.supplyAsync(() -> {
            try {
                if (editingUser == null) {
                    // Creating new user
                    return remoteService.createUser(sessionToken, user);
                } else {
                    // Updating existing user
                    boolean success = remoteService.updateUser(sessionToken, user);
                    
                    // Update password if provided
                    if (!password.isEmpty()) {
                        // Note: This would require a separate method to update password
                        // For now, we'll just return the update result
                    }
                    
                    return success;
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).thenAccept(success -> {
            SwingUtilities.invokeLater(() -> {
                setFormEnabled(true);
                
                if (success) {
                    userSaved = true;
                    JOptionPane.showMessageDialog(this, 
                            editingUser == null ? "User created successfully!" : "User updated successfully!",
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, 
                            "Failed to save user. Please try again.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
        }).exceptionally(throwable -> {
            SwingUtilities.invokeLater(() -> {
                setFormEnabled(true);
                logger.error("Failed to save user", throwable);
                
                String errorMessage = "Failed to save user: " + throwable.getMessage();
                if (throwable.getCause() != null) {
                    errorMessage = throwable.getCause().getMessage();
                }
                
                JOptionPane.showMessageDialog(this, errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
            });
            return null;
        });
    }
    
    /**
     * Enables or disables form components.
     */
    private void setFormEnabled(boolean enabled) {
        usernameField.setEnabled(enabled && editingUser == null);
        passwordField.setEnabled(enabled);
        confirmPasswordField.setEnabled(enabled);
        emailField.setEnabled(enabled);
        firstNameField.setEnabled(enabled);
        lastNameField.setEnabled(enabled);
        roleComboBox.setEnabled(enabled);
        activeCheckBox.setEnabled(enabled);
        
        studentNumberField.setEnabled(enabled);
        programField.setEnabled(enabled);
        yearLevelSpinner.setEnabled(enabled);
        employeeIdField.setEnabled(enabled);
        departmentField.setEnabled(enabled);
        specializationField.setEnabled(enabled);
        
        saveButton.setEnabled(enabled);
        cancelButton.setEnabled(enabled);
    }
    
    /**
     * Returns whether the user was saved.
     */
    public boolean isUserSaved() {
        return userSaved;
    }
    
    /**
     * Action listener for the save button.
     */
    private class SaveActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            saveUser();
        }
    }
}