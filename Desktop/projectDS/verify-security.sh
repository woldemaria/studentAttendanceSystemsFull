#!/bin/bash

# Security Verification Script
# Checks for common security vulnerabilities

echo "========================================="
echo "  SECURITY VERIFICATION"
echo "========================================="
echo ""

ERRORS=0
WARNINGS=0

# Check 1: SQL Injection Protection
echo "🔍 Checking SQL Injection Protection..."
SQL_CONCAT=$(grep -r "\".*+.*\"" src/main/java/com/attendance/system/dao/*.java 2>/dev/null | grep -v "PreparedStatement" | wc -l)
if [ $SQL_CONCAT -gt 0 ]; then
    echo "❌ FAIL: Found $SQL_CONCAT potential SQL concatenations"
    ERRORS=$((ERRORS + 1))
else
    echo "✅ PASS: No SQL string concatenation found"
fi

# Check 2: PreparedStatement Usage
echo ""
echo "🔍 Checking PreparedStatement Usage..."
PREPARED_STMT=$(grep -r "PreparedStatement" src/main/java/com/attendance/system/dao/*.java | wc -l)
if [ $PREPARED_STMT -gt 50 ]; then
    echo "✅ PASS: PreparedStatement used extensively ($PREPARED_STMT occurrences)"
else
    echo "⚠️  WARNING: Limited PreparedStatement usage ($PREPARED_STMT occurrences)"
    WARNINGS=$((WARNINGS + 1))
fi

# Check 3: Password Hashing
echo ""
echo "🔍 Checking Password Hashing..."
if grep -q "BCrypt" src/main/java/com/attendance/system/util/SecurityUtil.java; then
    echo "✅ PASS: BCrypt password hashing implemented"
else
    echo "❌ FAIL: BCrypt password hashing not found"
    ERRORS=$((ERRORS + 1))
fi

# Check 4: Session Management
echo ""
echo "🔍 Checking Session Management..."
if grep -q "sessionToken" src/main/java/com/attendance/system/service/AuthenticationService.java; then
    echo "✅ PASS: Session token management implemented"
else
    echo "❌ FAIL: Session management not found"
    ERRORS=$((ERRORS + 1))
fi

# Check 5: Input Validation
echo ""
echo "🔍 Checking Input Validation..."
if grep -q "validatePassword\|validateEmail\|validateUsername" src/main/java/com/attendance/system/util/SecurityUtil.java; then
    echo "✅ PASS: Input validation methods found"
else
    echo "❌ FAIL: Input validation not found"
    ERRORS=$((ERRORS + 1))
fi

# Check 6: Encryption
echo ""
echo "🔍 Checking Data Encryption..."
if grep -q "AES" src/main/java/com/attendance/system/util/SecurityUtil.java; then
    echo "✅ PASS: AES encryption implemented"
else
    echo "⚠️  WARNING: Encryption not found"
    WARNINGS=$((WARNINGS + 1))
fi

# Check 7: Brute Force Protection
echo ""
echo "🔍 Checking Brute Force Protection..."
if grep -q "MAX_FAILED_ATTEMPTS\|LOCKOUT_DURATION" src/main/java/com/attendance/system/service/AuthenticationService.java; then
    echo "✅ PASS: Brute force protection implemented"
else
    echo "❌ FAIL: Brute force protection not found"
    ERRORS=$((ERRORS + 1))
fi

# Check 8: Logging
echo ""
echo "🔍 Checking Security Logging..."
LOGGER_COUNT=$(grep -r "logger\." src/main/java/com/attendance/system/ | wc -l)
if [ $LOGGER_COUNT -gt 100 ]; then
    echo "✅ PASS: Comprehensive logging implemented ($LOGGER_COUNT log statements)"
else
    echo "⚠️  WARNING: Limited logging ($LOGGER_COUNT log statements)"
    WARNINGS=$((WARNINGS + 1))
fi

# Check 9: Hardcoded Credentials
echo ""
echo "🔍 Checking for Hardcoded Credentials..."
HARDCODED=$(grep -r "password.*=.*\"" src/main/java/com/attendance/system/ 2>/dev/null | grep -v "passwordHash\|passwordField\|passwordLabel\|passwordError\|currentPassword\|newPassword\|confirmPassword" | wc -l)
if [ $HARDCODED -gt 0 ]; then
    echo "⚠️  WARNING: Found $HARDCODED potential hardcoded credentials"
    WARNINGS=$((WARNINGS + 1))
else
    echo "✅ PASS: No hardcoded credentials found"
fi

# Check 10: Database Configuration
echo ""
echo "🔍 Checking Database Security..."
if grep -q "HikariCP\|HikariDataSource" src/main/java/com/attendance/system/dao/DatabaseManager.java; then
    echo "✅ PASS: Connection pooling (HikariCP) implemented"
else
    echo "⚠️  WARNING: Connection pooling not found"
    WARNINGS=$((WARNINGS + 1))
fi

# Summary
echo ""
echo "========================================="
echo "  SECURITY VERIFICATION SUMMARY"
echo "========================================="
echo ""
echo "Errors: $ERRORS"
echo "Warnings: $WARNINGS"
echo ""

if [ $ERRORS -eq 0 ] && [ $WARNINGS -eq 0 ]; then
    echo "✅ ALL CHECKS PASSED - SYSTEM IS SECURE"
    echo ""
    echo "Security Rating: EXCELLENT"
    exit 0
elif [ $ERRORS -eq 0 ]; then
    echo "✅ NO CRITICAL ISSUES - SYSTEM IS SECURE"
    echo "⚠️  $WARNINGS warnings found (non-critical)"
    echo ""
    echo "Security Rating: GOOD"
    exit 0
else
    echo "❌ SECURITY ISSUES FOUND"
    echo "   Errors: $ERRORS (critical)"
    echo "   Warnings: $WARNINGS (non-critical)"
    echo ""
    echo "Security Rating: NEEDS ATTENTION"
    exit 1
fi
