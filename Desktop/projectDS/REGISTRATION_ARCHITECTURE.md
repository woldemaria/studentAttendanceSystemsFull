# Registration System - Architecture and Integration

## System Architecture

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                    Student Attendance System                     │
├─────────────────────────────────────────────────────────────────┤
│                                                                   │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │                    Client Application                     │   │
│  │  ┌────────────────────────────────────────────────────┐  │   │
│  │  │              AttendanceGUI (Main Window)           │  │   │
│  │  │  ┌──────────────────────────────────────────────┐  │  │   │
│  │  │  │  LoginFrame                                  │  │  │   │
│  │  │  │  ┌────────────────────────────────────────┐  │  │  │   │
│  │  │  │  │ Username Field                         │  │  │  │   │
│  │  │  │  │ Password Field                         │  │  │  │   │
│  │  │  │  │ [Login] [Clear] [Register] ← NEW       │  │  │  │   │
│  │  │  │  └────────────────────────────────────────┘  │  │  │   │
│  │  │  └──────────────────────────────────────────────┘  │  │   │
│  │  │  ┌──────────────────────────────────────────────┐  │  │   │
│  │  │  │  RegistrationFrame (NEW)                    │  │  │   │
│  │  │  │  ┌────────────────────────────────────────┐  │  │  │   │
│  │  │  │  │ First Name Field                       │  │  │  │   │
│  │  │  │  │ Last Name Field                        │  │  │  │   │
│  │  │  │  │ Username Field                         │  │  │  │   │
│  │  │  │  │ Email Field                            │  │  │  │   │
│  │  │  │  │ Account Type (Student/Teacher)         │  │  │  │   │
│  │  │  │  │ Password Field                         │  │  │  │   │
│  │  │  │  │ Confirm Password Field                 │  │  │  │   │
│  │  │  │  │ [Register] [Cancel]                    │  │  │  │   │
│  │  │  │  └────────────────────────────────────────┘  │  │  │   │
│  │  │  └──────────────────────────────────────────────┘  │  │   │
│  │  │  ┌──────────────────────────────────────────────┐  │  │   │
│  │  │  │  AdminDashboard / TeacherDashboard /        │  │  │   │
│  │  │  │  StudentDashboard (Existing)                │  │  │   │
│  │  │  └──────────────────────────────────────────────┘  │  │   │
│  │  └────────────────────────────────────────────────────┘  │   │
│  └──────────────────────────────────────────────────────────┘   │
│                              │                                    │
│                              │ RMI                                │
│                              ▼                                    │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │                    RMI Server                             │   │
│  │  ┌────────────────────────────────────────────────────┐  │   │
│  │  │  AttendanceServer (RMI Implementation)            │  │   │
│  │  │  ┌──────────────────────────────────────────────┐  │  │   │
│  │  │  │ authenticateUser()                           │  │  │   │
│  │  │  │ registerUser() ← NEW                         │  │  │   │
│  │  │  │ createUser()                                 │  │  │   │
│  │  │  │ updateUser()                                 │  │  │   │
│  │  │  │ ... (other methods)                          │  │  │   │
│  │  │  └──────────────────────────────────────────────┘  │  │   │
│  │  └────────────────────────────────────────────────────┘  │   │
│  │                              │                              │   │
│  │                              │ JDBC                          │   │
│  │                              ▼                              │   │
│  │  ┌────────────────────────────────────────────────────┐  │   │
│  │  │  Data Access Layer (DAOs)                         │  │   │
│  │  │  ┌──────────────────────────────────────────────┐  │  │   │
│  │  │  │ UserDAO                                      │  │  │   │
│  │  │  │ - createUser()                               │  │  │   │
│  │  │  │ - findByUsername()                           │  │  │   │
│  │  │  │ - findByEmail()                              │  │  │   │
│  │  │  │ - updateUser()                               │  │  │   │
│  │  │  │ - deleteUser()                               │  │  │   │
│  │  │  └──────────────────────────────────────────────┘  │  │   │
│  │  │  ┌──────────────────────────────────────────────┐  │  │   │
│  │  │  │ AttendanceDAO, CourseDAO (Existing)         │  │  │   │
│  │  │  └──────────────────────────────────────────────┘  │  │   │
│  │  └────────────────────────────────────────────────────┘  │   │
│  │                              │                              │   │
│  │                              ▼                              │   │
│  │  ┌────────────────────────────────────────────────────┐  │   │
│  │  │  Database (MySQL)                                │  │   │
│  │  │  ┌──────────────────────────────────────────────┐  │  │   │
│  │  │  │ USERS Table                                  │  │  │   │
│  │  │  │ - userId (PK)                                │  │  │   │
│  │  │  │ - username (UNIQUE)                          │  │  │   │
│  │  │  │ - email (UNIQUE)                             │  │  │   │
│  │  │  │ - passwordHash                               │  │  │   │
│  │  │  │ - firstName                                  │  │  │   │
│  │  │  │ - lastName                                   │  │  │   │
│  │  │  │ - role (STUDENT/TEACHER/ADMIN)               │  │  │   │
│  │  │  │ - isActive                                   │  │  │   │
│  │  │  │ - createdAt                                  │  │  │   │
│  │  │  │ - updatedAt                                  │  │  │   │
│  │  │  └──────────────────────────────────────────────┘  │  │   │
│  │  │  ┌──────────────────────────────────────────────┐  │  │   │
│  │  │  │ STUDENTS Table                               │  │  │   │
│  │  │  │ - studentId (FK to USERS)                    │  │  │   │
│  │  │  │ - ... (student-specific fields)              │  │  │   │
│  │  │  └──────────────────────────────────────────────┘  │  │   │
│  │  │  ┌──────────────────────────────────────────────┐  │  │   │
│  │  │  │ TEACHERS Table                               │  │  │   │
│  │  │  │ - teacherId (FK to USERS)                    │  │  │   │
│  │  │  │ - ... (teacher-specific fields)              │  │  │   │
│  │  │  └──────────────────────────────────────────────┘  │  │   │
│  │  └────────────────────────────────────────────────────┘  │   │
│  └──────────────────────────────────────────────────────────┘   │
│                                                                   │
└─────────────────────────────────────────────────────────────────┘
```

## Component Interaction Diagram

### Registration Flow

```
┌─────────────────────────────────────────────────────────────────┐
│                    Registration Process Flow                     │
└─────────────────────────────────────────────────────────────────┘

User                LoginFrame           RegistrationFrame         Server
 │                      │                      │                    │
 │  Click Register      │                      │                    │
 ├─────────────────────>│                      │                    │
 │                      │  Show Registration   │                    │
 │                      ├─────────────────────>│                    │
 │                      │                      │                    │
 │  Fill Form & Click   │                      │                    │
 │  Register            │                      │                    │
 │<─────────────────────────────────────────────                    │
 │                      │                      │                    │
 │                      │  Validate Form       │                    │
 │                      │<─────────────────────┤                    │
 │                      │                      │                    │
 │                      │  Send Registration   │                    │
 │                      │  Request (RMI)       │                    │
 │                      │<─────────────────────────────────────────>│
 │                      │                      │                    │
 │                      │                      │  Validate Input    │
 │                      │                      │  Check Duplicates  │
 │                      │                      │  Hash Password     │
 │                      │                      │  Create User       │
 │                      │                      │  Log Event         │
 │                      │                      │                    │
 │                      │  Registration        │                    │
 │                      │  Response (Success)  │                    │
 │                      │<─────────────────────────────────────────┤
 │                      │                      │                    │
 │  Show Success        │                      │                    │
 │  Message             │                      │                    │
 │<─────────────────────────────────────────────                    │
 │                      │                      │                    │
 │  Redirect to Login   │                      │                    │
 │<─────────────────────────────────────────────                    │
 │                      │                      │                    │
```

## Data Flow Diagram

### Registration Data Flow

```
┌──────────────────────────────────────────────────────────────────┐
│                    Registration Data Flow                         │
└──────────────────────────────────────────────────────────────────┘

Input Data (Client):
├── username (String)
├── email (String)
├── firstName (String)
├── lastName (String)
├── password (String)
└── role (UserRole: STUDENT or TEACHER)
         │
         ▼
Client-Side Validation:
├── Username: 3-50 chars, alphanumeric + special chars
├── Email: Valid format
├── Names: Required, max 50 chars
├── Password: 8+ chars, uppercase, lowercase, digit, special char
└── Confirm Password: Must match password
         │
         ▼
Encryption (if enabled):
├── Encrypt username
├── Encrypt email
├── Encrypt firstName
├── Encrypt lastName
└── Encrypt password
         │
         ▼
RMI Transmission:
└── Send encrypted data to server
         │
         ▼
Server-Side Validation:
├── Decrypt data (if encryption enabled)
├── Validate all input parameters
├── Check username uniqueness (query USERS table)
├── Check email uniqueness (query USERS table)
├── Validate password strength
└── Validate role (STUDENT or TEACHER only)
         │
         ▼
User Creation:
├── Create User object (Student or Teacher)
├── Set username, email, firstName, lastName
├── Set role and isActive = true
├── Hash password using BCrypt
└── Set timestamps (createdAt, updatedAt)
         │
         ▼
Database Operations:
├── Insert into USERS table
├── Insert into STUDENTS or TEACHERS table
└── Commit transaction
         │
         ▼
Logging:
├── Log registration event
├── Log user details
├── Log timestamp
└── Log success/failure
         │
         ▼
Response (Server to Client):
├── Success: true
├── Message: "Account created successfully"
└── User can now log in
```

## Class Diagram

### Registration Components

```
┌─────────────────────────────────────────────────────────────────┐
│                    Class Relationships                           │
└─────────────────────────────────────────────────────────────────┘

┌──────────────────────────┐
│   RegistrationFrame      │
├──────────────────────────┤
│ - parentFrame            │
│ - onRegistrationSuccess  │
│ - usernameField          │
│ - emailField             │
│ - firstNameField         │
│ - lastNameField          │
│ - passwordField          │
│ - confirmPasswordField   │
│ - roleComboBox           │
│ - registerButton         │
│ - cancelButton           │
│ - statusLabel            │
│ - progressBar            │
├──────────────────────────┤
│ + initializeComponents() │
│ + setupLayout()          │
│ + setupEventHandlers()   │
│ + validateUsername()     │
│ + validateEmail()        │
│ + validateFirstName()    │
│ + validateLastName()     │
│ + validatePassword()     │
│ + validateConfirmPassword()
│ + performRegistration()  │
│ + setStatus()            │
│ + showProgress()         │
│ + setFormEnabled()       │
└──────────────────────────┘
         │
         │ uses
         ▼
┌──────────────────────────┐
│   AttendanceGUI          │
├──────────────────────────┤
│ - registrationFrame      │
│ - remoteService          │
├──────────────────────────┤
│ + showRegistrationFrame()│
│ + showLoginScreen()      │
│ + handleUserAuthenticated()
└──────────────────────────┘
         │
         │ calls
         ▼
┌──────────────────────────┐
│  AttendanceService       │
│  (RMI Interface)         │
├──────────────────────────┤
│ + registerUser()         │ ← NEW
│ + authenticateUser()     │
│ + createUser()           │
│ + updateUser()           │
│ + deleteUser()           │
│ + ... (other methods)    │
└──────────────────────────┘
         │
         │ implements
         ▼
┌──────────────────────────┐
│  AttendanceServer        │
│  (RMI Implementation)    │
├──────────────────────────┤
│ - authService            │
│ - userDAO                │
│ - attendanceDAO          │
│ - courseDAO              │
├──────────────────────────┤
│ + registerUser()         │ ← NEW
│ + authenticateUser()     │
│ + createUser()           │
│ + updateUser()           │
│ + deleteUser()           │
│ + ... (other methods)    │
└──────────────────────────┘
         │
         │ uses
         ▼
┌──────────────────────────┐
│      UserDAO             │
├──────────────────────────┤
│ + createUser()           │
│ + findByUsername()       │
│ + findByEmail()          │
│ + updateUser()           │
│ + deleteUser()           │
│ + getAllUsers()          │
└──────────────────────────┘
         │
         │ accesses
         ▼
┌──────────────────────────┐
│   Database (MySQL)       │
├──────────────────────────┤
│ - USERS table            │
│ - STUDENTS table         │
│ - TEACHERS table         │
└──────────────────────────┘
```

## Sequence Diagram

### Registration Sequence

```
User          Client              Server              Database
 │               │                   │                    │
 │ Click Register│                   │                    │
 ├──────────────>│                   │                    │
 │               │ Show Form         │                    │
 │               │<──────────────────│                    │
 │               │                   │                    │
 │ Fill Form     │                   │                    │
 │ Click Register│                   │                    │
 ├──────────────>│                   │                    │
 │               │ Validate Form     │                    │
 │               │ (Client-side)     │                    │
 │               │                   │                    │
 │               │ Send Registration │                    │
 │               │ Request (RMI)     │                    │
 │               ├──────────────────>│                    │
 │               │                   │ Validate Input     │
 │               │                   │ Check Duplicates   │
 │               │                   ├───────────────────>│
 │               │                   │ Query USERS        │
 │               │                   │<───────────────────┤
 │               │                   │ (username/email)   │
 │               │                   │                    │
 │               │                   │ Hash Password      │
 │               │                   │ Create User        │
 │               │                   ├───────────────────>│
 │               │                   │ Insert into USERS  │
 │               │                   │ Insert into        │
 │               │                   │ STUDENTS/TEACHERS  │
 │               │                   │<───────────────────┤
 │               │                   │ (Success)          │
 │               │                   │                    │
 │               │ Registration      │                    │
 │               │ Response (Success)│                    │
 │               │<──────────────────┤                    │
 │               │                   │                    │
 │ Show Success  │                   │                    │
 │ Message       │                   │                    │
 │<──────────────┤                   │                    │
 │               │                   │                    │
 │ Redirect to   │                   │                    │
 │ Login         │                   │                    │
 │<──────────────┤                   │                    │
 │               │                   │                    │
```

## Integration Points

### 1. GUI Integration
- **LoginFrame**: Added "Register" button
- **AttendanceGUI**: Added registration frame support
- **Card Layout**: Registration frame integrated into main panel

### 2. Service Integration
- **AttendanceService**: Added registerUser() method
- **AttendanceServer**: Implemented registerUser() logic
- **RMI Communication**: Registration requests via RMI

### 3. Database Integration
- **UserDAO**: Used for user creation and duplicate checking
- **USERS Table**: Stores user account information
- **STUDENTS/TEACHERS Tables**: Stores role-specific data

### 4. Security Integration
- **SecurityUtil**: Password hashing and encryption
- **AuthenticationService**: Compatible with existing auth
- **Encryption**: Supports AES-256 for transmission

### 5. Logging Integration
- **SystemLogger**: Logs registration events
- **Audit Trail**: Registration events tracked
- **Error Logging**: Failed registrations logged

## Technology Stack

### Client-Side
- **Java Swing**: GUI framework
- **RMI**: Remote communication
- **Validation**: Client-side input validation

### Server-Side
- **Java RMI**: Remote service implementation
- **JDBC**: Database access
- **BCrypt**: Password hashing
- **AES-256**: Data encryption (optional)

### Database
- **MySQL**: Data persistence
- **HikariCP**: Connection pooling
- **JDBC**: Database driver

### Utilities
- **SecurityUtil**: Password hashing and encryption
- **SystemLogger**: Logging and audit trail
- **ConfigManager**: Configuration management

## Error Handling Flow

```
Registration Request
        │
        ▼
Client-Side Validation
        │
        ├─ Valid ──────────────────┐
        │                          │
        └─ Invalid ────────────────┼──> Show Error Message
                                   │
                                   ▼
                          Send to Server (RMI)
                                   │
                                   ▼
                          Server-Side Validation
                                   │
                                   ├─ Valid ──────────────────┐
                                   │                          │
                                   └─ Invalid ────────────────┼──> Return Error
                                                              │
                                                              ▼
                                                      Create User
                                                              │
                                                              ├─ Success ────────────────┐
                                                              │                          │
                                                              └─ Failure ────────────────┼──> Return Error
                                                                                         │
                                                                                         ▼
                                                                                 Return Success
                                                                                         │
                                                                                         ▼
                                                                                 Show Success Message
                                                                                         │
                                                                                         ▼
                                                                                 Redirect to Login
```

## Deployment Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                    Deployment Architecture                       │
└─────────────────────────────────────────────────────────────────┘

Client Machine:
┌──────────────────────────────────────────────────────────────┐
│ Student Attendance System (Java Application)                 │
│ ├── RegistrationFrame (NEW)                                  │
│ ├── LoginFrame (MODIFIED)                                    │
│ ├── AttendanceGUI (MODIFIED)                                 │
│ └── Other GUI Components                                     │
└──────────────────────────────────────────────────────────────┘
                              │
                              │ RMI over Network
                              ▼
Server Machine:
┌──────────────────────────────────────────────────────────────┐
│ RMI Server                                                    │
│ ├── AttendanceServer (MODIFIED)                              │
│ │   └── registerUser() (NEW)                                 │
│ ├── AuthenticationService                                    │
│ ├── UserDAO                                                  │
│ ├── AttendanceDAO                                            │
│ └── CourseDAO                                                │
└──────────────────────────────────────────────────────────────┘
                              │
                              │ JDBC
                              ▼
Database Server:
┌──────────────────────────────────────────────────────────────┐
│ MySQL Database                                               │
│ ├── USERS table                                              │
│ ├── STUDENTS table                                           │
│ ├── TEACHERS table                                           │
│ └── Other tables                                             │
└──────────────────────────────────────────────────────────────┘
```

---

**Architecture Version**: 1.0
**Last Updated**: May 6, 2026
**Status**: Complete and Ready for Deployment
