#!/bin/bash

################################################################################
# MySQL/MariaDB Quick Installer
# Installs and configures MySQL for the Student Attendance System
################################################################################

set -e

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

print_header() {
    echo -e "${BLUE}============================================${NC}"
    echo -e "${BLUE}  MySQL Quick Installer${NC}"
    echo -e "${BLUE}============================================${NC}"
}

print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

print_error() {
    echo -e "${RED}✗ $1${NC}"
}

print_info() {
    echo -e "${BLUE}ℹ $1${NC}"
}

main() {
    print_header
    echo ""
    
    # Check if already installed
    if command -v mysql &> /dev/null; then
        print_success "MySQL client already installed"
        
        # Check if server is running
        if mysql -u root -e "SELECT 1;" &> /dev/null 2>&1; then
            print_success "MySQL server is already running!"
            echo ""
            echo "You can proceed to setup the database:"
            echo "  ./scripts/setup-database.sh"
            exit 0
        else
            print_info "MySQL client found but server not running"
            echo ""
            echo "Try starting MySQL:"
            echo "  sudo systemctl start mysql"
            echo "  OR"
            echo "  sudo systemctl start mariadb"
            exit 1
        fi
    fi
    
    # Install MySQL
    print_info "Installing MySQL Server..."
    echo ""
    
    sudo apt update
    sudo apt install -y mysql-server
    
    print_success "MySQL Server installed"
    
    # Start MySQL
    print_info "Starting MySQL service..."
    sudo systemctl start mysql
    sudo systemctl enable mysql
    
    print_success "MySQL service started"
    
    # Display success
    echo ""
    echo -e "${GREEN}========================================${NC}"
    echo -e "${GREEN}  MySQL Installation Complete!${NC}"
    echo -e "${GREEN}========================================${NC}"
    echo ""
    echo "Next Steps:"
    echo "  1. Setup database: ./scripts/setup-database.sh"
    echo "  2. Start server: ./scripts/start-server-network.sh"
    echo ""
    echo "Optional - Secure MySQL installation:"
    echo "  sudo mysql_secure_installation"
    echo ""
}

main "$@"
