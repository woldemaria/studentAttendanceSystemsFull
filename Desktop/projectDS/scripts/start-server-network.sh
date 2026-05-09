#!/bin/bash

################################################################################
# Student Attendance System - Server Startup Script
# This script starts the RMI server for network access
################################################################################

set -e  # Exit on error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
JAR_FILE="$PROJECT_DIR/target/student-attendance-system-1.0.0.jar"
LIB_DIR="$PROJECT_DIR/target/lib"
LOG_DIR="$PROJECT_DIR/logs"
RMI_PORT=1099

# Create logs directory if it doesn't exist
mkdir -p "$LOG_DIR"

################################################################################
# Functions
################################################################################

print_header() {
    echo -e "${BLUE}============================================${NC}"
    echo -e "${BLUE}  Student Attendance System - SERVER${NC}"
    echo -e "${BLUE}============================================${NC}"
}

print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

print_error() {
    echo -e "${RED}✗ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠ $1${NC}"
}

print_info() {
    echo -e "${BLUE}ℹ $1${NC}"
}

# Get server IP address
get_server_ip() {
    # Try different methods to get IP
    local ip=""
    
    # Method 1: hostname -I (Linux)
    if command -v hostname &> /dev/null; then
        ip=$(hostname -I 2>/dev/null | awk '{print $1}')
    fi
    
    # Method 2: ip command (Linux)
    if [ -z "$ip" ] && command -v ip &> /dev/null; then
        ip=$(ip route get 1 2>/dev/null | awk '{print $7; exit}')
    fi
    
    # Method 3: ifconfig (Mac/Linux)
    if [ -z "$ip" ] && command -v ifconfig &> /dev/null; then
        ip=$(ifconfig | grep "inet " | grep -v 127.0.0.1 | awk '{print $2}' | head -n1)
    fi
    
    # Fallback to localhost
    if [ -z "$ip" ]; then
        ip="127.0.0.1"
        print_warning "Could not detect IP address, using localhost"
    fi
    
    echo "$ip"
}

# Check if Java is installed
check_java() {
    if ! command -v java &> /dev/null; then
        print_error "Java is not installed!"
        echo "Please install Java 15 or higher:"
        echo "  Ubuntu/Debian: sudo apt install openjdk-17-jdk"
        echo "  CentOS/RHEL: sudo yum install java-17-openjdk"
        echo "  Mac: brew install openjdk@17"
        exit 1
    fi
    
    local java_version=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
    if [ "$java_version" -lt 15 ]; then
        print_error "Java version must be 15 or higher (found: $java_version)"
        exit 1
    fi
    
    print_success "Java version: $(java -version 2>&1 | head -n1)"
}

# Check if MySQL is running
check_mysql() {
    if command -v systemctl &> /dev/null; then
        if systemctl is-active --quiet mysql || systemctl is-active --quiet mysqld || systemctl is-active --quiet mariadb; then
            print_success "MySQL is running"
            return 0
        fi
    fi
    
    if command -v service &> /dev/null; then
        if service mysql status &> /dev/null || service mysqld status &> /dev/null || service mariadb status &> /dev/null; then
            print_success "MySQL is running"
            return 0
        fi
    fi
    
    # Check if MySQL is accessible (XAMPP or other)
    if mysql -u root -h 127.0.0.1 -e "SELECT 1;" &> /dev/null; then
        print_success "MySQL is running (detected via connection test)"
        return 0
    fi
    
    print_warning "Could not verify MySQL status - continuing anyway"
    return 0
}

# Check if JAR file exists
check_jar() {
    if [ ! -f "$JAR_FILE" ]; then
        print_error "JAR file not found: $JAR_FILE"
        echo ""
        echo "Building project (skipping tests)..."
        cd "$PROJECT_DIR"
        if mvn clean package -Dmaven.test.skip=true; then
            print_success "Project built successfully"
        else
            print_error "Failed to build project"
            exit 1
        fi
    else
        print_success "JAR file found"
    fi
}

# Check if port is already in use
check_port() {
    if command -v netstat &> /dev/null; then
        if netstat -tuln 2>/dev/null | grep -q ":$RMI_PORT "; then
            print_error "Port $RMI_PORT is already in use!"
            echo ""
            echo "Another server might be running. Stop it first:"
            echo "  ps aux | grep ServerLauncher"
            echo "  kill <PID>"
            exit 1
        fi
    elif command -v ss &> /dev/null; then
        if ss -tuln 2>/dev/null | grep -q ":$RMI_PORT "; then
            print_error "Port $RMI_PORT is already in use!"
            exit 1
        fi
    fi
    print_success "Port $RMI_PORT is available"
}

# Configure firewall
configure_firewall() {
    print_info "Checking firewall configuration..."
    
    # UFW (Ubuntu/Debian)
    if command -v ufw &> /dev/null; then
        local ufw_status=$(ufw status 2>/dev/null | grep -i "Status:" || echo "inactive")
        if echo "$ufw_status" | grep -qi "active"; then
            if ! ufw status 2>/dev/null | grep -q "$RMI_PORT"; then
                print_warning "Firewall is active but port $RMI_PORT is not open"
                echo "To open the port manually, run:"
                echo "  sudo ufw allow $RMI_PORT/tcp"
            else
                print_success "Port $RMI_PORT is open in firewall"
            fi
        else
            print_info "Firewall (UFW) is not active"
        fi
    fi
    
    # firewalld (CentOS/RHEL)
    if command -v firewall-cmd &> /dev/null; then
        if firewall-cmd --state 2>/dev/null | grep -q "running"; then
            if ! firewall-cmd --list-ports 2>/dev/null | grep -q "$RMI_PORT"; then
                print_warning "Firewall is active but port $RMI_PORT is not open"
                echo "To open the port manually, run:"
                echo "  sudo firewall-cmd --permanent --add-port=$RMI_PORT/tcp"
                echo "  sudo firewall-cmd --reload"
            else
                print_success "Port $RMI_PORT is open in firewall"
            fi
        else
            print_info "Firewall (firewalld) is not active"
        fi
    fi
}

# Start the server
start_server() {
    local server_ip="$1"
    
    print_info "Starting server..."
    echo ""
    
    # Java options
    JAVA_OPTS="-Xms512m -Xmx2g"
    JAVA_OPTS="$JAVA_OPTS -Djava.rmi.server.hostname=$server_ip"
    JAVA_OPTS="$JAVA_OPTS -Djava.rmi.server.useCodebaseOnly=false"
    JAVA_OPTS="$JAVA_OPTS -Djava.security.policy=$PROJECT_DIR/server.policy"
    JAVA_OPTS="$JAVA_OPTS -Dcom.sun.management.jmxremote"
    JAVA_OPTS="$JAVA_OPTS -Dcom.sun.management.jmxremote.port=9010"
    JAVA_OPTS="$JAVA_OPTS -Dcom.sun.management.jmxremote.authenticate=false"
    JAVA_OPTS="$JAVA_OPTS -Dcom.sun.management.jmxremote.ssl=false"
    
    # Set encryption key if not set
    if [ -z "$ATTENDANCE_ENCRYPTION_KEY" ]; then
        export ATTENDANCE_ENCRYPTION_KEY="MySecretKey123456789012345678901"
        print_warning "Using default encryption key (not secure for production!)"
    fi
    
    # Build classpath
    CLASSPATH="$PROJECT_DIR/target/classes:$JAR_FILE"
    if [ -d "$LIB_DIR" ]; then
        CLASSPATH="$CLASSPATH:$LIB_DIR/*"
    fi
    
    # Start server
    echo -e "${GREEN}Server starting on $server_ip:$RMI_PORT${NC}"
    echo ""
    echo "Press Ctrl+C to stop the server"
    echo ""
    echo "----------------------------------------"
    
    java $JAVA_OPTS -cp "$CLASSPATH" com.attendance.system.server.ServerLauncher 2>&1 | tee "$LOG_DIR/server.log"
}

################################################################################
# Main Script
################################################################################

main() {
    print_header
    echo ""
    
    # Pre-flight checks
    print_info "Running pre-flight checks..."
    check_java
    check_mysql
    check_jar
    check_port
    
    # Get server IP
    SERVER_IP=$(get_server_ip)
    
    echo ""
    print_info "Server Configuration:"
    echo "  IP Address: $SERVER_IP"
    echo "  RMI Port: $RMI_PORT"
    echo "  Project Dir: $PROJECT_DIR"
    echo "  JAR File: $JAR_FILE"
    echo ""
    
    # Configure firewall
    configure_firewall
    
    echo ""
    print_success "All checks passed!"
    echo ""
    
    # Display connection info
    echo -e "${YELLOW}========================================${NC}"
    echo -e "${YELLOW}  CLIENT CONNECTION INFORMATION${NC}"
    echo -e "${YELLOW}========================================${NC}"
    echo ""
    echo "Clients should connect to:"
    echo -e "  ${GREEN}Server IP: $SERVER_IP${NC}"
    echo -e "  ${GREEN}Port: $RMI_PORT${NC}"
    echo ""
    echo "On client computers, run:"
    echo -e "  ${BLUE}./start-client-network.sh $SERVER_IP${NC}"
    echo ""
    echo -e "${YELLOW}========================================${NC}"
    echo ""
    
    # Start server
    start_server "$SERVER_IP"
}

# Handle Ctrl+C
trap 'echo ""; print_info "Server stopped"; exit 0' INT TERM

# Run main function
main "$@"
