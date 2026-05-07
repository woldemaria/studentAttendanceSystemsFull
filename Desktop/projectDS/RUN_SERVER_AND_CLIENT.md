# How to Run Server and Client

## Prerequisites

Before running, ensure:
- ✅ XAMPP MySQL is running
- ✅ Databases created (`attendance_system`, `attendance_system_test`)
- ✅ Database user created (`attendance_user` / `attendance_pass`)
- ✅ Project built (`mvn clean install` completed successfully)

---

## Quick Start (2 Steps)

### Step 1: Start Server (Terminal 1)

```bash
mvn exec:java -Dexec.mainClass="com.attendance.system.server.ServerLauncher"
```

**Expected Output:**
```
[INFO] Student Attendance System Server Startup
[INFO] RMI Service: rmi://localhost:1099/AttendanceService
[INFO] Server started successfully
```

**Keep this terminal open!** The server must stay running.

---

### Step 2: Start Client (Terminal 2)

Open a **new terminal** and run:

```bash
mvn exec:java -Dexec.mainClass="com.attendance.system.client.ClientLauncher"
```

**Expected Output:**
- GUI window opens
- Login screen appears
- Window title: "Student Attendance System"

---

## Log In

Use these credentials:

**Admin:**
- Username: `admin`
- Password: `Admin@123`

**Teacher:**
- Username: `teacher1`
- Password: `Teacher@123`

**Student:**
- Username: `student1`
- Password: `Student@123`

---

## Alternative Methods

### Using Java Directly

**Terminal 1 - Server:**
```bash
java -cp "target/classes:target/dependency/*" com.attendance.system.server.ServerLauncher
```

**Terminal 2 - Client:**
```bash
java -cp "target/classes:target/dependency/*" com.attendance.system.client.ClientLauncher
```

### Using Shell Script (Linux/macOS)

**Terminal 1 - Server:**
```bash
./scripts/start-server.sh
```

**Terminal 2 - Client:**
```bash
mvn exec:java -Dexec.mainClass="com.attendance.system.client.ClientLauncher"
```

---

## Troubleshooting

### Server won't start

**Error: "Connection refused"**
```bash
# Check MySQL is running
mysql -u attendance_user -p attendance_system -e "SELECT 1;"
# Password: attendance_pass
```

**Error: "Port 1099 already in use"**
```bash
# Find process using port 1099
netstat -ano | findstr :1099  # Windows
lsof -i :1099                  # macOS/Linux

# Kill the process
taskkill /PID <PID> /F         # Windows
kill -9 <PID>                  # macOS/Linux
```

**Error: "Compiled classes not found"**
```bash
# Rebuild project
mvn clean compile
```

### Client won't connect

**Error: "Connection refused"**
- Ensure server is running (check Terminal 1)
- Verify port 1099 is available
- Check firewall settings

**Try explicit server URL:**
```bash
mvn exec:java -Dexec.mainClass="com.attendance.system.client.ClientLauncher" \
  -Dexec.args="rmi://localhost:1099/AttendanceService"
```

### Login fails

**Error: "Invalid credentials"**
- Verify you're using correct credentials (see above)
- Check database has sample data loaded
- Verify database connection in `src/main/resources/database.properties`

---

## System Architecture

```
Terminal 1                    Terminal 2
┌──────────────────┐         ┌──────────────────┐
│   RMI Server     │         │   GUI Client     │
│   (Port 1099)    │◄────────│   (Java Swing)   │
│                  │         │                  │
│ - Authentication │         │ - Login Form     │
│ - Business Logic │         │ - Dashboards     │
│ - Database Ops   │         │ - User Interface │
└──────────────────┘         └──────────────────┘
        ↓
┌──────────────────┐
│  MySQL Database  │
│  (Port 3306)     │
│                  │
│ - Users          │
│ - Attendance     │
│ - Courses        │
└──────────────────┘
```

---

## What to Do After Running

1. **Explore Admin Dashboard**
   - Manage users
   - View system statistics
   - Configure settings

2. **Mark Attendance (Teacher)**
   - Select class
   - Select date
   - Mark attendance for students

3. **View Attendance (Student)**
   - Check attendance records
   - View attendance percentage
   - See notifications

4. **Generate Reports**
   - Create attendance reports
   - Export to PDF/Excel
   - View statistics

---

## Stopping the System

### Stop Server (Terminal 1)
```bash
Press Ctrl+C
```

### Stop Client (Terminal 2)
```bash
Press Ctrl+C or close the window
```

### Stop XAMPP MySQL
- Windows: XAMPP Control Panel → Click "Stop" next to MySQL
- macOS: `sudo /Applications/XAMPP/xamppfiles/bin/mysql.server stop`
- Linux: `sudo /opt/lampp/bin/mysql.server stop`

---

## Running Tests

```bash
# Run all tests
mvn test

# Run specific test
mvn test -Dtest=RegistrationFrameTest

# Run with coverage
mvn test jacoco:report
```

---

## Common Commands

| Task | Command |
|------|---------|
| **Build** | `mvn clean install` |
| **Start Server** | `mvn exec:java -Dexec.mainClass="com.attendance.system.server.ServerLauncher"` |
| **Start Client** | `mvn exec:java -Dexec.mainClass="com.attendance.system.client.ClientLauncher"` |
| **Run Tests** | `mvn test` |
| **Check MySQL** | `mysql -u attendance_user -p attendance_system -e "SHOW TABLES;"` |
| **View Logs** | `tail -f logs/server.log` |

---

## Key Ports

| Service | Port | Status |
|---------|------|--------|
| **MySQL** | 3306 | Must be running |
| **RMI Server** | 1099 | Started by server |
| **GUI Client** | N/A | GUI window |

---

## Database Credentials

```
Host: localhost
Port: 3306
Database: attendance_system
Username: attendance_user
Password: attendance_pass
```

---

## RMI Service Details

```
Host: localhost
Port: 1099
Service Name: AttendanceService
URL: rmi://localhost:1099/AttendanceService
```

---

## Next Steps

1. ✅ Start server (Terminal 1)
2. ✅ Start client (Terminal 2)
3. ✅ Log in with test credentials
4. ✅ Explore the system
5. ✅ Test all features

---

**Status**: ✅ Ready to Run
**Version**: 1.0.0

**Let's get started! 🚀**
