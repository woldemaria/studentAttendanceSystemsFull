# Registration Feature - Actor Guides

## Overview

This document provides detailed guides for each actor (user role) on how to use and work with the registration feature in the Student Attendance System.

---

## 🎓 ACTOR 1: STUDENT

### Overview
Students can self-register to create their own accounts without requiring administrator intervention.

### Prerequisites
- Access to the Student Attendance System application
- Valid email address
- Internet connection

### Step-by-Step Guide

#### Step 1: Launch the Application
```
1. Open the Student Attendance System application
2. Wait for the connection to establish
3. You should see the login screen
```

#### Step 2: Access Registration
```
1. On the login screen, locate the green "Register" button
2. Click the "Register" button
3. The registration form will appear
```

#### Step 3: Fill in Personal Information
```
1. First Name:
   - Enter your first name (e.g., "John")
   - Maximum 50 characters
   - Required field

2. Last Name:
   - Enter your last name (e.g., "Smith")
   - Maximum 50 characters
   - Required field

3. Email:
   - Enter your valid email address (e.g., "john.smith@school.edu")
   - Must be in valid email format
   - Must be unique (not already registered)
   - Required field
```

#### Step 4: Create Username
```
1. Username:
   - Choose a unique username (e.g., "john.smith" or "jsmith123")
   - Must be 3-50 characters long
   - Can contain: letters, numbers, dots (.), underscores (_), hyphens (-)
   - Cannot contain: special characters like @, !, #, etc.
   - Must be unique (not already taken)
   - Required field

Example valid usernames:
- john.smith
- student_2024
- jsmith-123
- john123
```

#### Step 5: Create Strong Password
```
1. Password:
   - Must be at least 8 characters long
   - Must contain at least one uppercase letter (A-Z)
   - Must contain at least one lowercase letter (a-z)
   - Must contain at least one number (0-9)
   - Must contain at least one special character (!@#$%^&*()_+-=[]{}';:"\\|,.<>/?))
   - Required field

Example valid passwords:
- MyPassword123!
- SecurePass@2024
- Student#Pass99
- Attendance!2024

Example invalid passwords:
- password123 (no uppercase, no special char)
- PASSWORD123! (no lowercase)
- Pass123 (no special char)
- Pass! (too short)
```

#### Step 6: Confirm Password
```
1. Confirm Password:
   - Re-enter your password exactly as typed above
   - Must match the password field
   - Required field
   - Tip: Check "Show password" to verify you typed it correctly
```

#### Step 7: Select Account Type
```
1. Account Type:
   - Select "Student" from the dropdown
   - This determines your role in the system
   - Required field
```

#### Step 8: Review and Submit
```
1. Review all information for accuracy
2. Check "Show password" if you want to verify your password
3. Click the green "Register" button
4. Wait for the registration to complete (progress bar will show)
```

#### Step 9: Confirmation
```
1. If successful:
   - You'll see "Account created successfully!" message
   - You'll be redirected to the login screen after 2 seconds
   - Your account is now active

2. If failed:
   - You'll see an error message explaining the issue
   - Review the error and correct the information
   - Try registering again
```

#### Step 10: Log In
```
1. On the login screen, enter:
   - Username: (the username you created)
   - Password: (the password you created)
2. Click "Login"
3. You'll be taken to the Student Dashboard
```

### Common Issues & Solutions

#### "Username already exists"
- **Problem**: The username you chose is already taken
- **Solution**: Choose a different username

#### "Email address already exists"
- **Problem**: The email is already registered
- **Solution**: Use a different email address or log in if you already have an account

#### "Invalid email format"
- **Problem**: Your email doesn't follow the correct format
- **Solution**: Use format like: user@domain.com

#### "Password must contain..."
- **Problem**: Your password doesn't meet the requirements
- **Solution**: Add the missing character type (uppercase, lowercase, number, or special character)

#### "Passwords do not match"
- **Problem**: Confirm password doesn't match the password field
- **Solution**: Re-enter both passwords carefully, or check "Show password" to verify

### After Registration

#### First Login
1. Log in with your new credentials
2. Complete your profile if prompted
3. Explore the Student Dashboard

#### Dashboard Features
- View your attendance records
- Check your attendance percentage
- View notifications
- See your enrolled courses

#### Password Management
- Change your password anytime from Account Settings
- Never share your password with anyone
- Use a strong, unique password

### Tips for Success

✅ **Do's**:
- Use a memorable but secure password
- Keep your email address current
- Log out when finished
- Report any issues to your administrator

❌ **Don'ts**:
- Don't share your password
- Don't use the same password as other accounts
- Don't register multiple accounts
- Don't leave the application unattended while logged in

---

## 👨‍🏫 ACTOR 2: TEACHER

### Overview
Teachers can self-register to create their own accounts and access teaching features.

### Prerequisites
- Access to the Student Attendance System application
- Valid school email address
- Internet connection

### Registration Process

#### Step 1-7: Same as Student
Follow Steps 1-6 from the Student guide above.

#### Step 7: Select Account Type (Different)
```
1. Account Type:
   - Select "Teacher" from the dropdown
   - This determines your role in the system
   - Required field
```

#### Step 8-10: Same as Student
Follow Steps 8-10 from the Student guide above.

### Teacher Dashboard Features

After logging in, teachers have access to:

#### 1. Attendance Marking
```
- Select a class
- View enrolled students
- Mark attendance (Present, Absent, Late, Excused)
- Bulk mark all students
- Modify attendance within 24 hours
```

#### 2. Class Management
```
- View assigned courses
- Manage student enrollments
- View class details
- Access class reports
```

#### 3. Attendance Reports
```
- Generate attendance reports
- Filter by date range
- Export to PDF or Excel
- View attendance statistics
```

#### 4. Student Management
```
- View enrolled students
- Check student attendance
- View student performance
- Send notifications
```

### Teacher-Specific Tips

✅ **Best Practices**:
- Mark attendance at the end of each class
- Use bulk marking for efficiency
- Review reports regularly
- Communicate with students about attendance

❌ **Avoid**:
- Marking attendance for future dates
- Modifying attendance after 24 hours (not allowed)
- Sharing login credentials
- Leaving the system unattended

### Common Teacher Tasks

#### Task 1: Mark Attendance
```
1. Log in to your account
2. Go to Attendance Marking
3. Select your class
4. Select attendance status for each student
5. Click "Save"
```

#### Task 2: Generate Report
```
1. Go to Reports
2. Select date range
3. Choose report type
4. Click "Generate"
5. Export if needed
```

#### Task 3: View Student Attendance
```
1. Go to Class Management
2. Select a class
3. Click on a student
4. View their attendance history
```

---

## 👨‍💼 ACTOR 3: ADMINISTRATOR

### Overview
Administrators manage the system, including user accounts, system configuration, and monitoring.

### Prerequisites
- Administrator account (created by system)
- Access to the Student Attendance System application
- System administration knowledge

### Registration Monitoring

#### Accessing Registration Monitoring
```
1. Log in as administrator
2. Go to System Administration
3. Select "Registration Monitoring"
4. View real-time statistics
```

#### Monitoring Dashboard

The dashboard displays:

##### Statistics Panel
```
- Total Registrations: Total number of registration attempts
- Successful: Number of successful registrations
- Failed: Number of failed registrations
- Success Rate: Percentage of successful registrations
- Students: Number of student accounts created
- Teachers: Number of teacher accounts created
- Avg Processing Time: Average time to process registration
```

##### Validation Failures Table
```
- Shows breakdown of validation failures
- Lists failure types and counts
- Helps identify common issues
- Sorted by frequency
```

##### Recent Events Table
```
- Shows recent registration attempts
- Displays username, role, status
- Shows processing time
- Includes timestamp
```

#### Monitoring Tasks

##### Task 1: Check Registration Success Rate
```
1. Open Registration Monitoring
2. Look at "Success Rate" statistic
3. Target: 95%+ success rate
4. If lower:
   - Review validation failures
   - Check error messages
   - Improve user guidance
```

##### Task 2: Identify Common Issues
```
1. View "Validation Failures" table
2. Look for patterns
3. Top failures indicate:
   - User confusion
   - Unclear requirements
   - Need for better guidance
4. Take corrective action
```

##### Task 3: Monitor Performance
```
1. Check "Avg Processing Time"
2. Target: <500ms average
3. If higher:
   - Check server load
   - Review database performance
   - Optimize queries
```

##### Task 4: Export Analytics Report
```
1. Click "Export" button
2. Review the report
3. Save or print as needed
4. Share with stakeholders
```

##### Task 5: Reset Analytics
```
1. Click "Reset" button
2. Confirm the action
3. All analytics data will be cleared
4. Useful for starting fresh period
```

### User Management

#### Creating User Accounts
```
1. Go to User Management
2. Click "Create User"
3. Enter user details:
   - Username
   - Email
   - First Name
   - Last Name
   - Role (Student/Teacher/Admin)
   - Password
4. Click "Create"
```

#### Editing User Accounts
```
1. Go to User Management
2. Search for user
3. Click "Edit"
4. Modify details
5. Click "Save"
```

#### Deactivating Accounts
```
1. Go to User Management
2. Search for user
3. Click "Deactivate"
4. Confirm action
5. Account is now inactive
```

#### Resetting Passwords
```
1. Go to User Management
2. Search for user
3. Click "Reset Password"
4. Generate temporary password
5. Send to user
6. User must change on first login
```

### System Configuration

#### Configuring System Parameters
```
1. Go to System Configuration
2. Select "System Parameters" tab
3. Configure:
   - Session timeout (minutes)
   - Backup schedule
   - Notification settings
   - Max concurrent users
4. Click "Save"
```

#### Database Maintenance
```
1. Go to System Configuration
2. Select "Database Maintenance" tab
3. Available operations:
   - Cleanup old records
   - Optimize database
   - Perform backup
   - Restore backup
4. Click operation to execute
```

#### System Health Monitoring
```
1. Go to System Configuration
2. Select "System Health" tab
3. View status of:
   - System status
   - Database status
   - RMI server status
4. Check for issues
```

### Administrator Best Practices

✅ **Do's**:
- Monitor registration metrics regularly
- Review validation failures
- Maintain system performance
- Keep backups current
- Document changes
- Communicate with users

❌ **Don'ts**:
- Don't ignore low success rates
- Don't reset analytics without reason
- Don't make unnecessary configuration changes
- Don't share admin credentials
- Don't ignore system alerts

### Common Administrator Tasks

#### Daily Tasks
```
1. Check registration success rate
2. Review validation failures
3. Monitor system health
4. Check for errors in logs
```

#### Weekly Tasks
```
1. Generate analytics report
2. Review registration trends
3. Check database performance
4. Verify backups
```

#### Monthly Tasks
```
1. Comprehensive analytics review
2. System performance analysis
3. Capacity planning
4. User feedback review
```

---

## 👨‍💻 ACTOR 4: DEVELOPER

### Overview
Developers implement, maintain, and enhance the registration system.

### Prerequisites
- Java development knowledge
- Understanding of RMI and Swing
- Database knowledge
- Testing framework knowledge

### Development Tasks

#### Task 1: Understanding the Architecture
```
1. Read REGISTRATION_ARCHITECTURE.md
2. Review system diagrams
3. Understand component interactions
4. Study data flow
```

#### Task 2: Setting Up Development Environment
```
1. Clone the repository
2. Install Java 11+
3. Install Maven
4. Configure IDE
5. Run: mvn clean compile
6. Run: mvn test
```

#### Task 3: Running Tests
```
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=RegistrationFrameTest

# Run specific test method
mvn test -Dtest=RegistrationFrameTest#testValidateUsernameValid

# Run with coverage
mvn clean test jacoco:report
```

#### Task 4: Building the Project
```
# Clean build
mvn clean build

# Build with tests
mvn clean package

# Build without tests
mvn clean package -DskipTests
```

#### Task 5: Integrating Analytics
```
// In AttendanceServer.java
private RegistrationAnalytics analytics = new RegistrationAnalytics();

@Override
public boolean registerUser(...) {
    long startTime = System.currentTimeMillis();
    try {
        // ... perform registration ...
        long processingTime = System.currentTimeMillis() - startTime;
        analytics.recordSuccessfulRegistration(username, role, processingTime);
        return true;
    } catch (ValidationException e) {
        long processingTime = System.currentTimeMillis() - startTime;
        analytics.recordFailedRegistration(username, e.getMessage(), processingTime);
        throw e;
    }
}
```

#### Task 6: Adding Monitoring to Admin Dashboard
```
// In AdminDashboard.java
RegistrationAnalytics analytics = server.getRegistrationAnalytics();
RegistrationMonitoringPanel monitoringPanel = 
    new RegistrationMonitoringPanel(analytics);

// Add to dashboard
tabbedPane.addTab("Registration Monitoring", monitoringPanel);
```

### Code Review Checklist

When reviewing registration code:

```
✅ Validation
- [ ] All inputs validated
- [ ] Server-side validation present
- [ ] Error messages clear
- [ ] Edge cases handled

✅ Security
- [ ] Passwords hashed
- [ ] Data encrypted
- [ ] SQL injection prevented
- [ ] XSS prevented

✅ Performance
- [ ] Efficient queries
- [ ] No N+1 problems
- [ ] Caching used appropriately
- [ ] Response times acceptable

✅ Testing
- [ ] Unit tests present
- [ ] Integration tests present
- [ ] Edge cases tested
- [ ] Error scenarios tested

✅ Documentation
- [ ] Code commented
- [ ] JavaDoc present
- [ ] README updated
- [ ] Changes documented
```

### Debugging Tips

#### Issue: Registration Fails with Database Error
```
1. Check database connection
2. Verify database schema
3. Check user permissions
4. Review database logs
5. Test with direct SQL
```

#### Issue: Validation Not Working
```
1. Check validation logic
2. Verify regex patterns
3. Test with edge cases
4. Check error messages
5. Review test cases
```

#### Issue: Performance Issues
```
1. Profile the code
2. Check database queries
3. Review network calls
4. Check for memory leaks
5. Optimize hot spots
```

### Development Best Practices

✅ **Do's**:
- Write tests first (TDD)
- Follow coding standards
- Document your code
- Review others' code
- Keep commits small
- Write clear commit messages

❌ **Don'ts**:
- Don't skip tests
- Don't commit without testing
- Don't ignore warnings
- Don't hardcode values
- Don't make large commits
- Don't commit commented code

---

## 🏗️ ACTOR 5: ARCHITECT

### Overview
Architects design and oversee the system architecture and integration.

### Prerequisites
- System design knowledge
- Enterprise architecture experience
- Understanding of distributed systems
- Performance optimization knowledge

### Architecture Review

#### Component Review
```
1. RegistrationFrame
   - GUI component for user input
   - Validates input on client-side
   - Communicates via RMI

2. AttendanceServer
   - Implements registration logic
   - Validates on server-side
   - Manages database operations

3. RegistrationAnalytics
   - Tracks metrics
   - Generates reports
   - Thread-safe implementation

4. RegistrationMonitoringPanel
   - Displays analytics
   - Provides admin interface
   - Real-time updates
```

#### Integration Points
```
1. GUI Integration
   - LoginFrame → RegistrationFrame
   - AttendanceGUI → RegistrationFrame
   - AdminDashboard → RegistrationMonitoringPanel

2. Service Integration
   - AttendanceService interface
   - AttendanceServer implementation
   - RMI communication

3. Database Integration
   - UserDAO for database operations
   - USERS table for storage
   - STUDENTS/TEACHERS tables for roles

4. Security Integration
   - SecurityUtil for hashing
   - AuthenticationService for auth
   - Encryption for transmission
```

#### Performance Considerations
```
1. Database
   - Connection pooling (HikariCP)
   - Indexed queries
   - Batch operations

2. Network
   - RMI compression
   - Async operations
   - Caching

3. Memory
   - Analytics event storage
   - Cache management
   - Garbage collection

4. Scalability
   - Thread-safe implementation
   - Concurrent access handling
   - Load balancing ready
```

### Design Decisions

#### Why RMI?
```
- Distributed architecture
- Java-native solution
- Transparent remote calls
- Built-in security support
```

#### Why Swing?
```
- Cross-platform GUI
- Rich component library
- Mature framework
- Good for desktop apps
```

#### Why MySQL?
```
- Relational data model
- ACID compliance
- Good performance
- Wide adoption
```

#### Why Analytics?
```
- Monitor system health
- Identify issues
- Optimize performance
- Support decision-making
```

### Scalability Planning

#### Current Capacity
```
- Max concurrent users: 100
- Max registrations/minute: 10
- Database connections: 20
- Memory usage: ~100MB
```

#### Scaling Strategies
```
1. Vertical Scaling
   - Increase server resources
   - More database connections
   - More memory

2. Horizontal Scaling
   - Multiple servers
   - Load balancing
   - Database replication

3. Optimization
   - Query optimization
   - Caching strategy
   - Connection pooling
```

### Security Architecture

#### Authentication
```
- Username/password validation
- Session management
- Token-based access
- Timeout handling
```

#### Authorization
```
- Role-based access control
- Permission checking
- Admin-only operations
- User isolation
```

#### Data Protection
```
- Password hashing (BCrypt)
- Data encryption (AES-256)
- Secure transmission
- Audit logging
```

### Monitoring Strategy

#### Metrics to Track
```
1. Registration Metrics
   - Success rate
   - Processing time
   - Failure reasons

2. System Metrics
   - CPU usage
   - Memory usage
   - Database connections
   - Network traffic

3. Business Metrics
   - User growth
   - Registration trends
   - Peak times
```

#### Alerting Strategy
```
1. Critical Alerts
   - System down
   - Database error
   - High error rate

2. Warning Alerts
   - Low success rate
   - High processing time
   - High resource usage

3. Info Alerts
   - Daily summary
   - Weekly report
   - Monthly analysis
```

---

## 📊 ACTOR 6: PROJECT MANAGER

### Overview
Project managers oversee the project, track progress, and manage stakeholders.

### Prerequisites
- Project management knowledge
- Understanding of software development
- Stakeholder management skills
- Risk management knowledge

### Project Tracking

#### Deliverables Checklist
```
✅ Implementation
- [ ] Core registration feature
- [ ] Analytics system
- [ ] Monitoring dashboard
- [ ] All tests passing

✅ Documentation
- [ ] User guides
- [ ] Technical guides
- [ ] Deployment guide
- [ ] API documentation

✅ Quality
- [ ] Code review complete
- [ ] Tests passing
- [ ] Security verified
- [ ] Performance verified

✅ Deployment
- [ ] Deployment plan
- [ ] Rollback plan
- [ ] Monitoring setup
- [ ] Support materials
```

#### Status Reporting

##### Weekly Report
```
1. Completed Tasks
   - List completed items
   - Highlight achievements
   - Note any blockers

2. In Progress
   - Current work
   - Expected completion
   - Any risks

3. Upcoming
   - Next week's tasks
   - Dependencies
   - Resource needs

4. Metrics
   - Test coverage
   - Code quality
   - Performance
```

##### Monthly Report
```
1. Project Status
   - Overall progress
   - Schedule status
   - Budget status

2. Achievements
   - Major milestones
   - Quality metrics
   - Performance metrics

3. Risks
   - Identified risks
   - Mitigation plans
   - Contingencies

4. Next Steps
   - Upcoming phases
   - Resource needs
   - Timeline
```

### Stakeholder Communication

#### For Executives
```
Focus on:
- Business value
- ROI
- Timeline
- Risk mitigation

Key Metrics:
- Project completion %
- Budget status
- Quality metrics
- User satisfaction
```

#### For Users
```
Focus on:
- Features
- Ease of use
- Support
- Training

Key Messages:
- How to register
- Benefits
- Support available
- Timeline
```

#### For Development Team
```
Focus on:
- Requirements
- Priorities
- Blockers
- Support needed

Key Messages:
- Clear requirements
- Realistic timelines
- Recognition
- Support
```

### Risk Management

#### Identified Risks
```
1. Low Registration Success Rate
   - Impact: User frustration
   - Probability: Medium
   - Mitigation: Clear error messages, user guidance

2. Performance Issues
   - Impact: User experience
   - Probability: Low
   - Mitigation: Performance testing, optimization

3. Security Vulnerabilities
   - Impact: Data breach
   - Probability: Low
   - Mitigation: Security review, penetration testing

4. Integration Issues
   - Impact: System failure
   - Probability: Medium
   - Mitigation: Integration testing, staging environment
```

#### Risk Mitigation
```
1. Prevention
   - Thorough testing
   - Code review
   - Security review
   - Performance testing

2. Detection
   - Monitoring
   - Logging
   - Alerts
   - User feedback

3. Response
   - Incident plan
   - Rollback plan
   - Communication plan
   - Recovery plan
```

### Timeline Management

#### Project Phases
```
Phase 1: Implementation (Completed)
- Core registration feature
- Analytics system
- Monitoring dashboard

Phase 2: Testing (Completed)
- Unit tests
- Integration tests
- System tests

Phase 3: Documentation (Completed)
- User guides
- Technical guides
- Deployment guide

Phase 4: Deployment (Ready)
- Pre-deployment verification
- Deployment execution
- Post-deployment monitoring

Phase 5: Support (Ongoing)
- User support
- Issue resolution
- Optimization
```

### Success Criteria

#### Functional Requirements
```
✅ Users can self-register
✅ Validation works correctly
✅ Security measures in place
✅ Analytics tracking works
✅ Monitoring dashboard functional
```

#### Non-Functional Requirements
```
✅ 95%+ success rate
✅ <500ms average processing time
✅ 100% code coverage
✅ Zero critical security issues
✅ <2 second response time
```

#### Quality Requirements
```
✅ All tests passing
✅ No critical bugs
✅ Code review approved
✅ Documentation complete
✅ Performance verified
```

---

## Summary Table

| Actor | Role | Key Tasks | Success Metrics |
|-------|------|-----------|-----------------|
| **Student** | End User | Register, Login, Use Dashboard | Successful registration, Account active |
| **Teacher** | End User | Register, Mark Attendance, View Reports | Successful registration, Can mark attendance |
| **Admin** | System Manager | Monitor, Configure, Manage Users | System healthy, Users managed, Analytics tracked |
| **Developer** | Technical | Implement, Test, Maintain | Tests passing, Code quality, Performance |
| **Architect** | Design | Design, Review, Optimize | Architecture sound, Scalable, Secure |
| **PM** | Management | Track, Report, Manage Risks | On schedule, On budget, Quality met |

---

**Actor Guides Version**: 1.0
**Date**: May 6, 2026
**Status**: Complete
