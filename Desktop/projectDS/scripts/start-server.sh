#!/bin/bash

# Student Attendance System - Server Startup Script (Linux/macOS)
# This script starts the RMI server with proper configuration

# Script configuration
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
LOG_DIR="$PROJECT_ROOT/logs"
PID_FILE="$LOG_DIR/server.pid"

# Default configuration
RMI_PORT=1099
SERVER_PORT=0
SERVICE_NAME="AttendanceService"
JAVA_OPTS="-Xmx1024m -Xms512m"
DEBUG_MODE=false
BACKGROUND_MODE=false

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to display usage
show_usage() {
    echo "Usage: $0 [OPTIONS]"
    echo ""
    echo "Options:"
    echo "  -r, --rmi-port PORT        RMI registry port (default: 1099)"
    echo "  -s, --server-port PORT     Server port (default: 0 - anonymous)"
    echo "  -n, --service-name NAME    RMI service name (default: AttendanceService)"
    echo "  -m, --memory SIZE          Maximum heap size (default: 1024m)"
    echo "  -d, --debug               Enable debug mode"
    echo "  -b, --background          Run in background"
    echo "  -h, --help                Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0                        Start with default settings"
    echo "  $0 -r 2099 -d            Start on port 2099 with debug mode"
    echo "  $0 -b                     Start in background"
    echo "  $0 --memory 2048m         Start with 2GB heap size"
}

# Function to log messages
log_message() {
    local level=$1
    local message=$2
    local timestamp=$(date '+%Y-%m-%d %H:%M:%S')
    
    case $level in
        "INFO")
            echo -e "${GREEN}[INFO]${NC} $timestamp - $message"
            ;;
        "WARN")
            echo -e "${YELLOW}[WARN]${NC} $timestamp - $message"
            ;;
        "ERROR")
            echo -e "${RED}[ERROR]${NC} $timestamp - $message"
            ;;
        "DEBUG")
            if [ "$DEBUG_MODE" = true ]; then
                echo -e "${BLUE}[DEBUG]${NC} $timestamp - $message"
            fi
            ;;
    esac
}

# Function to check if server is already running
check_server_running() {
    if [ -f "$PID_FILE" ]; then
        local pid=$(cat "$PID_FILE")
        if ps -p $pid > /dev/null 2>&1; then
            return 0  # Server is running
        else
            rm -f "$PID_FILE"  # Remove stale PID file
            return 1  # Server is not running
        fi
    fi
    return 1  # PID file doesn't exist
}

# Function to check prerequisites
check_prerequisites() {
    log_message "INFO" "Checking prerequisites..."
    
    # Check Java installation
    if ! command -v java &> /dev/null; then
        log_message "ERROR" "Java is not installed or not in PATH"
        exit 1
    fi
    
    # Check Java version
    local java_version=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1-2)
    log_message "DEBUG" "Java version: $java_version"
    
    # Check if Maven is available for building
    if [ ! -f "$PROJECT_ROOT/target/classes" ] && command -v mvn &> /dev/null; then
        log_message "INFO" "Compiled classes not found. Building project..."
        cd "$PROJECT_ROOT"
        mvn compile -q
        if [ $? -ne 0 ]; then
            log_message "ERROR" "Failed to compile project"
            exit 1
        fi
    fi
    
    # Check if compiled classes exist
    if [ ! -d "$PROJECT_ROOT/target/classes" ]; then
        log_message "ERROR" "Compiled classes not found. Please run 'mvn compile' first."
        exit 1
    fi
    
    # Create logs directory
    mkdir -p "$LOG_DIR"
    
    log_message "INFO" "Prerequisites check completed"
}

# Function to check database connectivity
check_database() {
    log_message "INFO" "Checking database connectivity..."
    
    # Try to connect to database using Java
    cd "$PROJECT_ROOT"
    java -cp "target/classes:target/dependency/*" \
         com.attendance.system.dao.DatabaseManager --test-connection
    
    if [ $? -ne 0 ]; then
        log_message "WARN" "Database connectivity test failed"
        log_message "WARN" "Server will start but may not function properly"
        log_message "INFO" "Please ensure MySQL is running and database is configured"
    else
        log_message "INFO" "Database connectivity verified"
    fi
}

# Function to start the server
start_server() {
    log_message "INFO" "Starting Student Attendance System Server..."
    
    # Check if server is already running
    if check_server_running; then
        log_message "WARN" "Server is already running (PID: $(cat $PID_FILE))"
        exit 1
    fi
    
    # Build classpath
    local classpath="$PROJECT_ROOT/target/classes"
    if [ -d "$PROJECT_ROOT/target/dependency" ]; then
        classpath="$classpath:$PROJECT_ROOT/target/dependency/*"
    fi
    
    # Build Java command
    local java_cmd="java $JAVA_OPTS"
    
    # Add debug options if enabled
    if [ "$DEBUG_MODE" = true ]; then
        java_cmd="$java_cmd -Dcom.sun.management.jmxremote"
        java_cmd="$java_cmd -Dcom.sun.management.jmxremote.port=9999"
        java_cmd="$java_cmd -Dcom.sun.management.jmxremote.authenticate=false"
        java_cmd="$java_cmd -Dcom.sun.management.jmxremote.ssl=false"
        java_cmd="$java_cmd -Djava.util.logging.config.file=$PROJECT_ROOT/src/main/resources/logging.properties"
    fi
    
    # Add system properties
    java_cmd="$java_cmd -Drmi.registry.port=$RMI_PORT"
    java_cmd="$java_cmd -Drmi.server.port=$SERVER_PORT"
    java_cmd="$java_cmd -Drmi.service.name=$SERVICE_NAME"
    java_cmd="$java_cmd -Dproject.root=$PROJECT_ROOT"
    
    # Add classpath and main class
    java_cmd="$java_cmd -cp $classpath com.attendance.system.server.ServerLauncher"
    
    # Add command line arguments
    java_cmd="$java_cmd --service-name $SERVICE_NAME --rmi-port $RMI_PORT"
    if [ "$SERVER_PORT" != "0" ]; then
        java_cmd="$java_cmd --server-port $SERVER_PORT"
    fi
    
    log_message "DEBUG" "Java command: $java_cmd"
    
    # Start server
    if [ "$BACKGROUND_MODE" = true ]; then
        log_message "INFO" "Starting server in background mode..."
        nohup $java_cmd > "$LOG_DIR/server.log" 2>&1 &
        local server_pid=$!
        echo $server_pid > "$PID_FILE"
        
        # Wait a moment and check if server started successfully
        sleep 3
        if ps -p $server_pid > /dev/null 2>&1; then
            log_message "INFO" "Server started successfully in background (PID: $server_pid)"
            log_message "INFO" "Server logs: $LOG_DIR/server.log"
            log_message "INFO" "RMI Service: rmi://localhost:$RMI_PORT/$SERVICE_NAME"
        else
            log_message "ERROR" "Server failed to start. Check logs for details."
            rm -f "$PID_FILE"
            exit 1
        fi
    else
        log_message "INFO" "Starting server in foreground mode..."
        log_message "INFO" "Press Ctrl+C to stop the server"
        log_message "INFO" "RMI Service: rmi://localhost:$RMI_PORT/$SERVICE_NAME"
        
        # Set up signal handler for graceful shutdown
        trap 'log_message "INFO" "Shutting down server..."; exit 0' SIGINT SIGTERM
        
        exec $java_cmd
    fi
}

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        -r|--rmi-port)
            RMI_PORT="$2"
            shift 2
            ;;
        -s|--server-port)
            SERVER_PORT="$2"
            shift 2
            ;;
        -n|--service-name)
            SERVICE_NAME="$2"
            shift 2
            ;;
        -m|--memory)
            JAVA_OPTS="-Xmx$2 -Xms$(echo $2 | sed 's/[0-9]*\([a-zA-Z]*\)/512\1/')"
            shift 2
            ;;
        -d|--debug)
            DEBUG_MODE=true
            shift
            ;;
        -b|--background)
            BACKGROUND_MODE=true
            shift
            ;;
        -h|--help)
            show_usage
            exit 0
            ;;
        *)
            log_message "ERROR" "Unknown option: $1"
            show_usage
            exit 1
            ;;
    esac
done

# Main execution
log_message "INFO" "Student Attendance System Server Startup"
log_message "INFO" "Project root: $PROJECT_ROOT"
log_message "INFO" "Configuration: RMI Port=$RMI_PORT, Service Name=$SERVICE_NAME"

check_prerequisites
check_database
start_server