package com.attendance.system.client;

/**
 * Simple launcher for the GUI client application.
 * This class provides an entry point to start the attendance system client.
 */
public class ClientLauncher {
    
    /**
     * Main method to launch the GUI client.
     * @param args command line arguments (optional server URL)
     */
    public static void main(String[] args) {
        // Set system properties for better GUI experience
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        
        // Start the GUI application
        javax.swing.SwingUtilities.invokeLater(() -> {
            try {
                String serverUrl = "rmi://localhost:1099/AttendanceService";
                if (args.length > 0) {
                    serverUrl = args[0];
                }
                
                AttendanceGUI app = new AttendanceGUI(serverUrl);
                app.setVisible(true);
                
            } catch (Exception e) {
                e.printStackTrace();
                javax.swing.JOptionPane.showMessageDialog(
                        null,
                        "Failed to start application: " + e.getMessage(),
                        "Startup Error",
                        javax.swing.JOptionPane.ERROR_MESSAGE
                );
                System.exit(1);
            }
        });
    }
}