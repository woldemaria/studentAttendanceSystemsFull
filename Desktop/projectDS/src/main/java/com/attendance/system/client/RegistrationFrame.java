package com.attendance.system.client;

import com.attendance.system.exception.ValidationException;
import com.attendance.system.model.UserRole;
import com.attendance.system.service.AttendanceService;
import com.attendance.system.util.SecurityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.rmi.RemoteException;
import java.util.concurrent.CompletableFuture;

/**
 * Registration interface for new user account creation.
 * Provides form for user registration with validation and role selection.
 * Supports student and teacher self-registration with email verification.
 */
public class RegistrationFrame extends JPanel {
    private static final Logger logger = LoggerFactory.getLogger(RegistrationFrame.class);
    
    private final AttendanceGUI parentFrame;
    private final Runnable onRegistrationSuccess;
    
    // Form components
    private JTextField usernameField;
    private JTextField emailField;
    private JTextField firstNameField;
    private JTextField lastNameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JComboBox<UserRole> roleComboBox;
    private JButton registerButton;
    private JButton cancelButton;
    private JLabel statusLabel;
    private JProgressBar progressBar;
    private JCheckBox showPasswordCheckBox;
    
    // Validation components
    private JLabel usernameErrorLabel;
    private JLabel emailErrorLabel;
    private JLabel passwordErrorLabel;
    private JLabel confirmPasswordErrorLabel;
    private JLabel firstNameErrorLabel;
    private JLabel lastNameErrorLabel;
    
    public RegistrationFrame(AttendanceGUI parentFrame, Runnable onRegistrationSuccess) {
        this.parentFrame = parentFrame;
        this.onRegistrationSuccess = onRegistrationSuccess;
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
        
        // Create form fields
        usernameField = new JTextField(20);
        usernameField.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        usernameField.setBorder(createFieldBorder());
        
        emailField = new JTextField(20);
        emailField.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        emailField.setBorder(createFieldBorder());
        
        firstNameField = new JTextField(20);
        firstNameField.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        firstNameField.setBorder(createFieldBorder());
        
        lastNameField = new JTextField(20);
        lastNameField.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        lastNameField.setBorder(createFieldBorder());
        
        passwordField = new JPasswordField(20);
        passwordField.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        passwordField.setBorder(createFieldBorder());
        
        confirmPasswordField = new JPasswordField(20);
        confirmPasswordField.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        confirmPasswordField.setBorder(createFieldBorder());
        
        // Create role combo box
        roleComboBox = new JComboBox<>(new UserRole[]{UserRole.STUDENT, UserRole.TEACHER});
        roleComboBox.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        roleComboBox.setSelectedItem(UserRole.STUDENT);
        
        // Create buttons
        registerButton = new JButton("Register");
        registerButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        registerButton.setPreferredSize(new Dimension(120, 40));
        registerButton.setBackground(new Color(34, 139, 34));
        registerButton.setForeground(Color.WHITE);
        registerButton.setFocusPainted(false);
        registerButton.setBorderPainted(false);
        
        cancelButton = new JButton("Cancel");
        cancelButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        cancelButton.setPreferredSize(new Dimension(120, 40));
        cancelButton.setBackground(new Color(128, 128, 128));
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setFocusPainted(false);
        cancelButton.setBorderPainted(false);
        
        // Create checkbox for showing password
        showPasswordCheckBox = new JCheckBox("Show password");
        showPasswordCheckBox.setBackground(Color.WHITE);
        
        // Create status and progress components
        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        statusLabel.setForeground(Color.RED);
        
        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);
        progressBar.setPreferredSize(new Dimension(300, 20));
        
        // Create error labels
        usernameErrorLabel = new JLabel(" ");
        usernameErrorLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        usernameErrorLabel.setForeground(Color.RED);
        
        emailErrorLabel = new JLabel(" ");
        emailErrorLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        emailErrorLabel.setForeground(Color.RED);
        
        firstNameErrorLabel = new JLabel(" ");
        firstNameErrorLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        firstNameErrorLabel.setForeground(Color.RED);
        
        lastNameErrorLabel = new JLabel(" ");
        lastNameErrorLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        lastNameErrorLabel.setForeground(Color.RED);
        
        passwordErrorLabel = new JLabel(" ");
        passwordErrorLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        passwordErrorLabel.setForeground(Color.RED);
        
        confirmPasswordErrorLabel = new JLabel(" ");
        confirmPasswordErrorLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        confirmPasswordErrorLabel.setForeground(Color.RED);
    }
    
    /**
     * Creates a standard field border.
     */
    private Border createFieldBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        );
    }
    
    /**
     * Sets up the layout of components.
     */
    private void setupLayout() {
        // Create main container
        JPanel mainContainer = new JPanel(new GridBagLayout());
        mainContainer.setBackground(Color.WHITE);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        // Title
        JLabel titleLabel = new JLabel("Create New Account");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        titleLabel.setForeground(new Color(70, 130, 180));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        mainContainer.add(titleLabel, gbc);
        
        // Subtitle
        JLabel subtitleLabel = new JLabel("Fill in the form below to create your account");
        subtitleLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        subtitleLabel.setForeground(Color.GRAY);
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 10, 20, 10);
        mainContainer.add(subtitleLabel, gbc);
        
        // Reset insets and grid width
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        
        // First Name
        JLabel firstNameLabel = new JLabel("First Name:");
        firstNameLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        gbc.gridx = 0;
        gbc.gridy = 2;
        mainContainer.add(firstNameLabel, gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainContainer.add(firstNameField, gbc);
        
        // First Name error label
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.insets = new Insets(0, 10, 5, 10);
        mainContainer.add(firstNameErrorLabel, gbc);
        
        // Last Name
        gbc.insets = new Insets(5, 10, 5, 10);
        JLabel lastNameLabel = new JLabel("Last Name:");
        lastNameLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.NONE;
        mainContainer.add(lastNameLabel, gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainContainer.add(lastNameField, gbc);
        
        // Last Name error label
        gbc.gridx = 1;
        gbc.gridy = 5;
        gbc.insets = new Insets(0, 10, 5, 10);
        mainContainer.add(lastNameErrorLabel, gbc);
        
        // Username
        gbc.insets = new Insets(5, 10, 5, 10);
        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.fill = GridBagConstraints.NONE;
        mainContainer.add(usernameLabel, gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainContainer.add(usernameField, gbc);
        
        // Username error label
        gbc.gridx = 1;
        gbc.gridy = 7;
        gbc.insets = new Insets(0, 10, 5, 10);
        mainContainer.add(usernameErrorLabel, gbc);
        
        // Email
        gbc.insets = new Insets(5, 10, 5, 10);
        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.fill = GridBagConstraints.NONE;
        mainContainer.add(emailLabel, gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainContainer.add(emailField, gbc);
        
        // Email error label
        gbc.gridx = 1;
        gbc.gridy = 9;
        gbc.insets = new Insets(0, 10, 5, 10);
        mainContainer.add(emailErrorLabel, gbc);
        
        // Role
        gbc.insets = new Insets(5, 10, 5, 10);
        JLabel roleLabel = new JLabel("Account Type:");
        roleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        gbc.gridx = 0;
        gbc.gridy = 10;
        gbc.fill = GridBagConstraints.NONE;
        mainContainer.add(roleLabel, gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainContainer.add(roleComboBox, gbc);
        
        // Password
        gbc.insets = new Insets(5, 10, 5, 10);
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        gbc.gridx = 0;
        gbc.gridy = 11;
        gbc.fill = GridBagConstraints.NONE;
        mainContainer.add(passwordLabel, gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainContainer.add(passwordField, gbc);
        
        // Password error label
        gbc.gridx = 1;
        gbc.gridy = 12;
        gbc.insets = new Insets(0, 10, 5, 10);
        mainContainer.add(passwordErrorLabel, gbc);
        
        // Confirm Password
        gbc.insets = new Insets(5, 10, 5, 10);
        JLabel confirmPasswordLabel = new JLabel("Confirm Password:");
        confirmPasswordLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        gbc.gridx = 0;
        gbc.gridy = 13;
        gbc.fill = GridBagConstraints.NONE;
        mainContainer.add(confirmPasswordLabel, gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainContainer.add(confirmPasswordField, gbc);
        
        // Confirm Password error label
        gbc.gridx = 1;
        gbc.gridy = 14;
        gbc.insets = new Insets(0, 10, 5, 10);
        mainContainer.add(confirmPasswordErrorLabel, gbc);
        
        // Show password checkbox
        gbc.insets = new Insets(5, 10, 15, 10);
        gbc.gridy = 15;
        mainContainer.add(showPasswordCheckBox, gbc);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(registerButton);
        buttonPanel.add(cancelButton);
        
        gbc.gridx = 0;
        gbc.gridy = 16;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        mainContainer.add(buttonPanel, gbc);
        
        // Status label
        gbc.gridy = 17;
        gbc.insets = new Insets(15, 10, 5, 10);
        mainContainer.add(statusLabel, gbc);
        
        // Progress bar
        gbc.gridy = 18;
        gbc.insets = new Insets(5, 10, 10, 10);
        mainContainer.add(progressBar, gbc);
        
        // Add main container to center with scroll pane
        JScrollPane scrollPane = new JScrollPane(mainContainer);
        scrollPane.setBackground(Color.WHITE);
        add(scrollPane, BorderLayout.CENTER);
    }
    
    /**
     * Sets up event handlers for all interactive components.
     */
    private void setupEventHandlers() {
        // Register button action
        registerButton.addActionListener(new RegisterActionListener());
        
        // Cancel button action
        cancelButton.addActionListener(e -> {
            clearForm();
            parentFrame.showLoginFrame();
        });
        
        // Show password checkbox
        showPasswordCheckBox.addActionListener(e -> {
            char echoChar = showPasswordCheckBox.isSelected() ? (char) 0 : '*';
            passwordField.setEchoChar(echoChar);
            confirmPasswordField.setEchoChar(echoChar);
        });
        
        // Enter key handling
        KeyAdapter enterKeyListener = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (registerButton.isEnabled()) {
                        performRegistration();
                    }
                }
            }
        };
        
        usernameField.addKeyListener(enterKeyListener);
        emailField.addKeyListener(enterKeyListener);
        passwordField.addKeyListener(enterKeyListener);
        confirmPasswordField.addKeyListener(enterKeyListener);
        
        // Real-time validation
        usernameField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                validateUsername();
            }
        });
        
        emailField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                validateEmail();
            }
        });
        
        firstNameField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                validateFirstName();
            }
        });
        
        lastNameField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                validateLastName();
            }
        });
        
        passwordField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                validatePassword();
            }
        });
        
        confirmPasswordField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                validateConfirmPassword();
            }
        });
    }
    
    /**
     * Validates username field.
     */
    private boolean validateUsername() {
        String username = usernameField.getText().trim();
        
        if (username.isEmpty()) {
            setFieldError(usernameField, usernameErrorLabel, "Username is required");
            return false;
        }
        
        if (username.length() < 3) {
            setFieldError(usernameField, usernameErrorLabel, "Username must be at least 3 characters");
            return false;
        }
        
        if (username.length() > 50) {
            setFieldError(usernameField, usernameErrorLabel, "Username must not exceed 50 characters");
            return false;
        }
        
        if (!username.matches("^[a-zA-Z0-9._-]+$")) {
            setFieldError(usernameField, usernameErrorLabel, "Username can only contain letters, numbers, dots, underscores, and hyphens");
            return false;
        }
        
        clearFieldError(usernameField, usernameErrorLabel);
        return true;
    }
    
    /**
     * Validates email field.
     */
    private boolean validateEmail() {
        String email = emailField.getText().trim();
        
        if (email.isEmpty()) {
            setFieldError(emailField, emailErrorLabel, "Email is required");
            return false;
        }
        
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            setFieldError(emailField, emailErrorLabel, "Invalid email format");
            return false;
        }
        
        clearFieldError(emailField, emailErrorLabel);
        return true;
    }
    
    /**
     * Validates first name field.
     */
    private boolean validateFirstName() {
        String firstName = firstNameField.getText().trim();
        
        if (firstName.isEmpty()) {
            setFieldError(firstNameField, firstNameErrorLabel, "First name is required");
            return false;
        }
        
        if (firstName.length() > 50) {
            setFieldError(firstNameField, firstNameErrorLabel, "First name must not exceed 50 characters");
            return false;
        }
        
        clearFieldError(firstNameField, firstNameErrorLabel);
        return true;
    }
    
    /**
     * Validates last name field.
     */
    private boolean validateLastName() {
        String lastName = lastNameField.getText().trim();
        
        if (lastName.isEmpty()) {
            setFieldError(lastNameField, lastNameErrorLabel, "Last name is required");
            return false;
        }
        
        if (lastName.length() > 50) {
            setFieldError(lastNameField, lastNameErrorLabel, "Last name must not exceed 50 characters");
            return false;
        }
        
        clearFieldError(lastNameField, lastNameErrorLabel);
        return true;
    }
    
    /**
     * Validates password field.
     */
    private boolean validatePassword() {
        String password = new String(passwordField.getPassword());
        
        if (password.isEmpty()) {
            setFieldError(passwordField, passwordErrorLabel, "Password is required");
            return false;
        }
        
        if (password.length() < 8) {
            setFieldError(passwordField, passwordErrorLabel, "Password must be at least 8 characters");
            return false;
        }
        
        if (!password.matches(".*[A-Z].*")) {
            setFieldError(passwordField, passwordErrorLabel, "Password must contain at least one uppercase letter");
            return false;
        }
        
        if (!password.matches(".*[a-z].*")) {
            setFieldError(passwordField, passwordErrorLabel, "Password must contain at least one lowercase letter");
            return false;
        }
        
        if (!password.matches(".*\\d.*")) {
            setFieldError(passwordField, passwordErrorLabel, "Password must contain at least one digit");
            return false;
        }
        
        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) {
            setFieldError(passwordField, passwordErrorLabel, "Password must contain at least one special character");
            return false;
        }
        
        clearFieldError(passwordField, passwordErrorLabel);
        return true;
    }
    
    /**
     * Validates confirm password field.
     */
    private boolean validateConfirmPassword() {
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());
        
        if (confirmPassword.isEmpty()) {
            setFieldError(confirmPasswordField, confirmPasswordErrorLabel, "Please confirm your password");
            return false;
        }
        
        if (!password.equals(confirmPassword)) {
            setFieldError(confirmPasswordField, confirmPasswordErrorLabel, "Passwords do not match");
            return false;
        }
        
        clearFieldError(confirmPasswordField, confirmPasswordErrorLabel);
        return true;
    }
    
    /**
     * Sets error state for a field.
     */
    private void setFieldError(JComponent field, JLabel errorLabel, String message) {
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.RED, 2),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        errorLabel.setText(message);
    }
    
    /**
     * Clears error state for a field.
     */
    private void clearFieldError(JComponent field, JLabel errorLabel) {
        field.setBorder(createFieldBorder());
        errorLabel.setText(" ");
    }
    
    /**
     * Clears the registration form.
     */
    private void clearForm() {
        usernameField.setText("");
        emailField.setText("");
        firstNameField.setText("");
        lastNameField.setText("");
        passwordField.setText("");
        confirmPasswordField.setText("");
        roleComboBox.setSelectedItem(UserRole.STUDENT);
        showPasswordCheckBox.setSelected(false);
        statusLabel.setText(" ");
        statusLabel.setForeground(Color.RED);
        
        clearFieldError(usernameField, usernameErrorLabel);
        clearFieldError(emailField, emailErrorLabel);
        clearFieldError(firstNameField, firstNameErrorLabel);
        clearFieldError(lastNameField, lastNameErrorLabel);
        clearFieldError(passwordField, passwordErrorLabel);
        clearFieldError(confirmPasswordField, confirmPasswordErrorLabel);
    }
    
    /**
     * Performs the registration operation.
     */
    private void performRegistration() {
        // Validate all fields
        boolean isValid = validateUsername() && validateEmail() && validateFirstName() &&
                validateLastName() && validatePassword() && validateConfirmPassword();
        
        if (!isValid) {
            setStatus("Please fix the errors above", Color.RED);
            return;
        }
        
        setFormEnabled(false);
        showProgress(true);
        setStatus("Creating account...", Color.BLUE);
        
        // Perform registration asynchronously
        CompletableFuture.runAsync(() -> {
            try {
                AttendanceService service = parentFrame.getAttendanceService();
                
                String username = usernameField.getText().trim();
                String email = emailField.getText().trim();
                String firstName = firstNameField.getText().trim();
                String lastName = lastNameField.getText().trim();
                String password = new String(passwordField.getPassword());
                UserRole role = (UserRole) roleComboBox.getSelectedItem();
                
                // Call remote registration service
                service.registerUser(username, email, firstName, lastName, password, role);
                
                // Success
                SwingUtilities.invokeLater(() -> {
                    setStatus("Account created successfully! Redirecting to login...", new Color(0, 128, 0));
                    showProgress(false);
                    
                    // Clear form and return to login after 2 seconds
                    Timer timer = new Timer(2000, e -> {
                        clearForm();
                        if (onRegistrationSuccess != null) {
                            onRegistrationSuccess.run();
                        }
                    });
                    timer.setRepeats(false);
                    timer.start();
                });
                
                logger.info("User registered successfully: {}", username);
                
            } catch (ValidationException e) {
                SwingUtilities.invokeLater(() -> {
                    setStatus("Registration failed: " + e.getMessage(), Color.RED);
                    showProgress(false);
                    setFormEnabled(true);
                });
                logger.warn("Validation error during registration: {}", e.getMessage());
                
            } catch (RemoteException e) {
                SwingUtilities.invokeLater(() -> {
                    setStatus("Server error: Unable to create account. Please try again.", Color.RED);
                    showProgress(false);
                    setFormEnabled(true);
                });
                logger.error("Remote service error during registration", e);
                
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    setStatus("Error: " + e.getMessage(), Color.RED);
                    showProgress(false);
                    setFormEnabled(true);
                });
                logger.error("Unexpected error during registration", e);
            }
        });
    }
    
    /**
     * Sets the status message and color.
     */
    private void setStatus(String message, Color color) {
        statusLabel.setText(message);
        statusLabel.setForeground(color);
    }
    
    /**
     * Shows or hides the progress bar.
     */
    private void showProgress(boolean show) {
        progressBar.setVisible(show);
    }
    
    /**
     * Enables or disables the form.
     */
    private void setFormEnabled(boolean enabled) {
        usernameField.setEnabled(enabled);
        emailField.setEnabled(enabled);
        firstNameField.setEnabled(enabled);
        lastNameField.setEnabled(enabled);
        passwordField.setEnabled(enabled);
        confirmPasswordField.setEnabled(enabled);
        roleComboBox.setEnabled(enabled);
        registerButton.setEnabled(enabled);
        cancelButton.setEnabled(enabled);
        showPasswordCheckBox.setEnabled(enabled);
    }
    
    /**
     * Inner class for handling registration button action.
     */
    private class RegisterActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            performRegistration();
        }
    }
}
