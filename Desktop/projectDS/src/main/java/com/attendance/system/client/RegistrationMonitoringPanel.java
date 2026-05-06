package com.attendance.system.client;

import com.attendance.system.util.RegistrationAnalytics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Registration monitoring dashboard panel.
 * Displays registration analytics and statistics for administrators.
 */
public class RegistrationMonitoringPanel extends JPanel {
    private static final Logger logger = LoggerFactory.getLogger(RegistrationMonitoringPanel.class);
    
    private final RegistrationAnalytics analytics;
    private JLabel totalRegistrationsLabel;
    private JLabel successfulRegistrationsLabel;
    private JLabel failedRegistrationsLabel;
    private JLabel successRateLabel;
    private JLabel studentCountLabel;
    private JLabel teacherCountLabel;
    private JLabel avgProcessingTimeLabel;
    private JTable validationFailuresTable;
    private JTable recentEventsTable;
    private JButton refreshButton;
    private JButton resetButton;
    private JButton exportButton;
    
    public RegistrationMonitoringPanel(RegistrationAnalytics analytics) {
        this.analytics = analytics;
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        refreshData();
    }
    
    /**
     * Initializes all GUI components.
     */
    private void initializeComponents() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        
        // Statistics labels
        totalRegistrationsLabel = createStatLabel("Total Registrations: 0");
        successfulRegistrationsLabel = createStatLabel("Successful: 0");
        failedRegistrationsLabel = createStatLabel("Failed: 0");
        successRateLabel = createStatLabel("Success Rate: 0%");
        studentCountLabel = createStatLabel("Students: 0");
        teacherCountLabel = createStatLabel("Teachers: 0");
        avgProcessingTimeLabel = createStatLabel("Avg Processing Time: 0ms");
        
        // Buttons
        refreshButton = new JButton("Refresh");
        refreshButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        refreshButton.setBackground(new Color(70, 130, 180));
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setFocusPainted(false);
        
        resetButton = new JButton("Reset");
        resetButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        resetButton.setBackground(new Color(220, 20, 60));
        resetButton.setForeground(Color.WHITE);
        resetButton.setFocusPainted(false);
        
        exportButton = new JButton("Export");
        exportButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        exportButton.setBackground(new Color(34, 139, 34));
        exportButton.setForeground(Color.WHITE);
        exportButton.setFocusPainted(false);
        
        // Tables
        validationFailuresTable = new JTable();
        validationFailuresTable.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        
        recentEventsTable = new JTable();
        recentEventsTable.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
    }
    
    /**
     * Creates a statistics label.
     */
    private JLabel createStatLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        label.setForeground(new Color(70, 130, 180));
        return label;
    }
    
    /**
     * Sets up the layout of components.
     */
    private void setupLayout() {
        // Statistics panel
        JPanel statsPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        statsPanel.setBackground(Color.WHITE);
        statsPanel.setBorder(BorderFactory.createTitledBorder("Registration Statistics"));
        statsPanel.add(totalRegistrationsLabel);
        statsPanel.add(successfulRegistrationsLabel);
        statsPanel.add(failedRegistrationsLabel);
        statsPanel.add(successRateLabel);
        statsPanel.add(studentCountLabel);
        statsPanel.add(teacherCountLabel);
        statsPanel.add(avgProcessingTimeLabel);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(refreshButton);
        buttonPanel.add(exportButton);
        buttonPanel.add(resetButton);
        
        // Top panel
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);
        topPanel.add(statsPanel, BorderLayout.CENTER);
        topPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Validation failures panel
        JPanel validationPanel = new JPanel(new BorderLayout());
        validationPanel.setBackground(Color.WHITE);
        validationPanel.setBorder(BorderFactory.createTitledBorder("Validation Failures"));
        validationPanel.add(new JScrollPane(validationFailuresTable), BorderLayout.CENTER);
        
        // Recent events panel
        JPanel eventsPanel = new JPanel(new BorderLayout());
        eventsPanel.setBackground(Color.WHITE);
        eventsPanel.setBorder(BorderFactory.createTitledBorder("Recent Registration Events"));
        eventsPanel.add(new JScrollPane(recentEventsTable), BorderLayout.CENTER);
        
        // Main layout
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, validationPanel, eventsPanel);
        splitPane.setDividerLocation(200);
        
        add(topPanel, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);
    }
    
    /**
     * Sets up event handlers.
     */
    private void setupEventHandlers() {
        refreshButton.addActionListener(e -> refreshData());
        resetButton.addActionListener(e -> resetAnalytics());
        exportButton.addActionListener(e -> exportAnalytics());
    }
    
    /**
     * Refreshes the displayed data.
     */
    public void refreshData() {
        RegistrationAnalytics.AnalyticsReport report = analytics.getAnalyticsReport();
        
        // Update statistics labels
        totalRegistrationsLabel.setText("Total Registrations: " + report.totalRegistrations);
        successfulRegistrationsLabel.setText("Successful: " + report.successfulRegistrations);
        failedRegistrationsLabel.setText("Failed: " + report.failedRegistrations);
        successRateLabel.setText(String.format("Success Rate: %.2f%%", report.successRate));
        studentCountLabel.setText("Students: " + report.studentRegistrations);
        teacherCountLabel.setText("Teachers: " + report.teacherRegistrations);
        avgProcessingTimeLabel.setText(String.format("Avg Processing Time: %.2fms", report.averageProcessingTime));
        
        // Update validation failures table
        updateValidationFailuresTable(report.validationFailures);
        
        // Update recent events table
        updateRecentEventsTable();
    }
    
    /**
     * Updates the validation failures table.
     */
    private void updateValidationFailuresTable(Map<String, Integer> failures) {
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("Validation Type");
        model.addColumn("Count");
        
        failures.entrySet().stream()
            .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
            .forEach(entry -> {
                model.addRow(new Object[]{entry.getKey(), entry.getValue()});
            });
        
        validationFailuresTable.setModel(model);
    }
    
    /**
     * Updates the recent events table.
     */
    private void updateRecentEventsTable() {
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("Username");
        model.addColumn("Role");
        model.addColumn("Status");
        model.addColumn("Reason");
        model.addColumn("Time (ms)");
        model.addColumn("Timestamp");
        
        analytics.getRecentEvents(20).forEach(event -> {
            model.addRow(new Object[]{
                event.username,
                event.role != null ? event.role.toString() : "N/A",
                event.status,
                event.reason != null ? event.reason : "N/A",
                event.processingTimeMs,
                event.timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            });
        });
        
        recentEventsTable.setModel(model);
    }
    
    /**
     * Resets the analytics.
     */
    private void resetAnalytics() {
        int option = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to reset all registration analytics?",
            "Confirm Reset",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        
        if (option == JOptionPane.YES_OPTION) {
            analytics.reset();
            refreshData();
            JOptionPane.showMessageDialog(
                this,
                "Registration analytics have been reset.",
                "Reset Complete",
                JOptionPane.INFORMATION_MESSAGE
            );
            logger.info("Registration analytics reset by administrator");
        }
    }
    
    /**
     * Exports the analytics report.
     */
    private void exportAnalytics() {
        RegistrationAnalytics.AnalyticsReport report = analytics.getAnalyticsReport();
        
        // Create export string
        StringBuilder sb = new StringBuilder();
        sb.append("Registration Analytics Report\n");
        sb.append("Generated: ").append(report.generatedAt).append("\n\n");
        sb.append("Summary Statistics:\n");
        sb.append("  Total Registrations: ").append(report.totalRegistrations).append("\n");
        sb.append("  Successful: ").append(report.successfulRegistrations).append("\n");
        sb.append("  Failed: ").append(report.failedRegistrations).append("\n");
        sb.append("  Success Rate: ").append(String.format("%.2f%%", report.successRate)).append("\n");
        sb.append("  Students: ").append(report.studentRegistrations).append("\n");
        sb.append("  Teachers: ").append(report.teacherRegistrations).append("\n\n");
        sb.append("Performance Metrics:\n");
        sb.append("  Average Processing Time: ").append(String.format("%.2fms", report.averageProcessingTime)).append("\n");
        sb.append("  Max Processing Time: ").append(report.maxProcessingTime).append("ms\n");
        sb.append("  Min Processing Time: ").append(report.minProcessingTime).append("ms\n\n");
        sb.append("Validation Failures:\n");
        report.validationFailures.forEach((key, value) -> 
            sb.append("  ").append(key).append(": ").append(value).append("\n")
        );
        
        // Show export dialog
        JTextArea textArea = new JTextArea(sb.toString());
        textArea.setEditable(false);
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(600, 400));
        
        JOptionPane.showMessageDialog(
            this,
            scrollPane,
            "Registration Analytics Report",
            JOptionPane.INFORMATION_MESSAGE
        );
        
        logger.info("Registration analytics exported");
    }
}
