# Client-Server Deployment Guide
## Running Client and Server on Separate PCs

---

## 🏗️ Architecture Overview

Your system uses **Java RMI** for client-server communication:

```
┌─────────────────┐                    ┌─────────────────┐
│   CLIENT PC     │                    │   SERVER PC     │
│                 │                    │                 │
│  ┌───────────┐  │    RMI/TCP/IP     │  ┌───────────┐  │
│  │ Swing GUI │  │ ◄────────────────► │  │ RMI Server│  │
│  │  Client   │  │   Port 1099       │  │  Service  │  │
│  └───────────┘  │                    │  └─────┬─────┘  │
│                 │                    │        │        │
│                 │                    │  ┌─────▼─────┐  │
│                 │                    │  │  MySQL DB │  │
│                 │                    │  │           │  │
│                 │                    │  └───────────┘  │
└─────────────────┘                    └─────────────────┘
```

**Key Components:**
- **Server PC:** Runs RMI server + MySQL database
- **Client PC(s):** Run Swing GUI applications
- **Communication:** RMI over TCP/IP (default port 1099)

---

## 📋 Prerequisites

### Server PC Requirements
- **OS:** Linux/Windows/macOS
- **Java:** JDK 15 or higher
- **MySQL:** 8.0 or higher
- **RAM:** 4GB minimum (8GB recommended)
- **Network:** Static IP or hostname
- **Ports:** 1099 (RMI), 3306 (MySQL)

### Client PC Requirements
- **OS:** Linux/Windows/macOS
- **Java:** JRE 15 or higher
- **RAM:** 2GB minimum
- **Network:** Access to server PC

---

## 🖥️ SERVER PC SETUP

### Step 1: Install Dependencies

#### On Ubuntu/Debian:
```bash
# Install Java
sudo apt update
sudo apt install openjdk-17-jdk maven mysql-server -y

# Verify installations
java -version
mvn -version
mysql --version
```

#### On Windows:
```powershell
# Install Java from https://adoptium.net/
# Install MySQL from https://dev.mysql.com/downloads/installer/
# Install Maven from https://maven.apache.org/download.cgi
```

### Step 2: Configure MySQL Database

```bash
# Start MySQL
sudo systemctl start mysql
sudo systemctl enable mysql

# Secure MySQL installation
sudo mysql_secure_installation

# Create database and user
sudo mysql -u root -p
```

```sql
-- In MySQL prompt
CREATE DATABASE IF NOT EXISTS attendance_system;

CREATE USER IF NOT EXISTS 'attendance_user'@'%' 
IDENTIFIED BY 'SecurePassword123!';

GRANT ALL PRIVILEGES ON attendance_system.* 
TO 'attendance_user'@'%';

FLUSH PRIVILEGES;

-- Allow remote connections (if clients are on different network)
-- Edit /etc/mysql/mysql.conf.d/mysqld.cnf
-- Change: bind-address = 127.0.0.1
-- To:     bind-address = 0.0.0.0

EXIT;
```

```bash
# Restart MySQL
sudo systemctl restart mysql

# Test connection
mysql -u attendance_user -p attendance_system
```

### Step 3: Setup Project on Server

```bash
# Clone/copy project to server
cd /opt
sudo mkdir attendance-system
sudo chown $USER:$USER attendance-system
cd attendance-system

# Copy your project files here
# Or clone from git: git clone <your-repo>

# Build the project
mvn clean package -DskipTests
```

### Step 4: Configure Server Properties

**Edit:** `src/main/resources/database.properties`

```properties
# Database Configuration
db.url=jdbc:mysql://localhost:3306/attendance_system?useSSL=false&serverTimezone=UTC
db.username=attendance_user
db.password=SecurePassword123!
db.driver=com.mysql.cj.jdbc.Driver

# Connection Pool Settings
db.pool.maxSize=20
db.pool.minIdle=5
db.connectionTimeout=30000
db.idleTimeout=600000
db.maxLifetime=1800000
```

**Edit:** `src/main/resources/server.properties`

```properties
# RMI Server Configuration
rmi.host=0.0.0.0
rmi.port=1099
rmi.service.name=AttendanceService

# Server Settings
server.name=AttendanceServer
server.version=1.0.0

# Security Settings
server.ssl.enabled=false
server.ssl.keystore.path=/path/to/keystore.jks
server.ssl.keystore.password=changeit
```

### Step 5: Initialize Database

```bash
# Run database setup scripts
mysql -u attendance_user -p attendance_system < scripts/database/setup-database.sql
mysql -u attendance_user -p attendance_system < scripts/database/sample-data.sql

# Verify tables created
mysql -u attendance_user -p attendance_system -e "SHOW TABLES;"
```

### Step 6: Set Environment Variables

```bash
# Create environment file
sudo nano /etc/environment

# Add these lines:
export ATTENDANCE_ENCRYPTION_KEY="your-32-character-encryption-key-here"
export ATTENDANCE_DB_PASSWORD="SecurePassword123!"
export JAVA_RMI_SERVER_HOSTNAME="192.168.1.100"  # Server's IP address

# Reload environment
source /etc/environment
```

### Step 7: Configure Firewall

```bash
# Ubuntu/Debian
sudo ufw allow 1099/tcp comment "RMI Server"
sudo ufw allow 3306/tcp comment "MySQL" # Only if clients need direct DB access
sudo ufw enable

# CentOS/RHEL
sudo firewall-cmd --permanent --add-port=1099/tcp
sudo firewall-cmd --permanent --add-port=3306/tcp
sudo firewall-cmd --reload

# Windows
# Open Windows Firewall
# Add inbound rule for port 1099 TCP
```

### Step 8: Create Server Startup Script

**Create:** `scripts/start-server.sh`

```bash
#!/bin/bash

# Server Startup Script
echo "Starting Attendance System Server..."

# Set environment variables
export ATTENDANCE_ENCRYPTION_KEY="your-32-character-key"
export JAVA_RMI_SERVER_HOSTNAME="192.168.1.100"  # Your server IP

# Set Java options
JAVA_OPTS="-Xms512m -Xmx2g"
JAVA_OPTS="$JAVA_OPTS -Djava.rmi.server.hostname=$JAVA_RMI_SERVER_HOSTNAME"
JAVA_OPTS="$JAVA_OPTS -Djava.security.policy=server.policy"
JAVA_OPTS="$JAVA_OPTS -Dcom.sun.management.jmxremote"
JAVA_OPTS="$JAVA_OPTS -Dcom.sun.management.jmxremote.port=9010"
JAVA_OPTS="$JAVA_OPTS -Dcom.sun.management.jmxremote.authenticate=false"
JAVA_OPTS="$JAVA_OPTS -Dcom.sun.management.jmxremote.ssl=false"

# Start server
java $JAVA_OPTS -cp "target/student-attendance-system-1.0.0.jar:target/lib/*" \
    com.attendance.system.server.ServerLauncher

# Or using Maven
# mvn exec:java -Dexec.mainClass="com.attendance.system.server.ServerLauncher"
```

```bash
# Make executable
chmod +x scripts/start-server.sh
```

### Step 9: Create Systemd Service (Linux)

**Create:** `/etc/systemd/system/attendance-server.service`

```ini
[Unit]
Description=Student Attendance System Server
After=network.target mysql.service

[Service]
Type=simple
User=attendance
Group=attendance
WorkingDirectory=/opt/attendance-system
Environment="ATTENDANCE_ENCRYPTION_KEY=your-32-character-key"
Environment="JAVA_RMI_SERVER_HOSTNAME=192.168.1.100"
ExecStart=/opt/attendance-system/scripts/start-server.sh
Restart=on-failure
RestartSec=10
StandardOutput=journal
StandardError=journal

[Install]
WantedBy=multi-user.target
```

```bash
# Create service user
sudo useradd -r -s /bin/false attendance
sudo chown -R attendance:attendance /opt/attendance-system

# Enable and start service
sudo systemctl daemon-reload
sudo systemctl enable attendance-server
sudo systemctl start attendance-server

# Check status
sudo systemctl status attendance-server

# View logs
sudo journalctl -u attendance-server -f
```

### Step 10: Verify Server is Running

```bash
# Check if RMI registry is running
netstat -tulpn | grep 1099

# Check server logs
tail -f logs/server.log

# Test RMI connection
java -cp target/student-attendance-system-1.0.0.jar \
    com.attendance.system.util.TestRMIConnection \
    192.168.1.100 1099
```

---

## 💻 CLIENT PC SETUP

### Step 1: Install Java Runtime

```bash
# Ubuntu/Debian
sudo apt update
sudo apt install openjdk-17-jre -y

# Windows
# Download and install JRE from https://adoptium.net/
```

### Step 2: Copy Client Files

**Option A: Build from source**
```bash
# On client PC
cd ~/attendance-client
mvn clean package -DskipTests

# Copy JAR and dependencies
cp target/student-attendance-system-1.0.0.jar ~/attendance-client/
cp -r target/lib ~/attendance-client/
```

**Option B: Copy pre-built JAR from server**
```bash
# From server, copy to client
scp -r /opt/attendance-system/target/student-attendance-system-1.0.0.jar user@client-pc:~/attendance-client/
scp -r /opt/attendance-system/target/lib user@client-pc:~/attendance-client/
```

### Step 3: Configure Client Properties

**Create:** `client.properties`

```properties
# RMI Server Configuration
rmi.server.host=192.168.1.100  # Server PC IP address
rmi.server.port=1099
rmi.service.name=AttendanceService

# Client Settings
client.name=AttendanceClient
client.version=1.0.0
client.timeout=30000

# UI Settings
ui.theme=system
ui.language=en
```

### Step 4: Create Client Startup Script

**Linux/Mac:** `start-client.sh`

```bash
#!/bin/bash

# Client Startup Script
echo "Starting Attendance System Client..."

# Server configuration
SERVER_HOST="192.168.1.100"  # Change to your server IP
SERVER_PORT="1099"

# Java options
JAVA_OPTS="-Xms256m -Xmx1g"
JAVA_OPTS="$JAVA_OPTS -Djava.rmi.server.hostname=$SERVER_HOST"

# Start client
java $JAVA_OPTS -cp "student-attendance-system-1.0.0.jar:lib/*" \
    com.attendance.system.client.ClientLauncher \
    --server-host=$SERVER_HOST \
    --server-port=$SERVER_PORT

# Or using Maven (if source available)
# mvn exec:java -Dexec.mainClass="com.attendance.system.client.ClientLauncher" \
#     -Dexec.args="--server-host=$SERVER_HOST --server-port=$SERVER_PORT"
```

**Windows:** `start-client.bat`

```batch
@echo off
echo Starting Attendance System Client...

REM Server configuration
set SERVER_HOST=192.168.1.100
set SERVER_PORT=1099

REM Java options
set JAVA_OPTS=-Xms256m -Xmx1g
set JAVA_OPTS=%JAVA_OPTS% -Djava.rmi.server.hostname=%SERVER_HOST%

REM Start client
java %JAVA_OPTS% -cp "student-attendance-system-1.0.0.jar;lib/*" ^
    com.attendance.system.client.ClientLauncher ^
    --server-host=%SERVER_HOST% ^
    --server-port=%SERVER_PORT%

pause
```

```bash
# Make executable (Linux/Mac)
chmod +x start-client.sh
```

### Step 5: Create Desktop Shortcut

**Linux (.desktop file):**

```bash
# Create desktop entry
nano ~/.local/share/applications/attendance-client.desktop
```

```ini
[Desktop Entry]
Version=1.0
Type=Application
Name=Attendance System
Comment=Student Attendance Management System
Exec=/home/user/attendance-client/start-client.sh
Icon=/home/user/attendance-client/icon.png
Terminal=false
Categories=Education;Office;
```

**Windows (.lnk shortcut):**
- Right-click on `start-client.bat`
- Create shortcut
- Move to Desktop
- Change icon if desired

### Step 6: Test Client Connection

```bash
# Start client
./start-client.sh

# Or on Windows
start-client.bat

# You should see the login screen
# Try logging in with default admin credentials
```

---

## 🔧 CONFIGURATION FOR MULTIPLE CLIENTS

### Server Configuration (No changes needed)
The server can handle multiple concurrent clients automatically.

### Client Configuration (Each client PC)

**Client PC 1:**
```bash
# start-client.sh
SERVER_HOST="192.168.1.100"  # Server IP
CLIENT_ID="client-pc-1"
```

**Client PC 2:**
```bash
# start-client.sh
SERVER_HOST="192.168.1.100"  # Same server IP
CLIENT_ID="client-pc-2"
```

**Client PC 3:**
```bash
# start-client.sh
SERVER_HOST="192.168.1.100"  # Same server IP
CLIENT_ID="client-pc-3"
```

All clients connect to the same server simultaneously!

---

## 🌐 NETWORK CONFIGURATION

### Same Local Network (LAN)

**Server PC:**
- IP: `192.168.1.100` (static recommended)
- Port: `1099` (RMI)

**Client PCs:**
- IP: Any (DHCP or static)
- Must be able to reach `192.168.1.100:1099`

**Test connectivity:**
```bash
# From client PC
ping 192.168.1.100
telnet 192.168.1.100 1099
```

### Different Networks (WAN/Internet)

**Option 1: VPN**
- Setup VPN server (OpenVPN, WireGuard)
- Connect all clients to VPN
- Use VPN IP addresses

**Option 2: Port Forwarding**
- Forward port 1099 on router to server PC
- Use public IP address in client configuration
- **Security Warning:** Use SSL/TLS encryption!

**Option 3: SSH Tunnel**
```bash
# On client PC, create SSH tunnel
ssh -L 1099:localhost:1099 user@server-public-ip

# Then connect client to localhost:1099
```

---

## 🔒 SECURITY CONSIDERATIONS

### 1. Enable RMI SSL/TLS

**Server side:**
```java
// In ServerLauncher.java
System.setProperty("javax.net.ssl.keyStore", "/path/to/keystore.jks");
System.setProperty("javax.net.ssl.keyStorePassword", "password");
RMISSLConfiguration.enableSSL();
```

**Client side:**
```java
// In ClientLauncher.java
System.setProperty("javax.net.ssl.trustStore", "/path/to/truststore.jks");
System.setProperty("javax.net.ssl.trustStorePassword", "password");
```

### 2. Firewall Rules

```bash
# Server: Only allow specific client IPs
sudo ufw allow from 192.168.1.0/24 to any port 1099

# Or specific IPs
sudo ufw allow from 192.168.1.101 to any port 1099
sudo ufw allow from 192.168.1.102 to any port 1099
```

### 3. Authentication

Already implemented in your system:
- Username/password authentication
- Session management
- Role-based access control

### 4. Network Encryption

Consider using:
- VPN for all client-server communication
- SSH tunnels for individual clients
- RMI over SSL/TLS

---

## 🧪 TESTING THE SETUP

### Test 1: Server Connectivity

```bash
# From client PC
ping 192.168.1.100

# Test RMI port
telnet 192.168.1.100 1099
# Should connect successfully

# Or use nc (netcat)
nc -zv 192.168.1.100 1099
```

### Test 2: RMI Registry

```bash
# From client PC
java -cp student-attendance-system-1.0.0.jar \
    com.attendance.system.util.TestRMIConnection \
    192.168.1.100 1099
```

### Test 3: Login Test

```bash
# Start client
./start-client.sh

# Try logging in with:
# Username: admin
# Password: Admin@123
```

### Test 4: Multiple Clients

```bash
# Start client on PC 1
./start-client.sh

# Start client on PC 2
./start-client.sh

# Start client on PC 3
./start-client.sh

# All should connect successfully
# Try logging in with different users
```

---

## 🐛 TROUBLESHOOTING

### Issue 1: "Connection refused" error

**Cause:** Server not running or firewall blocking

**Solution:**
```bash
# Check server is running
sudo systemctl status attendance-server

# Check port is listening
netstat -tulpn | grep 1099

# Check firewall
sudo ufw status
sudo ufw allow 1099/tcp
```

### Issue 2: "java.rmi.ConnectException"

**Cause:** Wrong server IP or hostname

**Solution:**
```bash
# Verify server IP
ip addr show  # Linux
ipconfig      # Windows

# Update client configuration
# Edit start-client.sh and change SERVER_HOST
```

### Issue 3: "java.rmi.NotBoundException"

**Cause:** RMI service not registered

**Solution:**
```bash
# Check server logs
tail -f logs/server.log

# Restart server
sudo systemctl restart attendance-server

# Verify service is bound
java -cp target/student-attendance-system-1.0.0.jar \
    com.attendance.system.util.ListRMIServices \
    192.168.1.100 1099
```

### Issue 4: Slow performance

**Cause:** Network latency or server overload

**Solution:**
```bash
# Check network latency
ping -c 10 192.168.1.100

# Check server resources
top
free -h
df -h

# Increase server memory
# Edit /etc/systemd/system/attendance-server.service
# Change: -Xmx2g to -Xmx4g
```

### Issue 5: "Session expired" frequently

**Cause:** Session timeout too short

**Solution:**
```java
// In AuthenticationService.java
// Increase session timeout
private final long sessionTimeoutMillis = 60 * 60 * 1000; // 1 hour
```

---

## 📊 MONITORING

### Server Monitoring

```bash
# Check server status
sudo systemctl status attendance-server

# View real-time logs
sudo journalctl -u attendance-server -f

# Check connections
netstat -an | grep 1099

# Monitor resources
htop
```

### Client Monitoring

```bash
# Check client logs
tail -f logs/client.log

# Monitor network traffic
iftop
nethogs
```

### Database Monitoring

```sql
-- Check active connections
SHOW PROCESSLIST;

-- Check database size
SELECT 
    table_schema AS 'Database',
    ROUND(SUM(data_length + index_length) / 1024 / 1024, 2) AS 'Size (MB)'
FROM information_schema.tables
WHERE table_schema = 'attendance_system'
GROUP BY table_schema;

-- Check slow queries
SHOW VARIABLES LIKE 'slow_query_log';
```

---

## 📋 DEPLOYMENT CHECKLIST

### Server PC
- [ ] Java JDK 15+ installed
- [ ] MySQL 8.0+ installed and configured
- [ ] Database created and initialized
- [ ] Project built successfully
- [ ] Environment variables set
- [ ] Firewall configured
- [ ] Server service created and running
- [ ] RMI port (1099) accessible
- [ ] Server logs show no errors

### Client PC(s)
- [ ] Java JRE 15+ installed
- [ ] Client JAR and dependencies copied
- [ ] Client configuration updated with server IP
- [ ] Startup script created
- [ ] Desktop shortcut created (optional)
- [ ] Can ping server PC
- [ ] Can connect to RMI port
- [ ] Login successful

### Network
- [ ] Server has static IP or hostname
- [ ] Clients can reach server
- [ ] Firewall rules configured
- [ ] VPN configured (if needed)
- [ ] SSL/TLS enabled (recommended)

---

## 🚀 QUICK START COMMANDS

### Server PC
```bash
# Start server
sudo systemctl start attendance-server

# Check status
sudo systemctl status attendance-server

# View logs
sudo journalctl -u attendance-server -f
```

### Client PC
```bash
# Start client
./start-client.sh

# Or on Windows
start-client.bat
```

---

## 📞 SUPPORT

### Common Commands

```bash
# Restart server
sudo systemctl restart attendance-server

# Stop server
sudo systemctl stop attendance-server

# Check server IP
hostname -I

# Test RMI connection
telnet <server-ip> 1099

# View active sessions
mysql -u attendance_user -p -e "SELECT * FROM attendance_system.AUDIT_LOG ORDER BY created_at DESC LIMIT 10;"
```

### Log Locations

- **Server logs:** `/opt/attendance-system/logs/server.log`
- **System logs:** `sudo journalctl -u attendance-server`
- **MySQL logs:** `/var/log/mysql/error.log`
- **Client logs:** `~/attendance-client/logs/client.log`

---

## ✅ SUCCESS CRITERIA

Your client-server setup is working correctly when:

- [ ] Server starts without errors
- [ ] Multiple clients can connect simultaneously
- [ ] Users can login from any client
- [ ] Attendance can be marked from any client
- [ ] Data is synchronized across all clients
- [ ] No connection timeouts or errors
- [ ] Performance is acceptable (<200ms response time)

---

**Your system is now ready for multi-PC deployment!** 🎉

Each client PC can run the GUI application while the server PC handles all business logic and database operations.
