#!/bin/bash

################################################################################
# Student Attendance System - Database Setup Script
# This script sets up the MySQL database for the attendance system
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
SCHEMA_FILE="$PROJECT_DIR/src/main/resources/schema.sql"
DB_NAME="Wolde"
DB_USER="root"
DB_PASS=""

################################################################################
# Functions
################################################################################

print_header() {
    echo -e "${BLUE}============================================${NC}"
    echo -e "${BLUE}  Database Setup - Attendance System${NC}"
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

# Check if MySQL is installed
check_mysql_installed() {
    if ! command -v mysql &> /dev/null; then
        print_error "MySQL client is not installed!"
        echo ""
        echo "Please install MySQL or MariaDB:"
        echo "  Ubuntu/Debian: sudo apt install mysql-server"
        echo "  Or MariaDB: sudo apt install mariadb-server"
        exit 1
    fi
    print_success "MySQL client found"
}

# Check if MySQL server is running
check_mysql_running() {
    if ! mysql -u "$DB_USER" -e "SELECT 1;" &> /dev/null; then
        print_error "Cannot connect to MySQL server!"
        echo ""
        echo "MySQL server might not be running. Try:"
        echo "  sudo systemctl start mysql"
        echo "  OR"
        echo "  sudo systemctl start mariadb"
        exit 1
    fi
    print_success "MySQL server is running"
}

# Create database
create_database() {
    print_info "Creating database: $DB_NAME"
    
    # Check if database already exists
    if mysql -u "$DB_USER" -e "USE $DB_NAME;" &> /dev/null; then
        print_warning "Database '$DB_NAME' already exists"
        read -p "Drop and recreate? (y/n): " -n 1 -r
        echo
        if [[ $REPLY =~ ^[Yy]$ ]]; then
            mysql -u "$DB_USER" -e "DROP DATABASE $DB_NAME;"
            print_info "Dropped existing database"
        else
            print_info "Keeping existing database"
            return 0
        fi
    fi
    
    # Create database
    mysql -u "$DB_USER" -e "CREATE DATABASE $DB_NAME CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
    print_success "Database created: $DB_NAME"
}

# Load schema
load_schema() {
    if [ ! -f "$SCHEMA_FILE" ]; then
        print_error "Schema file not found: $SCHEMA_FILE"
        exit 1
    fi
    
    print_info "Loading database schema..."
    mysql -u "$DB_USER" "$DB_NAME" < "$SCHEMA_FILE"
    print_success "Schema loaded successfully"
}

# Verify tables
verify_tables() {
    print_info "Verifying database tables..."
    
    local tables=$(mysql -u "$DB_USER" "$DB_NAME" -e "SHOW TABLES;" -s)
    local table_count=$(echo "$tables" | wc -l)
    
    if [ "$table_count" -gt 0 ]; then
        print_success "Found $table_count tables:"
        echo "$tables" | while read table; do
            echo "  - $table"
        done
    else
        print_error "No tables found in database!"
        exit 1
    fi
}

# Create admin user (optional)
create_admin_user() {
    echo ""
    read -p "Create default admin user? (username: admin, password: admin) (y/n): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        print_info "Creating admin user..."
        
        # Check if admin already exists
        local admin_exists=$(mysql -u "$DB_USER" "$DB_NAME" -se "SELECT COUNT(*) FROM users WHERE username='admin';")
        
        if [ "$admin_exists" -gt 0 ]; then
            print_warning "Admin user already exists"
        else
            # Use Java to create admin with proper password hashing
            cd "$PROJECT_DIR"
            if [ -f "CreateAdminUser.class" ]; then
                java -cp ".:target/classes:target/student-attendance-system-1.0.0.jar:target/lib/*" CreateAdminUser
                print_success "Admin user created (username: admin, password: admin)"
            else
                print_warning "CreateAdminUser.class not found, skipping admin creation"
                echo "You can create admin manually later using CreateAdminUser.java"
            fi
        fi
    fi
}

# Display connection info
display_info() {
    echo ""
    echo -e "${GREEN}========================================${NC}"
    echo -e "${GREEN}  Database Setup Complete!${NC}"
    echo -e "${GREEN}========================================${NC}"
    echo ""
    echo "Database Configuration:"
    echo "  Database: $DB_NAME"
    echo "  Host: localhost"
    echo "  Port: 3306"
    echo "  Username: $DB_USER"
    echo "  Password: (empty)"
    echo ""
    echo "Next Steps:"
    echo "  1. Start the server: ./scripts/start-server-network.sh"
    echo "  2. Connect clients: ./scripts/start-client-network.sh <SERVER_IP>"
    echo ""
}

################################################################################
# Main Script
################################################################################

main() {
    print_header
    echo ""
    
    # Run checks
    print_info "Running pre-flight checks..."
    check_mysql_installed
    check_mysql_running
    
    echo ""
    
    # Setup database
    create_database
    load_schema
    verify_tables
    
    # Optional: Create admin user
    create_admin_user
    
    # Display info
    display_info
}

# Run main function
main "$@"
