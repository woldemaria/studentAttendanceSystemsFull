package com.attendance.system.server;

import com.attendance.system.dao.UserDAO;
import com.attendance.system.exception.DatabaseException;
import com.attendance.system.exception.ValidationException;
import com.attendance.system.model.Student;
import com.attendance.system.model.Teacher;
import com.attendance.system.model.User;
import com.attendance.system.model.UserRole;
import com.attendance.system.service.AttendanceServiceImpl;
import com.attendance.system.service.AuthenticationService;
import com.attendance.system.util.SecurityUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.rmi.RemoteException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for server-side registration functionality.
 * Tests validation, duplicate prevention, and user creation.
 */
@DisplayName("Registration Server Tests")
public class RegistrationServerTest {
    
    private AttendanceServer server;
    private UserDAO mockUserDAO;
    private AuthenticationService mockAuthService;
    private AttendanceServiceImpl mockAttendanceService;
    
    @BeforeEach
    public void setUp() throws RemoteException {
        mockUserDAO = mock(UserDAO.class);
        mockAuthService = mock(AuthenticationService.class);
        mockAttendanceService = mock(AttendanceServiceImpl.class);
        
        server = new AttendanceServer(
            mockAuthService,
            mockAttendanceService,
            mockUserDAO,
            mock(com.attendance.system.dao.AttendanceDAO.class),
            mock(com.attendance.system.dao.CourseDAO.class)
        );
    }
    
    // Username Validation Tests
    
    @Test
    @DisplayName("Registration: empty username should throw ValidationException")
    public void testRegisterEmptyUsername() {
        assertThrows(ValidationException.class, () -> {
            server.registerUser("", "user@example.com", "John", "Doe", "ValidPass123!", UserRole.STUDENT);
        });
    }
    
    @Test
    @DisplayName("Registration: username too short should throw ValidationException")
    public void testRegisterUsernameTooShort() {
        assertThrows(ValidationException.class, () -> {
            server.registerUser("ab", "user@example.com", "John", "Doe", "ValidPass123!", UserRole.STUDENT);
        });
    }
    
    @Test
    @DisplayName("Registration: username too long should throw ValidationException")
    public void testRegisterUsernameTooLong() {
        assertThrows(ValidationException.class, () -> {
            server.registerUser("a".repeat(51), "user@example.com", "John", "Doe", "ValidPass123!", UserRole.STUDENT);
        });
    }
    
    @Test
    @DisplayName("Registration: username with invalid characters should throw ValidationException")
    public void testRegisterUsernameInvalidCharacters() {
        assertThrows(ValidationException.class, () -> {
            server.registerUser("user@name!", "user@example.com", "John", "Doe", "ValidPass123!", UserRole.STUDENT);
        });
    }
    
    // Email Validation Tests
    
    @Test
    @DisplayName("Registration: empty email should throw ValidationException")
    public void testRegisterEmptyEmail() {
        assertThrows(ValidationException.class, () -> {
            server.registerUser("validuser", "", "John", "Doe", "ValidPass123!", UserRole.STUDENT);
        });
    }
    
    @Test
    @DisplayName("Registration: invalid email format should throw ValidationException")
    public void testRegisterInvalidEmailFormat() {
        assertThrows(ValidationException.class, () -> {
            server.registerUser("validuser", "notanemail", "John", "Doe", "ValidPass123!", UserRole.STUDENT);
        });
    }
    
    // Name Validation Tests
    
    @Test
    @DisplayName("Registration: empty first name should throw ValidationException")
    public void testRegisterEmptyFirstName() {
        assertThrows(ValidationException.class, () -> {
            server.registerUser("validuser", "user@example.com", "", "Doe", "ValidPass123!", UserRole.STUDENT);
        });
    }
    
    @Test
    @DisplayName("Registration: empty last name should throw ValidationException")
    public void testRegisterEmptyLastName() {
        assertThrows(ValidationException.class, () -> {
            server.registerUser("validuser", "user@example.com", "John", "", "ValidPass123!", UserRole.STUDENT);
        });
    }
    
    @Test
    @DisplayName("Registration: first name too long should throw ValidationException")
    public void testRegisterFirstNameTooLong() {
        assertThrows(ValidationException.class, () -> {
            server.registerUser("validuser", "user@example.com", "a".repeat(51), "Doe", "ValidPass123!", UserRole.STUDENT);
        });
    }
    
    @Test
    @DisplayName("Registration: last name too long should throw ValidationException")
    public void testRegisterLastNameTooLong() {
        assertThrows(ValidationException.class, () -> {
            server.registerUser("validuser", "user@example.com", "John", "a".repeat(51), "ValidPass123!", UserRole.STUDENT);
        });
    }
    
    // Password Validation Tests
    
    @Test
    @DisplayName("Registration: empty password should throw ValidationException")
    public void testRegisterEmptyPassword() {
        assertThrows(ValidationException.class, () -> {
            server.registerUser("validuser", "user@example.com", "John", "Doe", "", UserRole.STUDENT);
        });
    }
    
    @Test
    @DisplayName("Registration: password too short should throw ValidationException")
    public void testRegisterPasswordTooShort() {
        assertThrows(ValidationException.class, () -> {
            server.registerUser("validuser", "user@example.com", "John", "Doe", "Pass1!", UserRole.STUDENT);
        });
    }
    
    @Test
    @DisplayName("Registration: password without uppercase should throw ValidationException")
    public void testRegisterPasswordNoUppercase() {
        assertThrows(ValidationException.class, () -> {
            server.registerUser("validuser", "user@example.com", "John", "Doe", "password123!", UserRole.STUDENT);
        });
    }
    
    @Test
    @DisplayName("Registration: password without lowercase should throw ValidationException")
    public void testRegisterPasswordNoLowercase() {
        assertThrows(ValidationException.class, () -> {
            server.registerUser("validuser", "user@example.com", "John", "Doe", "PASSWORD123!", UserRole.STUDENT);
        });
    }
    
    @Test
    @DisplayName("Registration: password without digit should throw ValidationException")
    public void testRegisterPasswordNoDigit() {
        assertThrows(ValidationException.class, () -> {
            server.registerUser("validuser", "user@example.com", "John", "Doe", "Password!", UserRole.STUDENT);
        });
    }
    
    @Test
    @DisplayName("Registration: password without special character should throw ValidationException")
    public void testRegisterPasswordNoSpecialChar() {
        assertThrows(ValidationException.class, () -> {
            server.registerUser("validuser", "user@example.com", "John", "Doe", "Password123", UserRole.STUDENT);
        });
    }
    
    // Role Validation Tests
    
    @Test
    @DisplayName("Registration: ADMIN role should throw ValidationException")
    public void testRegisterAdminRole() {
        assertThrows(ValidationException.class, () -> {
            server.registerUser("validuser", "user@example.com", "John", "Doe", "ValidPass123!", UserRole.ADMIN);
        });
    }
    
    // Duplicate Prevention Tests
    
    @Test
    @DisplayName("Registration: duplicate username should throw ValidationException")
    public void testRegisterDuplicateUsername() {
        User existingUser = new Student();
        existingUser.setUsername("validuser");
        when(mockUserDAO.findByUsername("validuser")).thenReturn(existingUser);
        
        assertThrows(ValidationException.class, () -> {
            server.registerUser("validuser", "user@example.com", "John", "Doe", "ValidPass123!", UserRole.STUDENT);
        });
    }
    
    @Test
    @DisplayName("Registration: duplicate email should throw ValidationException")
    public void testRegisterDuplicateEmail() {
        User existingUser = new Student();
        existingUser.setEmail("user@example.com");
        when(mockUserDAO.findByUsername("validuser")).thenReturn(null);
        when(mockUserDAO.findByEmail("user@example.com")).thenReturn(existingUser);
        
        assertThrows(ValidationException.class, () -> {
            server.registerUser("validuser", "user@example.com", "John", "Doe", "ValidPass123!", UserRole.STUDENT);
        });
    }
    
    // Successful Registration Tests
    
    @Test
    @DisplayName("Registration: valid student registration should succeed")
    public void testRegisterValidStudent() throws RemoteException, ValidationException, DatabaseException {
        when(mockUserDAO.findByUsername("validuser")).thenReturn(null);
        when(mockUserDAO.findByEmail("user@example.com")).thenReturn(null);
        when(mockUserDAO.createUser(any(User.class))).thenReturn(true);
        
        boolean result = server.registerUser("validuser", "user@example.com", "John", "Doe", "ValidPass123!", UserRole.STUDENT);
        
        assertTrue(result);
        verify(mockUserDAO).createUser(any(Student.class));
    }
    
    @Test
    @DisplayName("Registration: valid teacher registration should succeed")
    public void testRegisterValidTeacher() throws RemoteException, ValidationException, DatabaseException {
        when(mockUserDAO.findByUsername("validuser")).thenReturn(null);
        when(mockUserDAO.findByEmail("user@example.com")).thenReturn(null);
        when(mockUserDAO.createUser(any(User.class))).thenReturn(true);
        
        boolean result = server.registerUser("validuser", "user@example.com", "Jane", "Smith", "ValidPass123!", UserRole.TEACHER);
        
        assertTrue(result);
        verify(mockUserDAO).createUser(any(Teacher.class));
    }
    
    @Test
    @DisplayName("Registration: password should be hashed before storage")
    public void testRegisterPasswordHashing() throws RemoteException, ValidationException, DatabaseException {
        when(mockUserDAO.findByUsername("validuser")).thenReturn(null);
        when(mockUserDAO.findByEmail("user@example.com")).thenReturn(null);
        when(mockUserDAO.createUser(any(User.class))).thenReturn(true);
        
        server.registerUser("validuser", "user@example.com", "John", "Doe", "ValidPass123!", UserRole.STUDENT);
        
        // Verify that createUser was called with a user that has a hashed password
        verify(mockUserDAO).createUser(argThat(user -> 
            user.getPasswordHash() != null && 
            !user.getPasswordHash().equals("ValidPass123!")
        ));
    }
    
    @Test
    @DisplayName("Registration: user should be active after registration")
    public void testRegisterUserActive() throws RemoteException, ValidationException, DatabaseException {
        when(mockUserDAO.findByUsername("validuser")).thenReturn(null);
        when(mockUserDAO.findByEmail("user@example.com")).thenReturn(null);
        when(mockUserDAO.createUser(any(User.class))).thenReturn(true);
        
        server.registerUser("validuser", "user@example.com", "John", "Doe", "ValidPass123!", UserRole.STUDENT);
        
        // Verify that createUser was called with an active user
        verify(mockUserDAO).createUser(argThat(user -> user.isActive()));
    }
    
    @Test
    @DisplayName("Registration: database failure should throw DatabaseException")
    public void testRegisterDatabaseFailure() {
        when(mockUserDAO.findByUsername("validuser")).thenReturn(null);
        when(mockUserDAO.findByEmail("user@example.com")).thenReturn(null);
        when(mockUserDAO.createUser(any(User.class))).thenThrow(new DatabaseException("Database error"));
        
        assertThrows(DatabaseException.class, () -> {
            server.registerUser("validuser", "user@example.com", "John", "Doe", "ValidPass123!", UserRole.STUDENT);
        });
    }
}
