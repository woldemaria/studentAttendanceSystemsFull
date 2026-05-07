# How to Run the Student Attendance System

This guide provides step-by-step instructions to run the Student Attendance System on your local machine.

## Prerequisites

Before running the system, ensure you have the following installed:

1. **Java Development Kit (JDK) 11 or higher**
   - Download from: https://www.oracle.com/java/technologies/downloads/
   - Verify installation: `java -version`

2. **Apache Maven 3.6 or higher**
   - Download from: https://maven.apache.org/download.cgi
   - Verify installation: `mvn -version`

3. **MySQL Server 8.0 or higher**
   - Download from: https://dev.mysql.com/downloads/mysql/
   - Verify installation: `mysql --version`

4. **Git (optional, for cloning the repository)**
   - Download from: https://git-scm.com/

## Step 1: Set Up the Database

### Option A: Using XAMPP (Recommended for Windows/macOS)

#### 1.1 Start XAMPP

1. **Download XAMPP** from https://www.apachefriends.org/
2. **Install XAMPP** on your system
3. **Open XAMPP Control Panel**
4. **Start Apache and MySQL** by clicking the "Start" buttons

**Expected output:**
```
Apache: Running (Port 80)
MySQL: Running (Port 3306)
```

#### 1.2 Create Database and User via phpMyAdmin

1. **Open phpMyAdmin** in your browser: `http://localhost/phpmyadmin`
2. **Log in** with:
   - Username: `root`
   - Password: (leave blank)

3. **Create Database:**
   - Click "New" in the left sidebar
   - Database name: `attendance_system`
   - Collation: `utf8mb4_unicode_ci`
   - Click "Create"

4. **Create Test Database:**
   - Repeat above with database name: `attendance_system_test`

5. **Create User:**
   - Go to "User accounts" tab
   - Click "Add user account"
   - Username: `attendance_user`
   - Host: `localhost`
   - Password: `attendance_pass`
   - Confirm password: `attendance_pass`
   - Check "Create database with same name" (optional)
   - Click "Go"

6. **Grant Privileges:**
   - Click on `attendance_user` in the user list
   - Go to "Database" tab
   - Select `attendance_system` and `attendance_system_test`
   - Check all privileges
   - Click "Go"

#### 1.3 Initialize Database Schema

From the project root directory:

```bash
# Using XAMPP MySQL (Windows)
"C:\xampp\mysql\bin\mysql.exe" -u attendance_user -p attendance_system < scripts/database/setup-database.sql

# Using XAMPP MySQL (macOS)
/Applications/XAMPP/xamppfiles/bin/mysql -u attendance_user -p attendance_system < scripts/database/setup-database.sql

# Using XAMPP MySQL (Linux)
/opt/lampp/bin/mysql -u attendance_user -p attendance_system < scripts/database/setup-database.sql

# When prompted, enter password: attendance_pass
```

#### 1.4 Load Sample Data (Optional)

```bash
# Windows
"C:\xampp\mysql\bin\mysql.exe" -u attendance_user -p attendance_system < scripts/database/sample-data.sql

# macOS
/Applications/XAMPP/xamppfiles/bin/mysql -u attendance_user -p attendance_system < scripts/database/sample-data.sql

# Linux
/opt/lampp/bin/mysql -u attendance_user -p attendance_system < scripts/database/sample-data.sql
```

#### 1.5 Verify Database Setup

```bash
# Windows
"C:\xampp\mysql\bin\mysql.exe" -u attendance_user -p attendance_system -e "SHOW TABLES;"

# macOS
/Applications/XAMPP/xamppfiles/bin/mysql -u attendance_user -p attendance_system -e "SHOW TABLES;"

# Linux
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

---

### Option B: Using Command Line (Linux/macOS)

#### 1.1 Start MySQL Server

**On Linux/macOS:**
```bash
# Using Homebrew (if installed)
brew services start mysql

# Or manually
mysql.server start
```

**On Windows:**
```bash
# Using Services or MySQL Installer
# Or from command line:
net start MySQL80
```

#### 1.2 Create Database and User

Connect to MySQL and run the setup script:

```bash
# Connect to MySQL as root
mysql -u root -p

# Then in MySQL prompt, run:
CREATE DATABASE attendance_system;
CREATE DATABASE attendance_system_test;
CREATE USER 'attendance_user'@'localhost' IDENTIFIED BY 'attendance_pass';
GRANT ALL PRIVILEGES ON attendance_system.* TO 'attendance_user'@'localhost';
GRANT ALL PRIVILEGES ON attendance_system_test.* TO 'attendance_user'@'localhost';
FLUSH PRIVILEGES;
EXIT;
```

#### 1.3 Initialize Database Schema

From the project root directory:

```bash
# Run the database setup script
mysql -u attendance_user -p attendance_system < scripts/database/setup-database.sql

# When prompted, enter password: attendance_pass

# (Optional) Load sample data
mysql -u attendance_user -p attendance_system < scripts/database/sample-data.sql
```

#### 1.4 Verify Database Setup

```bash
mysql -u attendance_user -p attendance_system -e "SHOW TABLES;"
```

You should see tables like: USERS, STUDENTS, TEACHERS, COURSES, ENROLLMENTS, ATTENDANCE_RECORDS, NOTIFICATIONS

## Step 2: Build the Project

From the project root directory:

```bash
# Clean and build the project
mvn clean install

# Or just compile without running tests (faster)
mvn clean compile
```

This will:
- Download all dependencies
- Compile the Java source code
- Run tests (if using `install`)
- Create the `target/` directory with compiled classes

**Expected output:**
```
[INFO] BUILD SUCCESS
```

## Step 3: Run the Server

The RMI server must be running before clients can connect.

### Option A: Using the Shell Script (Linux/macOS)

```bash
# Make the script executable
chmod +x scripts/start-server.sh

# Start the server with default settings
./scripts/start-server.sh

# Or with custom options
./scripts/start-server.sh -r 1099 -d          # Debug mode
./scripts/start-server.sh -b                   # Background mode
./scripts/start-server.sh --memory 2048m       # Custom memory
```

**Expected output:**
```
[INFO] 2024-01-15 10:30:45 - Student Attendance System Server Startup
[INFO] 2024-01-15 10:30:45 - Checking prerequisites...
[INFO] 2024-01-15 10:30:46 - Prerequisites check completed
[INFO] 2024-01-15 10:30:46 - Checking database connectivity...
[INFO] 2024-01-15 10:30:47 - Database connectivity verified
[INFO] 2024-01-15 10:30:47 - Starting Student Attendance System Server...
[INFO] 2024-01-15 10:30:48 - RMI Service: rmi://localhost:1099/AttendanceService
[INFO] 2024-01-15 10:30:48 - Server started successfully
```

### Option B: Using Maven

```bash
# Start the server using Maven
mvn exec:java -Dexec.mainClass="com.attendance.system.server.ServerLauncher"

# With custom RMI port
mvn exec:java -Dexec.mainClass="com.attendance.system.server.ServerLauncher" \
  -Dexec.args="--rmi-port 2099"
```

### Option C: Using Java Directly

```bash
# First, ensure dependencies are downloaded
mvn dependency:copy-dependencies

# Then run the server
java -cp "target/classes:target/dependency/*" \
  com.attendance.system.server.ServerLauncher
```

### Server Configuration Options

```bash
./scripts/start-server.sh [OPTIONS]

Options:
  -r, --rmi-port PORT        RMI registry port (default: 1099)
  -s, --server-port PORT     Server port (default: 0 - anonymous)
  -n, --service-name NAME    RMI service name (default: AttendanceService)
  -m, --memory SIZE          Maximum heap size (default: 1024m)
  -d, --debug               Enable debug mode
  -b, --background          Run in background
  -h, --help                Show help message
```

**Keep the server running** - it must stay active for clients to connect.

## Step 4: Run the Client (in a new terminal)

The client is a Java Swing GUI application. Open a new terminal/command prompt and run:

### Option A: Using Maven

```bash
# Start the client GUI
mvn exec:java -Dexec.mainClass="com.attendance.system.client.ClientLauncher"

# With custom server URL
mvn exec:java -Dexec.mainClass="com.attendance.system.client.ClientLauncher" \
  -Dexec.args="rmi://localhost:2099/AttendanceService"
```

### Option B: Using Java Directly

```bash
# Ensure dependencies are available
mvn dependency:copy-dependencies

# Run the client
java -cp "target/classes:target/dependency/*" \
  com.attendance.system.client.ClientLauncher
```

### Option C: Using the Maven Profile

```bash
mvn exec:java -Pserver    # Start server
mvn exec:java -Pclient    # Start client (in another terminal)
```

**Expected output:**
- A GUI window will open with the login screen
- The window title should be "Student Attendance System"

## Step 5: Log In to the System

Once the client GUI opens, you can log in with the following test credentials:

### Default Test Users

**Admin Account:**
- Username: `admin`
- Password: `Admin@123`

**Teacher Account:**
- Username: `teacher1`
- Password: `Teacher@123`

**Student Account:**
- Username: `student1`
- Password: `Student@123`

> **Note:** These credentials are loaded from `scripts/database/sample-data.sql`. If you didn't load sample data, you'll need to create users through the admin interface or manually insert them into the database.

## Complete Workflow Example

Here's a complete example of running the system from scratch:

### Terminal 1 - Start the Server

```bash
cd /path/to/student-attendance-system

# Build the project
mvn clean install

# Start the server
./scripts/start-server.sh

# Output should show:
# [INFO] RMI Service: rmi://localhost:1099/AttendanceService
# [INFO] Server started successfully
```

### Terminal 2 - Start the Client

```bash
cd /path/to/student-attendance-system

# In a new terminal, start the client
mvn exec:java -Dexec.mainClass="com.attendance.system.client.ClientLauncher"

# Or using Java directly
java -cp "target/classes:target/dependency/*" \
  com.attendance.system.client.ClientLauncher
```

### Terminal 3 - Run Tests (Optional)

```bash
cd /path/to/student-attendance-system

# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=RegistrationFrameTest

# Run with coverage
mvn test jacoco:report
```

## Troubleshooting

### Issue: "Connection refused" when starting client

**Solution:**
- Ensure the server is running (check Terminal 1)
- Verify RMI port is correct (default: 1099)
- Check firewall settings
- Try: `mvn exec:java -Dexec.mainClass="com.attendance.system.client.ClientLauncher" -Dexec.args="rmi://localhost:1099/AttendanceService"`

### Issue: "Database connection failed"

**Solution:**
- Verify MySQL is running: `mysql -u root -p`
- Check database credentials in `src/main/resources/database.properties`
- Ensure database exists: `mysql -u attendance_user -p attendance_system -e "SELECT 1;"`
- Run setup script again: `mysql -u attendance_user -p attendance_system < scripts/database/setup-database.sql`

### Issue: "Java version not supported"

**Solution:**
- Check Java version: `java -version`
- Ensure JDK 11 or higher is installed
- Update JAVA_HOME environment variable if needed

### Issue: "Maven command not found"

**Solution:**
- Install Maven from https://maven.apache.org/download.cgi
- Add Maven to PATH environment variable
- Verify: `mvn -version`

### Issue: "Compiled classes not found"

**Solution:**
- Run: `mvn clean compile`
- Ensure `target/classes` directory exists
- Check for compilation errors in output

### Issue: "Port 1099 already in use"

**Solution:**
- Use a different RMI port: `./scripts/start-server.sh -r 2099`
- Or kill the process using the port:
  - Linux/macOS: `lsof -i :1099 | grep LISTEN | awk '{print $2}' | xargs kill -9`
  - Windows: `netstat -ano | findstr :1099` then `taskkill /PID <PID> /F`

## System Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Client Tier                              │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  Java Swing GUI Client (ClientLauncher)              │   │
│  │  - Login Interface                                   │   │
│  │  - Role-Specific Dashboards (Admin/Teacher/Student) │   │
│  │  - Attendance Marking & Viewing                      │   │
│  └──────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
                            ↓ RMI
┌─────────────────────────────────────────────────────────────┐
│                 Application Tier (Server)                   │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  RMI Server (ServerLauncher)                         │   │
│  │  - Authentication & Authorization                   │   │
│  │  - Business Logic Services                          │   │
│  │  - Attendance Management                            │   │
│  │  - Reporting & Analytics                            │   │
│  └──────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
                            ↓ JDBC
┌─────────────────────────────────────────────────────────────┐
│                    Data Tier                                │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  MySQL Database (attendance_system)                  │   │
│  │  - Users, Students, Teachers, Courses               │   │
│  │  - Attendance Records, Notifications                │   │
│  └──────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

## Performance Tips

1. **Increase Heap Memory** for large datasets:
   ```bash
   ./scripts/start-server.sh --memory 2048m
   ```

2. **Enable Debug Mode** for troubleshooting:
   ```bash
   ./scripts/start-server.sh -d
   ```

3. **Run Tests in Parallel**:
   ```bash
   mvn test -DparallelTestClasses=true
   ```

4. **Skip Tests During Build** (faster):
   ```bash
   mvn clean install -DskipTests
   ```

## Next Steps

After successfully running the system:

1. **Explore the Admin Dashboard** - Manage users and view system statistics
2. **Mark Attendance** - Use the Teacher dashboard to mark student attendance
3. **View Reports** - Generate and export attendance reports
4. **Check Notifications** - View system notifications and alerts
5. **Review Documentation** - See `REGISTRATION_ACTOR_GUIDES.md` for detailed workflows

## Additional Resources

- **Architecture Documentation**: `REGISTRATION_ARCHITECTURE.md`
- **Actor Guides**: `REGISTRATION_ACTOR_GUIDES.md`
- **Quick Reference**: `REGISTRATION_QUICK_REFERENCE.md`
- **API Documentation**: `REGISTRATION_IMPLEMENTATION_SUMMARY.md`
- **Database Schema**: `scripts/database/setup-database.sql`

## Support

For issues or questions:

1. Check the **Troubleshooting** section above
2. Review the **ERROR_HANDLING_AND_LOGGING.md** documentation
3. Check server logs in `logs/server.log`
4. Review test output: `mvn test`

---

**System Status:** ✅ Production Ready
**Last Updated:** 2024
**Version:** 1.0.0
