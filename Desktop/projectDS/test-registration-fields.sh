#!/bin/bash

# Test Registration Fields - Verification Script
# This script helps verify that all new registration fields are working

echo "=========================================="
echo "Registration Fields Verification"
echo "=========================================="
echo ""

# Check if uploads directory exists
echo "1. Checking uploads directory..."
if [ -d "uploads/profile_images" ]; then
    echo "   ✅ uploads/profile_images directory exists"
    ls -la uploads/profile_images/ | head -5
else
    echo "   ❌ uploads/profile_images directory NOT found"
    echo "   Creating directory..."
    mkdir -p uploads/profile_images
    chmod 755 uploads/profile_images
    echo "   ✅ Directory created"
fi
echo ""

# Check if code compiles
echo "2. Checking code compilation..."
mvn compile -DskipTests -q
if [ $? -eq 0 ]; then
    echo "   ✅ Code compiles successfully"
else
    echo "   ❌ Compilation failed"
    exit 1
fi
echo ""

# Check if database has new columns
echo "3. Checking database schema..."
mysql -u root -p -e "USE Wolde; DESCRIBE USERS;" 2>/dev/null | grep -E "phone_number|gender|photo_path"
if [ $? -eq 0 ]; then
    echo "   ✅ Database has new columns"
else
    echo "   ⚠️  Database columns not found or MySQL not accessible"
    echo "   Run: mysql -u root -p Wolde < scripts/database/add-profile-fields.sql"
fi
echo ""

# Check if RegistrationFrame has new fields
echo "4. Checking RegistrationFrame.java..."
if grep -q "phoneNumberField" src/main/java/com/attendance/system/client/RegistrationFrame.java; then
    echo "   ✅ phoneNumberField found"
else
    echo "   ❌ phoneNumberField NOT found"
fi

if grep -q "genderComboBox" src/main/java/com/attendance/system/client/RegistrationFrame.java; then
    echo "   ✅ genderComboBox found"
else
    echo "   ❌ genderComboBox NOT found"
fi

if grep -q "departmentField" src/main/java/com/attendance/system/client/RegistrationFrame.java; then
    echo "   ✅ departmentField found"
else
    echo "   ❌ departmentField NOT found"
fi

if grep -q "uploadPhotoButton" src/main/java/com/attendance/system/client/RegistrationFrame.java; then
    echo "   ✅ uploadPhotoButton found"
else
    echo "   ❌ uploadPhotoButton NOT found"
fi
echo ""

# Check if AttendanceService has updated signature
echo "5. Checking AttendanceService.java..."
if grep -q "String phoneNumber" src/main/java/com/attendance/system/service/AttendanceService.java; then
    echo "   ✅ registerUser() has phoneNumber parameter"
else
    echo "   ❌ registerUser() missing phoneNumber parameter"
fi

if grep -q "String gender" src/main/java/com/attendance/system/service/AttendanceService.java; then
    echo "   ✅ registerUser() has gender parameter"
else
    echo "   ❌ registerUser() missing gender parameter"
fi

if grep -q "String photoPath" src/main/java/com/attendance/system/service/AttendanceService.java; then
    echo "   ✅ registerUser() has photoPath parameter"
else
    echo "   ❌ registerUser() missing photoPath parameter"
fi

if grep -q "String department" src/main/java/com/attendance/system/service/AttendanceService.java; then
    echo "   ✅ registerUser() has department parameter"
else
    echo "   ❌ registerUser() missing department parameter"
fi
echo ""

# Check if AttendanceServer has updated implementation
echo "6. Checking AttendanceServer.java..."
if grep -q "setPhoneNumber" src/main/java/com/attendance/system/server/AttendanceServer.java; then
    echo "   ✅ Server sets phone number"
else
    echo "   ❌ Server doesn't set phone number"
fi

if grep -q "setGender" src/main/java/com/attendance/system/server/AttendanceServer.java; then
    echo "   ✅ Server sets gender"
else
    echo "   ❌ Server doesn't set gender"
fi

if grep -q "setPhotoPath" src/main/java/com/attendance/system/server/AttendanceServer.java; then
    echo "   ✅ Server sets photo path"
else
    echo "   ❌ Server doesn't set photo path"
fi
echo ""

# Summary
echo "=========================================="
echo "Verification Complete!"
echo "=========================================="
echo ""
echo "Next Steps:"
echo "1. Start server: ./run.sh server"
echo "2. Start client: ./run.sh client"
echo "3. Click 'Register' button"
echo "4. Test student registration with all fields"
echo "5. Test teacher registration with department"
echo ""
echo "Expected Results:"
echo "- Phone number field visible (optional)"
echo "- Gender dropdown visible (Male/Female/Other)"
echo "- Department field visible for teachers only"
echo "- Profile photo upload button visible"
echo "- Class section visible for students only"
echo "- All fields save to database"
echo ""
echo "Documentation:"
echo "- REGISTRATION_FORM_COMPLETE.md"
echo "- PROFILE_FIELDS_COMPLETE_SUMMARY.md"
echo ""
