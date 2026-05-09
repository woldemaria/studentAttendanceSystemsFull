#!/bin/bash

################################################################################
# Network Setup Script
# Prepares the system for client-server deployment
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
    echo -e "${BLUE}  Network Setup for Attendance System${NC}"
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

# Detect OS
detect_os() {
    if [ -f /etc/os-release ]; then
        . /etc/os-release
        OS=$ID
        VER=$VERSION_ID
    elif type lsb_release >/dev/null 2>&1; then
        OS=$(lsb_release -si | tr '[:upper:]' '[:lower:]')
        VER=$(lsb_release -sr)
    else
        OS=$(uname -s | tr '[:upper:]' '[:lower:]')
        VER=$(uname -r)
    fi
    
    echo "Detected OS: $OS $VER"
}

# Install Java
install_java() {
    print_info "Checking Java installation..."
    
    if command -v java &> /dev/null; then
        print_success "Java is already installed"
        java -version
        return 0
    fi
    
    print_info "Installing Java..."
    
    case $OS in
        ubuntu|debian)
            sudo apt update
            sudo apt install -y openjdk-17-jdk maven
            ;;
        centos|rhel|fedora)
            sudo yum install -y java-17-openjdk java-17-openjdk-devel maven
            ;;
        *)
            print_error "Unsupported OS: $OS"
            echo "Please install Java 17 manually"
            exit 1
            ;;
    esac
    
    print_success "Java installed successfully"
}

# Install MySQL (server only)
install_mysql() {
    read -p "Is this the SERVER computer? (y/n): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        print_info "Skipping MySQL installation (client only)"
        return 0
    fi
    
    print_info "Checking MySQL installation..."
    
    if command -v mysql &> /dev/null; then
        print_success "MySQL is already installed"
        return 0
    fi
    
    print_info "Installing MySQL..."
    
    case $OS in
        ubuntu|debian)
            sudo apt update
            sudo apt install -y mysql-server
            sudo systemctl start mysql
            sudo systemctl enable mysql
            ;;
        centos|rhel|fedora)
            sudo yum install -y mysql-server
            sudo systemctl start mysqld
            sudo systemctl enable mysqld
            ;;
        *)
            print_error "Unsupported OS: $OS"
            echo "Please install MySQL manually"
            exit 1
            ;;
    esac
    
    print_success "MySQL installed successfully"
    
    # Secure MySQL installation
    print_info "Securing MySQL installation..."
    echo "Please run: sudo mysql_secure_installation"
}

# Configure firewall
configure_firewall() {
    print_info "Configuring firewall..."
    
    # UFW (Ubuntu/Debian)
    if command -v ufw &> /dev/null; then
        if sudo ufw status | grep -q "Status: active"; then
            sudo ufw allow 1099/tcp comment "RMI Server"
            print_success "Firewall configured (UFW)"
        fi
    fi
    
    # firewalld (CentOS/RHEL)
    if command -v firewall-cmd &> /dev/null; then
        if sudo firewall-cmd --state 2>/dev/null | grep -q "running"; then
            sudo firewall-cmd --permanent --add-port=1099/tcp
            sudo firewall-cmd --reload
            print_success "Firewall configured (firewalld)"
        fi
    fi
}

# Build project
build_project() {
    print_info "Building project..."
    
    PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
    cd "$PROJECT_DIR"
    
    if [ -f pom.xml ]; then
        mvn clean package -DskipTests
        print_success "Project built successfully"
    else
        print_error "pom.xml not found. Are you in the project directory?"
        exit 1
    fi
}

# Make scripts executable
setup_scripts() {
    print_info "Setting up scripts..."
    
    PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
    
    chmod +x "$PROJECT_DIR/scripts/start-server-network.sh"
    chmod +x "$PROJECT_DIR/scripts/start-client-network.sh"
    chmod +x "$PROJECT_DIR/scripts/setup-network.sh"
    
    print_success "Scripts are now executable"
}

# Show network info
show_network_info() {
    echo ""
    echo -e "${YELLOW}========================================${NC}"
    echo -e "${YELLOW}  Network Information${NC}"
    echo -e "${YELLOW}========================================${NC}"
    echo ""
    
    # Get IP addresses
    if command -v hostname &> /dev/null; then
        echo "IP Addresses:"
        hostname -I | tr ' ' '\n' | grep -v '^$' | while read ip; do
            echo "  - $ip"
        done
    fi
    
    echo ""
    echo "RMI Port: 1099"
    echo ""
}

# Main setup
main() {
    print_header
    echo ""
    
    detect_os
    echo ""
    
    install_java
    echo ""
    
    install_mysql
    echo ""
    
    configure_firewall
    echo ""
    
    build_project
    echo ""
    
    setup_scripts
    echo ""
    
    show_network_info
    
    echo -e "${GREEN}========================================${NC}"
    echo -e "${GREEN}  Setup Complete!${NC}"
    echo -e "${GREEN}========================================${NC}"
    echo ""
    echo "Next steps:"
    echo ""
    echo "For SERVER computer:"
    echo "  1. Setup database:"
    echo "     mysql -u root -p < scripts/database/setup-database.sql"
    echo "  2. Start server:"
    echo "     ./scripts/start-server-network.sh"
    echo ""
    echo "For CLIENT computer:"
    echo "  1. Get server IP from server computer"
    echo "  2. Start client:"
    echo "     ./scripts/start-client-network.sh <server-ip>"
    echo ""
}

main "$@"
