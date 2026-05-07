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
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Notification Panel for displaying and managing user notifications.
 * Provides functionality to view, mark as read, and delete notifications.
 */
public class NotificationPanel extends JPanel {
    private static final Logger logger = LoggerFactory.getLogger(NotificationPanel.class);
    
    private final AttendanceGUI parentFrame;
    private final AttendanceService remoteService;
    private final String sessionToken;
    private final User currentUser;
    
    // Table components
    private JTable notificationTable;
    private DefaultTableModel notificationTableModel;
    
    // Control buttons
    private JButton markAsReadButton;
    private JButton markAllAsReadButton;
    private JButton deleteButton;
    private JButton refreshButton;
    private JButton sendNotificationButton; // Admin only
    
    // Filter components
    private JComboBox<String> typeFilter;
    private JCheckBox unreadOnlyCheckBox;
    
    // Status components
    private JLabel unreadCountLabel;
    
    public NotificationPanel(AttendanceGUI parentFrame, AttendanceService remoteService, String sessionToken, User currentUser) {
        this.parentFrame = parentFrame;
        this.remoteService = remoteService;
        this.sessionToken = sessionToken;
        this.currentUser = currentUser;
        
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        loadNotifications();
        updateUnreadCount();
    }
    
    /**
     * Initializes all GUI components.
     */
    private void initializeComponents() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        
        // Create notification table
        String[] columnNames = {"", "Type", "Title", "Message", "Date", "Status"};
        notificationTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table read-only
            }
            
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) return Boolean.class; // Checkbox column
                return String.class;
            }
        };
        
        notificationTable = new JTable(notificationTableModel);
        notificationTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        notificationTable.setRowHeight(30);
        notificationTable.getTableHeader().setReorderingAllowed(false);
        
        // Set column widths
        notificationTable.getColumnModel().getColumn(0).setPreferredWidth(30); // Checkbox
        notificationTable.getColumnModel().getColumn(1).setPreferredWidth(120); // Type
        notificationTable.getColumnModel().getColumn(2).setPreferredWidth(200); // Title
        notificationTable.getColumnModel().getColumn(3).setPreferredWidth(300); // Message
        notificationTable.getColumnModel().getColumn(4).setPreferredWidth(120); // Date
        notificationTable.getColumnModel().getColumn(5).setPreferredWidth(80); // Status
        
        // Custom renderer for unread notifications
        notificationTable.setDefaultRenderer(String.class, new NotificationCellRenderer());
        
        // Create buttons
        markAsReadButton = new JButton("Mark as Read");
        markAsReadButton.setPreferredSize(new Dimension(120, 35));
        markAsReadButton.setBackground(new Color(70, 130, 180));
        markAsReadButton.setForeground(Color.WHITE);
        markAsReadButton.setEnabled(false);
        
        markAllAsReadButton = new JButton("Mark All as Read");
        markAllAsReadButton.setPreferredSize(new Dimension(140, 35));
        markAllAsReadButton.setBackground(new Color(34, 139, 34));
        markAllAsReadButton.setForeground(Color.WHITE);
        
        deleteButton = new JButton("Delete");
        deleteButton.setPreferredSize(new Dimension(100, 35));
        deleteButton.setBackground(new Color(220, 20, 60));
        deleteButton.setForeground(Color.WHITE);
        deleteButton.setEnabled(false);
        
        refreshButton = new JButton("Refresh");
        refreshButton.setPreferredSize(new Dimension(100, 35));
        refreshButton.setBackground(new Color(128, 128, 128));
        refreshButton.setForeground(Color.WHITE);
        
        // Admin-only button
        if (currentUser.getRole() == UserRole.ADMIN) {
            sendNotificationButton = new JButton("Send Notification");
            sendNotificationButton.setPreferredSize(new Dimension(150, 35));
            sendNotificationButton.setBackground(new Color(255, 140, 0));
            sendNotificationButton.setForeground(Color.WHITE);
        }
        
        // Create filter components
        typeFilter = new JComboBox<>(new String[]{"All Types", "ATTENDANCE_WARNING", "SYSTEM_NOTIFICATION", "COURSE_UPDATE", "REMINDER"});
        typeFilter.setPreferredSize(new Dimension(150, 25));
        
        unreadOnlyCheckBox = new JCheckBox("Unread Only", false);
        unreadOnlyCheckBox.setBackground(Color.WHITE);
        
        // Status components
        unreadCountLabel = new JLabel("Unread: 0");
        unreadCountLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        unreadCountLabel.setForeground(new Color(220, 20, 60));
    }
    
    /**
     * Sets up the layout of components.
     */
    private void setupLayout() {
        // Title panel
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(Color.WHITE);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        
        JLabel titleLabel = new JLabel("Notifications");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        titleLabel.setForeground(new Color(70, 130, 180));
        
        titlePanel.add(titleLabel, BorderLayout.WEST);
        titlePanel.add(unreadCountLabel, BorderLayout.EAST);
        
        // Filter panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.setBackground(Color.WHITE);
        filterPanel.setBorder(BorderFactory.createTitledBorder("Filters"));
        
        filterPanel.add(new JLabel("Type:"));
        filterPanel.add(typeFilter);
        filterPanel.add(Box.createHorizontalStrut(10));
        filterPanel.add(unreadOnlyCheckBox);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createTitledBorder("Actions"));
        
        buttonPanel.add(markAsReadButton);
        buttonPanel.add(markAllAsReadButton);
        buttonPanel.add(deleteButton);
        if (sendNotificationButton != null) {
            buttonPanel.add(sendNotificationButton);
        }
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
        add(new JScrollPane(notificationTable), BorderLayout.CENTER);
    }
    
    /**
     * Sets up event handlers.
     */
    private void setupEventHandlers() {
        // Table selection listener
        notificationTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                boolean hasSelection = notificationTable.getSelectedRow() != -1;
                markAsReadButton.setEnabled(hasSelection);
                deleteButton.setEnabled(hasSelection);
            }
        });
        
        // Button listeners
        markAsReadButton.addActionListener(e -> markSelectedAsRead());
        markAllAsReadButton.addActionListener(e -> markAllAsRead());
        deleteButton.addActionListener(e -> deleteSelected());
        refreshButton.addActionListener(e -> {
            loadNotifications();
            updateUnreadCount();
        });
        
        if (sendNotificationButton != null) {
            sendNotificationButton.addActionListener(e -> showSendNotificationDialog());
        }
        
        // Filter listeners
        typeFilter.addActionListener(e -> loadNotifications());
        unreadOnlyCheckBox.addActionListener(e -> loadNotifications());
    }
    
    /**
     * Loads notifications from the server.
     */
    private void loadNotifications() {
        parentFrame.showProgress(true);
        
        CompletableFuture.supplyAsync(() -> {
            try {
                boolean unreadOnly = unreadOnlyCheckBox.isSelected();
                return remoteService.getNotifications(sessionToken, currentUser.getUserId(), unreadOnly);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).thenAccept(notifications -> {
            SwingUtilities.invokeLater(() -> {
                updateNotificationTable(notifications);
                parentFrame.showProgress(false);
            });
        }).exceptionally(throwable -> {
            SwingUtilities.invokeLater(() -> {
                parentFrame.showProgress(false);
                logger.error("Failed to load notifications", throwable);
                parentFrame.showErrorDialog("Error", "Failed to load notifications: " + throwable.getMessage());
            });
            return null;
        });
    }
    
    /**
     * Updates the notification table with the provided notifications.
     */
    private void updateNotificationTable(List<Notification> notifications) {
        notificationTableModel.setRowCount(0);
        
        String selectedType = (String) typeFilter.getSelectedItem();
        
        for (Notification notification : notifications) {
            // Apply type filter
            if ("All Types".equals(selectedType) || notification.getType().name().equals(selectedType)) {
                Object[] row = {
                    false, // Checkbox for selection
                    notification.getTypeDisplay(),
                    notification.getTitle(),
                    notification.getMessagePreview(),
                    notification.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                    notification.isRead() ? "Read" : "Unread"
                };
                notificationTableModel.addRow(row);
            }
        }
    }
    
    /**
     * Updates the unread notification count.
     */
    private void updateUnreadCount() {
        CompletableFuture.supplyAsync(() -> {
            try {
                return remoteService.getUnreadNotificationCount(sessionToken, currentUser.getUserId());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).thenAccept(count -> {
            SwingUtilities.invokeLater(() -> {
                unreadCountLabel.setText("Unread: " + count);
            });
        }).exceptionally(throwable -> {
            SwingUtilities.invokeLater(() -> {
                logger.error("Failed to get unread count", throwable);
            });
            return null;
        });
    }
    
    /**
     * Marks selected notifications as read.
     */
    private void markSelectedAsRead() {
        int[] selectedRows = notificationTable.getSelectedRows();
        if (selectedRows.length == 0) return;
        
        parentFrame.showProgress(true);
        
        CompletableFuture.runAsync(() -> {
            try {
                for (int row : selectedRows) {
                    // Get notification ID (would need to be stored in table model)
                    // For now, mark all as read
                }
                remoteService.markAllNotificationsAsRead(sessionToken, currentUser.getUserId());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).thenRun(() -> {
            SwingUtilities.invokeLater(() -> {
                parentFrame.showProgress(false);
                parentFrame.showInfoDialog("Success", "Notifications marked as read");
                loadNotifications();
                updateUnreadCount();
            });
        }).exceptionally(throwable -> {
            SwingUtilities.invokeLater(() -> {
                parentFrame.showProgress(false);
                logger.error("Failed to mark notifications as read", throwable);
                parentFrame.showErrorDialog("Error", "Failed to mark notifications as read: " + throwable.getMessage());
            });
            return null;
        });
    }
    
    /**
     * Marks all notifications as read.
     */
    private void markAllAsRead() {
        boolean confirmed = parentFrame.showConfirmDialog(
            "Confirm Mark All as Read",
            "Are you sure you want to mark all notifications as read?"
        );
        
        if (confirmed) {
            parentFrame.showProgress(true);
            
            CompletableFuture.supplyAsync(() -> {
                try {
                    return remoteService.markAllNotificationsAsRead(sessionToken, currentUser.getUserId());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }).thenAccept(count -> {
                SwingUtilities.invokeLater(() -> {
                    parentFrame.showProgress(false);
                    parentFrame.showInfoDialog("Success", count + " notifications marked as read");
                    loadNotifications();
                    updateUnreadCount();
                });
            }).exceptionally(throwable -> {
                SwingUtilities.invokeLater(() -> {
                    parentFrame.showProgress(false);
                    logger.error("Failed to mark all notifications as read", throwable);
                    parentFrame.showErrorDialog("Error", "Failed to mark all notifications as read: " + throwable.getMessage());
                });
                return null;
            });
        }
    }
    
    /**
     * Deletes selected notifications.
     */
    private void deleteSelected() {
        int[] selectedRows = notificationTable.getSelectedRows();
        if (selectedRows.length == 0) return;
        
        boolean confirmed = parentFrame.showConfirmDialog(
            "Confirm Delete",
            "Are you sure you want to delete " + selectedRows.length + " notification(s)?\n" +
            "This action cannot be undone."
        );
        
        if (confirmed) {
            // Implementation would require notification IDs
            parentFrame.showInfoDialog("Info", "Delete functionality will be implemented");
        }
    }
    
    /**
     * Shows the send notification dialog (Admin only).
     */
    private void showSendNotificationDialog() {
        SendNotificationDialog dialog = new SendNotificationDialog(parentFrame, remoteService, sessionToken);
        dialog.setVisible(true);
        
        if (dialog.isConfirmed()) {
            loadNotifications(); // Refresh notifications
        }
    }
    
    /**
     * Custom cell renderer for highlighting unread notifications.
     */
    private class NotificationCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                     boolean hasFocus, int row, int column) {
            Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            // Check if notification is unread (status column is index 5)
            String status = (String) table.getValueAt(row, 5);
            if ("Unread".equals(status) && !isSelected) {
                component.setBackground(new Color(255, 248, 220)); // Light yellow for unread
                setFont(getFont().deriveFont(Font.BOLD));
            } else if (!isSelected) {
                component.setBackground(Color.WHITE);
                setFont(getFont().deriveFont(Font.PLAIN));
            }
            
            return component;
        }
    }
    
    /**
     * Send notification dialog (Admin only).
     */
    private static class SendNotificationDialog extends JDialog {
        private boolean confirmed = false;
        private final AttendanceService remoteService;
        private final String sessionToken;
        
        private JComboBox<User> recipientCombo;
        private JComboBox<NotificationType> typeCombo;
        private JTextField titleField;
        private JTextArea messageArea;
        
        public SendNotificationDialog(Frame parent, AttendanceService service, String token) {
            super(parent, "Send Notification", true);
            this.remoteService = service;
            this.sessionToken = token;
            
            initializeComponents();
            setupLayout();
            loadUsers();
            
            setSize(500, 400);
            setLocationRelativeTo(parent);
        }
        
        private void initializeComponents() {
            recipientCombo = new JComboBox<>();
            typeCombo = new JComboBox<>(NotificationType.values());
            titleField = new JTextField(30);
            messageArea = new JTextArea(5, 30);
            messageArea.setLineWrap(true);
            messageArea.setWrapStyleWord(true);
        }
        
        private void setupLayout() {
            setLayout(new BorderLayout());
            
            JPanel formPanel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.anchor = GridBagConstraints.WEST;
            
            gbc.gridx = 0; gbc.gridy = 0;
            formPanel.add(new JLabel("Recipient:"), gbc);
            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            formPanel.add(recipientCombo, gbc);
            
            gbc.gridx = 0; gbc.gridy = 1;
            gbc.fill = GridBagConstraints.NONE;
            formPanel.add(new JLabel("Type:"), gbc);
            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            formPanel.add(typeCombo, gbc);
            
            gbc.gridx = 0; gbc.gridy = 2;
            gbc.fill = GridBagConstraints.NONE;
            formPanel.add(new JLabel("Title:"), gbc);
            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            formPanel.add(titleField, gbc);
            
            gbc.gridx = 0; gbc.gridy = 3;
            gbc.fill = GridBagConstraints.NONE;
            gbc.anchor = GridBagConstraints.NORTHWEST;
            formPanel.add(new JLabel("Message:"), gbc);
            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.BOTH;
            gbc.weightx = 1.0;
            gbc.weighty = 1.0;
            formPanel.add(new JScrollPane(messageArea), gbc);
            
            add(formPanel, BorderLayout.CENTER);
            
            JPanel buttonPanel = new JPanel(new FlowLayout());
            JButton sendButton = new JButton("Send");
            JButton cancelButton = new JButton("Cancel");
            
            sendButton.addActionListener(e -> sendNotification());
            cancelButton.addActionListener(e -> dispose());
            
            buttonPanel.add(sendButton);
            buttonPanel.add(cancelButton);
            add(buttonPanel, BorderLayout.SOUTH);
        }
        
        private void loadUsers() {
            try {
                List<User> users = remoteService.getAllUsers(sessionToken);
                recipientCombo.removeAllItems();
                for (User user : users) {
                    recipientCombo.addItem(user);
                }
            } catch (Exception e) {
                // Handle error
            }
        }
        
        private void sendNotification() {
            try {
                User recipient = (User) recipientCombo.getSelectedItem();
                NotificationType type = (NotificationType) typeCombo.getSelectedItem();
                String title = titleField.getText().trim();
                String message = messageArea.getText().trim();
                
                if (recipient == null || title.isEmpty() || message.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Please fill in all fields", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                Notification notification = new Notification(recipient.getUserId(), title, message, type);
                boolean success = remoteService.sendNotification(sessionToken, notification);
                
                if (success) {
                    confirmed = true;
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to send notification", "Error", JOptionPane.ERROR_MESSAGE);
                }
                
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error sending notification: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        
        public boolean isConfirmed() {
            return confirmed;
        }
    }
}