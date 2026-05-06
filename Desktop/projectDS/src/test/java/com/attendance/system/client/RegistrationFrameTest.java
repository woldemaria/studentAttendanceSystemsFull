package com.attendance.system.client;

import com.attendance.system.exception.ValidationException;
import com.attendance.system.model.UserRole;
import com.attendance.system.service.AttendanceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import javax.swing.*;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RegistrationFrame component.
 * Tests validation logic and form behavior.
 */
@DisplayName("RegistrationFrame Tests")
public class RegistrationFrameTest {
    
    private RegistrationFrame registrationFrame;
    private AttendanceGUI mockParentFrame;
    private AttendanceService mockService;
    
    @BeforeEach
    public void setUp() {
        mockParentFrame = mock(AttendanceGUI.class);
        mockService = mock(AttendanceService.class);
        when(mockParentFrame.getAttendanceService()).thenReturn(mockService);
        
        registrationFrame = new RegistrationFrame(mockParentFrame, () -> {});
    }
    
    // Username Validation Tests
    
    @Test
    @DisplayName("Username validation: empty username should fail")
    public void testValidateUsernameEmpty() {
        setFieldValue("usernameField", new JTextField(""));
        assertFalse(invokeValidateUsername());
    }
    
    @Test
    @DisplayName("Username validation: username too short should fail")
    public void testValidateUsernameTooShort() {
        setFieldValue("usernameField", new JTextField("ab"));
        assertFalse(invokeValidateUsername());
    }
    
    @Test
    @DisplayName("Username validation: username too long should fail")
    public void testValidateUsernameTooLong() {
        setFieldValue("usernameField", new JTextField("a".repeat(51)));
        assertFalse(invokeValidateUsername());
    }
    
    @Test
    @DisplayName("Username validation: valid username should pass")
    public void testValidateUsernameValid() {
        setFieldValue("usernameField", new JTextField("validuser123"));
        assertTrue(invokeValidateUsername());
    }
    
    @Test
    @DisplayName("Username validation: username with invalid characters should fail")
    public void testValidateUsernameInvalidCharacters() {
        setFieldValue("usernameField", new JTextField("user@name!"));
        assertFalse(invokeValidateUsername());
    }
    
    @Test
    @DisplayName("Username validation: username with dots and underscores should pass")
    public void testValidateUsernameWithSpecialChars() {
        setFieldValue("usernameField", new JTextField("user.name_123"));
        assertTrue(invokeValidateUsername());
    }
    
    // Email Validation Tests
    
    @Test
    @DisplayName("Email validation: empty email should fail")
    public void testValidateEmailEmpty() {
        setFieldValue("emailField", new JTextField(""));
        assertFalse(invokeValidateEmail());
    }
    
    @Test
    @DisplayName("Email validation: invalid email format should fail")
    public void testValidateEmailInvalidFormat() {
        setFieldValue("emailField", new JTextField("notanemail"));
        assertFalse(invokeValidateEmail());
    }
    
    @Test
    @DisplayName("Email validation: valid email should pass")
    public void testValidateEmailValid() {
        setFieldValue("emailField", new JTextField("user@example.com"));
        assertTrue(invokeValidateEmail());
    }
    
    @Test
    @DisplayName("Email validation: email with plus sign should pass")
    public void testValidateEmailWithPlus() {
        setFieldValue("emailField", new JTextField("user+tag@example.com"));
        assertTrue(invokeValidateEmail());
    }
    
    // First Name Validation Tests
    
    @Test
    @DisplayName("First name validation: empty first name should fail")
    public void testValidateFirstNameEmpty() {
        setFieldValue("firstNameField", new JTextField(""));
        assertFalse(invokeValidateFirstName());
    }
    
    @Test
    @DisplayName("First name validation: first name too long should fail")
    public void testValidateFirstNameTooLong() {
        setFieldValue("firstNameField", new JTextField("a".repeat(51)));
        assertFalse(invokeValidateFirstName());
    }
    
    @Test
    @DisplayName("First name validation: valid first name should pass")
    public void testValidateFirstNameValid() {
        setFieldValue("firstNameField", new JTextField("John"));
        assertTrue(invokeValidateFirstName());
    }
    
    // Last Name Validation Tests
    
    @Test
    @DisplayName("Last name validation: empty last name should fail")
    public void testValidateLastNameEmpty() {
        setFieldValue("lastNameField", new JTextField(""));
        assertFalse(invokeValidateLastName());
    }
    
    @Test
    @DisplayName("Last name validation: last name too long should fail")
    public void testValidateLastNameTooLong() {
        setFieldValue("lastNameField", new JTextField("a".repeat(51)));
        assertFalse(invokeValidateLastName());
    }
    
    @Test
    @DisplayName("Last name validation: valid last name should pass")
    public void testValidateLastNameValid() {
        setFieldValue("lastNameField", new JTextField("Smith"));
        assertTrue(invokeValidateLastName());
    }
    
    // Password Validation Tests
    
    @Test
    @DisplayName("Password validation: empty password should fail")
    public void testValidatePasswordEmpty() {
        setFieldValue("passwordField", new JPasswordField(""));
        assertFalse(invokeValidatePassword());
    }
    
    @Test
    @DisplayName("Password validation: password too short should fail")
    public void testValidatePasswordTooShort() {
        setFieldValue("passwordField", new JPasswordField("Pass1!"));
        assertFalse(invokeValidatePassword());
    }
    
    @Test
    @DisplayName("Password validation: password without uppercase should fail")
    public void testValidatePasswordNoUppercase() {
        setFieldValue("passwordField", new JPasswordField("password123!"));
        assertFalse(invokeValidatePassword());
    }
    
    @Test
    @DisplayName("Password validation: password without lowercase should fail")
    public void testValidatePasswordNoLowercase() {
        setFieldValue("passwordField", new JPasswordField("PASSWORD123!"));
        assertFalse(invokeValidatePassword());
    }
    
    @Test
    @DisplayName("Password validation: password without digit should fail")
    public void testValidatePasswordNoDigit() {
        setFieldValue("passwordField", new JPasswordField("Password!"));
        assertFalse(invokeValidatePassword());
    }
    
    @Test
    @DisplayName("Password validation: password without special character should fail")
    public void testValidatePasswordNoSpecialChar() {
        setFieldValue("passwordField", new JPasswordField("Password123"));
        assertFalse(invokeValidatePassword());
    }
    
    @Test
    @DisplayName("Password validation: valid password should pass")
    public void testValidatePasswordValid() {
        setFieldValue("passwordField", new JPasswordField("ValidPass123!"));
        assertTrue(invokeValidatePassword());
    }
    
    // Confirm Password Validation Tests
    
    @Test
    @DisplayName("Confirm password validation: empty confirm password should fail")
    public void testValidateConfirmPasswordEmpty() {
        setFieldValue("passwordField", new JPasswordField("ValidPass123!"));
        setFieldValue("confirmPasswordField", new JPasswordField(""));
        assertFalse(invokeValidateConfirmPassword());
    }
    
    @Test
    @DisplayName("Confirm password validation: mismatched passwords should fail")
    public void testValidateConfirmPasswordMismatch() {
        setFieldValue("passwordField", new JPasswordField("ValidPass123!"));
        setFieldValue("confirmPasswordField", new JPasswordField("DifferentPass123!"));
        assertFalse(invokeValidateConfirmPassword());
    }
    
    @Test
    @DisplayName("Confirm password validation: matching passwords should pass")
    public void testValidateConfirmPasswordMatch() {
        setFieldValue("passwordField", new JPasswordField("ValidPass123!"));
        setFieldValue("confirmPasswordField", new JPasswordField("ValidPass123!"));
        assertTrue(invokeValidateConfirmPassword());
    }
    
    // Helper Methods
    
    private void setFieldValue(String fieldName, Object value) {
        try {
            Field field = RegistrationFrame.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(registrationFrame, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail("Failed to set field: " + fieldName, e);
        }
    }
    
    private boolean invokeValidateUsername() {
        try {
            var method = RegistrationFrame.class.getDeclaredMethod("validateUsername");
            method.setAccessible(true);
            return (boolean) method.invoke(registrationFrame);
        } catch (Exception e) {
            fail("Failed to invoke validateUsername", e);
            return false;
        }
    }
    
    private boolean invokeValidateEmail() {
        try {
            var method = RegistrationFrame.class.getDeclaredMethod("validateEmail");
            method.setAccessible(true);
            return (boolean) method.invoke(registrationFrame);
        } catch (Exception e) {
            fail("Failed to invoke validateEmail", e);
            return false;
        }
    }
    
    private boolean invokeValidateFirstName() {
        try {
            var method = RegistrationFrame.class.getDeclaredMethod("validateFirstName");
            method.setAccessible(true);
            return (boolean) method.invoke(registrationFrame);
        } catch (Exception e) {
            fail("Failed to invoke validateFirstName", e);
            return false;
        }
    }
    
    private boolean invokeValidateLastName() {
        try {
            var method = RegistrationFrame.class.getDeclaredMethod("validateLastName");
            method.setAccessible(true);
            return (boolean) method.invoke(registrationFrame);
        } catch (Exception e) {
            fail("Failed to invoke validateLastName", e);
            return false;
        }
    }
    
    private boolean invokeValidatePassword() {
        try {
            var method = RegistrationFrame.class.getDeclaredMethod("validatePassword");
            method.setAccessible(true);
            return (boolean) method.invoke(registrationFrame);
        } catch (Exception e) {
            fail("Failed to invoke validatePassword", e);
            return false;
        }
    }
    
    private boolean invokeValidateConfirmPassword() {
        try {
            var method = RegistrationFrame.class.getDeclaredMethod("validateConfirmPassword");
            method.setAccessible(true);
            return (boolean) method.invoke(registrationFrame);
        } catch (Exception e) {
            fail("Failed to invoke validateConfirmPassword", e);
            return false;
        }
    }
}
