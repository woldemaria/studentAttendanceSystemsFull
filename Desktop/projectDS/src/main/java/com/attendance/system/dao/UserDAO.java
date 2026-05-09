package com.attendance.system.dao;

import com.attendance.system.exception.DatabaseException;
import com.attendance.system.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for User operations.
 */
public class UserDAO {
    private static final Logger logger = LoggerFactory.getLogger(UserDAO.class);
    
    private final DatabaseManager databaseManager;
    
    public UserDAO() {
        this.databaseManager = DatabaseManager.getInstance();
    }
    
    public UserDAO(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }
    
    /**
     * Finds a user by username and password hash.
     * @param username the username
     * @param passwordHash the password hash
     * @return User object or null if not found
     * @throws DatabaseException if database error occurs
     */
    public User findByCredentials(String username, String passwordHash) throws DatabaseException {
        String sql = """
            SELECT u.*, s.student_number, s.program, s.year_level, s.class_section, s.enrollment_date,
                   t.employee_id, t.department, t.specialization
            FROM USERS u
            LEFT JOIN STUDENTS s ON u.user_id = s.user_id
            LEFT JOIN TEACHERS t ON u.user_id = t.user_id
            WHERE u.username = ? AND u.password_hash = ? AND u.is_active = TRUE
            """;
        
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setString(1, username);
            statement.setString(2, passwordHash);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToUser(resultSet);
                }
            }
            
            return null;
            
        } catch (SQLException e) {
            logger.error("Failed to find user by credentials", e);
            throw DatabaseException.queryFailed(sql, e);
        }
    }
    
    /**
     * Finds a user by email and password hash (for email-based login).
     * @param email the email
     * @param passwordHash the password hash
     * @return User object or null if not found
     * @throws DatabaseException if database error occurs
     */
    public User findByEmailAndPassword(String email, String passwordHash) throws DatabaseException {
        String sql = """
            SELECT u.*, s.student_number, s.program, s.year_level, s.class_section, s.enrollment_date,
                   t.employee_id, t.department, t.specialization
            FROM USERS u
            LEFT JOIN STUDENTS s ON u.user_id = s.user_id
            LEFT JOIN TEACHERS t ON u.user_id = t.user_id
            WHERE u.email = ? AND u.password_hash = ? AND u.is_active = TRUE
            """;
        
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setString(1, email);
            statement.setString(2, passwordHash);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToUser(resultSet);
                }
            }
            
            return null;
            
        } catch (SQLException e) {
            logger.error("Failed to find user by email and password", e);
            throw DatabaseException.queryFailed(sql, e);
        }
    }
    
    /**
     * Finds a user by username.
     * @param username the username
     * @return User object or null if not found
     * @throws DatabaseException if database error occurs
     */
    public User findByUsername(String username) throws DatabaseException {
        String sql = """
            SELECT u.*, s.student_number, s.program, s.year_level, s.class_section, s.enrollment_date,
                   t.employee_id, t.department, t.specialization
            FROM USERS u
            LEFT JOIN STUDENTS s ON u.user_id = s.user_id
            LEFT JOIN TEACHERS t ON u.user_id = t.user_id
            WHERE u.username = ?
            """;
        
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setString(1, username);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToUser(resultSet);
                }
            }
            
            return null;
            
        } catch (SQLException e) {
            logger.error("Failed to find user by username: " + username, e);
            throw DatabaseException.queryFailed(sql, e);
        }
    }
    
    /**
     * Finds a user by email.
     * @param email the email
     * @return User object or null if not found
     * @throws DatabaseException if database error occurs
     */
    public User findByEmail(String email) throws DatabaseException {
        String sql = """
            SELECT u.*, s.student_number, s.program, s.year_level, s.class_section, s.enrollment_date,
                   t.employee_id, t.department, t.specialization
            FROM USERS u
            LEFT JOIN STUDENTS s ON u.user_id = s.user_id
            LEFT JOIN TEACHERS t ON u.user_id = t.user_id
            WHERE u.email = ?
            """;
        
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setString(1, email);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToUser(resultSet);
                }
            }
            
            return null;
            
        } catch (SQLException e) {
            logger.error("Failed to find user by email: " + email, e);
            throw DatabaseException.queryFailed(sql, e);
        }
    }
    
    /**
     * Finds a user by ID.
     * @param userId the user ID
     * @return User object or null if not found
     * @throws DatabaseException if database error occurs
     */
    public User findById(int userId) throws DatabaseException {
        String sql = """
            SELECT u.*, s.student_number, s.program, s.year_level, s.class_section, s.enrollment_date,
                   t.employee_id, t.department, t.specialization
            FROM USERS u
            LEFT JOIN STUDENTS s ON u.user_id = s.user_id
            LEFT JOIN TEACHERS t ON u.user_id = t.user_id
            WHERE u.user_id = ?
            """;
        
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setInt(1, userId);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToUser(resultSet);
                }
            }
            
            return null;
            
        } catch (SQLException e) {
            logger.error("Failed to find user by ID: " + userId, e);
            throw DatabaseException.queryFailed(sql, e);
        }
    }
    
    /**
     * Creates a new user.
     * @param user the user to create
     * @return true if user was created successfully
     * @throws DatabaseException if database error occurs
     */
    public boolean createUser(User user) throws DatabaseException {
        Boolean result = databaseManager.executeTransaction(connection -> {
            // Insert into USERS table
            String userSql = """
                INSERT INTO USERS (username, password_hash, email, first_name, last_name, phone_number, gender, photo_path, role, is_active)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
            
            int userId;
            try (PreparedStatement userStatement = connection.prepareStatement(userSql, Statement.RETURN_GENERATED_KEYS)) {
                userStatement.setString(1, user.getUsername());
                userStatement.setString(2, user.getPasswordHash());
                userStatement.setString(3, user.getEmail());
                userStatement.setString(4, user.getFirstName());
                userStatement.setString(5, user.getLastName());
                userStatement.setString(6, user.getPhoneNumber());
                userStatement.setString(7, user.getGender());
                userStatement.setString(8, user.getPhotoPath());
                userStatement.setString(9, user.getRole().name());
                userStatement.setBoolean(10, user.isActive());
                
                int rowsAffected = userStatement.executeUpdate();
                if (rowsAffected == 0) {
                    throw new SQLException("Creating user failed, no rows affected");
                }
                
                try (ResultSet generatedKeys = userStatement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        userId = generatedKeys.getInt(1);
                        user.setUserId(userId);
                    } else {
                        throw new SQLException("Creating user failed, no ID obtained");
                    }
                }
            }
            
            // Insert role-specific data
            if (user instanceof Student) {
                createStudentProfile(connection, (Student) user);
            } else if (user instanceof Teacher) {
                createTeacherProfile(connection, (Teacher) user);
            }
            
            return true;
        });
        
        return result != null && result;
    }
    
    /**
     * Updates an existing user.
     * @param user the user to update
     * @return true if user was updated successfully
     * @throws DatabaseException if database error occurs
     */
    public boolean updateUser(User user) throws DatabaseException {
        Boolean result = databaseManager.executeTransaction(connection -> {
            // Update USERS table
            String userSql = """
                UPDATE USERS 
                SET username = ?, email = ?, first_name = ?, last_name = ?, 
                    phone_number = ?, gender = ?, photo_path = ?,
                    role = ?, is_active = ?, updated_at = CURRENT_TIMESTAMP
                WHERE user_id = ?
                """;
            
            try (PreparedStatement userStatement = connection.prepareStatement(userSql)) {
                userStatement.setString(1, user.getUsername());
                userStatement.setString(2, user.getEmail());
                userStatement.setString(3, user.getFirstName());
                userStatement.setString(4, user.getLastName());
                userStatement.setString(5, user.getPhoneNumber());
                userStatement.setString(6, user.getGender());
                userStatement.setString(7, user.getPhotoPath());
                userStatement.setString(8, user.getRole().name());
                userStatement.setBoolean(9, user.isActive());
                userStatement.setInt(10, user.getUserId());
                
                int rowsAffected = userStatement.executeUpdate();
                if (rowsAffected == 0) {
                    throw new SQLException("Updating user failed, no rows affected");
                }
            }
            
            // Update role-specific data
            if (user instanceof Student) {
                updateStudentProfile(connection, (Student) user);
            } else if (user instanceof Teacher) {
                updateTeacherProfile(connection, (Teacher) user);
            }
            
            return true;
        });
        
        return result != null && result;
    }
    
    /**
     * Deletes a user and all related records.
     * @param userId the user ID to delete
     * @return true if user was deleted successfully
     * @throws DatabaseException if database error occurs
     */
    public boolean deleteUser(int userId) throws DatabaseException {
        Connection connection = null;
        try {
            connection = databaseManager.getConnection();
            connection.setAutoCommit(false); // Start transaction
            
            // First, check if user exists
            User user = findById(userId);
            if (user == null) {
                throw new DatabaseException("User not found with ID: " + userId);
            }
            
            // Handle cascade deletion based on user role
            if (user.getRole() == UserRole.TEACHER) {
                // Check if teacher has courses assigned
                String checkCoursesSql = """
                    SELECT COUNT(*) FROM COURSES c 
                    JOIN TEACHERS t ON c.teacher_id = t.teacher_id 
                    WHERE t.user_id = ?
                    """;
                
                try (PreparedStatement checkStmt = connection.prepareStatement(checkCoursesSql)) {
                    checkStmt.setInt(1, userId);
                    try (ResultSet rs = checkStmt.executeQuery()) {
                        if (rs.next() && rs.getInt(1) > 0) {
                            throw new DatabaseException("Cannot delete teacher: Teacher has courses assigned. Please reassign or delete courses first.");
                        }
                    }
                }
                
                // Delete teacher record first
                String deleteTeacherSql = "DELETE FROM TEACHERS WHERE user_id = ?";
                try (PreparedStatement stmt = connection.prepareStatement(deleteTeacherSql)) {
                    stmt.setInt(1, userId);
                    stmt.executeUpdate();
                }
                
            } else if (user.getRole() == UserRole.STUDENT) {
                // For students, attendance records and enrollments will be cascade deleted
                // due to ON DELETE CASCADE constraints
            }
            
            // Delete the user (this will cascade delete STUDENTS/TEACHERS records due to ON DELETE CASCADE)
            String deleteUserSql = "DELETE FROM USERS WHERE user_id = ?";
            int rowsAffected;
            try (PreparedStatement statement = connection.prepareStatement(deleteUserSql)) {
                statement.setInt(1, userId);
                rowsAffected = statement.executeUpdate();
            }
            
            connection.commit();
            
            if (rowsAffected > 0) {
                logger.info("User deleted successfully: ID = " + userId + ", Role = " + user.getRole());
                return true;
            } else {
                logger.warn("No user found to delete with ID: " + userId);
                return false;
            }
            
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackEx) {
                    logger.error("Failed to rollback transaction", rollbackEx);
                }
            }
            
            // Provide more specific error messages
            String errorMessage = e.getMessage();
            if (errorMessage.contains("foreign key constraint")) {
                throw new DatabaseException("Cannot delete user: User has related records that must be removed first.");
            } else if (errorMessage.contains("Cannot delete or update a parent row")) {
                throw new DatabaseException("Cannot delete user: User has dependent records. Please remove related data first.");
            } else {
                logger.error("Failed to delete user: " + userId, e);
                throw new DatabaseException("Failed to delete user: " + errorMessage);
            }
        } catch (DatabaseException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackEx) {
                    logger.error("Failed to rollback transaction", rollbackEx);
                }
            }
            throw e;
        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                    connection.close();
                } catch (SQLException e) {
                    logger.error("Failed to close connection", e);
                }
            }
        }
    }
    
    /**
     * Finds users by role.
     * @param role the user role
     * @return list of users with the specified role
     * @throws DatabaseException if database error occurs
     */
    public List<User> findByRole(UserRole role) throws DatabaseException {
        String sql = """
            SELECT u.*, s.student_number, s.program, s.year_level, s.class_section, s.enrollment_date,
                   t.employee_id, t.department, t.specialization
            FROM USERS u
            LEFT JOIN STUDENTS s ON u.user_id = s.user_id
            LEFT JOIN TEACHERS t ON u.user_id = t.user_id
            WHERE u.role = ? AND u.is_active = TRUE
            ORDER BY u.last_name, u.first_name
            """;
        
        List<User> users = new ArrayList<>();
        
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setString(1, role.name());
            
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    users.add(mapResultSetToUser(resultSet));
                }
            }
            
            return users;
            
        } catch (SQLException e) {
            logger.error("Failed to find users by role: " + role, e);
            throw DatabaseException.queryFailed(sql, e);
        }
    }
    
    /**
     * Gets all users.
     * @return list of all users
     * @throws DatabaseException if database error occurs
     */
    public List<User> findAll() throws DatabaseException {
        String sql = """
            SELECT u.*, s.student_number, s.program, s.year_level, s.class_section, s.enrollment_date,
                   t.employee_id, t.department, t.specialization
            FROM USERS u
            LEFT JOIN STUDENTS s ON u.user_id = s.user_id
            LEFT JOIN TEACHERS t ON u.user_id = t.user_id
            ORDER BY u.role, u.last_name, u.first_name
            """;
        
        List<User> users = new ArrayList<>();
        
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            
            while (resultSet.next()) {
                users.add(mapResultSetToUser(resultSet));
            }
            
            return users;
            
        } catch (SQLException e) {
            logger.error("Failed to find all users", e);
            throw DatabaseException.queryFailed(sql, e);
        }
    }
    
    /**
     * Updates user password.
     * @param userId the user ID
     * @param newPasswordHash the new password hash
     * @return true if password was updated successfully
     * @throws DatabaseException if database error occurs
     */
    public boolean updatePassword(int userId, String newPasswordHash) throws DatabaseException {
        String sql = "UPDATE USERS SET password_hash = ?, updated_at = CURRENT_TIMESTAMP WHERE user_id = ?";
        
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setString(1, newPasswordHash);
            statement.setInt(2, userId);
            
            int rowsAffected = statement.executeUpdate();
            connection.commit();
            
            logger.info("Password updated for user ID: " + userId);
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            logger.error("Failed to update password for user: " + userId, e);
            throw DatabaseException.queryFailed(sql, e);
        }
    }
    
    // Helper methods
    
    private void createStudentProfile(Connection connection, Student student) throws SQLException {
        String sql = """
            INSERT INTO STUDENTS (user_id, student_number, program, department, year_level, class_section, enrollment_date)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;
        
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, student.getUserId());
            statement.setString(2, student.getStudentNumber());
            statement.setString(3, student.getProgram());
            statement.setString(4, student.getDepartment());
            statement.setInt(5, student.getYearLevel());
            statement.setString(6, student.getClassSection() != null ? student.getClassSection() : "A");
            
            // Set enrollment date - use current date if null
            LocalDate enrollmentDate = student.getEnrollmentDate();
            if (enrollmentDate == null) {
                enrollmentDate = LocalDate.now();
            }
            statement.setDate(7, Date.valueOf(enrollmentDate));
            
            statement.executeUpdate();
        }
    }
    
    private void createTeacherProfile(Connection connection, Teacher teacher) throws SQLException {
        String sql = """
            INSERT INTO TEACHERS (user_id, employee_id, department, specialization)
            VALUES (?, ?, ?, ?)
            """;
        
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, teacher.getUserId());
            statement.setString(2, teacher.getEmployeeId());
            statement.setString(3, teacher.getDepartment());
            statement.setString(4, teacher.getSpecialization());
            
            statement.executeUpdate();
        }
    }
    
    private void updateStudentProfile(Connection connection, Student student) throws SQLException {
        String sql = """
            UPDATE STUDENTS 
            SET student_number = ?, program = ?, year_level = ?, enrollment_date = ?
            WHERE user_id = ?
            """;
        
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, student.getStudentNumber());
            statement.setString(2, student.getProgram());
            statement.setInt(3, student.getYearLevel());
            statement.setDate(4, Date.valueOf(student.getEnrollmentDate()));
            statement.setInt(5, student.getUserId());
            
            statement.executeUpdate();
        }
    }
    
    private void updateTeacherProfile(Connection connection, Teacher teacher) throws SQLException {
        String sql = """
            UPDATE TEACHERS 
            SET employee_id = ?, department = ?, specialization = ?
            WHERE user_id = ?
            """;
        
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, teacher.getEmployeeId());
            statement.setString(2, teacher.getDepartment());
            statement.setString(3, teacher.getSpecialization());
            statement.setInt(4, teacher.getUserId());
            
            statement.executeUpdate();
        }
    }
    
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        UserRole role = UserRole.valueOf(rs.getString("role"));
        
        User user;
        switch (role) {
            case STUDENT:
                user = new Student();
                Student student = (Student) user;
                student.setStudentNumber(rs.getString("student_number"));
                student.setProgram(rs.getString("program"));
                student.setDepartment(rs.getString("department"));
                student.setYearLevel(rs.getInt("year_level"));
                student.setClassSection(rs.getString("class_section"));
                Date enrollmentDate = rs.getDate("enrollment_date");
                if (enrollmentDate != null) {
                    student.setEnrollmentDate(enrollmentDate.toLocalDate());
                }
                break;
                
            case TEACHER:
                user = new Teacher();
                Teacher teacher = (Teacher) user;
                teacher.setEmployeeId(rs.getString("employee_id"));
                teacher.setDepartment(rs.getString("department"));
                teacher.setSpecialization(rs.getString("specialization"));
                break;
                
            case ADMIN:
                user = new Admin();
                break;
                
            default:
                throw new SQLException("Unknown user role: " + role);
        }
        
        // Set common user fields
        user.setUserId(rs.getInt("user_id"));
        user.setUsername(rs.getString("username"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setEmail(rs.getString("email"));
        user.setFirstName(rs.getString("first_name"));
        user.setLastName(rs.getString("last_name"));
        user.setPhoneNumber(rs.getString("phone_number"));
        user.setGender(rs.getString("gender"));
        user.setPhotoPath(rs.getString("photo_path"));
        user.setRole(role);
        user.setActive(rs.getBoolean("is_active"));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            user.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            user.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        
        return user;
    }

    /**
     * Tests database connection.
     * @return true if connection is successful
     * @throws DatabaseException if connection test fails
     */
    public boolean testConnection() throws DatabaseException {
        try (Connection connection = databaseManager.getConnection();
             Statement statement = connection.createStatement()) {
            
            statement.executeQuery("SELECT 1");
            return true;
            
        } catch (SQLException e) {
            logger.error("Database connection test failed", e);
            throw new DatabaseException("Connection test failed", e);
        }
    }
    
    /**
     * Gets the total number of users in the system.
     * @return total user count
     * @throws DatabaseException if database operation fails
     */
    public int getTotalUserCount() throws DatabaseException {
        String sql = "SELECT COUNT(*) FROM USERS";
        
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
            
        } catch (SQLException e) {
            logger.error("Failed to get total user count", e);
            throw new DatabaseException("Failed to get total user count", e);
        }
    }
    
    /**
     * Gets the number of active users in the system.
     * @return active user count
     * @throws DatabaseException if database operation fails
     */
    public int getActiveUserCount() throws DatabaseException {
        String sql = "SELECT COUNT(*) FROM USERS WHERE is_active = true";
        
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
            
        } catch (SQLException e) {
            logger.error("Failed to get active user count", e);
            throw new DatabaseException("Failed to get active user count", e);
        }
    }
}