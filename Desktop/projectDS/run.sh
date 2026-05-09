#!/bin/bash

# Student Attendance System - Simple Run Script
# Usage: ./run.sh server  OR  ./run.sh client

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print colored messages
print_info() {
    echo -e "${BLUE}ℹ ${NC}$1"
}

print_success() {
    echo -e "${GREEN}✓${NC} $1"
}

print_error() {
    echo -e "${RED}✗${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}⚠${NC} $1"
}

# Function to check if Maven is installed
check_maven() {
    if ! command -v mvn &> /dev/null; then
        print_error "Maven is not installed!"
        echo "Please install Maven first:"
        echo "  Ubuntu/Debian: sudo apt-get install maven"
        echo "  Fedora: sudo dnf install maven"
        echo "  Or download from: https://maven.apache.org/download.cgi"
        exit 1
    fi
    print_success "Maven found: $(mvn -version | head -n 1)"
}

# Function to check if MySQL is running
check_mysql() {
    if ! pgrep -x "mysqld" > /dev/null; then
        print_warning "MySQL might not be running!"
        echo "Please ensure XAMPP MySQL is started"
        echo "You can start it from XAMPP Control Panel"
    else
        print_success "MySQL is running"
    fi
}

# Function to compile the project
compile_project() {
    print_info "Compiling project..."
    if mvn clean compile -DskipTests -q; then
        print_success "Compilation successful!"
        return 0
    else
        print_error "Compilation failed!"
        echo "Run 'mvn clean compile' to see detailed errors"
        return 1
    fi
}

# Function to start the server
start_server() {
    echo ""
    echo "=========================================="
    echo "  Starting Attendance System SERVER"
    echo "=========================================="
    echo ""
    
    check_maven
    check_mysql
    
    print_info "Checking if server is already running..."
    if pgrep -f "ServerLauncher" > /dev/null; then
        print_warning "Server appears to be already running!"
        read -p "Do you want to kill it and restart? (y/n): " -n 1 -r
        echo
        if [[ $REPLY =~ ^[Yy]$ ]]; then
            pkill -f "ServerLauncher"
            sleep 2
            print_success "Previous server stopped"
        else
            print_info "Exiting..."
            exit 0
        fi
    fi
    
    # Compile if needed
    if [ ! -d "target/classes" ]; then
        compile_project || exit 1
    fi
    
    echo ""
    print_info "Starting server on RMI port 1100..."
    print_info "Press Ctrl+C to stop the server"
    echo ""
    
    # Start the server
    mvn exec:java -Dexec.mainClass="com.attendance.system.server.ServerLauncher" -q
}

# Function to start the client
start_client() {
    echo ""
    echo "=========================================="
    echo "  Starting Attendance System CLIENT"
    echo "=========================================="
    echo ""
    
    check_maven
    
    print_info "Checking if server is running..."
    if ! pgrep -f "ServerLauncher" > /dev/null; then
        print_warning "Server doesn't appear to be running!"
        echo "Please start the server first with: ./run.sh server"
        read -p "Do you want to continue anyway? (y/n): " -n 1 -r
        echo
        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            exit 0
        fi
    else
        print_success "Server is running"
    fi
    
    # Compile if needed
    if [ ! -d "target/classes" ]; then
        compile_project || exit 1
    fi
    
    echo ""
    print_info "Starting client GUI..."
    print_info "Login credentials:"
    echo "  Admin:   username: admin    password: Admin@123"
    echo "  Teacher: username: teacher1 password: Teacher@123"
    echo "  Student: username: student1 password: Student@123"
    echo ""
    
    # Start the client
    mvn exec:java -Dexec.mainClass="com.attendance.system.client.ClientLauncher" -q
}

# Function to show usage
show_usage() {
    echo ""
    echo "=========================================="
    echo "  Student Attendance System"
    echo "=========================================="
    echo ""
    echo "Usage: ./run.sh [server|client]"
    echo ""
    echo "Commands:"
    echo "  server    Start the attendance system server"
    echo "  client    Start the attendance system client GUI"
    echo ""
    echo "Examples:"
    echo "  ./run.sh server    # Start server (run this first)"
    echo "  ./run.sh client    # Start client (in new terminal)"
    echo ""
    echo "Quick Start:"
    echo "  1. Terminal 1: ./run.sh server"
    echo "  2. Terminal 2: ./run.sh client"
    echo "  3. Login with: admin / Admin@123"
    echo ""
}

# Main script logic
case "$1" in
    server)
        start_server
        ;;
    client)
        start_client
        ;;
    *)
        show_usage
        exit 1
        ;;
esac
