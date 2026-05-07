# Student Attendance System - XAMPP Setup Guide

This guide provides step-by-step instructions to set up and run the Student Attendance System using XAMPP on Windows, macOS, or Linux.

## What is XAMPP?

XAMPP is a free, open-source cross-platform web server solution stack that includes:
- **Apache** - Web server
- **MySQL** - Database server
- **PHP** - Server-side scripting language
- **Perl** - Programming language

For this project, we primarily use **MySQL** from XAMPP.

## Prerequisites

1. **XAMPP** (any recent version)
2. **Java Development Kit (JDK) 11 or higher**
3. **Apache Maven 3.6 or higher**
4. **Git** (optional)

## Step 1: Install and Start XAMPP

### Windows

1. **Download XAMPP** from https://www.apachefriends.org/
   - Choose the Windows installer
   - Download the latest version (PHP 8.x recommended)

2. **Install XAMPP**
   - Run the installer
   - Choose installation directory (default: `C:\xampp`)
   - Select components (Apache and MySQL are required)
   - Complete the installation

3. **Start XAMPP Control Panel**
   - Open `C:\xampp\xampp-control.exe`
   - Click "Start" next to **Apache** (optional for this project)
   - Click "Start" next to **MySQL**

**Expected output:**
```
Apache: Running (Port 80)
MySQL: Running (Port 3306)
```

### macOS

1. **Download XAMPP** from https://www.apachefriends.org/
   - Choose the macOS installer
   - Download the latest version

2. **Install XAMPP**
   - Open the DMG file
   - Drag XAMPP folder to Applications
   - Or run the installer

3. **Start XAMPP**
   - Open `/Applications/XAMPP/xamppfiles/xampp` (or use the control panel)
   - Or from terminal:
     ```bash
     sudo /Applications/XAMPP/xamppfiles/bin/mysql.server start
     ```

### Linux

1. **Download XAMPP** from https://www.apachefriends.org/
   - Choose the Linux installer

2. **Install XAMPP**
   ```bash
   # Download the installer
   wget https://www.apachefriends.org/xampp-installer-version-linux-x64.run
   
   # Make it executable
   chmod +x xampp-installer-version-linux-x64.run
   
   # Run the installer
   sudo ./xampp-installer-version-linux-x64.run
   ```

3. **Start XAMPP**
   ```bash
   sudo /opt/lampp/manager-linux-x64.run
   # Or from terminal:
   sudo /opt/lampp/bin/mysql.server start
   ```

## Step 2: Create Database and User

### Method A: Using phpMyAdmin (GUI - Easiest)

1. **Open phpMyAdmin**
   - Open your browser
   - Go to: `http://localhost/phpmyadmin`
   - Log in with:
     - Username: `root`
     - Password: (leave blank)

2. **Create Main Database**
   - Click "New" in the left sidebar
   - Database name: `attendance_system`
   - Collation: `utf8mb4_unicode_ci`
   - Click "Create"

3. **Create Test Database**
   - Click "New" again
   - Database name: `attendance_system_test`
   - Collation: `utf8mb4_unicode_ci`
   - Click "Create"

4. **Create User Account**
   - Click "User accounts" tab at the top
   - Click "Add user account"
   - Fill in the form:
     - **Login name:** `attendance_user`
     - **Host:** `localhost`
     - **Password:** `attendance_pass`
     - **Re-type:** `attendance_pass`
   - Under "Database for user account":
     - Select "Grant all privileges on database(s)"
     - Check both `attendance_system` and `attendance_system_test`
   - Click "Go"

5. **Verify User Creation**
   - You should see `attendance_user@localhost` in the user list
   - Status should show "Yes" for all privileges

### Method B: Using Command Line

#### Windows

```bash
# Open Command Prompt and navigate to XAMPP MySQL directory
cd C:\xampp\mysql\bin

# Connect to MySQL as root
mysql -u root

# Create databases and user
CREATE DATABASE attendance_system;
CREATE DATABASE attendance_system_test;
CREATE USER 'attendance_user'@'localhost' IDENTIFIED BY 'attendance_pass';
GRANT ALL PRIVILEGES ON attendance_system.* TO 'attendance_user'@'localhost';
GRANT ALL PRIVILEGES ON attendance_system_test.* TO 'attendance_user'@'localhost';
FLUSH PRIVILEGES;
EXIT;
```

#### macOS

```bash
# Connect to MySQL as root
/Applications/XAMPP/xamppfiles/bin/mysql -u root

# Create databases and user
CREATE DATABASE attendance_system;
CREATE DATABASE attendance_system_test;
CREATE USER 'attendance_user'@'localhost' IDENTIFIED BY 'attendance_pass';
GRANT ALL PRIVILEGES ON attendance_system.* TO 'attendance_user'@'localhost';
GRANT ALL PRIVILEGES ON attendance_system_test.* TO 'attendance_user'@'localhost';
FLUSH PRIVILEGES;
EXIT;
```

#### Linux

```bash
# Connect to MySQL as root
/opt/lampp/bin/mysql -u root

# Create databases and user
CREATE DATABASE attendance_system;
CREATE DATABASE attendance_system_test;
CREATE USER 'attendance_user'@'localhost' IDENTIFIED BY 'attendance_pass';
GRANT ALL PRIVILEGES ON attendance_system.* TO 'attendance_user'@'localhost';
GRANT ALL PRIVILEGES ON attendance_system_test.* TO 'attendance_user'@'localhost';
FLUSH PRIVILEGES;
EXIT;
```

## Step 3: Initialize Database Schema

Navigate to your project root directory and run the setup script:

### Windows

```bash
# Using XAMPP MySQL
"C:\xampp\mysql\bin\mysql.exe" -u attendance_user -p attendance_system < scripts/database/setup-database.sql

# When prompted, enter password: attendance_pass

# Load sample data (optional)
"C:\xampp\mysql\bin\mysql.exe" -u attendance_user -p attendance_system < scripts/database/sample-data.sql
```

### macOS

```bash
# Using XAMPP MySQL
/Applications/XAMPP/xamppfiles/bin/mysql -u attendance_user -p attendance_system < scripts/database/setup-database.sql

# When prompted, enter password: attendance_pass

# Load sample data (optional)
/Applications/XAMPP/xamppfiles/bin/mysql -u attendance_user -p attendance_system < scripts/database/sample-data.sql
```

### Linux

```bash
# Using XAMPP MySQL
/opt/lampp/bin/mysql -u attendance_user -p attendance_system < scripts/database/setup-database.sql

# When prompted, enter password: attendance_pass

# Load sample data (optional)
/opt/lampp/bin/mysql -u attendance_user -p attendance_system < scripts/database/sample-data.sql
```

## Step 4: Verify Database Setup

### Windows

```bash
"C:\xampp\mysql\bin\mysql.exe" -u attendance_user -p attendance_system -e "SHOW TABLES;"
```

### macOS

```bash
/Applications/XAMPP/xamppfiles/bin/mysql -u attendance_user -p attendance_system -e "SHOW TABLES;"
```

### Linux

```bash
/opt/lampp/bin/mysql -u attendance_user -p attendance_system -e "SHOW TABLES;"
```

**Expected output:**
```
Tables_in_attendance_system
USERS
STUDENTS
TEACHERS
COURSES
ENROLLMENTS
ATTENDANCE_RECORDS
NOTIFICATIONS
```

## Step 5: Update Database Configuration (if needed)

If you used different credentials, update the configuration file:

**File:** `src/main/resources/database.properties`

```properties
# Database Connection
db.url=jdbc:mysql://localhost:3306/attendance_system?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
db.username=attendance_user
db.password=attendance_pass
db.driver=com.mysql.cj.jdbc.Driver

# Connection Pool Settings (HikariCP)
db.pool.maximumPoolSize=20
db.pool.minimumIdle=5
db.pool.connectionTimeout=30000
db.pool.idleTimeout=600000
db.pool.maxLifetime=1800000
db.pool.leakDetectionThreshold=60000

# Test Database Configuration
test.db.url=jdbc:mysql://localhost:3306/attendance_system_test?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
test.db.username=attendance_user
test.db.password=attendance_pass

# Application Settings
app.session.timeout=1800000
app.rmi.port=1099
app.rmi.host=localhost
app.backup.schedule=0 2 * * *
app.notification.batch.size=100
```

## Step 6: Build the Project

From the project root directory:

```bash
# Clean and build
mvn clean install

# Or just compile (faster, skips tests)
mvn clean compile
```

**Expected output:**
```
[INFO] BUILD SUCCESS
```

## Step 7: Run the Server

Open a terminal/command prompt and run:

### Option A: Using Maven

```bash
mvn exec:java -Dexec.mainClass="com.attendance.system.server.ServerLauncher"
```

### Option B: Using Java Directly

```bash
# First ensure dependencies are downloaded
mvn dependency:copy-dependencies

# Then run
java -cp "target/classes:target/dependency/*" com.attendance.system.server.ServerLauncher
```

**Expected output:**
```
[INFO] Student Attendance System Server Startup
[INFO] Checking prerequisites...
[INFO] Prerequisites check completed
[INFO] Checking database connectivity...
[INFO] Database connectivity verified
[INFO] Starting Student Attendance System Server...
[INFO] RMI Service: rmi://localhost:1099/AttendanceService
[INFO] Server started successfully
```

**Keep this terminal open** - the server must stay running.

## Step 8: Run the Client (in a new terminal)

Open a new terminal/command prompt and run:

```bash
mvn exec:java -Dexec.mainClass="com.attendance.system.client.ClientLauncher"
```

**Expected output:**
- A GUI window will open with the login screen
- Window title: "Student Attendance System"

## Step 9: Log In

Use the following test credentials:

**Admin Account:**
- Username: `admin`
- Password: `Admin@123`

**Teacher Account:**
- Username: `teacher1`
- Password: `Teacher@123`

**Student Account:**
- Username: `student1`
- Password: `Student@123`

## Complete XAMPP Workflow

### Terminal 1 - Start XAMPP MySQL

**Windows:**
```bash
# Open XAMPP Control Panel
C:\xampp\xampp-control.exe
# Click "Start" next to MySQL
```

**macOS:**
```bash
sudo /Applications/XAMPP/xamppfiles/bin/mysql.server start
```

**Linux:**
```bash
sudo /opt/lampp/bin/mysql.server start
```

### Terminal 2 - Start the Server

```bash
cd /path/to/student-attendance-system
mvn exec:java -Dexec.mainClass="com.attendance.system.server.ServerLauncher"
```

### Terminal 3 - Start the Client

```bash
cd /path/to/student-attendance-system
mvn exec:java -Dexec.mainClass="com.attendance.system.client.ClientLauncher"
```

## Troubleshooting

### Issue: "Can't connect to MySQL server"

**Solution:**
1. Verify XAMPP MySQL is running
2. Check if port 3306 is in use:
   - Windows: `netstat -ano | findstr :3306`
   - macOS/Linux: `lsof -i :3306`
3. Restart MySQL:
   - Windows: Stop and Start in XAMPP Control Panel
   - macOS/Linux: `sudo /path/to/mysql.server restart`

### Issue: "Access denied for user 'attendance_user'"

**Solution:**
1. Verify user was created correctly in phpMyAdmin
2. Check password is correct: `attendance_pass`
3. Verify privileges were granted
4. Try connecting manually:
   ```bash
   mysql -u attendance_user -p attendance_system
   # Enter password: attendance_pass
   ```

### Issue: "Database 'attendance_system' doesn't exist"

**Solution:**
1. Verify database was created in phpMyAdmin
2. Run setup script again:
   ```bash
   # Windows
   "C:\xampp\mysql\bin\mysql.exe" -u attendance_user -p attendance_system < scripts/database/setup-database.sql
   
   # macOS
   /Applications/XAMPP/xamppfiles/bin/mysql -u attendance_user -p attendance_system < scripts/database/setup-database.sql
   
   # Linux
   /opt/lampp/bin/mysql -u attendance_user -p attendance_system < scripts/database/setup-database.sql
   ```

### Issue: "Connection refused" when starting client

**Solution:**
1. Ensure server is running (check Terminal 2)
2. Verify RMI port 1099 is available
3. Check firewall settings
4. Try with explicit server URL:
   ```bash
   mvn exec:java -Dexec.mainClass="com.attendance.system.client.ClientLauncher" \
     -Dexec.args="rmi://localhost:1099/AttendanceService"
   ```

### Issue: "Port 1099 already in use"

**Solution:**
1. Find process using port 1099:
   - Windows: `netstat -ano | findstr :1099`
   - macOS/Linux: `lsof -i :1099`
2. Kill the process:
   - Windows: `taskkill /PID <PID> /F`
   - macOS/Linux: `kill -9 <PID>`
3. Or use a different port:
   ```bash
   java -Drmi.registry.port=2099 -cp "target/classes:target/dependency/*" \
     com.attendance.system.server.ServerLauncher --rmi-port 2099
   ```

### Issue: "Tables don't exist after setup"

**Solution:**
1. Verify setup script ran without errors
2. Check if tables exist:
   ```bash
   # Windows
   "C:\xampp\mysql\bin\mysql.exe" -u attendance_user -p attendance_system -e "SHOW TABLES;"
   
   # macOS
   /Applications/XAMPP/xamppfiles/bin/mysql -u attendance_user -p attendance_system -e "SHOW TABLES;"
   
   # Linux
   /opt/lampp/bin/mysql -u attendance_user -p attendance_system -e "SHOW TABLES;"
   ```
3. If empty, check setup-database.sql file exists
4. Run setup again with verbose output:
   ```bash
   # Windows
   "C:\xampp\mysql\bin\mysql.exe" -u attendance_user -p attendance_system < scripts/database/setup-database.sql -v
   ```

## XAMPP Paths Reference

### Windows
- XAMPP Root: `C:\xampp`
- MySQL: `C:\xampp\mysql\bin\mysql.exe`
- phpMyAdmin: `http://localhost/phpmyadmin`
- Control Panel: `C:\xampp\xampp-control.exe`

### macOS
- XAMPP Root: `/Applications/XAMPP/xamppfiles`
- MySQL: `/Applications/XAMPP/xamppfiles/bin/mysql`
- phpMyAdmin: `http://localhost/phpmyadmin`
- Control Panel: `/Applications/XAMPP/manager-osx.app`

### Linux
- XAMPP Root: `/opt/lampp`
- MySQL: `/opt/lampp/bin/mysql`
- phpMyAdmin: `http://localhost/phpmyadmin`
- Control Panel: `/opt/lampp/manager-linux-x64.run`

## Next Steps

After successfully setting up XAMPP and running the system:

1. **Explore the Admin Dashboard** - Manage users and view system statistics
2. **Mark Attendance** - Use the Teacher dashboard to mark student attendance
3. **View Reports** - Generate and export attendance reports
4. **Check Notifications** - View system notifications and alerts
5. **Review Documentation** - See `REGISTRATION_ACTOR_GUIDES.md` for detailed workflows

## Additional Resources

- **Main Setup Guide**: `HOW_TO_RUN.md`
- **Architecture Documentation**: `REGISTRATION_ARCHITECTURE.md`
- **Actor Guides**: `REGISTRATION_ACTOR_GUIDES.md`
- **Quick Reference**: `REGISTRATION_QUICK_REFERENCE.md`
- **Database Schema**: `scripts/database/setup-database.sql`

## Support

For issues or questions:

1. Check the **Troubleshooting** section above
2. Verify XAMPP MySQL is running
3. Check database credentials in `src/main/resources/database.properties`
4. Review server logs in `logs/server.log`
5. Run tests to verify setup: `mvn test`

---

**System Status:** ✅ Production Ready
**Last Updated:** 2024
**Version:** 1.0.0
**XAMPP Compatible:** Yes (All Versions)
