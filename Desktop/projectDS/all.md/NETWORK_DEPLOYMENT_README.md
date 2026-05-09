# Network Deployment - Complete Bash Scripts
## Connect Client and Server on Different PCs

---

## 🚀 QUICK START (5 Commands)

### On SERVER Computer:

```bash
# 1. Make scripts executable
chmod +x scripts/*.sh

# 2. Run setup (installs Java, MySQL, configures firewall)
./scripts/setup-network.sh

# 3. Setup database
mysql -u root -p < scripts/database/setup-database.sql

# 4. Start server (auto-detects IP)
./scripts/start-server-network.sh
```

### On CLIENT Computer(s):

```bash
# 1. Make scripts executable
chmod +x scripts/*.sh

# 2. Run setup (installs Java only)
./scripts/setup-network.sh

# 3. Start client (replace with YOUR server IP)
./scripts/start-client-network.sh 192.168.1.100
```

**✅ Done! Login screen appears!**

---

## 📁 FILES CREATED

```
project/
├── scripts/
│   ├── start-server-network.sh    ← Start server
│   ├── start-client-network.sh    ← Start client
│   └── setup-network.sh           ← Initial setup
├── server.policy                  ← Server security policy
├── client.policy                  ← Client security policy
└── NETWORK_DEPLOYMENT_README.md   ← This file
```

---

## 📋 DETAILED INSTRUCTIONS

### STEP 1: Initial Setup (One-time)

#### On BOTH Server and Client computers:

```bash
# Navigate to project directory
cd /path/to/student-attendance-system

# Make all scripts executable
chmod +x scripts/*.sh
chmod +x *.sh

# Run setup script
./scripts/setup-network.sh
```

**What this does:**
- ✅ Detects your operating system
- ✅ Installs Java 17 (if not installed)
- ✅ Installs MySQL (server only)
- ✅ Configures firewall (opens port 1099)
- ✅ Builds the project
- ✅ Shows your IP address

---

### STEP 2: Database Setup (Server Only)

```bash
# Start MySQL
sudo systemctl start mysql

# Create database and user
sudo mysql -u root -p
```

```sql
CREATE DATABASE IF NOT EXISTS attendance_system;
CREATE USER IF NOT EXISTS 'attendance_user'@'%' IDENTIFIED BY 'SecurePassword123!';
GRANT ALL PRIVILEGES ON attendance_system.* TO 'attendance_user'@'%';
FLUSH PRIVILEGES;
EXIT;
```

```bash
# Initialize database schema
mysql -u attendance_user -p attendance_system < scripts/database/setup-database.sql

# Load sample data (optional)
mysql -u attendance_user -p attendance_system < scripts/database/sample-data.sql
```

---

### STEP 3: Start Server

```bash
# Start server (auto-detects IP and configures everything)
./scripts/start-server-network.sh
```

**What you'll see:**
```
============================================
  Student Attendance System - SERVER
============================================

Running pre-flight checks...
✓ Java version: openjdk version "17.0.8"
✓ MySQL is running
✓ JAR file found
✓ Port 1099 is available
✓ Port 1099 is open in firewall

Server Configuration:
  IP Address: 192.168.1.100
  RMI Port: 1099
  Project Dir: /home/user/attendance-system
  JAR File: target/student-attendance-system-1.0.0.jar

✓ All checks passed!

========================================
  CLIENT CONNECTION INFORMATION
========================================

Clients should connect to:
  Server IP: 192.168.1.100
  Port: 1099

On client computers, run:
  ./start-client-network.sh 192.168.1.100

========================================

Server starting on 192.168.1.100:1099

Press Ctrl+C to stop the server

----------------------------------------
[INFO] Database connection pool initialized successfully
[INFO] RMI Registry started on port 1099
[INFO] AttendanceService bound in registry
[INFO] Server started successfully
[INFO] Server is ready to accept connections
```

**✅ Server is now running!**

**Write down the server IP address shown!**

---

### STEP 4: Start Client(s)

```bash
# Start client (replace 192.168.1.100 with YOUR server IP)
./scripts/start-client-network.sh 192.168.1.100
```

**What you'll see:**
```
============================================
  Student Attendance System - CLIENT
============================================

Running pre-flight checks...
✓ Java version: openjdk version "17.0.8"
✓ JAR file found

Client Configuration:
  Server IP: 192.168.1.100
  Server Port: 1099
  Project Dir: /home/user/attendance-system
  JAR File: target/student-attendance-system-1.0.0.jar

Testing connection to server...
  Checking if server is reachable... OK
  Checking if RMI port is accessible... OK
✓ Connection tests completed

✓ All checks passed!

Connecting to server at 192.168.1.100:1099

Login credentials:
  Username: admin
  Password: Admin@123

----------------------------------------
[INFO] Connected to server successfully
[INFO] Client GUI initialized
```

**✅ Login screen appears!**

---

## 🔧 SCRIPT FEATURES

### `start-server-network.sh` Features:

✅ **Auto-detects server IP address**
✅ **Checks if Java is installed**
✅ **Checks if MySQL is running**
✅ **Checks if port 1099 is available**
✅ **Configures firewall automatically**
✅ **Builds project if JAR not found**
✅ **Shows connection info for clients**
✅ **Logs everything to logs/server.log**
✅ **Handles Ctrl+C gracefully**

### `start-client-network.sh` Features:

✅ **Validates server IP format**
✅ **Tests connection to server (ping)**
✅ **Tests if RMI port is accessible**
✅ **Checks if Java is installed**
✅ **Shows helpful error messages**
✅ **Logs everything to logs/client.log**
✅ **Handles Ctrl+C gracefully**

### `setup-network.sh` Features:

✅ **Detects operating system**
✅ **Installs Java automatically**
✅ **Installs MySQL (server only)**
✅ **Configures firewall**
✅ **Builds project**
✅ **Makes scripts executable**
✅ **Shows network information**

---

## 🌐 NETWORK SCENARIOS

### Scenario 1: Same WiFi Network (Most Common)

```bash
# Both computers connected to same WiFi
# Server: 192.168.1.100 (automatic)
# Client: 192.168.1.101 (automatic)

# On server:
./scripts/start-server-network.sh

# On client:
./scripts/start-client-network.sh 192.168.1.100
```

### Scenario 2: Direct Ethernet Cable

```bash
# Connect computers with ethernet cable
# Configure static IPs:

# On server:
sudo ip addr add 192.168.100.1/24 dev eth0
./scripts/start-server-network.sh

# On client:
sudo ip addr add 192.168.100.2/24 dev eth0
./scripts/start-client-network.sh 192.168.100.1
```

### Scenario 3: Mobile Hotspot

```bash
# Create hotspot on server computer
# Connect client to hotspot

# On server:
nmcli device wifi hotspot ssid "AttendanceServer" password "password123"
./scripts/start-server-network.sh

# On client:
# Connect to "AttendanceServer" WiFi
./scripts/start-client-network.sh 192.168.137.1  # or 10.42.0.1 on Linux
```

### Scenario 4: VPN (Remote)

```bash
# Install ZeroTier on both computers
curl -s https://install.zerotier.com | sudo bash

# Join same network (get network ID from zerotier.com)
sudo zerotier-cli join <network-id>

# Get VPN IP
sudo zerotier-cli listnetworks
# Example: 10.147.17.10

# On server:
./scripts/start-server-network.sh

# On client:
./scripts/start-client-network.sh 10.147.17.10
```

---

## 🔥 TROUBLESHOOTING

### Problem: "Connection refused"

```bash
# Check if server is running
netstat -tulpn | grep 1099

# Check firewall
sudo ufw status
sudo ufw allow 1099/tcp

# Restart server
./scripts/start-server-network.sh
```

### Problem: "Cannot reach server"

```bash
# Check if you can ping server
ping 192.168.1.100

# Check if both on same network
ip addr show  # Linux
ipconfig      # Windows

# Check server IP is correct
# On server: hostname -I
```

### Problem: "Port already in use"

```bash
# Find process using port 1099
sudo netstat -tulpn | grep 1099

# Kill the process
kill <PID>

# Or use the script (it checks automatically)
./scripts/start-server-network.sh
```

### Problem: "JAR file not found"

```bash
# Build the project
mvn clean package -DskipTests

# Or let the script build it
./scripts/start-server-network.sh  # Auto-builds if needed
```

### Problem: "MySQL not running"

```bash
# Start MySQL
sudo systemctl start mysql

# Enable MySQL on boot
sudo systemctl enable mysql

# Check status
sudo systemctl status mysql
```

---

## 📊 TESTING THE CONNECTION

### Test 1: Ping Test

```bash
# From client computer
ping 192.168.1.100

# Should see:
# 64 bytes from 192.168.1.100: icmp_seq=1 ttl=64 time=2.1 ms
```

### Test 2: Port Test

```bash
# From client computer
telnet 192.168.1.100 1099

# Or using nc
nc -zv 192.168.1.100 1099

# Should see:
# Connection to 192.168.1.100 1099 port [tcp/*] succeeded!
```

### Test 3: Server Status

```bash
# On server computer
netstat -tulpn | grep 1099

# Should see:
# tcp  0  0  0.0.0.0:1099  0.0.0.0:*  LISTEN  12345/java
```

---

## 🎯 MULTIPLE CLIENTS

```bash
# All clients connect to SAME server IP

# Client 1:
./scripts/start-client-network.sh 192.168.1.100

# Client 2:
./scripts/start-client-network.sh 192.168.1.100

# Client 3:
./scripts/start-client-network.sh 192.168.1.100

# All can login simultaneously!
```

---

## 🔒 SECURITY NOTES

### For Production:

1. **Change default passwords:**
```sql
UPDATE USERS SET password_hash = '<new-hash>' WHERE username = 'admin';
```

2. **Set encryption key:**
```bash
export ATTENDANCE_ENCRYPTION_KEY=$(openssl rand -base64 32)
```

3. **Restrict firewall:**
```bash
# Only allow specific IPs
sudo ufw allow from 192.168.1.0/24 to any port 1099
```

4. **Use SSL/TLS:**
```bash
# Generate keystore
keytool -genkey -alias attendance -keyalg RSA -keystore keystore.jks
```

---

## 📝 LOGS

```bash
# Server logs
tail -f logs/server.log

# Client logs
tail -f logs/client.log

# System logs
sudo journalctl -f
```

---

## 🛑 STOPPING THE SYSTEM

### Stop Server:
```bash
# Press Ctrl+C in server terminal
# Or:
ps aux | grep ServerLauncher
kill <PID>
```

### Stop Client:
```bash
# Press Ctrl+C in client terminal
# Or close the GUI window
```

---

## ✅ VERIFICATION CHECKLIST

### Server:
- [ ] Java installed: `java -version`
- [ ] MySQL running: `sudo systemctl status mysql`
- [ ] Database created: `mysql -u root -p -e "SHOW DATABASES;"`
- [ ] Project built: `ls target/*.jar`
- [ ] Firewall configured: `sudo ufw status`
- [ ] Server starts: `./scripts/start-server-network.sh`
- [ ] Port listening: `netstat -tulpn | grep 1099`

### Client:
- [ ] Java installed: `java -version`
- [ ] Can ping server: `ping <server-ip>`
- [ ] Can reach port: `telnet <server-ip> 1099`
- [ ] Client starts: `./scripts/start-client-network.sh <server-ip>`
- [ ] Login screen appears
- [ ] Can login successfully

---

## 🎉 SUCCESS!

When everything works, you'll see:

**Server:**
```
[INFO] Server started successfully
[INFO] Server is ready to accept connections
```

**Client:**
```
[INFO] Connected to server successfully
[INFO] Client GUI initialized
```

**Login Screen:**
- Username: `admin`
- Password: `Admin@123`

---

## 📞 QUICK REFERENCE

```bash
# Find server IP
hostname -I

# Start server
./scripts/start-server-network.sh

# Start client
./scripts/start-client-network.sh <server-ip>

# Check server status
netstat -tulpn | grep 1099

# Test connection
ping <server-ip>
telnet <server-ip> 1099

# View logs
tail -f logs/server.log
tail -f logs/client.log

# Stop server
Ctrl+C (in server terminal)

# Firewall
sudo ufw allow 1099/tcp
sudo ufw status
```

---

**All scripts are ready to use! Just run them!** 🚀
