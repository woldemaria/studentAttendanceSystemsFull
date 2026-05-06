-- Student Attendance System Database Setup Script
-- This script creates the database, user, and initial schema
-- Run this script as MySQL root user to set up the system

-- Create database and user
CREATE DATABASE IF NOT EXISTS attendance_system 
    CHARACTER SET utf8mb4 
    COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS attendance_system_test 
    CHARACTER SET utf8mb4 
    COLLATE utf8mb4_unicode_ci;

-- Create dedicated user for the application
CREATE USER IF NOT EXISTS 'attendance_user'@'localhost' IDENTIFIED BY 'attendance_pass';
CREATE USER IF NOT EXISTS 'attendance_user'@'%' IDENTIFIED BY 'attendance_pass';

-- Grant permissions
GRANT ALL PRIVILEGES ON attendance_system.* TO 'attendance_user'@'localhost';
GRANT ALL PRIVILEGES ON attendance_system.* TO 'attendance_user'@'%';
GRANT ALL PRIVILEGES ON attendance_system_test.* TO 'attendance_user'@'localhost';
GRANT ALL PRIVILEGES ON attendance_system_test.* TO 'attendance_user'@'%';

FLUSH PRIVILEGES;

-- Use the main database
USE attendance_system;

-- Source the main schema file
SOURCE schema.sql;

-- Use the test database and create schema there too
USE attendance_system_test;
SOURCE schema.sql;

-- Display setup completion message
SELECT 'Database setup completed successfully!' AS Status;
SELECT 'Main database: attendance_system' AS Info;
SELECT 'Test database: attendance_system_test' AS Info;
SELECT 'Database user: attendance_user' AS Info;