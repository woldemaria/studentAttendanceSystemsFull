# Quick Start Guide - Student Attendance System with XAMPP

This is a fast, step-by-step guide to get your system running in 15 minutes.

## Prerequisites Check

Before starting, verify you have:
- ✅ XAMPP installed (with MySQL)
- ✅ Java 11+ installed (`java -version`)
- ✅ Maven 3.6+ installed (`mvn -version`)
- ✅ Project files downloaded

---

## Step 1: Start XAMPP MySQL (2 minutes)

### Windows
1. Open XAMPP Control Panel: `C:\xampp\xampp-control.exe`
2. Click **Start** next to **MySQL**
3. Wait for it to show "Running"

### macOS
```bash
sudo /Applications/XAMPP/xamppfiles/bin/mysql.server start
```

### Linux
```bash
sudo /opt/lampp/bin/mysql.server start
```

**Verify:** Open browser → `http://localhost/phpmyadmin` (should load)

---

## Step 2: Create Database & User (3 minutes)

### Via phpMyAdmin (Easiest)

1. Open: `http://localhost/phpmyadmin`
2. Login: Username `root`, Password (leave blank)
3. Click **New** → Create database `attendance_system`
4. Click **New** → Create database `attendance_system_test`
5. Go to **User accounts** tab
6. Click **Add user account**
   - Username: `attendance_user`
   - Host: `localhost`
   - Password: `attendance_pass`
   - Confirm: `attendance_pass`
7. Check "Grant all privileges on database(s)"
8. Select both databases
9. Click **Go**

**Done!** Databases and user created.

---

## Step 3: Initialize Database Schema (2 minutes)

Open terminal/command prompt in your project root and run:

### Windows
```bash
"C:\xampp\mysql\bin\mysql.exe" -u attendance_user -p attendance_system < scripts/database/setup-database.sql
```
When prompted: `attendance_pass`

### macOS
```bash
/Applications/XAMPP/xamppfiles/bin/mysql -u attendance_user -p attendance_system < scripts/database/setup-database.sql
```
When prompted: `attendance_pass`

### Linux
```bash
/opt/lampp/bin/mysql -u attendance_user -p attendance_system < scripts/database/setup-database.sql
```
When prompted: `attendance_pass`

**Verify:** Tables created successfully (no errors shown)

---

## Step 4: Load Sample Data (1 minute)

Same commands as above, but with sample data:

### Windows
```bash
"C:\xampp\mysql\bin\mysql.exe" -u attendance_user -p attendance_system < scripts/database/sample-data.sql
```

### macOS
```bash
/Applications/XAMPP/xamppfiles/bin/mysql -u attendance_user -p attendance_system < scripts/database/sample-data.sql
```

### Linux
```bash
/opt/lampp/bin/mysql -u attendance_user -p attendance_system < scripts/database/sample-data.sql
```

**Verify:** Sample data loaded (no errors shown)

---

## Step 5: Build Project (3 minutes)

```bash
cd /path/to/student-attendance-system
mvn clean install
```

**Expected output:**
```
[INFO] BUILD SUCCESS
```

---

## Step 6: Start Server (Terminal 1)

```bash
mvn exec:java -Dexec.mainClass="com.attendance.system.server.ServerLauncher"
```

**Expected output:**
```
[INFO] Student Attendance System Server Startup
[INFO] RMI Service: rmi://localhost:1099/AttendanceService
[INFO] Server started successfully
```

**Keep this terminal open!**

---

## Step 7: Start Client (Terminal 2)

Open a **new terminal** and run:

```bash
cd /path/to/student-attendance-system
mvn exec:java -Dexec.mainClass="com.attendance.system.client.ClientLauncher"
```

**Expected output:**
- GUI window opens with login screen
- Window title: "Student Attendance System"

---

## Step 8: Log In

Use these test credentials:

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

## ✅ You're Done!

The system is now running. You can:

1. **Explore Admin Dashboard** - Manage users, view statistics
2. **Mark Attendance** - Use Teacher account to mark attendance
3. **View Attendance** - Use Student account to view records
4. **Generate Reports** - Create and export attendance reports

---

## Troubleshooting

### "Connection refused" when starting client
- ✅ Ensure server is running (check Terminal 1)
- ✅ Verify MySQL is running in XAMPP
- ✅ Check port 1099 is available

### "Database connection failed"
- ✅ Verify XAMPP MySQL is running
- ✅ Check credentials: `attendance_user` / `attendance_pass`
- ✅ Verify databases exist in phpMyAdmin

### "Compiled classes not found"
- ✅ Run: `mvn clean compile`
- ✅ Ensure `target/classes` directory exists

### "Port 1099 already in use"
- ✅ Use different port: `mvn exec:java -Dexec.mainClass="com.attendance.system.server.ServerLauncher" -Dexec.args="--rmi-port 2099"`

---

## Next Steps

After running the system:

1. **Test all features** - Try each role's functionality
2. **Run tests** - `mvn test`
3. **Review documentation** - See REGISTRATION_ACTOR_GUIDES.md
4. **Implement optional tests** - See IMPLEMENTATION_NEXT_STEPS.md

---

## Quick Reference

| Component | Command | Port |
|-----------|---------|------|
| **XAMPP MySQL** | Start in Control Panel | 3306 |
| **Server** | `mvn exec:java -Dexec.mainClass="com.attendance.system.server.ServerLauncher"` | 1099 |
| **Client** | `mvn exec:java -Dexec.mainClass="com.attendance.system.client.ClientLauncher"` | GUI |
| **phpMyAdmin** | `http://localhost/phpmyadmin` | 80 |

---

**Total Time:** ~15 minutes
**Status:** ✅ Production Ready
**Next:** Start using the system or implement optional tests
