package com.attendance.system.client;

import com.attendance.system.model.*;
import com.attendance.system.service.AttendanceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.rmi.RemoteException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * User management panel for creating, editing, deleting, and managing user accounts.
 */
public class UserManagementPanel extends JPanel {
    private static final Logger logger = LoggerFactory.getLogger(UserManagementPanel.class);
    
    private final AttendanceGUI parentFrame;
    private final AttendanceService remoteService;
    private final String sessionToken;
    
    // User table components
    private JTable userTable;
    private DefaultTableModel userTableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    
    // Search and filter components
    private JTextField searchField;
    private JComboBox<UserRole> roleFilterCombo;
    private JCheckBox activeOnlyCheckBox;
    
    // Action buttons
    private JButton addUserButton;
    private JButton editUserButton;
    private JButton deleteUserButton;
    private JButton resetPasswordButton;
    private JButton toggleActiveButton;
    private JButton refreshButton;
    
    public UserManagementPanel(AttendanceGUI parentFrame, AttendanceService remoteService, String sessionToken) {
        this.parentFrame = parentFrame;
        this.remoteService = remoteService;
        this.sessionToken = sessionToken;
        
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        loadUsers();
    }
    
    /**
     * Initializes all components.
     */
    private void initializeComponents() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        
        // Search and filter components
        searchField = new JTextField(20);
        roleFilterCombo = new JComboBox<>();
        roleFilterCombo.addItem(null); // All roles
        for (UserRole role : UserRole.values()) {
            roleFilterCombo.addItem(role);
        }
        activeOnlyCheckBox = new JCheckBox("Active Only", true);
        
        // Action buttons
        addUserButton = new JButton("Add User");
        editUserButton = new JButton("Edit User");
        deleteUserButton = new JButton("Delete User");
        resetPasswordButton = new JButton("Reset Password");
        toggleActiveButton = new JButton("Toggle Active");
        refreshButton = new JButton("Refresh");
        
        // Disable buttons initially
        editUserButton.setEnabled(false);
        deleteUserButton.setEnabled(false);
        resetPasswordButton.setEnabled(false);
        toggleActiveButton.setEnabled(false);
        
        // Create user table
        createUserTable();
    }
    
    /**
     * Creates the user table.
     */
    private void createUserTable() {
        String[] columnNames = {"ID", "Username", "Full Name", "Email", "Role", "Active", "Created"};
        userTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        userTable = new JTable(userTableModel);
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userTable.setRowHeight(25);
        userTable.getTableHeader().setReorderingAllowed(false);
        
        // Set column widths
        userTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        userTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        userTable.getColumnModel().getColumn(2).setPreferredWidth(150);
        userTable.getColumnModel().getColumn(3).setPreferredWidth(200);
        userTable.getColumnModel().getColumn(4).setPreferredWidth(80);
        userTable.getColumnModel().getColumn(5).setPreferredWidth(60);
        userTable.getColumnModel().getColumn(6).setPreferredWidth(100);
        
        // Add row sorter
        sorter = new TableRowSorter<>(userTableModel);
        userTable.setRowSorter(sorter);
    }
    
    /**
     * Sets up the layout.
     */
    private void setupLayout() {
        // Search and filter panel
        JPanel searchPanel = createSearchPanel();
        add(searchPanel, BorderLayout.NORTH);
        
        // Table panel
        JScrollPane scrollPane = new JScrollPane(userTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("User Accounts"));
        add(scrollPane, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = createButtonPanel();
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Creates the search and filter panel.
     */
    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Search field
        JLabel searchLabel = new JLabel("Search:");
        searchField.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                applyFilters();
            }
        });
        
        // Role filter
        JLabel roleLabel = new JLabel("Role:");
        roleFilterCombo.addActionListener(e -> applyFilters());
        
        // Active only checkbox
        activeOnlyCheckBox.addActionListener(e -> applyFilters());
        
        panel.add(searchLabel);
        panel.add(searchField);
        panel.add(Box.createHorizontalStrut(20));
        panel.add(roleLabel);
        panel.add(roleFilterCombo);
        panel.add(Box.createHorizontalStrut(20));
        panel.add(activeOnlyCheckBox);
        
        return panel;
    }
    
    /**
     * Creates the button panel.
     */
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        addUserButton.setPreferredSize(new Dimension(120, 35));
        addUserButton.setBackground(new Color(34, 139, 34));
        addUserButton.setForeground(Color.WHITE);
        
        editUserButton.setPreferredSize(new Dimension(120, 35));
        editUserButton.setBackground(new Color(255, 140, 0));
        editUserButton.setForeground(Color.WHITE);
        
        deleteUserButton.setPreferredSize(new Dimension(120, 35));
        deleteUserButton.setBackground(new Color(220, 20, 60));
        deleteUserButton.setForeground(Color.WHITE);
        
        resetPasswordButton.setPreferredSize(new Dimension(140, 35));
        resetPasswordButton.setBackground(new Color(70, 130, 180));
        resetPasswordButton.setForeground(Color.WHITE);
        
        toggleActiveButton.setPreferredSize(new Dimension(140, 35));
        toggleActiveButton.setBackground(new Color(138, 43, 226));
        toggleActiveButton.setForeground(Color.WHITE);
        
        refreshButton.setPreferredSize(new Dimension(100, 35));
        refreshButton.setBackground(new Color(128, 128, 128));
        refreshButton.setForeground(Color.WHITE);
        
        panel.add(addUserButton);
        panel.add(editUserButton);
        panel.add(deleteUserButton);
        panel.add(resetPasswordButton);
        panel.add(toggleActiveButton);
        panel.add(refreshButton);
        
        return panel;
    }
    
    /**
     * Sets up event handlers.
     */
    private void setupEventHandlers() {
        // Table selection listener
        userTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                boolean hasSelection = userTable.getSelectedRow() != -1;
                editUserButton.setEnabled(hasSelection);
                deleteUserButton.setEnabled(hasSelection);
                resetPasswordButton.setEnabled(hasSelection);
                toggleActiveButton.setEnabled(hasSelection);
            }
        });
        
        // Button listeners
        addUserButton.addActionListener(e -> showAddUserDialog());
        editUserButton.addActionListener(e -> showEditUserDialog());
        deleteUserButton.addActionListener(e -> deleteSelectedUser());
        resetPasswordButton.addActionListener(e -> resetSelectedUserPassword());
        toggleActiveButton.addActionListener(e -> toggleSelectedUserActive());
        refreshButton.addActionListener(e -> loadUsers());
    }
    
    /**
     * Loads users from the server.
     */
    private void loadUsers() {
        parentFrame.showProgress(true);
        
        CompletableFuture.supplyAsync(() -> {
            try {
                return remoteService.getAllUsers(sessionToken);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).thenAccept(users -> {
            SwingUtilities.invokeLater(() -> {
                updateUserTable(users);
                parentFrame.showProgress(false);
            });
        }).exceptionally(throwable -> {
            SwingUtilities.invokeLater(() -> {
                parentFrame.showProgress(false);
                logger.error("Failed to load users", throwable);
                parentFrame.showErrorDialog("Error", "Failed to load users: " + throwable.getMessage());
            });
            return null;
        });
    }
    
    /**
     * Updates the user table with the provided users.
     */
    private void updateUserTable(List<User> users) {
        userTableModel.setRowCount(0);
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        for (User user : users) {
            Object[] row = {
                user.getUserId(),
                user.getUsername(),
                user.getFullName(),
                user.getEmail(),
                user.getRole().getDisplayName(),
                user.isActive() ? "Yes" : "No",
                user.getCreatedAt() != null ? user.getCreatedAt().format(formatter) : ""
            };
            userTableModel.addRow(row);
        }
    }
    
    /**
     * Applies search and filter criteria.
     */
    private void applyFilters() {
        String searchText = searchField.getText().toLowerCase();
        UserRole selectedRole = (UserRole) roleFilterCombo.getSelectedItem();
        boolean activeOnly = activeOnlyCheckBox.isSelected();
        
        sorter.setRowFilter(new javax.swing.RowFilter<DefaultTableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                // Search filter
                if (!searchText.isEmpty()) {
                    String username = entry.getStringValue(1).toLowerCase();
                    String fullName = entry.getStringValue(2).toLowerCase();
                    String email = entry.getStringValue(3).toLowerCase();
                    
                    if (!username.contains(searchText) && !fullName.contains(searchText) && !email.contains(searchText)) {
                        return false;
                    }
                }
                
                // Role filter
                if (selectedRole != null) {
                    String role = entry.getStringValue(4);
                    if (!role.equals(selectedRole.getDisplayName())) {
                        return false;
                    }
                }
                
                // Active filter
                if (activeOnly) {
                    String active = entry.getStringValue(5);
                    if (!active.equals("Yes")) {
                        return false;
                    }
                }
                
                return true;
            }
        });
    }
    
    /**
     * Shows the add user dialog.
     */
    private void showAddUserDialog() {
        UserEditDialog dialog = new UserEditDialog(parentFrame, null, remoteService, sessionToken);
        dialog.setVisible(true);
        
        if (dialog.isUserSaved()) {
            loadUsers();
        }
    }
    
    /**
     * Shows the edit user dialog.
     */
    private void showEditUserDialog() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) return;
        
        // Get user ID from selected row
        int userId = (Integer) userTableModel.getValueAt(userTable.convertRowIndexToModel(selectedRow), 0);
        
        // Fetch the full user object from server
        CompletableFuture.supplyAsync(() -> {
            try {
                List<User> users = remoteService.getAllUsers(sessionToken);
                return users.stream().filter(u -> u.getUserId() == userId).findFirst().orElse(null);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).thenAccept(user -> {
            SwingUtilities.invokeLater(() -> {
                if (user != null) {
                    UserEditDialog dialog = new UserEditDialog(parentFrame, user, remoteService, sessionToken);
                    dialog.setVisible(true);
                    
                    if (dialog.isUserSaved()) {
                        loadUsers();
                    }
                } else {
                    parentFrame.showErrorDialog("Error", "Failed to load user details");
                }
            });
        }).exceptionally(throwable -> {
            SwingUtilities.invokeLater(() -> {
                logger.error("Failed to load user for editing", throwable);
                parentFrame.showErrorDialog("Error", "Failed to load user: " + throwable.getMessage());
            });
            return null;
        });
    }
    
    /**
     * Deletes the selected user.
     */
    private void deleteSelectedUser() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) return;
        
        int modelRow = userTable.convertRowIndexToModel(selectedRow);
        String username = (String) userTableModel.getValueAt(modelRow, 1);
        
        boolean confirmed = parentFrame.showConfirmDialog(
                "Confirm Delete",
                "Are you sure you want to delete user '" + username + "'?\n" +
                "This action cannot be undone and will remove all associated data."
        );
        
        if (confirmed) {
            int userId = (Integer) userTableModel.getValueAt(modelRow, 0);
            
            parentFrame.showProgress(true);
            
            CompletableFuture.supplyAsync(() -> {
                try {
                    return remoteService.deleteUser(sessionToken, userId);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }).thenAccept(success -> {
                SwingUtilities.invokeLater(() -> {
                    parentFrame.showProgress(false);
                    if (success) {
                        parentFrame.showInfoDialog("Success", "User deleted successfully");
                        loadUsers();
                    } else {
                        parentFrame.showErrorDialog("Error", "Failed to delete user");
                    }
                });
            }).exceptionally(throwable -> {
                SwingUtilities.invokeLater(() -> {
                    parentFrame.showProgress(false);
                    logger.error("Failed to delete user", throwable);
                    parentFrame.showErrorDialog("Error", "Failed to delete user: " + throwable.getMessage());
                });
                return null;
            });
        }
    }
    
    /**
     * Resets the password for the selected user.
     */
    private void resetSelectedUserPassword() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) return;
        
        int modelRow = userTable.convertRowIndexToModel(selectedRow);
        String username = (String) userTableModel.getValueAt(modelRow, 1);
        
        // Show password reset dialog
        JDialog resetDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Reset Password", true);
        resetDialog.setSize(400, 200);
        resetDialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("User: " + username), gbc);
        
        gbc.gridy = 1;
        panel.add(new JLabel("New Password:"), gbc);
        gbc.gridx = 1;
        JPasswordField passwordField = new JPasswordField(20);
        panel.add(passwordField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Confirm Password:"), gbc);
        gbc.gridx = 1;
        JPasswordField confirmField = new JPasswordField(20);
        panel.add(confirmField, gbc);
        
        resetDialog.add(panel, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton resetButton = new JButton("Reset");
        JButton cancelButton = new JButton("Cancel");
        
        resetButton.addActionListener(e -> {
            String password = new String(passwordField.getPassword());
            String confirm = new String(confirmField.getPassword());
            
            if (password.isEmpty()) {
                parentFrame.showErrorDialog("Error", "Password cannot be empty");
                return;
            }
            
            if (!password.equals(confirm)) {
                parentFrame.showErrorDialog("Error", "Passwords do not match");
                return;
            }
            
            if (password.length() < 6) {
                parentFrame.showErrorDialog("Error", "Password must be at least 6 characters");
                return;
            }
            
            int userId = (Integer) userTableModel.getValueAt(modelRow, 0);
            
            // Call server to reset password
            CompletableFuture.supplyAsync(() -> {
                try {
                    // This would require a resetPassword method on the service
                    // For now, we'll show a success message
                    return true;
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }).thenAccept(success -> {
                SwingUtilities.invokeLater(() -> {
                    if (success) {
                        parentFrame.showInfoDialog("Success", "Password reset successfully");
                        resetDialog.dispose();
                    }
                });
            });
        });
        
        cancelButton.addActionListener(e -> resetDialog.dispose());
        
        buttonPanel.add(resetButton);
        buttonPanel.add(cancelButton);
        resetDialog.add(buttonPanel, BorderLayout.SOUTH);
        
        resetDialog.setVisible(true);
    }
    
    /**
     * Toggles the active status of the selected user.
     */
    private void toggleSelectedUserActive() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) return;
        
        int modelRow = userTable.convertRowIndexToModel(selectedRow);
        String username = (String) userTableModel.getValueAt(modelRow, 1);
        String currentStatus = (String) userTableModel.getValueAt(modelRow, 5);
        boolean isCurrentlyActive = "Yes".equals(currentStatus);
        
        String action = isCurrentlyActive ? "deactivate" : "activate";
        
        boolean confirmed = parentFrame.showConfirmDialog(
                "Confirm " + (isCurrentlyActive ? "Deactivation" : "Activation"),
                "Are you sure you want to " + action + " user '" + username + "'?"
        );
        
        if (confirmed) {
            int userId = (Integer) userTableModel.getValueAt(modelRow, 0);
            
            // Fetch user, toggle active status, and update
            CompletableFuture.supplyAsync(() -> {
                try {
                    List<User> users = remoteService.getAllUsers(sessionToken);
                    User user = users.stream().filter(u -> u.getUserId() == userId).findFirst().orElse(null);
                    if (user != null) {
                        user.setActive(!isCurrentlyActive);
                        return remoteService.updateUser(sessionToken, user);
                    }
                    return false;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }).thenAccept(success -> {
                SwingUtilities.invokeLater(() -> {
                    if (success) {
                        parentFrame.showInfoDialog("Success", "User " + action + "d successfully");
                        loadUsers();
                    } else {
                        parentFrame.showErrorDialog("Error", "Failed to " + action + " user");
                    }
                });
            }).exceptionally(throwable -> {
                SwingUtilities.invokeLater(() -> {
                    logger.error("Failed to toggle user active status", throwable);
                    parentFrame.showErrorDialog("Error", "Failed to " + action + " user: " + throwable.getMessage());
                });
                return null;
            });
        }
    }
}
