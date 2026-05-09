package com.attendance.system.integration;

import com.attendance.system.dao.UserDAO;
import com.attendance.system.exception.DatabaseException;
import com.attendance.system.exception.ValidationException;
import com.attendance.system.model.Student;
import com.attendance.system.model.Teacher;
import com.attendance.system.model.User;
import com.attendance.system.model.UserRole;
import com.attendance.system.server.AttendanceServer;
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
 * Integration tests for registration feature.
 * Tests end-to-end registration flow with database operations.
 */
@DisplayName("Registration Integration Tests")
public class RegistrationIntegrationTest {
    
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
    
    // End-to-End Registration Tests
    
    @Test
    @DisplayName("Integration: complete student registration flow")
    public void testCompleteStudentRegistrationFlow() throws RemoteException, ValidationException, DatabaseException {
        // Setup
        when(mockUserDAO.findByUsername("john.doe")).thenReturn(null);
        when(mockUserDAO.findByEmail("john.doe@example.com")).thenReturn(null);
        when(mockUserDAO.createUser(any(User.class))).thenReturn(true);
        
        // Execute
        boolean result = server.registerUser("john.doe", "john.doe@example.com", "John", "Doe", "SecurePass123!", UserRole.STUDENT, "A"
        , null, null, null, null);
        
        // Verify
        assertTrue(result);
        verify(mockUserDAO).findByUsername("john.doe");
        verify(mockUserDAO).findByEmail("john.doe@example.com");
        verify(mockUserDAO).createUser(any(Student.class));
    }
    
    @Test
    @DisplayName("Integration: complete teacher registration flow")
    public void testCompleteTeacherRegistrationFlow() throws RemoteException, ValidationException, DatabaseException {
        // Setup
        when(mockUserDAO.findByUsername("jane.smith")).thenReturn(null);
        when(mockUserDAO.findByEmail("jane.smith@example.com")).thenReturn(null);
        when(mockUserDAO.createUser(any(User.class))).thenReturn(true);
        
        // Execute
        boolean result = server.registerUser("jane.smith", "jane.smith@example.com", "Jane", "Smith", "TeacherPass123!", UserRole.TEACHER, null
        , null, null, null, "Computer Science");
        
        // Verify
        assertTrue(result);
        verify(mockUserDAO).findByUsername("jane.smith");
        verify(mockUserDAO).findByEmail("jane.smith@example.com");
        verify(mockUserDAO).createUser(any(Teacher.class));
    }
    
    @Test
    @DisplayName("Integration: registration with multiple validation checks")
    public void testRegistrationWithMultipleValidations() throws RemoteException, ValidationException, DatabaseException {
        // Setup - simulate existing user
        User existingUser = new Student();
        existingUser.setUsername("existing.user");
        when(mockUserDAO.findByUsername("existing.user")).thenReturn(existingUser);
        
        // Execute & Verify - should fail due to duplicate username
        assertThrows(ValidationException.class, () -> {
            server.registerUser("existing.user", "new@example.com", "New", "User", "ValidPass123!", UserRole.STUDENT, "A"
            , null, null, null, null);
        });
        
        // Verify that email check was not performed (failed at username check)
        verify(mockUserDAO).findByUsername("existing.user");
        verify(mockUserDAO, never()).findByEmail(anyString());
    }
    
    @Test
    @DisplayName("Integration: registration failure with database error")
    public void testRegistrationDatabaseError() throws DatabaseException {
        // Setup
        when(mockUserDAO.findByUsername("newuser")).thenReturn(null);
        when(mockUserDAO.findByEmail("new@example.com")).thenReturn(null);
        when(mockUserDAO.createUser(any(User.class))).thenThrow(
            new DatabaseException("Connection failed")
        );
        
        // Execute & Verify
        assertThrows(DatabaseException.class, () -> {
            server.registerUser("newuser", "new@example.com", "New", "User", "ValidPass123!", UserRole.STUDENT, "A"
            , null, null, null, null);
        });
    }
    
    // Data Integrity Tests
    
    @Test
    @DisplayName("Integration: registered user data integrity")
    public void testRegisteredUserDataIntegrity() throws RemoteException, ValidationException, DatabaseException {
        // Setup
        when(mockUserDAO.findByUsername("testuser")).thenReturn(null);
        when(mockUserDAO.findByEmail("test@example.com")).thenReturn(null);
        when(mockUserDAO.createUser(any(User.class))).thenReturn(true);
        
        // Execute
        server.registerUser("testuser", "test@example.com", "Test", "User", "TestPass123!", UserRole.STUDENT, "A"
        , null, null, null, null);
        
        // Verify user data
        verify(mockUserDAO).createUser(argThat(user -> {
            return user.getUsername().equals("testuser") &&
                   user.getEmail().equals("test@example.com") &&
                   user.getFirstName().equals("Test") &&
                   user.getLastName().equals("User") &&
                   user.getRole() == UserRole.STUDENT &&
                   user.isActive();
        }));
    }
    
    @Test
    @DisplayName("Integration: password is properly hashed")
    public void testPasswordProperlyHashed() throws RemoteException, ValidationException, DatabaseException {
        // Setup
        when(mockUserDAO.findByUsername("testuser")).thenReturn(null);
        when(mockUserDAO.findByEmail("test@example.com")).thenReturn(null);
        when(mockUserDAO.createUser(any(User.class))).thenReturn(true);
        
        String plainPassword = "TestPass123!";
        
        // Execute
        server.registerUser("testuser", "test@example.com", "Test", "User", plainPassword, UserRole.STUDENT, "A"
        , null, null, null, null);
        
        // Verify password is hashed
        verify(mockUserDAO).createUser(argThat(user -> {
            String hashedPassword = user.getPasswordHash();
            // Password should not be plaintext
            return !hashedPassword.equals(plainPassword) &&
                   hashedPassword != null &&
                   !hashedPassword.isEmpty();
        }));
    }
    
    // Concurrent Registration Tests
    
    @Test
    @DisplayName("Integration: concurrent registrations with same username")
    public void testConcurrentRegistrationsSameUsername() throws InterruptedException, DatabaseException {
        // Setup
        when(mockUserDAO.findByUsername("concurrent")).thenReturn(null);
        when(mockUserDAO.findByEmail("user1@example.com")).thenReturn(null);
        when(mockUserDAO.findByEmail("user2@example.com")).thenReturn(null);
        when(mockUserDAO.createUser(any(User.class))).thenReturn(true);
        
        // Execute concurrent registrations
        Thread thread1 = new Thread(() -> {
            try {
                server.registerUser("concurrent", "user1@example.com", "User", "One", "Pass123!", UserRole.STUDENT, "A", null, null, null, null);
            } catch (Exception e) {
                // Expected to fail
            }
        });
        
        Thread thread2 = new Thread(() -> {
            try {
                server.registerUser("concurrent", "user2@example.com", "User", "Two", "Pass123!", UserRole.STUDENT, "A", null, null, null, null);
            } catch (Exception e) {
                // Expected to fail
            }
        });
        
        thread1.start();
        thread2.start();
        thread1.join();
        thread2.join();
        
        // Verify that createUser was called (at least once)
        verify(mockUserDAO, atLeastOnce()).createUser(any(User.class));
    }
    
    // Edge Case Tests
    
    @Test
    @DisplayName("Integration: registration with special characters in name")
    public void testRegistrationWithSpecialCharactersInName() throws RemoteException, ValidationException, DatabaseException {
        // Setup
        when(mockUserDAO.findByUsername("testuser")).thenReturn(null);
        when(mockUserDAO.findByEmail("test@example.com")).thenReturn(null);
        when(mockUserDAO.createUser(any(User.class))).thenReturn(true);
        
        // Execute
        boolean result = server.registerUser("testuser", "test@example.com", "Jean-Pierre", "O'Brien", "TestPass123!", UserRole.STUDENT, "A"
        , null, null, null, null);
        
        // Verify
        assertTrue(result);
        verify(mockUserDAO).createUser(argThat(user -> 
            user.getFirstName().equals("Jean-Pierre") &&
            user.getLastName().equals("O'Brien")
        ));
    }
    
    @Test
    @DisplayName("Integration: registration with maximum length fields")
    public void testRegistrationWithMaximumLengthFields() throws RemoteException, ValidationException, DatabaseException {
        // Setup
        String maxUsername = "a".repeat(50);
        String maxFirstName = "b".repeat(50);
        String maxLastName = "c".repeat(50);
        
        when(mockUserDAO.findByUsername(maxUsername)).thenReturn(null);
        when(mockUserDAO.findByEmail("test@example.com")).thenReturn(null);
        when(mockUserDAO.createUser(any(User.class))).thenReturn(true);
        
        // Execute
        boolean result = server.registerUser(maxUsername, "test@example.com", maxFirstName, maxLastName, "TestPass123!", UserRole.STUDENT, "A"
        , null, null, null, null);
        
        // Verify
        assertTrue(result);
        verify(mockUserDAO).createUser(any(User.class));
    }
    
    @Test
    @DisplayName("Integration: registration with minimum length fields")
    public void testRegistrationWithMinimumLengthFields() throws RemoteException, ValidationException, DatabaseException {
        // Setup
        when(mockUserDAO.findByUsername("abc")).thenReturn(null);
        when(mockUserDAO.findByEmail("a@b.c")).thenReturn(null);
        when(mockUserDAO.createUser(any(User.class))).thenReturn(true);
        
        // Execute
        boolean result = server.registerUser("abc", "a@b.c", "A", "B", "TestPass123!", UserRole.STUDENT, "A"
        , null, null, null, null);
        
        // Verify
        assertTrue(result);
        verify(mockUserDAO).createUser(any(User.class));
    }
}
