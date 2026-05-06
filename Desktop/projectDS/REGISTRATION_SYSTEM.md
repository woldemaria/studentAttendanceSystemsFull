# Registration System Documentation

## Overview

The Registration System enables new users (students and teachers) to self-register for accounts in the Student Attendance System without requiring administrator intervention. This feature provides a user-friendly interface for account creation with comprehensive validation and security measures.

## Features

### 1. Self-Registration Interface
- **RegistrationFrame Component**: A comprehensive GUI panel for user registration
- **Role Selection**: Users can choose between Student and Teacher roles during registration
- **Form Validation**: Real-time validation with user-friendly error messages
- **Password Strength Requirements**: Enforces strong password policies

### 2. User Input Validation

#### Username Validation
- Required field
- Minimum 3 characters, maximum 50 characters
- Allowed characters: letters, numbers, dots (.), underscores (_), hyphens (-)
- Must be unique in the system

#### Email Validation
- Required field
- Must follow standard email format (user@domain.com)
- Must be unique in the system

#### Name Validation
- First name and last name both required
- Maximum 50 characters each
- Supports all alphabetic characters and spaces

#### Password Validation
- Minimum 8 characters
- Must contain at least one uppercase letter (A-Z)
- Must contain at least one lowercase letter (a-z)
- Must contain at least one digit (0-9)
- Must contain at least one special character (!@#$%^&*()_+-=[]{}';:"\\|,.<>/?))

### 3. Security Features

#### Data Encryption
- All sensitive data (username, email, password) is encrypted during transmission if encryption is enabled
- Passwords are hashed using BCrypt with salt before storage
- No plaintext passwords are stored in the database

#### Account Activation
- New accounts are automatically activated upon registration
- Users can immediately log in after successful registration

#### Validation on Server-Side
- All validation is performed on the server to prevent bypass attempts
- Duplicate username/email checks are performed at registration time
- Password policy enforcement is validated server-side

### 4. User Experience

#### Real-Time Feedback
- Field validation occurs as users type
- Error messages appear immediately below invalid fields
- Error fields are highlighted with red borders
- Success messages are displayed upon completion

#### Progress Indication
- Progress bar shows during registration processing
- Form is disabled during server communication
- Status messages keep users informed of registration progress

#### Navigation
- "Cancel" button returns users to the login screen
- "Register" button submits the registration form
- Enter key can be used to submit the form

## Architecture

### Components

#### 1. RegistrationFrame (Client-Side)
**Location**: `src/main/java/com/attendance/system/client/RegistrationFrame.java`

**Responsibilities**:
- Display registration form with all required fields
- Perform client-side validation
- Handle user input and form submission
- Display error messages and status updates
- Communicate with remote registration service

**Key Methods**:
- `initializeComponents()`: Creates all GUI components
- `setupLayout()`: Arranges components in the form
- `setupEventHandlers()`: Configures event listeners
- `validateUsername()`: Validates username field
- `validateEmail()`: Validates email field
- `validateFirstName()`: Validates first name field
- `validateLastName()`: Validates last name field
- `validatePassword()`: Validates password field
- `validateConfirmPassword()`: Validates password confirmation
- `performRegistration()`: Submits registration to server

#### 2. LoginFrame (Updated)
**Location**: `src/main/java/com/attendance/system/client/LoginFrame.java`

**Changes**:
- Added "Register" button to the login form
- Button navigates to registration frame when clicked
- Allows users to switch between login and registration

#### 3. AttendanceGUI (Updated)
**Location**: `src/main/java/com/attendance/system/client/AttendanceGUI.java`

**Changes**:
- Added `registrationFrame` field to store registration panel
- Added `showRegistrationFrame()` method to display registration
- Integrated registration frame into card layout

#### 4. AttendanceService Interface (Updated)
**Location**: `src/main/java/com/attendance/system/service/AttendanceService.java`

**New Method**:
```java
boolean registerUser(String username, String email, String firstName, String lastName, 
                    String password, UserRole role) 
        throws RemoteException, ValidationException, DatabaseException;
```

#### 5. AttendanceServer (Updated)
**Location**: `src/main/java/com/attendance/system/server/AttendanceServer.java`

**New Implementation**:
- `registerUser()` method implements server-side registration logic
- Validates all input parameters
- Checks for duplicate usernames and emails
- Creates new user account in database
- Logs registration events for audit trail

## Registration Flow

### Client-Side Flow
1. User clicks "Register" button on login screen
2. Registration frame is displayed
3. User fills in all required fields
4. Client-side validation occurs in real-time
5. User clicks "Register" button
6. Form is validated completely
7. Registration request is sent to server
8. Progress bar is displayed during processing
9. Upon success, user is redirected to login screen
10. Upon failure, error message is displayed and form remains active

### Server-Side Flow
1. Server receives registration request
2. Server checks capacity and connection limits
3. Credentials are decrypted if encryption is enabled
4. All input parameters are validated
5. Username uniqueness is checked
6. Email uniqueness is checked
7. New user object is created (Student or Teacher)
8. Password is hashed using BCrypt
9. User is created in database
10. Success/failure response is sent to client
11. Registration event is logged for audit trail

## Usage

### For Users

#### To Register a New Account:
1. Launch the Student Attendance System application
2. On the login screen, click the "Register" button
3. Fill in all required fields:
   - First Name
   - Last Name
   - Username
   - Email
   - Account Type (Student or Teacher)
   - Password
   - Confirm Password
4. Check the "Show password" checkbox if you want to see your password
5. Click the "Register" button
6. Wait for the registration to complete
7. Upon success, you will be redirected to the login screen
8. Log in with your new username and password

#### Password Requirements:
- At least 8 characters long
- Contains uppercase letters (A-Z)
- Contains lowercase letters (a-z)
- Contains numbers (0-9)
- Contains special characters (!@#$%^&*()_+-=[]{}';:"\\|,.<>/?))

### For Administrators

#### To Manage Registered Users:
1. Log in as an administrator
2. Navigate to the System Administration panel
3. Go to User Management
4. View all registered users
5. Edit, deactivate, or delete user accounts as needed

## Error Handling

### Common Validation Errors

#### Username Errors
- "Username is required" - Username field is empty
- "Username must be at least 3 characters" - Username is too short
- "Username must not exceed 50 characters" - Username is too long
- "Username can only contain letters, numbers, dots, underscores, and hyphens" - Invalid characters
- "Username already exists" - Username is already taken

#### Email Errors
- "Email is required" - Email field is empty
- "Invalid email format" - Email doesn't match standard format
- "Email address already exists" - Email is already registered

#### Name Errors
- "First name is required" - First name field is empty
- "Last name is required" - Last name field is empty
- "First name must not exceed 50 characters" - First name is too long
- "Last name must not exceed 50 characters" - Last name is too long

#### Password Errors
- "Password is required" - Password field is empty
- "Password must be at least 8 characters" - Password is too short
- "Password must contain at least one uppercase letter" - Missing uppercase
- "Password must contain at least one lowercase letter" - Missing lowercase
- "Password must contain at least one digit" - Missing number
- "Password must contain at least one special character" - Missing special character
- "Passwords do not match" - Password and confirm password don't match

#### Server Errors
- "Server error: Unable to create account. Please try again." - Server communication error
- "Registration failed: [specific error]" - Validation error from server

## Security Considerations

### Password Security
- Passwords are never transmitted in plaintext
- Passwords are encrypted during transmission if encryption is enabled
- Passwords are hashed using BCrypt with salt before storage
- Password policy enforces strong passwords

### Data Protection
- All sensitive data is encrypted during transmission
- Email addresses are validated to prevent typos
- Usernames are checked for uniqueness to prevent account duplication

### Access Control
- Only STUDENT and TEACHER roles can self-register
- ADMIN accounts must be created by existing administrators
- New accounts are automatically activated

### Audit Trail
- All registration events are logged with timestamp and user details
- Failed registration attempts are logged for security monitoring
- Registration events can be reviewed in system logs

## Integration Points

### Database
- User data is stored in the USERS table
- Student-specific data is stored in the STUDENTS table
- Teacher-specific data is stored in the TEACHERS table

### Authentication Service
- Registration uses the same password hashing as authentication
- Registered users can immediately authenticate with their credentials

### RMI Communication
- Registration requests are transmitted via RMI
- All RMI communication is encrypted if encryption is enabled

## Testing

### Unit Tests
- RegistrationFrame validation methods are tested
- Server-side registration logic is tested
- Input validation is tested with various inputs

### Integration Tests
- End-to-end registration flow is tested
- Database integration is tested
- RMI communication is tested

### Manual Testing
- Registration with valid data
- Registration with invalid data
- Duplicate username/email handling
- Password policy enforcement
- Error message display
- Navigation between login and registration

## Future Enhancements

### Potential Improvements
1. Email verification before account activation
2. CAPTCHA to prevent automated registration
3. Registration approval workflow for teachers
4. Social media account integration
5. Two-factor authentication setup during registration
6. User profile completion after registration
7. Welcome email with account information
8. Registration analytics and reporting

## Troubleshooting

### Registration Not Working
1. Check server connection status
2. Verify server is running and accessible
3. Check firewall settings
4. Review server logs for errors

### Validation Errors
1. Ensure all required fields are filled
2. Check password meets all requirements
3. Verify username/email are not already registered
4. Check for special characters in fields

### Account Not Accessible After Registration
1. Verify registration completed successfully
2. Check username and password are correct
3. Verify account is active in admin panel
4. Check server logs for authentication errors

## Support

For issues or questions regarding the registration system:
1. Check this documentation
2. Review server logs for error details
3. Contact system administrator
4. Submit bug report with error details and steps to reproduce
