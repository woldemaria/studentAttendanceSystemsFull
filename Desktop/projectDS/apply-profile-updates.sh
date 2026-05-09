#!/bin/bash

# Apply Profile Updates Script
# This script applies all database changes for the new profile fields

echo "========================================="
echo "  APPLYING PROFILE FIELD UPDATES"
echo "========================================="
echo ""

# Check if MySQL is running
if ! /opt/lampp/bin/mysql -u root -e "SELECT 1" > /dev/null 2>&1; then
    echo "❌ Error: MySQL is not running!"
    echo "Please start XAMPP MySQL first."
    exit 1
fi

echo "✅ MySQL is running"
echo ""

# Apply database changes
echo "📊 Applying database schema updates..."
/opt/lampp/bin/mysql -u root < scripts/database/add-profile-fields.sql

if [ $? -eq 0 ]; then
    echo "✅ Database schema updated successfully!"
else
    echo "❌ Error: Failed to update database schema"
    exit 1
fi

echo ""
echo "========================================="
echo "  CHANGES APPLIED SUCCESSFULLY"
echo "========================================="
echo ""
echo "New fields added to USERS table:"
echo "  - phone_number VARCHAR(20)"
echo "  - gender ENUM('MALE', 'FEMALE', 'OTHER')"
echo "  - photo_path VARCHAR(255)"
echo ""
echo "Updated STUDENTS table:"
echo "  - class_section VARCHAR(1) DEFAULT 'A'"
echo ""
echo "========================================="
echo "  NEXT STEPS"
echo "========================================="
echo ""
echo "1. Compile the updated code:"
echo "   mvn clean compile -DskipTests"
echo ""
echo "2. Restart the server:"
echo "   ./run.sh server"
echo ""
echo "3. Test the new registration form with:"
echo "   - Phone number field"
echo "   - Gender selection"
echo "   - Department field (for teachers)"
echo "   - Profile image upload"
echo ""
echo "4. Test login with email instead of username"
echo ""
echo "✅ All updates applied successfully!"
