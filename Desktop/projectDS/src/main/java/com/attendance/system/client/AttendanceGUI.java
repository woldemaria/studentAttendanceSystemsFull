package com.attendance.system.client;

import com.attendance.system.exception.AuthenticationException;
import com.attendance.system.exception.RemoteServiceException;
import com.attendance.system.model.User;
import com.attendance.system.model.UserRole;
import com.attendance.system.service.AttendanceService;
import com.attendance.system.service.AuthenticationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Main GUI application window for the Student Attendance System.
 * Manages RMI connection, user authentication, and dashboard switching.
 */
public class AttendanceGUI extends JFrame {
    private static final Logger logger = LoggerFactory.getLogger(AttendanceGUI.class);
    
    // Application constants
    private static final String APP_TITLE = "Student Attendance System";
    private static final String DEFAULT_SERVER_URL = "rmi://localhost:1100/AttendanceService";
    private static final int RECONNECT_INTERVAL_SECONDS = 5;
    private static final int MAX_RECONNECT_ATTEMPTS = 10;
    
    // Application state
    private AttendanceService remoteService;
    private AuthenticationService.AuthenticatedUser currentUser;
    private String serverUrl;
    private boolean isConnected;
    private int reconnectAttempts;
    
    // GUI components
    private JPanel mainPanel;
    private JPanel currentPanel;
    private JLabel statusLabel;
    private JProgressBar connectionProgress;
    private Timer reconnectTimer;
    
    // Dashboard panels
    private LoginFrame loginFrame;
    private RegistrationFrame registrationFrame;
    private AdminDashboard adminDashboard;
    private TeacherDashboard teacherDashboard;
    private StudentDashboard studentDashboard;
    
    public AttendanceGUI() {
        this(DEFAULT_SERVER_URL);
    }
    
    public AttendanceGUI(String serverUrl) {
        this.serverUrl = serverUrl;
        this.isConnected = false;
        this.reconnectAttempts = 0;
        
        initializeGUI();
        initializeConnection();
    }
    
    /**
     * Initializes the main GUI components and layout.
     */
    private void initializeGUI() {
        setTitle(APP_TITLE);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        
        // Set application icon
        try {
            setIconImage(createApplicationIcon());
        } catch (Exception e) {
            logger.warn("Could not set application icon", e);
        }
        
        // Create main layout
        setLayout(new BorderLayout());
        
        // Create main panel with card layout for switching between views
        mainPanel = new JPanel(new CardLayout());
        add(mainPanel, BorderLayout.CENTER);
        
        // Create status bar
        createStatusBar();
        
        // Set up window close handler
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handleApplicationExit();
            }
        });
        
        // Apply look and feel
        applyLookAndFeel();
        
        logger.info("GUI initialized successfully");
    }
    
    /**
     * Creates the status bar at the bottom of the window.
     */
    private void createStatusBar() {
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBorder(BorderFactory.createLoweredBevelBorder());
        statusBar.setPreferredSize(new Dimension(0, 25));
        
        statusLabel = new JLabel("Initializing...");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        statusBar.add(statusLabel, BorderLayout.WEST);
        
        connectionProgress = new JProgressBar();
        connectionProgress.setIndeterminate(true);
        connectionProgress.setVisible(false);
        connectionProgress.setPreferredSize(new Dimension(150, 20));
        statusBar.add(connectionProgress, BorderLayout.EAST);
        
        add(statusBar, BorderLayout.SOUTH);
    }
    
    private void applyLookAndFeel() {
        try {
            // Force a cross-platform Light Look And Feel (Nimbus) instead of System 
            // to avoid dark-mode clashing with white backgrounds.
            boolean nimbusSet = false;
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    nimbusSet = true;
                    break;
                }
            }
            if (!nimbusSet) {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            }
            SwingUtilities.updateComponentTreeUI(this);
        } catch (Exception e) {
            logger.warn("Could not set system look and feel", e);
        }
    }
    
    /**
     * Creates a simple application icon.
     */
    private Image createApplicationIcon() {
        // Create a simple 32x32 icon
        BufferedImage icon = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = icon.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw a simple attendance book icon
        g2d.setColor(new Color(70, 130, 180));
        g2d.fillRoundRect(4, 4, 24, 20, 4, 4);
        
        g2d.setColor(Color.WHITE);
        g2d.fillRect(8, 8, 16, 2);
        g2d.fillRect(8, 12, 16, 2);
        g2d.fillRect(8, 16, 16, 2);
        
        g2d.dispose();
        return icon;
    }
    
    /**
     * Initializes the RMI connection to the server.
     */
    public void initializeConnection() {
        updateStatus("Connecting to server...");
        showProgress(true);
        
        CompletableFuture.supplyAsync(() -> {
            try {
                logger.info("Attempting to connect to server: " + serverUrl);
                AttendanceService service = (AttendanceService) Naming.lookup(serverUrl);
                
                // Test the connection
                service.isHealthy();
                
                return service;
            } catch (Exception e) {
                logger.error("Failed to connect to server", e);
                throw new RuntimeException(e);
            }
        }).thenAccept(service -> {
            SwingUtilities.invokeLater(() -> {
                this.remoteService = service;
                this.isConnected = true;
                this.reconnectAttempts = 0;
                
                updateStatus("Connected to server");
                showProgress(false);
                showLoginScreen();
                
                logger.info("Successfully connected to server");
            });
        }).exceptionally(throwable -> {
            SwingUtilities.invokeLater(() -> {
                handleConnectionFailure(throwable);
            });
            return null;
        });
    }
    
    /**
     * Handles connection failure and attempts reconnection.
     */
    private void handleConnectionFailure(Throwable throwable) {
        this.isConnected = false;
        reconnectAttempts++;
        
        String errorMessage = "Connection failed";
        if (throwable.getCause() != null) {
            errorMessage += ": " + throwable.getCause().getMessage();
        }
        
        updateStatus(errorMessage);
        showProgress(false);
        
        if (reconnectAttempts < MAX_RECONNECT_ATTEMPTS) {
            updateStatus(String.format("Reconnecting in %d seconds... (Attempt %d/%d)", 
                    RECONNECT_INTERVAL_SECONDS, reconnectAttempts, MAX_RECONNECT_ATTEMPTS));
            
            if (reconnectTimer != null) {
                reconnectTimer.stop();
            }
            
            reconnectTimer = new Timer(RECONNECT_INTERVAL_SECONDS * 1000, e -> {
                reconnectTimer.stop();
                initializeConnection();
            });
            reconnectTimer.setRepeats(false);
            reconnectTimer.start();
        } else {
            showConnectionErrorDialog();
        }
    }
    
    /**
     * Shows the connection error dialog with retry options.
     */
    private void showConnectionErrorDialog() {
        int option = JOptionPane.showOptionDialog(
                this,
                "Unable to connect to the attendance server.\n" +
                "Please check your network connection and server status.\n\n" +
                "Would you like to retry or exit the application?",
                "Connection Error",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.ERROR_MESSAGE,
                null,
                new String[]{"Retry", "Exit"},
                "Retry"
        );
        
        if (option == JOptionPane.YES_OPTION) {
            reconnectAttempts = 0;
            initializeConnection();
        } else {
            System.exit(1);
        }
    }
    
    /**
     * Shows the login screen.
     */
    public void showLoginScreen() {
        if (loginFrame == null) {
            loginFrame = new LoginFrame(this);
        }
        
        switchToPanel(loginFrame, "login");
        updateStatus("Please log in");
    }
    
    /**
     * Shows the registration screen.
     */
    public void showRegistrationFrame() {
        if (registrationFrame == null) {
            registrationFrame = new RegistrationFrame(this, this::showLoginScreen);
        }
        
        switchToPanel(registrationFrame, "registration");
        updateStatus("Create a new account");
    }
    
    /**
     * Handles successful user authentication.
     */
    public void handleUserAuthenticated(AuthenticationService.AuthenticatedUser authenticatedUser) {
        this.currentUser = authenticatedUser;
        User user = authenticatedUser.getUser();
        
        updateStatus("Welcome, " + user.getFullName());
        loadUserDashboard(user);
        
        logger.info("User authenticated: " + user.getUsername() + " (" + user.getRole() + ")");
    }
    
    /**
     * Loads the appropriate dashboard based on user role.
     */
    public void loadUserDashboard(User user) {
        try {
            JPanel dashboard = null;
            String panelName = null;
            
            switch (user.getRole()) {
                case ADMIN:
                    if (adminDashboard == null) {
                        adminDashboard = new AdminDashboard(this, remoteService, currentUser.getSessionToken());
                    }
                    dashboard = adminDashboard;
                    panelName = "admin";
                    break;
                    
                case TEACHER:
                    if (teacherDashboard == null) {
                        teacherDashboard = new TeacherDashboard(this, remoteService, currentUser.getSessionToken());
                    }
                    dashboard = teacherDashboard;
                    panelName = "teacher";
                    break;
                    
                case STUDENT:
                    if (studentDashboard == null) {
                        studentDashboard = new StudentDashboard(this, remoteService, currentUser.getSessionToken());
                    }
                    dashboard = studentDashboard;
                    panelName = "student";
                    break;
                    
                default:
                    throw new IllegalArgumentException("Unknown user role: " + user.getRole());
            }
            
            if (dashboard != null) {
                switchToPanel(dashboard, panelName);
                setTitle(APP_TITLE + " - " + user.getDisplayName());
            }
            
        } catch (Exception e) {
            logger.error("Failed to load user dashboard", e);
            showErrorDialog("Failed to load dashboard", e.getMessage());
            handleLogout();
        }
    }
    
    /**
     * Switches to a different panel in the main area.
     */
    private void switchToPanel(JPanel panel, String panelName) {
        if (currentPanel != null) {
            mainPanel.remove(currentPanel);
        }
        
        currentPanel = panel;
        mainPanel.add(panel, panelName);
        
        CardLayout cardLayout = (CardLayout) mainPanel.getLayout();
        cardLayout.show(mainPanel, panelName);
        
        revalidate();
        repaint();
    }
    
    /**
     * Handles user logout.
     */
    public void handleLogout() {
        if (currentUser != null) {
            try {
                remoteService.logout(currentUser.getSessionToken());
                logger.info("User logged out: " + currentUser.getUser().getUsername());
            } catch (Exception e) {
                logger.warn("Error during logout", e);
            }
        }
        
        // Clear user session
        currentUser = null;
        
        // Clear dashboards to force recreation on next login
        adminDashboard = null;
        teacherDashboard = null;
        studentDashboard = null;
        
        // Show login screen
        setTitle(APP_TITLE);
        showLoginScreen();
    }
    
    /**
     * Handles application exit.
     */
    private void handleApplicationExit() {
        int option = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to exit the application?",
                "Confirm Exit",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        
        if (option == JOptionPane.YES_OPTION) {
            // Logout if user is logged in
            if (currentUser != null) {
                try {
                    remoteService.logout(currentUser.getSessionToken());
                } catch (Exception e) {
                    logger.warn("Error during logout on exit", e);
                }
            }
            
            // Stop timers
            if (reconnectTimer != null) {
                reconnectTimer.stop();
            }
            
            logger.info("Application exiting");
            System.exit(0);
        }
    }
    
    /**
     * Updates the status bar message.
     */
    public void updateStatus(String message) {
        if (statusLabel != null) {
            statusLabel.setText(message);
        }
    }
    
    /**
     * Shows or hides the progress indicator.
     */
    public void showProgress(boolean show) {
        if (connectionProgress != null) {
            connectionProgress.setVisible(show);
        }
    }
    
    /**
     * Shows an error dialog.
     */
    public void showErrorDialog(String title, String message) {
        JOptionPane.showMessageDialog(
                this,
                message,
                title,
                JOptionPane.ERROR_MESSAGE
        );
    }
    
    /**
     * Shows an information dialog.
     */
    public void showInfoDialog(String title, String message) {
        JOptionPane.showMessageDialog(
                this,
                message,
                title,
                JOptionPane.INFORMATION_MESSAGE
        );
    }
    
    /**
     * Shows a confirmation dialog.
     */
    public boolean showConfirmDialog(String title, String message) {
        int option = JOptionPane.showConfirmDialog(
                this,
                message,
                title,
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        return option == JOptionPane.YES_OPTION;
    }
    
    // Getters
    public AttendanceService getRemoteService() {
        return remoteService;
    }
    
    public AuthenticationService.AuthenticatedUser getCurrentUser() {
        return currentUser;
    }
    
    public boolean isConnected() {
        return isConnected;
    }
    
    /**
     * Shows the login frame.
     */
    public void showLoginFrame() {
        showLoginScreen();
    }
    
    /**
     * Gets the attendance service.
     */
    public AttendanceService getAttendanceService() {
        return remoteService;
    }
    
    /**
     * Shows a warning dialog.
     */
    public void showWarningDialog(String title, String message) {
        JOptionPane.showMessageDialog(
                this,
                message,
                title,
                JOptionPane.WARNING_MESSAGE
        );
    }
    
    /**
     * Main method to start the application.
     */
    public static void main(String[] args) {
        // Set system properties for better GUI experience
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        
        SwingUtilities.invokeLater(() -> {
            try {
                String serverUrl = DEFAULT_SERVER_URL;
                if (args.length > 0) {
                    serverUrl = args[0];
                }
                
                AttendanceGUI app = new AttendanceGUI(serverUrl);
                app.setVisible(true);
                
            } catch (Exception e) {
                logger.error("Failed to start application", e);
                JOptionPane.showMessageDialog(
                        null,
                        "Failed to start application: " + e.getMessage(),
                        "Startup Error",
                        JOptionPane.ERROR_MESSAGE
                );
                System.exit(1);
            }
        });
    }
}