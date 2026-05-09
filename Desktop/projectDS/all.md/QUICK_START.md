# 🚀 Quick Start Guide - Student Attendance System

## ✅ Current Status

| Component | Status | Action Needed |
|-----------|--------|---------------|
| ✅ Java 21 | Installed | None |
| ✅ Maven | Working | None |
| ✅ Project Build | Success | None |
| ✅ JAR File | Created (395KB) | None |
| ❌ MySQL | Not Running | **Install & Start** |
| ⏳ Database | Not Created | **Setup after MySQL** |

---

## 🎯 Three Simple Steps to Start

### Step 1: Install MySQL (2 minutes)

```bash
./install-mysql.sh
```

This will:
- Install MySQL Server
- Start MySQL service
- Enable auto-start on boot

### Step 2: Setup Database (1 minute)

```bash
./scripts/setup-database.sh
```

This will:
- Create `Wolde` database
- Load database schema (tables, indexes)
- Optionally create admin user (username: admin, password: admin)

### Step 3: Start Server (instant)

```bash
./scripts/start-server-network.sh
```

This will:
- Auto-detect your IP address
- Start RMI server on port 1099
- Display connection info for clients

---

## 📱 Connect Clients

After server starts, you'll see:

```
========================================
  CLIENT CONNECTION INFORMATION
========================================

Clients should connect to:
  Server IP: 192.168.1.100
  Port: 1099

On client computers, run:
  ./scripts/start-client-network.sh 192.168.1.100
========================================
```

### On Client Computers:

```bash
# Copy the entire project folder to client computer
# Then run:
./scripts/start-client-network.sh 192.168.1.100
```

Replace `192.168.1.100` with your actual server IP.

---

## 🔧 Alternative: Manual MySQL Installation

If the automatic installer doesn't work:

```bash
# Install MySQL
sudo apt update
sudo apt install mysql-server

# Start MySQL
sudo systemctl start mysql
sudo systemctl enable mysql

# Verify it's running
sudo systemctl status mysql
```

Then proceed to Step 2 (Setup Database).

---

## 📋 Default Credentials

### Admin Account
- **Username**: admin
- **Password**: admin
- **Role**: Administrator

### Database
- **Database Name**: Wolde
- **Host**: localhost
- **Port**: 3306
- **Username**: root
- **Password**: (empty)

---

## 🌐 Network Connection Methods

### 1. Same WiFi Network (Easiest)
- Both computers on same WiFi
- Use server's WiFi IP address
- No additional setup needed

### 2. Ethernet Cable (Fastest)
- Direct cable between computers
- Configure static IPs
- Best for 2-computer setup

### 3. Mobile Hotspot (No Router)
- Use phone as WiFi hotspot
- Connect both computers to hotspot
- Good for testing/demos

### 4. Internet/VPN (Remote)
- Port forwarding on router
- Or use VPN like Tailscale
- For remote access

See `CLIENT_SERVER_DEPLOYMENT_GUIDE.md` for detailed network setup.

---

## 🔍 Troubleshooting

### MySQL won't start?
```bash
# Check status
sudo systemctl status mysql

# View logs
sudo journalctl -u mysql -n 50

# Try restarting
sudo systemctl restart mysql
```

### Can't connect to database?
```bash
# Test MySQL connection
mysql -u root -e "SELECT VERSION();"

# If it works, check database exists
mysql -u root -e "SHOW DATABASES;" | grep Wolde
```

### Port 1099 already in use?
```bash
# Find what's using it
sudo netstat -tulpn | grep 1099

# Kill the process
kill <PID>
```

### Firewall blocking connections?
```bash
# Ubuntu/Debian (UFW)
sudo ufw allow 1099/tcp

# CentOS/RHEL (firewalld)
sudo firewall-cmd --permanent --add-port=1099/tcp
sudo firewall-cmd --reload
```

---

## 📚 Documentation

- **This Guide**: Quick start (you are here)
- **SERVER_STARTUP_GUIDE.md**: Detailed server setup
- **CLIENT_SERVER_DEPLOYMENT_GUIDE.md**: Complete deployment guide
- **NETWORK_DEPLOYMENT_README.md**: Network configuration
- **COMPREHENSIVE_CODE_ANALYSIS_REPORT.md**: Security & performance analysis

---

## 🎓 System Features

### For Students
- ✅ View attendance records
- ✅ Check attendance percentage
- ✅ View enrolled courses
- ✅ Update profile information
- ✅ View notifications

### For Teachers
- ✅ Mark student attendance
- ✅ View class attendance reports
- ✅ Manage courses
- ✅ Generate attendance reports (PDF/Excel)
- ✅ Send notifications to students

### For Administrators
- ✅ Manage users (students, teachers, admins)
- ✅ Manage courses
- ✅ View system-wide reports
- ✅ Configure system settings
- ✅ Database backup/restore

---

## 🔒 Security Features

- ✅ BCrypt password hashing
- ✅ Session-based authentication
- ✅ Role-based access control (RBAC)
- ✅ SQL injection prevention (PreparedStatements)
- ✅ Input validation
- ✅ Connection pooling (HikariCP)
- ✅ Encrypted data transmission (optional)
- ✅ Account lockout after failed attempts
- ✅ Session timeout (30 minutes)

---

## ⚡ Performance Features

- ✅ Connection pooling (20 max connections)
- ✅ Database indexes on key fields
- ✅ Prepared statement caching
- ✅ Efficient query optimization
- ✅ Concurrent user support (100 max)
- ✅ Thread-safe operations
- ✅ Memory-efficient design

---

## 🆘 Need Help?

### Check Logs
```bash
# Server logs
tail -f logs/server.log

# MySQL logs
sudo tail -f /var/log/mysql/error.log
```

### Test Database Connection
```bash
# From project directory
java -cp "target/classes:target/student-attendance-system-1.0.0.jar:target/lib/*" \
  com.attendance.system.util.DatabaseConnectionTest
```

### Verify System
```bash
# Compile and run verification
javac -cp "target/classes:target/student-attendance-system-1.0.0.jar:target/lib/*" VerifySystem.java
java -cp ".:target/classes:target/student-attendance-system-1.0.0.jar:target/lib/*" VerifySystem
```

---

## 🎉 Success Checklist

- [ ] MySQL installed and running
- [ ] Database `Wolde` created
- [ ] Schema loaded (tables created)
- [ ] Admin user created
- [ ] Server started successfully
- [ ] Server IP address displayed
- [ ] Client connected to server
- [ ] Logged in successfully

---

**Ready to start? Run: `./install-mysql.sh`**
