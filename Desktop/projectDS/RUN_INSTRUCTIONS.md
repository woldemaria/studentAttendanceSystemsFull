# Student Attendance System - Run Instructions

## Prerequisites

1. **XAMPP MySQL Server** - Must be running
   - Start XAMPP and ensure MySQL is running
   - Default: localhost:3306
   - Username: root
   - Password: (empty by default)

2. **Java 15 or higher** - Required to run the application

## Setup Steps

### 1. Create Database

Open phpMyAdmin (http://localhost/phpmyadmin) and create a new database:

```sql
CREATE DATABASE attendance_system;
```

### 2. Import Schema

Run the SQL schema to create tables:

```sql
-- Copy and paste the contents of src/main/resources/schema.sql into phpMyAdmin
```

Or use MySQL command line:

```bash
mysql -u root attendance_system < src/main/resources/schema.sql
```

### 3. Configure Database Connection

Edit `src/main/resources/database.properties`:

```properties
db.url=jdbc:mysql://localhost:3306/attendance_system
db.username=root
db.password=
db.driver=com.mysql.cj.jdbc.Driver
```

## Running the Application

### Build the Project

```bash
mvn clean package -DskipTests -Dmaven.test.skip=true
```

### Start the Server

In Terminal 1:

```bash
mvn exec:java -Dexec.mainClass="com.attendance.system.server.ServerLauncher"
```

Expected output:
```
[INFO] RMI Server started on port 1099
[INFO] Database connection established successfully
[INFO] Server ready to accept connections
```

### Start the Client

In Terminal 2:

```bash
mvn exec:java -Dexec.mainClass="com.attendance.system.client.AttendanceGUI"
```

## Login Credentials

### Student Registration

1. Click "Register" on the login screen
2. Fill in the form:
   - Username: (choose any username)
   - Email: (any valid email)
   - First Name: (your first name)
   - Last Name: (your last name)
   - Password: (must contain uppercase, lowercase, number, and special character)
   - Confirm Password: (same as above)
3. Account Type: **STUDENT** (automatically set, cannot be changed)
4. Click "Register"

### Admin Login

Use the default admin account:
- Username: `admin`
- Password: `Admin@123`

### Teacher Login

Use the default teacher account:
- Username: `teacher1`
- Password: `Teacher@123`

## Features

### Student Features
- View personal attendance records
- View course information
- Download attendance reports (PDF/Excel)
- Update profile information

### Teacher Features
- Mark attendance for students
- View attendance reports
- Manage courses
- Generate attendance statistics

### Admin Features
- Manage users (create, edit, delete)
- Manage courses
- View system statistics
- Configure system settings
- Enable/disable maintenance mode

## Troubleshooting

### Database Connection Failed
- Ensure XAMPP MySQL is running
- Check database.properties configuration
- Verify database exists: `attendance_system`

### Port Already in Use
- RMI Server uses port 1099
- If port is in use, modify ServerLauncher.java and rebuild

### GUI Not Appearing
- Ensure Java 15+ is installed
- Check DISPLAY variable on Linux/Mac
- Try running with: `java -Djava.awt.headless=false ...`

## Database Schema

The system creates the following tables:
- `users` - User accounts
- `students` - Student information
- `teachers` - Teacher information
- `courses` - Course information
- `attendance` - Attendance records
- `notifications` - System notifications
- `audit_logs` - System audit logs

## Notes

- Only **STUDENT** role can self-register
- Teachers and Admins must be created by an existing Admin
- All passwords are encrypted using BCrypt
- All data is stored in XAMPP MySQL database
- System supports concurrent users via RMI
