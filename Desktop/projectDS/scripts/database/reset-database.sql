-- Reset Database Script for Student Attendance System
-- This script clears all data and resets the database to initial state
-- WARNING: This will delete ALL data in the database

USE attendance_system;

-- Disable foreign key checks temporarily
SET FOREIGN_KEY_CHECKS = 0;

-- Clear all data from tables (in reverse dependency order)
DELETE FROM AUDIT_LOG;
DELETE FROM NOTIFICATIONS;
DELETE FROM ATTENDANCE_RECORDS;
DELETE FROM ENROLLMENTS;
DELETE FROM COURSES;
DELETE FROM STUDENTS;
DELETE FROM TEACHERS;
DELETE FROM USERS;
DELETE FROM SYSTEM_CONFIG;

-- Reset auto-increment counters
ALTER TABLE AUDIT_LOG AUTO_INCREMENT = 1;
ALTER TABLE NOTIFICATIONS AUTO_INCREMENT = 1;
ALTER TABLE ATTENDANCE_RECORDS AUTO_INCREMENT = 1;
ALTER TABLE ENROLLMENTS AUTO_INCREMENT = 1;
ALTER TABLE COURSES AUTO_INCREMENT = 1;
ALTER TABLE STUDENTS AUTO_INCREMENT = 1;
ALTER TABLE TEACHERS AUTO_INCREMENT = 1;
ALTER TABLE USERS AUTO_INCREMENT = 1;
ALTER TABLE SYSTEM_CONFIG AUTO_INCREMENT = 1;

-- Re-enable foreign key checks
SET FOREIGN_KEY_CHECKS = 1;

-- Re-insert default system configuration
INSERT INTO SYSTEM_CONFIG (config_key, config_value, description) VALUES
('session.timeout', '1800', 'Session timeout in seconds (30 minutes)'),
('attendance.modification.window', '24', 'Hours within which attendance can be modified'),
('attendance.warning.threshold', '75', 'Attendance percentage below which warnings are sent'),
('backup.schedule', '0 2 * * *', 'Cron expression for daily backup schedule'),
('notification.batch.size', '100', 'Number of notifications to process in each batch'),
('system.maintenance.mode', 'false', 'Whether system is in maintenance mode');

-- Re-create default admin user (password: admin123)
INSERT INTO USERS (username, password_hash, email, first_name, last_name, role) VALUES
('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMye.Uo0ePPO4tyh/OpGrrabJefPCt/Nu/G', 'admin@attendance.system', 'System', 'Administrator', 'ADMIN');

-- Display reset completion message
SELECT 'Database reset completed successfully!' AS Status;
SELECT 'All data has been cleared and default admin user recreated.' AS Info;
SELECT 'Default admin credentials: username=admin, password=admin123' AS Credentials;