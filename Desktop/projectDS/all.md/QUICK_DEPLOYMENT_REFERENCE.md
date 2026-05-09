# Quick Deployment Reference
## Client-Server Setup in 30 Minutes

---

## 🖥️ SERVER PC (15 minutes)

### 1. Install & Setup (5 min)
```bash
# Install dependencies
sudo apt install openjdk-17-jdk maven mysql-server -y

# Create database
sudo mysql -u root -p
```
```sql
CREATE DATABASE attendance_system;
CREATE USER 'attendance_user'@'%' IDENTIFIED BY 'SecurePass123!';
GRANT ALL PRIVILEGES ON attendance_system.* TO 'attendance_user'@'%';
FLUSH PRIVILEGES;
EXIT;
```

### 2. Configure & Build (5 min)
```bash
# Get your server IP
hostname -I
# Example output: 192.168.1.100

# Set environment
export JAVA_RMI_SERVER_HOSTNAME="192.168.1.100"
export ATTENDANCE_ENCRYPTION_KEY=$(openssl rand -base64 32)

# Build project
cd /path/to/project
mvn clean package -DskipTests

# Initialize database
mysql -u attendance_user -p attendance_system < scripts/database/setup-database.sql
```

### 3. Start Server (5 min)
```bash
# Quick start
java -Djava.rmi.server.hostname=192.168.1.100 \
     -cp "target/student-attendance-system-1.0.0.jar:target/lib/*" \
     com.attendance.system.server.ServerLauncher

# Or create service (recommended)
sudo systemctl start attendance-server
```

**✅ Server Ready!** You should see: `Server started successfully on port 1099`

---

## 💻 CLIENT PC (15 minutes)

### 1. Install Java (5 min)
```bash
# Ubuntu/Debian
sudo apt install openjdk-17-jre -y

# Windows: Download from https://adoptium.net/
```

### 2. Copy Files (5 min)
```bash
# Create client directory
mkdir ~/attendance-client
cd ~/attendance-client

# Copy JAR from server (or build locally)
scp user@192.168.1.100:/path/to/project/target/student-attendance-system-1.0.0.jar .
scp -r user@192.168.1.100:/path/to/project/target/lib .
```

### 3. Create Startup Script (5 min)

**Linux/Mac:** `start-client.sh`
```bash
#!/bin/bash
SERVER_HOST="192.168.1.100"  # Change to your server IP
java -Djava.rmi.server.hostname=$SERVER_HOST \
     -cp "student-attendance-system-1.0.0.jar:lib/*" \
     com.attendance.system.client.ClientLauncher
```

**Windows:** `start-client.bat`
```batch
@echo off
set SERVER_HOST=192.168.1.100
java -Djava.rmi.server.hostname=%SERVER_HOST% ^
     -cp "student-attendance-system-1.0.0.jar;lib/*" ^
     com.attendance.system.client.ClientLauncher
pause
```

```bash
# Make executable (Linux/Mac)
chmod +x start-client.sh

# Run client
./start-client.sh
```

**✅ Client Ready!** Login screen should appear!

---

## 🧪 QUICK TEST

### From Client PC:
```bash
# Test connectivity
ping 192.168.1.100

# Test RMI port
telnet 192.168.1.100 1099
# Press Ctrl+] then type 'quit' to exit

# Start client
./start-client.sh
```

### Login with default admin:
- **Username:** `admin`
- **Password:** `Admin@123`

---

## 🔥 COMMON ISSUES & FIXES

### ❌ "Connection refused"
```bash
# On server, check if running
netstat -tulpn | grep 1099

# Open firewall
sudo ufw allow 1099/tcp
sudo ufw reload
```

### ❌ "Cannot connect to server"
```bash
# Verify server IP
# On server:
hostname -I

# Update client script with correct IP
nano start-client.sh
# Change SERVER_HOST="192.168.1.100" to your actual IP
```

### ❌ "java.rmi.NotBoundException"
```bash
# Restart server
sudo systemctl restart attendance-server

# Or if running manually, stop and restart
```

### ❌ "Session expired" too quickly
```java
// In AuthenticationService.java, increase timeout:
private final long sessionTimeoutMillis = 60 * 60 * 1000; // 1 hour
```

---

## 📊 NETWORK SETUP

### Same Network (LAN)
```
Server PC:  192.168.1.100:1099
Client PC1: 192.168.1.101 → connects to 192.168.1.100:1099
Client PC2: 192.168.1.102 → connects to 192.168.1.100:1099
Client PC3: 192.168.1.103 → connects to 192.168.1.100:1099
```

### Different Networks (VPN Required)
```
Server PC:  10.8.0.1:1099 (VPN IP)
Client PC1: 10.8.0.2 → connects to 10.8.0.1:1099
Client PC2: 10.8.0.3 → connects to 10.8.0.1:1099
```

---

## 🚀 PRODUCTION DEPLOYMENT

### Server as Service (Linux)

**Create:** `/etc/systemd/system/attendance-server.service`
```ini
[Unit]
Description=Attendance System Server
After=network.target mysql.service

[Service]
Type=simple
User=attendance
WorkingDirectory=/opt/attendance-system
Environment="JAVA_RMI_SERVER_HOSTNAME=192.168.1.100"
Environment="ATTENDANCE_ENCRYPTION_KEY=your-key-here"
ExecStart=/opt/attendance-system/scripts/start-server.sh
Restart=on-failure

[Install]
WantedBy=multi-user.target
```

```bash
# Enable and start
sudo systemctl daemon-reload
sudo systemctl enable attendance-server
sudo systemctl start attendance-server

# Check status
sudo systemctl status attendance-server
```

### Client Desktop Shortcut (Linux)

**Create:** `~/.local/share/applications/attendance.desktop`
```ini
[Desktop Entry]
Name=Attendance System
Exec=/home/user/attendance-client/start-client.sh
Icon=/home/user/attendance-client/icon.png
Type=Application
Categories=Education;Office;
```

---

## 📋 VERIFICATION CHECKLIST

### Server
- [ ] MySQL running: `sudo systemctl status mysql`
- [ ] Database created: `mysql -u attendance_user -p -e "SHOW DATABASES;"`
- [ ] Server running: `netstat -tulpn | grep 1099`
- [ ] No errors in logs: `tail -f logs/server.log`

### Client
- [ ] Can ping server: `ping 192.168.1.100`
- [ ] Can reach RMI port: `telnet 192.168.1.100 1099`
- [ ] Client starts: `./start-client.sh`
- [ ] Can login successfully

### Multiple Clients
- [ ] Client 1 connected
- [ ] Client 2 connected
- [ ] Client 3 connected
- [ ] All can login simultaneously
- [ ] Data syncs across clients

---

## 🔒 SECURITY QUICK SETUP

### 1. Firewall (Server)
```bash
# Allow only specific clients
sudo ufw allow from 192.168.1.0/24 to any port 1099
sudo ufw enable
```

### 2. Strong Passwords
```bash
# Generate secure encryption key
openssl rand -base64 32

# Set as environment variable
export ATTENDANCE_ENCRYPTION_KEY="generated-key-here"
```

### 3. SSL/TLS (Optional but Recommended)
```bash
# Generate keystore
keytool -genkey -alias attendance -keyalg RSA -keystore keystore.jks

# Configure in server.properties
server.ssl.enabled=true
server.ssl.keystore.path=/path/to/keystore.jks
```

---

## 📞 QUICK COMMANDS

### Server Management
```bash
# Start
sudo systemctl start attendance-server

# Stop
sudo systemctl stop attendance-server

# Restart
sudo systemctl restart attendance-server

# Status
sudo systemctl status attendance-server

# Logs
sudo journalctl -u attendance-server -f
```

### Client Management
```bash
# Start
./start-client.sh

# Kill if frozen
pkill -f ClientLauncher

# Check logs
tail -f logs/client.log
```

### Database Management
```bash
# Backup
mysqldump -u attendance_user -p attendance_system > backup.sql

# Restore
mysql -u attendance_user -p attendance_system < backup.sql

# Check connections
mysql -u attendance_user -p -e "SHOW PROCESSLIST;"
```

---

## 🎯 PERFORMANCE TIPS

### Server Optimization
```bash
# Increase Java heap
java -Xms1g -Xmx4g -cp ...

# Enable JMX monitoring
java -Dcom.sun.management.jmxremote.port=9010 -cp ...
```

### Network Optimization
```bash
# Check latency
ping -c 10 192.168.1.100

# Should be < 50ms for good performance
```

### Database Optimization
```sql
-- Add indexes (if not already done)
CREATE INDEX idx_attendance_student ON ATTENDANCE_RECORDS(student_id);
CREATE INDEX idx_users_username ON USERS(username);
```

---

## 📈 SCALING

### Support More Clients
```bash
# Increase connection pool size
# Edit database.properties
db.pool.maxSize=50  # Default is 20
db.pool.minIdle=10  # Default is 5
```

### Multiple Servers (Load Balancing)
```
Client → Load Balancer → Server 1 (Primary)
                      → Server 2 (Backup)
                      → Server 3 (Backup)
```

---

## ✅ SUCCESS INDICATORS

Your setup is working when:

1. **Server:** `netstat -tulpn | grep 1099` shows LISTEN
2. **Client:** Login screen appears
3. **Login:** Admin credentials work
4. **Multiple:** 3+ clients connected simultaneously
5. **Performance:** Login < 2 seconds
6. **Sync:** Changes visible on all clients

---

## 🆘 EMERGENCY PROCEDURES

### Server Crashed
```bash
# Check logs
sudo journalctl -u attendance-server -n 100

# Restart
sudo systemctl restart attendance-server

# If still failing, check database
sudo systemctl status mysql
```

### Database Corrupted
```bash
# Restore from backup
mysql -u attendance_user -p attendance_system < backup.sql

# Restart server
sudo systemctl restart attendance-server
```

### Network Issues
```bash
# Check connectivity
ping 192.168.1.100

# Check firewall
sudo ufw status

# Restart network
sudo systemctl restart networking
```

---

## 📚 ADDITIONAL RESOURCES

- **Full Guide:** `CLIENT_SERVER_DEPLOYMENT_GUIDE.md`
- **Security Fixes:** `CRITICAL_FIXES_IMPLEMENTATION_PLAN.md`
- **Code Analysis:** `COMPREHENSIVE_CODE_ANALYSIS_REPORT.md`
- **Quick Fixes:** `QUICK_FIX_CHECKLIST.md`

---

**You're all set! Your client-server system is ready to deploy!** 🚀

**Typical Setup Time:**
- Server: 15 minutes
- Each Client: 5 minutes
- Total for 1 server + 3 clients: 30 minutes
