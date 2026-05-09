#!/bin/bash

################################################################################
# Student Attendance System - Client Startup Script
# This script starts the GUI client and connects to remote server
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
    echo -e "${BLUE}  Student Attendance System - CLIENT${NC}"
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

# Show usage
show_usage() {
    echo "Usage: $0 <server-ip>"
    echo ""
    echo "Example:"
    echo "  $0 192.168.1.100"
    echo ""
    echo "The server IP should be the IP address of the computer running the server."
    echo "You can find it by running 'hostname -I' on the server computer."
    exit 1
}

# Check if Java is installed
check_java() {
    if ! command -v java &> /dev/null; then
        print_error "Java is not installed!"
        echo "Please install Java 15 or higher:"
        echo "  Ubuntu/Debian: sudo apt install openjdk-17-jre"
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

# Check if JAR file exists
check_jar() {
    if [ ! -f "$JAR_FILE" ]; then
        print_error "JAR file not found: $JAR_FILE"
        echo ""
        echo "Options:"
        echo "1. Build the project:"
        echo "   cd $PROJECT_DIR"
        echo "   mvn clean package -DskipTests"
        echo ""
        echo "2. Copy JAR from server:"
        echo "   scp user@server-ip:$JAR_FILE $PROJECT_DIR/target/"
        echo "   scp -r user@server-ip:$LIB_DIR $PROJECT_DIR/target/"
        exit 1
    else
        print_success "JAR file found"
    fi
}

# Validate IP address format
validate_ip() {
    local ip=$1
    local stat=1

    if [[ $ip =~ ^[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}$ ]]; then
        OIFS=$IFS
        IFS='.'
        ip=($ip)
        IFS=$OIFS
        [[ ${ip[0]} -le 255 && ${ip[1]} -le 255 && ${ip[2]} -le 255 && ${ip[3]} -le 255 ]]
        stat=$?
    fi
    return $stat
}

# Test connection to server
test_connection() {
    local server_ip=$1
    
    print_info "Testing connection to server..."
    
    # Test 1: Ping
    echo -n "  Checking if server is reachable... "
    if ping -c 1 -W 2 "$server_ip" &> /dev/null; then
        echo -e "${GREEN}OK${NC}"
    else
        echo -e "${RED}FAILED${NC}"
        print_error "Cannot reach server at $server_ip"
        echo ""
        echo "Possible issues:"
        echo "  1. Server is not running"
        echo "  2. Wrong IP address"
        echo "  3. Firewall blocking connection"
        echo "  4. Not on same network"
        echo ""
        read -p "Continue anyway? (y/n): " -n 1 -r
        echo
        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            exit 1
        fi
    fi
    
    # Test 2: Port connectivity
    echo -n "  Checking if RMI port is accessible... "
    if command -v nc &> /dev/null; then
        if nc -z -w2 "$server_ip" "$RMI_PORT" 2>/dev/null; then
            echo -e "${GREEN}OK${NC}"
        else
            echo -e "${RED}FAILED${NC}"
            print_warning "Port $RMI_PORT is not accessible"
            echo ""
            echo "Make sure:"
            echo "  1. Server is running"
            echo "  2. Server firewall allows port $RMI_PORT"
            echo "  3. Network allows the connection"
            echo ""
            read -p "Continue anyway? (y/n): " -n 1 -r
            echo
            if [[ ! $REPLY =~ ^[Yy]$ ]]; then
                exit 1
            fi
        fi
    elif command -v telnet &> /dev/null; then
        if timeout 2 telnet "$server_ip" "$RMI_PORT" 2>&1 | grep -q "Connected"; then
            echo -e "${GREEN}OK${NC}"
        else
            echo -e "${YELLOW}UNKNOWN${NC}"
            print_warning "Could not verify port accessibility"
        fi
    else
        echo -e "${YELLOW}SKIPPED${NC}"
        print_warning "nc or telnet not available, skipping port check"
    fi
    
    print_success "Connection tests completed"
}

# Start the client
start_client() {
    local server_ip=$1
    
    print_info "Starting client..."
    echo ""
    
    # Java options
    JAVA_OPTS="-Xms256m -Xmx1g"
    JAVA_OPTS="$JAVA_OPTS -Djava.rmi.server.hostname=$server_ip"
    JAVA_OPTS="$JAVA_OPTS -Djava.security.policy=$PROJECT_DIR/client.policy"
    
    # Build classpath
    CLASSPATH="$JAR_FILE"
    if [ -d "$LIB_DIR" ]; then
        CLASSPATH="$CLASSPATH:$LIB_DIR/*"
    fi
    
    # Start client
    echo -e "${GREEN}Connecting to server at $server_ip:$RMI_PORT${NC}"
    echo ""
    echo "Login credentials:"
    echo "  Username: admin"
    echo "  Password: Admin@123"
    echo ""
    echo "----------------------------------------"
    
    java $JAVA_OPTS -cp "$CLASSPATH" com.attendance.system.client.ClientLauncher 2>&1 | tee "$LOG_DIR/client.log"
}

################################################################################
# Main Script
################################################################################

main() {
    print_header
    echo ""
    
    # Check if server IP is provided
    if [ $# -eq 0 ]; then
        print_error "Server IP address is required!"
        echo ""
        show_usage
    fi
    
    SERVER_IP=$1
    
    # Validate IP address
    if ! validate_ip "$SERVER_IP"; then
        print_error "Invalid IP address format: $SERVER_IP"
        echo ""
        echo "IP address should be in format: xxx.xxx.xxx.xxx"
        echo "Example: 192.168.1.100"
        exit 1
    fi
    
    # Pre-flight checks
    print_info "Running pre-flight checks..."
    check_java
    check_jar
    
    echo ""
    print_info "Client Configuration:"
    echo "  Server IP: $SERVER_IP"
    echo "  Server Port: $RMI_PORT"
    echo "  Project Dir: $PROJECT_DIR"
    echo "  JAR File: $JAR_FILE"
    echo ""
    
    # Test connection
    test_connection "$SERVER_IP"
    
    echo ""
    print_success "All checks passed!"
    echo ""
    
    # Start client
    start_client "$SERVER_IP"
}

# Handle Ctrl+C
trap 'echo ""; print_info "Client stopped"; exit 0' INT TERM

# Run main function
main "$@"
