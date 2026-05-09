#!/bin/bash

# Create Default Users Script
# This script creates the default users: admin, teacher1, student1

echo "=========================================="
echo "  Creating Default Users"
echo "=========================================="
echo ""

# Compile and run the CreateUsers utility
javac -cp "target/classes:$(mvn dependency:build-classpath -q -Dmdep.outputFile=/dev/stdout)" CreateUsers.java

if [ $? -eq 0 ]; then
    java -cp ".:target/classes:$(mvn dependency:build-classpath -q -Dmdep.outputFile=/dev/stdout)" CreateUsers
    
    if [ $? -eq 0 ]; then
        echo ""
        echo "=========================================="
        echo "  ✓ Users Created Successfully!"
        echo "=========================================="
        echo ""
        echo "You can now login with:"
        echo "  Admin:   admin / Admin@123"
        echo "  Teacher: teacher1 / Teacher@123"
        echo "  Student: student1 / Student@123"
        echo ""
    else
        echo ""
        echo "✗ Failed to create users"
        exit 1
    fi
else
    echo ""
    echo "✗ Failed to compile CreateUsers.java"
    exit 1
fi
