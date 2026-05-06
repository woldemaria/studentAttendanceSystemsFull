package com.attendance.system.client;

import com.attendance.system.exception.AuthenticationException;
import com.attendance.system.service.AttendanceService;
import com.attendance.system.service.AuthenticationService;
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
 * Login interface for user authentication.
 * Provides credential input, validation, and connection status display.
 */
public class LoginFrame extends JPanel {
    private static final Logger logger = LoggerFactory.getLogger(LoginFrame.class);
    
    private final AttendanceGUI parentFrame;
    
    // Form components
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton clearButton;
    private JButton registerButton;
    private JLabel statusLabel;
    private JProgressBar progressBar;
    private JCheckBox showPasswordCheckBox;
    
    // Validation components
    private JLabel usernameErrorLabel;
    private JLabel passwordErrorLabel;
    
    public LoginFrame(AttendanceGUI parentFrame) {
        this.parentFrame = parentFrame;
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
        usernameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        
        passwordField = new JPasswordField(20);
        passwordField.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        
        // Create buttons
        loginButton = new JButton("Login");
        loginButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        loginButton.setPreferredSize(new Dimension(120, 40));
        loginButton.setBackground(new Color(70, 130, 180));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setBorderPainted(false);
        
        clearButton = new JButton("Clear");
        clearButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        clearButton.setPreferredSize(new Dimension(120, 40));
        clearButton.setBackground(new Color(128, 128, 128));
        clearButton.setForeground(Color.WHITE);
        clearButton.setFocusPainted(false);
        clearButton.setBorderPainted(false);
        
        // Create register button
        registerButton = new JButton("Register");
        registerButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        registerButton.setPreferredSize(new Dimension(120, 40));
        registerButton.setBackground(new Color(34, 139, 34));
        registerButton.setForeground(Color.WHITE);
        registerButton.setFocusPainted(false);
        registerButton.setBorderPainted(false);
        
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
        
        passwordErrorLabel = new JLabel(" ");
        passwordErrorLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        passwordErrorLabel.setForeground(Color.RED);
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
        JLabel titleLabel = new JLabel("Student Attendance System");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        titleLabel.setForeground(new Color(70, 130, 180));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        mainContainer.add(titleLabel, gbc);
        
        // Subtitle
        JLabel subtitleLabel = new JLabel("Please enter your credentials to continue");
        subtitleLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        subtitleLabel.setForeground(Color.GRAY);
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 10, 20, 10);
        mainContainer.add(subtitleLabel, gbc);
        
        // Reset insets and grid width
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        
        // Username label and field
        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        gbc.gridx = 0;
        gbc.gridy = 2;
        mainContainer.add(usernameLabel, gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainContainer.add(usernameField, gbc);
        
        // Username error label
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.insets = new Insets(0, 10, 5, 10);
        mainContainer.add(usernameErrorLabel, gbc);
        
        // Password label and field
        gbc.insets = new Insets(5, 10, 5, 10);
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.NONE;
        mainContainer.add(passwordLabel, gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainContainer.add(passwordField, gbc);
        
        // Password error label
        gbc.gridx = 1;
        gbc.gridy = 5;
        gbc.insets = new Insets(0, 10, 5, 10);
        mainContainer.add(passwordErrorLabel, gbc);
        
        // Show password checkbox
        gbc.insets = new Insets(5, 10, 15, 10);
        gbc.gridy = 6;
        mainContainer.add(showPasswordCheckBox, gbc);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(loginButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(registerButton);
        
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        mainContainer.add(buttonPanel, gbc);
        
        // Status label
        gbc.gridy = 8;
        gbc.insets = new Insets(15, 10, 5, 10);
        mainContainer.add(statusLabel, gbc);
        
        // Progress bar
        gbc.gridy = 9;
        gbc.insets = new Insets(5, 10, 10, 10);
        mainContainer.add(progressBar, gbc);
        
        // Add main container to center
        add(mainContainer, BorderLayout.CENTER);
        
        // Add connection status at bottom
        JPanel connectionPanel = createConnectionStatusPanel();
        add(connectionPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Creates the connection status panel.
     */
    private JPanel createConnectionStatusPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBackground(new Color(240, 240, 240));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        
        JLabel connectionLabel = new JLabel("Server Status: ");
        connectionLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        
        JLabel connectionStatus = new JLabel();
        connectionStatus.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        
        if (parentFrame.isConnected()) {
            connectionStatus.setText("Connected");
            connectionStatus.setForeground(new Color(0, 128, 0));
        } else {
            connectionStatus.setText("Disconnected");
            connectionStatus.setForeground(Color.RED);
        }
        
        panel.add(connectionLabel);
        panel.add(connectionStatus);
        
        return panel;
    }
    
    /**
     * Sets up event handlers for all interactive components.
     */
    private void setupEventHandlers() {
        // Login button action
        loginButton.addActionListener(new LoginActionListener());
        
        // Clear button action
        clearButton.addActionListener(e -> clearForm());
        
        // Register button action
        registerButton.addActionListener(e -> parentFrame.showRegistrationFrame());
        
        // Show password checkbox
        showPasswordCheckBox.addActionListener(e -> {
            if (showPasswordCheckBox.isSelected()) {
                passwordField.setEchoChar((char) 0);
            } else {
                passwordField.setEchoChar('*');
            }
        });
        
        // Enter key handling
        KeyAdapter enterKeyListener = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (loginButton.isEnabled()) {
                        performLogin();
                    }
                }
            }
        };
        
        usernameField.addKeyListener(enterKeyListener);
        passwordField.addKeyListener(enterKeyListener);
        
        // Real-time validation
        usernameField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                validateUsername();
            }
        });
        
        passwordField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                validatePassword();
            }
        });
        
        // Focus handling
        usernameField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                usernameField.selectAll();
            }
        });
        
        passwordField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                passwordField.selectAll();
            }
        });
    }
    
    /**
     * Validates the username field.
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
        
        clearFieldError(usernameField, usernameErrorLabel);
        return true;
    }
    
    /**
     * Validates the password field.
     */
    private boolean validatePassword() {
        char[] password = passwordField.getPassword();
        
        if (password.length == 0) {
            setFieldError(passwordField, passwordErrorLabel, "Password is required");
            return false;
        }
        
        if (password.length < 4) {
            setFieldError(passwordField, passwordErrorLabel, "Password must be at least 4 characters");
            return false;
        }
        
        clearFieldError(passwordField, passwordErrorLabel);
        return true;
    }
    
    /**
     * Sets an error state for a field.
     */
    private void setFieldError(JComponent field, JLabel errorLabel, String message) {
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.RED, 2),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        errorLabel.setText(message);
    }
    
    /**
     * Clears the error state for a field.
     */
    private void clearFieldError(JComponent field, JLabel errorLabel) {
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        errorLabel.setText(" ");
    }
    
    /**
     * Clears all form fields and errors.
     */
    private void clearForm() {
        usernameField.setText("");
        passwordField.setText("");
        showPasswordCheckBox.setSelected(false);
        passwordField.setEchoChar('*');
        
        clearFieldError(usernameField, usernameErrorLabel);
        clearFieldError(passwordField, passwordErrorLabel);
        
        statusLabel.setText(" ");
        statusLabel.setForeground(Color.RED);
        
        usernameField.requestFocus();
    }
    
    /**
     * Performs the login operation.
     */
    private void performLogin() {
        // Validate form
        boolean isValid = validateUsername() && validatePassword();
        
        if (!isValid) {
            return;
        }
        
        // Check connection
        if (!parentFrame.isConnected()) {
            setStatus("Not connected to server. Please wait for connection.", Color.RED);
            return;
        }
        
        String username = usernameField.getText().trim();
        char[] password = passwordField.getPassword();
        
        // Disable form during login
        setFormEnabled(false);
        setStatus("Authenticating...", Color.BLUE);
        showProgress(true);
        
        // Perform authentication asynchronously
        CompletableFuture.supplyAsync(() -> {
            try {
                AttendanceService service = parentFrame.getRemoteService();
                return service.authenticateUser(username, new String(password));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).thenAccept(authenticatedUser -> {
            SwingUtilities.invokeLater(() -> {
                // Clear password from memory
                java.util.Arrays.fill(password, ' ');
                
                setFormEnabled(true);
                showProgress(false);
                setStatus("Login successful!", Color.GREEN);
                
                // Notify parent frame
                parentFrame.handleUserAuthenticated(authenticatedUser);
            });
        }).exceptionally(throwable -> {
            SwingUtilities.invokeLater(() -> {
                // Clear password from memory
                java.util.Arrays.fill(password, ' ');
                
                setFormEnabled(true);
                showProgress(false);
                
                String errorMessage = "Login failed";
                if (throwable.getCause() instanceof AuthenticationException) {
                    errorMessage = throwable.getCause().getMessage();
                } else if (throwable.getCause() instanceof RemoteException) {
                    errorMessage = "Server communication error";
                } else if (throwable.getCause() != null) {
                    errorMessage = throwable.getCause().getMessage();
                }
                
                setStatus(errorMessage, Color.RED);
                passwordField.selectAll();
                passwordField.requestFocus();
                
                logger.warn("Login failed for user: " + username, throwable);
            });
            return null;
        });
    }
    
    /**
     * Sets the status message.
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
     * Enables or disables the form components.
     */
    private void setFormEnabled(boolean enabled) {
        usernameField.setEnabled(enabled);
        passwordField.setEnabled(enabled);
        loginButton.setEnabled(enabled);
        clearButton.setEnabled(enabled);
        registerButton.setEnabled(enabled);
        showPasswordCheckBox.setEnabled(enabled);
    }
    
    /**
     * Sets focus to the username field when the panel becomes visible.
     */
    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            SwingUtilities.invokeLater(() -> {
                usernameField.requestFocus();
            });
        }
    }
    
    /**
     * Action listener for the login button.
     */
    private class LoginActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            performLogin();
        }
    }
}