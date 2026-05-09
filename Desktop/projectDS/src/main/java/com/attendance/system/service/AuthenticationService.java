package com.attendance.system.service;

import com.attendance.system.dao.UserDAO;
import com.attendance.system.exception.AuthenticationException;
import com.attendance.system.exception.DatabaseException;
import com.attendance.system.exception.ValidationException;
import com.attendance.system.model.User;
import com.attendance.system.util.SecurityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for handling user authentication and session management.
 */
public class AuthenticationService {
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);
    
    private final UserDAO userDAO;
    private final Map<String, UserSession> activeSessions;
    private final long sessionTimeoutMillis;
    
    // Failed login attempt tracking
    private final Map<String, FailedLoginAttempts> failedAttempts;
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final long LOCKOUT_DURATION_MILLIS = 15 * 60 * 1000; // 15 minutes
    
    public AuthenticationService() {
        this.userDAO = new UserDAO();
        this.activeSessions = new ConcurrentHashMap<>();
        this.sessionTimeoutMillis = 30 * 60 * 1000; // 30 minutes
        this.failedAttempts = new ConcurrentHashMap<>();
        
        // Start session cleanup thread
        startSessionCleanupThread();
    }
    
    public AuthenticationService(UserDAO userDAO) {
        this.userDAO = userDAO;
        this.activeSessions = new ConcurrentHashMap<>();
        this.sessionTimeoutMillis = 30 * 60 * 1000; // 30 minutes
        this.failedAttempts = new ConcurrentHashMap<>();
        
        startSessionCleanupThread();
    }
    
    /**
     * Authenticates a user with username and password.
     * @param usernameOrEmail the username or email
     * @param password the plain text password
     * @return authenticated user with session token
     * @throws AuthenticationException if authentication fails
     */
    public AuthenticatedUser authenticateUser(String usernameOrEmail, String password) throws AuthenticationException {
        try {
            // Validate input
            SecurityUtil.validateRequired(usernameOrEmail, "username or email");
            SecurityUtil.validateRequired(password, "password");
            
            // Check if account is locked
            if (isAccountLocked(usernameOrEmail)) {
                logger.warn("Authentication attempt on locked account: " + usernameOrEmail);
                throw AuthenticationException.accountLocked();
            }
            
            // Find user by username or email
            User user = null;
            if (usernameOrEmail.contains("@")) {
                // It's an email
                user = userDAO.findByEmail(usernameOrEmail);
            } else {
                // It's a username
                user = userDAO.findByUsername(usernameOrEmail);
            }
            
            if (user == null) {
                recordFailedAttempt(usernameOrEmail);
                logger.warn("Authentication failed - user not found: " + usernameOrEmail);
                throw AuthenticationException.invalidCredentials();
            }
            
            // Check if account is active
            if (!user.isActive()) {
                logger.warn("Authentication attempt on disabled account: " + usernameOrEmail);
                throw AuthenticationException.accountDisabled();
            }
            
            // Verify password
            if (!SecurityUtil.verifyPassword(password, user.getPasswordHash())) {
                recordFailedAttempt(usernameOrEmail);
                logger.warn("Authentication failed - invalid password for user: " + usernameOrEmail);
                throw AuthenticationException.invalidCredentials();
            }
            
            // Clear failed attempts on successful login
            clearFailedAttempts(usernameOrEmail);
            
            // Create session
            String sessionToken = SecurityUtil.generateSecureToken(32);
            UserSession session = new UserSession(user, sessionToken);
            activeSessions.put(sessionToken, session);
            
            logger.info("User authenticated successfully: " + usernameOrEmail + " (Role: " + user.getRole() + ")");
            
            return new AuthenticatedUser(user, sessionToken);
            
        } catch (ValidationException e) {
            logger.warn("Authentication failed - validation error: " + e.getMessage());
            throw new AuthenticationException("Invalid input: " + e.getMessage());
        } catch (DatabaseException e) {
            logger.error("Authentication failed - database error", e);
            throw new AuthenticationException("Authentication service unavailable");
        }
    }
    
    /**
     * Validates a session token and returns the associated user.
     * @param sessionToken the session token
     * @return user associated with the session
     * @throws AuthenticationException if session is invalid or expired
     */
    public User validateSession(String sessionToken) throws AuthenticationException {
        if (sessionToken == null || sessionToken.isEmpty()) {
            throw new AuthenticationException("Session token is required");
        }
        
        UserSession session = activeSessions.get(sessionToken);
        if (session == null) {
            throw AuthenticationException.sessionExpired();
        }
        
        // Check if session has expired
        if (session.isExpired(sessionTimeoutMillis)) {
            activeSessions.remove(sessionToken);
            logger.info("Session expired for user: " + session.getUser().getUsername());
            throw AuthenticationException.sessionExpired();
        }
        
        // Update last access time
        session.updateLastAccess();
        
        return session.getUser();
    }
    
    /**
     * Checks if a user has a specific permission.
     * @param sessionToken the session token
     * @param permission the permission to check
     * @return true if user has the permission
     * @throws AuthenticationException if session is invalid or user lacks permission
     */
    public boolean hasPermission(String sessionToken, String permission) throws AuthenticationException {
        User user = validateSession(sessionToken);
        
        if (!user.hasPermission(permission)) {
            logger.warn("Permission denied for user " + user.getUsername() + " - required: " + permission);
            throw AuthenticationException.insufficientPermissions();
        }
        
        return true;
    }
    
    /**
     * Logs out a user by invalidating their session.
     * @param sessionToken the session token to invalidate
     */
    public void logout(String sessionToken) {
        UserSession session = activeSessions.remove(sessionToken);
        if (session != null) {
            logger.info("User logged out: " + session.getUser().getUsername());
        }
    }
    
    /**
     * Gets the number of active sessions.
     * @return number of active sessions
     */
    public int getActiveSessionCount() {
        return activeSessions.size();
    }
    
    /**
     * Gets all active sessions (for admin monitoring).
     * @return map of session tokens to user sessions
     */
    public Map<String, UserSession> getActiveSessions() {
        return new ConcurrentHashMap<>(activeSessions);
    }
    
    /**
     * Forces logout of all sessions for a specific user.
     * @param userId the user ID
     */
    public void forceLogoutUser(int userId) {
        activeSessions.entrySet().removeIf(entry -> {
            if (entry.getValue().getUser().getUserId() == userId) {
                logger.info("Force logout user: " + entry.getValue().getUser().getUsername());
                return true;
            }
            return false;
        });
    }
    
    /**
     * Changes a user's password.
     * @param sessionToken the session token
     * @param currentPassword the current password
     * @param newPassword the new password
     * @throws AuthenticationException if current password is incorrect
     * @throws ValidationException if new password doesn't meet policy
     */
    public void changePassword(String sessionToken, String currentPassword, String newPassword) 
            throws AuthenticationException, ValidationException, DatabaseException {
        
        User user = validateSession(sessionToken);
        
        // Verify current password
        if (!SecurityUtil.verifyPassword(currentPassword, user.getPasswordHash())) {
            throw AuthenticationException.invalidCredentials();
        }
        
        // Validate new password
        SecurityUtil.validatePassword(newPassword);
        
        // Hash new password
        String newPasswordHash = SecurityUtil.hashPassword(newPassword);
        
        // Update password in database
        boolean updated = userDAO.updatePassword(user.getUserId(), newPasswordHash);
        if (!updated) {
            throw new DatabaseException("Failed to update password");
        }
        
        // Update user object in session
        user.setPasswordHash(newPasswordHash);
        user.touch();
        
        logger.info("Password changed for user: " + user.getUsername());
    }
    
    // Private helper methods
    
    private void recordFailedAttempt(String username) {
        FailedLoginAttempts attempts = failedAttempts.computeIfAbsent(username, 
            k -> new FailedLoginAttempts());
        attempts.recordAttempt();
        
        if (attempts.getCount() >= MAX_FAILED_ATTEMPTS) {
            logger.warn("Account locked due to failed login attempts: " + username);
        }
    }
    
    private void clearFailedAttempts(String username) {
        failedAttempts.remove(username);
    }
    
    private boolean isAccountLocked(String username) {
        FailedLoginAttempts attempts = failedAttempts.get(username);
        return attempts != null && attempts.isLocked(LOCKOUT_DURATION_MILLIS);
    }
    
    private void startSessionCleanupThread() {
        Thread cleanupThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(60000); // Check every minute
                    cleanupExpiredSessions();
                } catch (InterruptedException e) {
                    logger.info("Session cleanup thread interrupted");
                    break;
                }
            }
        });
        cleanupThread.setDaemon(true);
        cleanupThread.setName("SessionCleanup");
        cleanupThread.start();
    }
    
    private void cleanupExpiredSessions() {
        int removedCount = 0;
        for (Map.Entry<String, UserSession> entry : activeSessions.entrySet()) {
            if (entry.getValue().isExpired(sessionTimeoutMillis)) {
                activeSessions.remove(entry.getKey());
                removedCount++;
            }
        }
        
        if (removedCount > 0) {
            logger.debug("Cleaned up " + removedCount + " expired sessions");
        }
    }
    
    /**
     * Inner class representing a user session.
     */
    public static class UserSession implements java.io.Serializable {
        private static final long serialVersionUID = 1L;
        private final User user;
        private final String sessionToken;
        private final LocalDateTime createdAt;
        private LocalDateTime lastAccessAt;
        
        public UserSession(User user, String sessionToken) {
            this.user = user;
            this.sessionToken = sessionToken;
            this.createdAt = LocalDateTime.now();
            this.lastAccessAt = LocalDateTime.now();
        }
        
        public User getUser() {
            return user;
        }
        
        public String getSessionToken() {
            return sessionToken;
        }
        
        public LocalDateTime getCreatedAt() {
            return createdAt;
        }
        
        public LocalDateTime getLastAccessAt() {
            return lastAccessAt;
        }
        
        public void updateLastAccess() {
            this.lastAccessAt = LocalDateTime.now();
        }
        
        public boolean isExpired(long timeoutMillis) {
            return LocalDateTime.now().isAfter(lastAccessAt.plusNanos(timeoutMillis * 1_000_000));
        }
        
        public long getIdleTimeMillis() {
            return java.time.Duration.between(lastAccessAt, LocalDateTime.now()).toMillis();
        }
    }
    
    /**
     * Inner class for tracking failed login attempts.
     */
    private static class FailedLoginAttempts {
        private int count = 0;
        private LocalDateTime lastAttempt;
        
        public void recordAttempt() {
            this.count++;
            this.lastAttempt = LocalDateTime.now();
        }
        
        public int getCount() {
            return count;
        }
        
        public boolean isLocked(long lockoutDurationMillis) {
            if (count < MAX_FAILED_ATTEMPTS) {
                return false;
            }
            
            if (lastAttempt == null) {
                return false;
            }
            
            LocalDateTime unlockTime = lastAttempt.plusNanos(lockoutDurationMillis * 1_000_000);
            return LocalDateTime.now().isBefore(unlockTime);
        }
    }
    
    /**
     * Inner class representing an authenticated user with session token.
     */
    public static class AuthenticatedUser implements java.io.Serializable {
        private static final long serialVersionUID = 1L;
        private final User user;
        private final String sessionToken;
        
        public AuthenticatedUser(User user, String sessionToken) {
            this.user = user;
            this.sessionToken = sessionToken;
        }
        
        public User getUser() {
            return user;
        }
        
        public String getSessionToken() {
            return sessionToken;
        }
    }
}