#!/bin/bash

# Fix all test files to use the new 11-parameter registerUser signature
# Old: registerUser(username, email, firstName, lastName, password, role, classSection)
# New: registerUser(username, email, firstName, lastName, password, role, classSection, phoneNumber, gender, photoPath, department)

echo "Fixing test files..."

# Fix RegistrationServerTest.java
sed -i 's/registerUser(\([^,]*\), \([^,]*\), \([^,]*\), \([^,]*\), \([^,]*\), UserRole\.STUDENT, \([^)]*\))/registerUser(\1, \2, \3, \4, \5, UserRole.STUDENT, \6, null, null, null, null)/g' \
    src/test/java/com/attendance/system/server/RegistrationServerTest.java

sed -i 's/registerUser(\([^,]*\), \([^,]*\), \([^,]*\), \([^,]*\), \([^,]*\), UserRole\.TEACHER, \([^)]*\))/registerUser(\1, \2, \3, \4, \5, UserRole.TEACHER, \6, null, null, null, "Computer Science")/g' \
    src/test/java/com/attendance/system/server/RegistrationServerTest.java

sed -i 's/registerUser(\([^,]*\), \([^,]*\), \([^,]*\), \([^,]*\), \([^,]*\), UserRole\.ADMIN, \([^)]*\))/registerUser(\1, \2, \3, \4, \5, UserRole.ADMIN, \6, null, null, null, null)/g' \
    src/test/java/com/attendance/system/server/RegistrationServerTest.java

# Fix RegistrationIntegrationTest.java
sed -i 's/registerUser(\([^,]*\), \([^,]*\), \([^,]*\), \([^,]*\), \([^,]*\), UserRole\.STUDENT, \([^)]*\))/registerUser(\1, \2, \3, \4, \5, UserRole.STUDENT, \6, null, null, null, null)/g' \
    src/test/java/com/attendance/system/integration/RegistrationIntegrationTest.java

sed -i 's/registerUser(\([^,]*\), \([^,]*\), \([^,]*\), \([^,]*\), \([^,]*\), UserRole\.TEACHER, \([^)]*\))/registerUser(\1, \2, \3, \4, \5, UserRole.TEACHER, \6, null, null, null, "Computer Science")/g' \
    src/test/java/com/attendance/system/integration/RegistrationIntegrationTest.java

echo "Test files fixed!"
echo "Now compiling tests..."

mvn test-compile

if [ $? -eq 0 ]; then
    echo "✓ All tests compile successfully!"
else
    echo "✗ Test compilation failed. Manual fixes may be needed."
fi
