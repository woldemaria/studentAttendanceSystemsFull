# Step-by-Step Connection Guide
## Connecting Client and Server on Different Computers

---

## 🎯 What You Need

### Hardware
- **1 Server Computer** (can be laptop or desktop)
- **1+ Client Computer(s)** (can be laptop or desktop)
- **Network Connection** (WiFi or Ethernet cable)

### Software
- **Java 15+** on all computers
- **MySQL** on server computer only
- **Your project JAR files**

### Network Options (Choose ONE)

#### ✅ **Option 1: Same WiFi Network** (EASIEST)
- Both computers connected to same WiFi
- No configuration needed
- Works immediately

#### ✅ **Option 2: Ethernet Cable** (FASTEST)
- Direct cable between computers
- Requires IP configuration
- Best performance

#### ✅ **Option 3: Hotspot** (MOBILE)
- One computer creates hotspot
- Other connects to it
- Works anywhere

#### ✅ **Option 4: Internet/VPN** (REMOTE)
- Computers in different locations
- Requires VPN or port forwarding
- More complex setup

---

## 🚀 QUICK START (Same WiFi - 15 Minutes)

### Step 1: Find Server Computer's IP Address

**On Linux/Mac:**
```bash
# Find IP address
ip addr show | grep "inet " | grep -v 127.0.0.1

# Or simpler
hostname -I

# Example output: 192.168.1.100
```

**On Windows:**
```cmd
# Find IP address
ipconfig

# Look for "IPv4 Address" under your WiFi adapter
# Example: 192.168.1.100
```

**Write down this IP address!** You'll need it for clients.

---

### Step 2: Start Server on Server Computer

```bash
# Navigate to project directory
cd /path/to/your/project

# Build project (first time only)
mvn clean package -DskipTests

# Start server with your IP address
java -Djava.rmi.server.hostname=192.168.1.100 \
     -Djava.security.policy=server.policy \
     -cp "target/student-attendance-system-1.0.0.jar:target/lib/*" \
     com.attendance.system.server.ServerLauncher
```

**Replace `192.168.1.100` with YOUR server's IP address!**

**You should see:**
```
[INFO] Database connection pool initialized successfully
[INFO] RMI Registry started on port 1099
[INFO] AttendanceService bound in registry
[INFO] Server started successfully
[INFO] Server is ready to accept connections
```

**✅ Server is now running!**

---

### Step 3: Test Server is Accessible

**From another terminal on server computer:**
```bash
# Test if port 1099 is listening
netstat -tulpn | grep 1099

# Should show:
# tcp  0  0  0.0.0.0:1099  0.0.0.0:*  LISTEN
```

**From client computer:**
```bash
# Test if you can reach server
ping 192.168.1.100

# Test if port 1099 is open
telnet 192.168.1.100 1099
# If it connects, press Ctrl+] then type 'quit'

# Or use nc (netcat)
nc -zv 192.168.1.100 1099
# Should show: Connection to 192.168.1.100 1099 port [tcp/*] succeeded!
```

**✅ If ping and telnet work, you're ready for clients!**

---

### Step 4: Start Client on Client Computer

**Option A: Copy JAR files from server**
```bash
# On client computer, create directory
mkdir ~/attendance-client
cd ~/attendance-client

# Copy files from server (replace 'user' and IP)
scp user@192.168.1.100:/path/to/project/target/student-attendance-system-1.0.0.jar .
scp -r user@192.168.1.100:/path/to/project/target/lib .
```

**Option B: Build on client computer**
```bash
# Copy entire project to client
# Then build
mvn clean package -DskipTests
```

**Start the client:**
```bash
# Start client (replace with YOUR server IP)
java -Djava.rmi.server.hostname=192.168.1.100 \
     -Djava.security.policy=client.policy \
     -cp "student-attendance-system-1.0.0.jar:lib/*" \
     com.attendance.system.client.ClientLauncher
```

**✅ Login screen should appear!**

---

## 📝 CREATE EASY STARTUP SCRIPTS

### Server Startup Script

**Create:** `start-server.sh` (Linux/Mac) or `start-server.bat` (Windows)

**Linux/Mac:**
```bash
#!/bin/bash

# Get server IP automatically
SERVER_IP=$(hostname -I | awk '{print $1}')

echo "=========================================="
echo "  Student Attendance System - SERVER"
echo "=========================================="
echo "Server IP: $SERVER_IP"
echo "RMI Port: 1099"
echo "=========================================="
echo ""

# Set Java options
JAVA_OPTS="-Xms512m -Xmx2g"
JAVA_OPTS="$JAVA_OPTS -Djava.rmi.server.hostname=$SERVER_IP"
JAVA_OPTS="$JAVA_OPTS -Djava.security.policy=server.policy"

# Start server
java $JAVA_OPTS \
    -cp "target/student-attendance-system-1.0.0.jar:target/lib/*" \
    com.attendance.system.server.ServerLauncher

echo ""
echo "Server stopped."
```

**Windows:**
```batch
@echo off
echo ==========================================
echo   Student Attendance System - SERVER
echo ==========================================

REM Get server IP
for /f "tokens=2 delims=:" %%a in ('ipconfig ^| findstr /c:"IPv4 Address"') do (
    set SERVER_IP=%%a
)
set SERVER_IP=%SERVER_IP:~1%

echo Server IP: %SERVER_IP%
echo RMI Port: 1099
echo ==========================================
echo.

REM Set Java options
set JAVA_OPTS=-Xms512m -Xmx2g
set JAVA_OPTS=%JAVA_OPTS% -Djava.rmi.server.hostname=%SERVER_IP%
set JAVA_OPTS=%JAVA_OPTS% -Djava.security.policy=server.policy

REM Start server
java %JAVA_OPTS% ^
    -cp "target/student-attendance-system-1.0.0.jar;target/lib/*" ^
    com.attendance.system.server.ServerLauncher

echo.
echo Server stopped.
pause
```

```bash
# Make executable (Linux/Mac)
chmod +x start-server.sh

# Run
./start-server.sh
```

---

### Client Startup Script

**Create:** `start-client.sh` (Linux/Mac) or `start-client.bat` (Windows)

**Linux/Mac:**
```bash
#!/bin/bash

# CHANGE THIS TO YOUR SERVER'S IP ADDRESS!
SERVER_IP="192.168.1.100"

echo "=========================================="
echo "  Student Attendance System - CLIENT"
echo "=========================================="
echo "Connecting to server: $SERVER_IP:1099"
echo "=========================================="
echo ""

# Test connection first
echo "Testing connection to server..."
if ping -c 1 -W 2 $SERVER_IP > /dev/null 2>&1; then
    echo "✓ Server is reachable"
else
    echo "✗ Cannot reach server at $SERVER_IP"
    echo "  Please check:"
    echo "  1. Server is running"
    echo "  2. Server IP address is correct"
    echo "  3. Both computers are on same network"
    exit 1
fi

# Set Java options
JAVA_OPTS="-Xms256m -Xmx1g"
JAVA_OPTS="$JAVA_OPTS -Djava.rmi.server.hostname=$SERVER_IP"
JAVA_OPTS="$JAVA_OPTS -Djava.security.policy=client.policy"

# Start client
java $JAVA_OPTS \
    -cp "student-attendance-system-1.0.0.jar:lib/*" \
    com.attendance.system.client.ClientLauncher

echo ""
echo "Client closed."
```

**Windows:**
```batch
@echo off
REM CHANGE THIS TO YOUR SERVER'S IP ADDRESS!
set SERVER_IP=192.168.1.100

echo ==========================================
echo   Student Attendance System - CLIENT
echo ==========================================
echo Connecting to server: %SERVER_IP%:1099
echo ==========================================
echo.

REM Test connection
echo Testing connection to server...
ping -n 1 -w 2000 %SERVER_IP% >nul
if %errorlevel% equ 0 (
    echo [OK] Server is reachable
) else (
    echo [ERROR] Cannot reach server at %SERVER_IP%
    echo   Please check:
    echo   1. Server is running
    echo   2. Server IP address is correct
    echo   3. Both computers are on same network
    pause
    exit /b 1
)

REM Set Java options
set JAVA_OPTS=-Xms256m -Xmx1g
set JAVA_OPTS=%JAVA_OPTS% -Djava.rmi.server.hostname=%SERVER_IP%
set JAVA_OPTS=%JAVA_OPTS% -Djava.security.policy=client.policy

REM Start client
java %JAVA_OPTS% ^
    -cp "student-attendance-system-1.0.0.jar;lib/*" ^
    com.attendance.system.client.ClientLauncher

echo.
echo Client closed.
pause
```

```bash
# Make executable (Linux/Mac)
chmod +x start-client.sh

# Edit to set your server IP
nano start-client.sh
# Change: SERVER_IP="192.168.1.100"
# To your actual server IP

# Run
./start-client.sh
```

---

## 🔧 DETAILED NETWORK SETUP OPTIONS

### Option 1: Same WiFi Network (EASIEST) ✅

**Setup:**
1. Connect both computers to same WiFi
2. Find server IP: `hostname -I`
3. Start server with that IP
4. Start client with that IP
5. Done!

**Pros:**
- ✅ Easiest setup
- ✅ No configuration needed
- ✅ Works immediately

**Cons:**
- ❌ Both must be on same WiFi
- ❌ Depends on WiFi speed

**Example:**
```
WiFi Router: 192.168.1.1
Server PC:   192.168.1.100 (automatic)
Client PC 1: 192.168.1.101 (automatic)
Client PC 2: 192.168.1.102 (automatic)
```

---

### Option 2: Direct Ethernet Cable ⚡

**Setup:**

**Step 1: Connect computers with Ethernet cable**

**Step 2: Configure static IPs**

**On Server (Linux):**
```bash
# Find ethernet interface name
ip link show
# Example: enp0s3, eth0, eno1

# Set static IP
sudo ip addr add 192.168.100.1/24 dev enp0s3
sudo ip link set enp0s3 up
```

**On Server (Windows):**
1. Open Network Connections
2. Right-click Ethernet adapter
3. Properties → IPv4 → Use the following IP:
   - IP: `192.168.100.1`
   - Subnet: `255.255.255.0`
   - Gateway: (leave empty)

**On Client (Linux):**
```bash
sudo ip addr add 192.168.100.2/24 dev enp0s3
sudo ip link set enp0s3 up
```

**On Client (Windows):**
1. Same as server but use IP: `192.168.100.2`

**Step 3: Test connection**
```bash
# From client
ping 192.168.100.1
```

**Step 4: Start server with `192.168.100.1`**
**Step 5: Start client connecting to `192.168.100.1`**

**Pros:**
- ✅ Fastest connection
- ✅ No WiFi needed
- ✅ Most reliable

**Cons:**
- ❌ Requires ethernet cable
- ❌ Manual IP configuration
- ❌ Only 2 computers (without switch)

---

### Option 3: Mobile Hotspot 📱

**Setup:**

**Step 1: Create hotspot on server computer**

**On Linux:**
```bash
# Using nmcli
nmcli device wifi hotspot ssid "AttendanceServer" password "password123"
```

**On Windows:**
1. Settings → Network & Internet → Mobile hotspot
2. Turn on "Share my Internet connection"
3. Set network name and password

**On Mac:**
1. System Preferences → Sharing
2. Enable "Internet Sharing"
3. Share from: WiFi
4. To computers using: WiFi

**Step 2: Connect client to hotspot**
- Connect to "AttendanceServer" WiFi
- Password: "password123"

**Step 3: Find server IP**
```bash
# On server
hostname -I
# Usually: 192.168.137.1 (Windows) or 10.42.0.1 (Linux)
```

**Step 4: Start server and client as usual**

**Pros:**
- ✅ No router needed
- ✅ Works anywhere
- ✅ Easy setup

**Cons:**
- ❌ Slower than WiFi router
- ❌ Limited range
- ❌ Drains battery

---

### Option 4: Internet/VPN (Remote) 🌐

**For computers in different locations**

**Option 4A: Using VPN (Recommended)**

**Step 1: Setup VPN server**

**Using ZeroTier (Easiest):**
```bash
# On both computers
curl -s https://install.zerotier.com | sudo bash

# Create network at https://my.zerotier.com
# Get network ID (example: 1234567890abcdef)

# Join network on both computers
sudo zerotier-cli join 1234567890abcdef

# Authorize devices on ZeroTier website

# Check IP addresses
sudo zerotier-cli listnetworks
# Example: Server gets 10.147.17.10
#          Client gets 10.147.17.11
```

**Step 2: Use VPN IPs**
```bash
# Start server with VPN IP
java -Djava.rmi.server.hostname=10.147.17.10 ...

# Start client connecting to VPN IP
# (edit start-client.sh to use 10.147.17.10)
```

**Option 4B: Using SSH Tunnel**

**On client computer:**
```bash
# Create SSH tunnel to server
ssh -L 1099:localhost:1099 user@server-public-ip

# In another terminal, start client connecting to localhost
java -Djava.rmi.server.hostname=localhost ...
```

**Option 4C: Port Forwarding (Not Recommended)**

**On server's router:**
1. Login to router admin panel
2. Find "Port Forwarding" section
3. Forward port 1099 to server's local IP
4. Use router's public IP on client

**⚠️ Security Warning:** This exposes your server to the internet!

---

## 🔥 TROUBLESHOOTING

### Problem 1: "Connection refused"

**Symptoms:**
```
java.rmi.ConnectException: Connection refused to host: 192.168.1.100
```

**Solutions:**

**Check 1: Is server running?**
```bash
# On server
netstat -tulpn | grep 1099
# Should show: tcp  0  0  0.0.0.0:1099  LISTEN
```

**Check 2: Is firewall blocking?**
```bash
# Ubuntu/Debian
sudo ufw allow 1099/tcp
sudo ufw reload

# CentOS/RHEL
sudo firewall-cmd --add-port=1099/tcp --permanent
sudo firewall-cmd --reload

# Windows
# Windows Firewall → Advanced Settings → Inbound Rules
# New Rule → Port → TCP → 1099 → Allow
```

**Check 3: Can client reach server?**
```bash
# From client
ping 192.168.1.100
telnet 192.168.1.100 1099
```

---

### Problem 2: "java.rmi.NotBoundException"

**Symptoms:**
```
java.rmi.NotBoundException: AttendanceService
```

**Solutions:**

**Check 1: Server started correctly?**
```bash
# Check server logs
tail -f logs/server.log
# Should show: "AttendanceService bound in registry"
```

**Check 2: Restart server**
```bash
# Stop server (Ctrl+C)
# Start again
./start-server.sh
```

---

### Problem 3: Wrong IP address

**Symptoms:**
- Client can't connect
- Ping fails

**Solutions:**

**Find correct IP:**
```bash
# Linux/Mac
ip addr show | grep "inet " | grep -v 127.0.0.1

# Windows
ipconfig | findstr "IPv4"

# Look for IP starting with:
# 192.168.x.x (WiFi/LAN)
# 10.x.x.x (VPN or some LANs)
# 172.16-31.x.x (some LANs)
```

**Update scripts:**
```bash
# Edit start-server.sh
nano start-server.sh
# Change SERVER_IP line

# Edit start-client.sh
nano start-client.sh
# Change SERVER_IP line
```

---

### Problem 4: Slow connection

**Symptoms:**
- Login takes > 5 seconds
- Operations timeout

**Solutions:**

**Check 1: Network latency**
```bash
ping -c 10 192.168.1.100
# Should be < 50ms average
```

**Check 2: Use wired connection**
- Ethernet is faster than WiFi

**Check 3: Check server resources**
```bash
# On server
top
free -h
# Make sure server has enough RAM/CPU
```

---

### Problem 5: Multiple network interfaces

**Symptoms:**
- Server has multiple IPs
- Client connects to wrong one

**Solutions:**

**Find correct interface:**
```bash
# List all interfaces
ip addr show

# Common interfaces:
# wlan0, wlp2s0 = WiFi
# eth0, enp0s3 = Ethernet
# tun0, zt0 = VPN
```

**Use specific IP:**
```bash
# Start server with specific IP
java -Djava.rmi.server.hostname=192.168.1.100 ...
# NOT the VPN IP or localhost
```

---

## ✅ VERIFICATION CHECKLIST

### Server Side
- [ ] Java installed: `java -version`
- [ ] MySQL running: `sudo systemctl status mysql`
- [ ] Database created: `mysql -u root -p -e "SHOW DATABASES;"`
- [ ] Project built: `ls target/*.jar`
- [ ] Server IP known: `hostname -I`
- [ ] Firewall allows port 1099: `sudo ufw status`
- [ ] Server starts without errors
- [ ] Port 1099 listening: `netstat -tulpn | grep 1099`

### Client Side
- [ ] Java installed: `java -version`
- [ ] JAR files copied
- [ ] Server IP configured in script
- [ ] Can ping server: `ping <server-ip>`
- [ ] Can reach port 1099: `telnet <server-ip> 1099`
- [ ] Client starts and shows login screen

### Network
- [ ] Both computers on same network (or VPN)
- [ ] No firewall blocking
- [ ] Ping works both ways
- [ ] Port 1099 accessible

---

## 🎯 QUICK REFERENCE

### Essential Commands

**Find IP:**
```bash
hostname -I                    # Linux/Mac
ipconfig                       # Windows
```

**Test Connection:**
```bash
ping <server-ip>               # Test reachability
telnet <server-ip> 1099        # Test port
nc -zv <server-ip> 1099        # Alternative port test
```

**Check Server:**
```bash
netstat -tulpn | grep 1099     # Check if listening
ps aux | grep ServerLauncher   # Check if running
tail -f logs/server.log        # View logs
```

**Firewall:**
```bash
sudo ufw allow 1099/tcp        # Ubuntu/Debian
sudo firewall-cmd --add-port=1099/tcp --permanent  # CentOS/RHEL
```

---

## 📞 NEED HELP?

### Common Scenarios

**Scenario 1: Home network (2 computers)**
→ Use Option 1 (Same WiFi)

**Scenario 2: Lab/classroom (10+ computers)**
→ Use Option 1 (Same WiFi/LAN)

**Scenario 3: No WiFi available**
→ Use Option 2 (Ethernet) or Option 3 (Hotspot)

**Scenario 4: Different buildings/cities**
→ Use Option 4 (VPN - ZeroTier recommended)

---

**You're ready to connect! Start with Option 1 (Same WiFi) - it's the easiest!** 🚀
