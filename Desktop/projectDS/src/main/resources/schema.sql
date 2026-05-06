-- Student Attendance System Database Schema
-- MySQL Database Schema Creation Script

-- Create database if not exists
CREATE DATABASE IF NOT EXISTS attendance_system;
USE attendance_system;

-- Users table (base table for all user types)
CREATE TABLE IF NOT EXISTS USERS (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    role ENUM('ADMIN', 'TEACHER', 'STUDENT') NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_role (role)
);

-- Students table (extends Users)
CREATE TABLE IF NOT EXISTS STUDENTS (
    student_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT UNIQUE NOT NULL,
    student_number VARCHAR(20) UNIQUE NOT NULL,
    program VARCHAR(100) NOT NULL,
    year_level INT NOT NULL CHECK (year_level BETWEEN 1 AND 4),
    enrollment_date DATE NOT NULL,
    FOREIGN KEY (user_id) REFERENCES USERS(user_id) ON DELETE CASCADE,
    INDEX idx_student_number (student_number),
    INDEX idx_program (program)
);

-- Teachers table (extends Users)
CREATE TABLE IF NOT EXISTS TEACHERS (
    teacher_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT UNIQUE NOT NULL,
    employee_id VARCHAR(20) UNIQUE NOT NULL,
    department VARCHAR(100) NOT NULL,
    specialization VARCHAR(100),
    FOREIGN KEY (user_id) REFERENCES USERS(user_id) ON DELETE CASCADE,
    INDEX idx_employee_id (employee_id),
    INDEX idx_department (department)
);

-- Courses table
CREATE TABLE IF NOT EXISTS COURSES (
    course_id INT AUTO_INCREMENT PRIMARY KEY,
    course_code VARCHAR(20) UNIQUE NOT NULL,
    course_name VARCHAR(100) NOT NULL,
    description TEXT,
    credits INT NOT NULL CHECK (credits > 0),
    teacher_id INT NOT NULL,
    semester VARCHAR(20) NOT NULL,
    academic_year VARCHAR(10) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (teacher_id) REFERENCES TEACHERS(teacher_id) ON DELETE RESTRICT,
    INDEX idx_course_code (course_code),
    INDEX idx_teacher_id (teacher_id),
    INDEX idx_semester (semester, academic_year)
);

-- Enrollments table (many-to-many relationship between Students and Courses)
CREATE TABLE IF NOT EXISTS ENROLLMENTS (
    enrollment_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    course_id INT NOT NULL,
    enrollment_date DATE NOT NULL,
    status ENUM('ENROLLED', 'DROPPED', 'COMPLETED') DEFAULT 'ENROLLED',
    grade VARCHAR(5),
    FOREIGN KEY (student_id) REFERENCES STUDENTS(student_id) ON DELETE CASCADE,
    FOREIGN KEY (course_id) REFERENCES COURSES(course_id) ON DELETE CASCADE,
    UNIQUE KEY unique_enrollment (student_id, course_id),
    INDEX idx_student_course (student_id, course_id),
    INDEX idx_enrollment_date (enrollment_date)
);

-- Attendance Records table
CREATE TABLE IF NOT EXISTS ATTENDANCE_RECORDS (
    attendance_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    course_id INT NOT NULL,
    attendance_date DATE NOT NULL,
    class_time TIME NOT NULL,
    status ENUM('PRESENT', 'ABSENT', 'LATE', 'EXCUSED') NOT NULL,
    marked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    marked_by INT NOT NULL,
    remarks TEXT,
    FOREIGN KEY (student_id) REFERENCES STUDENTS(student_id) ON DELETE CASCADE,
    FOREIGN KEY (course_id) REFERENCES COURSES(course_id) ON DELETE CASCADE,
    FOREIGN KEY (marked_by) REFERENCES USERS(user_id) ON DELETE RESTRICT,
    UNIQUE KEY unique_attendance (student_id, course_id, attendance_date, class_time),
    INDEX idx_student_date (student_id, attendance_date),
    INDEX idx_course_date (course_id, attendance_date),
    INDEX idx_marked_by (marked_by)
);

-- Notifications table
CREATE TABLE IF NOT EXISTS NOTIFICATIONS (
    notification_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    title VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    type ENUM('ATTENDANCE_WARNING', 'SYSTEM_NOTIFICATION', 'COURSE_UPDATE', 'REMINDER') NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    read_at TIMESTAMP NULL,
    FOREIGN KEY (user_id) REFERENCES USERS(user_id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_type (type),
    INDEX idx_created_at (created_at),
    INDEX idx_unread (user_id, is_read)
);

-- Audit Log table for security and compliance
CREATE TABLE IF NOT EXISTS AUDIT_LOG (
    log_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    action VARCHAR(100) NOT NULL,
    table_name VARCHAR(50),
    record_id INT,
    old_values JSON,
    new_values JSON,
    ip_address VARCHAR(45),
    user_agent TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES USERS(user_id) ON DELETE SET NULL,
    INDEX idx_user_action (user_id, action),
    INDEX idx_table_record (table_name, record_id),
    INDEX idx_created_at (created_at)
);

-- System Configuration table
CREATE TABLE IF NOT EXISTS SYSTEM_CONFIG (
    config_id INT AUTO_INCREMENT PRIMARY KEY,
    config_key VARCHAR(100) UNIQUE NOT NULL,
    config_value TEXT NOT NULL,
    description TEXT,
    updated_by INT,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (updated_by) REFERENCES USERS(user_id) ON DELETE SET NULL,
    INDEX idx_config_key (config_key)
);

-- Insert default system configuration
INSERT INTO SYSTEM_CONFIG (config_key, config_value, description) VALUES
('session.timeout', '1800', 'Session timeout in seconds (30 minutes)'),
('attendance.modification.window', '24', 'Hours within which attendance can be modified'),
('attendance.warning.threshold', '75', 'Attendance percentage below which warnings are sent'),
('backup.schedule', '0 2 * * *', 'Cron expression for daily backup schedule'),
('notification.batch.size', '100', 'Number of notifications to process in each batch'),
('system.maintenance.mode', 'false', 'Whether system is in maintenance mode');

-- Create default admin user (password: admin123)
-- Password hash for 'admin123' using BCrypt
INSERT INTO USERS (username, password_hash, email, first_name, last_name, role) VALUES
('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMye.Uo0ePPO4tyh/OpGrrabJefPCt/Nu/G', 'admin@attendance.system', 'System', 'Administrator', 'ADMIN');

-- Create views for common queries
CREATE VIEW student_attendance_summary AS
SELECT 
    s.student_id,
    u.first_name,
    u.last_name,
    s.student_number,
    c.course_code,
    c.course_name,
    COUNT(ar.attendance_id) as total_classes,
    SUM(CASE WHEN ar.status IN ('PRESENT', 'LATE') THEN 1 ELSE 0 END) as attended_classes,
    ROUND((SUM(CASE WHEN ar.status IN ('PRESENT', 'LATE') THEN 1 ELSE 0 END) / COUNT(ar.attendance_id)) * 100, 2) as attendance_percentage
FROM STUDENTS s
JOIN USERS u ON s.user_id = u.user_id
JOIN ENROLLMENTS e ON s.student_id = e.student_id
JOIN COURSES c ON e.course_id = c.course_id
LEFT JOIN ATTENDANCE_RECORDS ar ON s.student_id = ar.student_id AND c.course_id = ar.course_id
WHERE e.status = 'ENROLLED'
GROUP BY s.student_id, c.course_id;

-- Create triggers for audit logging
DELIMITER //

CREATE TRIGGER users_audit_insert AFTER INSERT ON USERS
FOR EACH ROW
BEGIN
    INSERT INTO AUDIT_LOG (user_id, action, table_name, record_id, new_values)
    VALUES (NEW.user_id, 'INSERT', 'USERS', NEW.user_id, JSON_OBJECT(
        'username', NEW.username,
        'email', NEW.email,
        'first_name', NEW.first_name,
        'last_name', NEW.last_name,
        'role', NEW.role,
        'is_active', NEW.is_active
    ));
END//

CREATE TRIGGER users_audit_update AFTER UPDATE ON USERS
FOR EACH ROW
BEGIN
    INSERT INTO AUDIT_LOG (user_id, action, table_name, record_id, old_values, new_values)
    VALUES (NEW.user_id, 'UPDATE', 'USERS', NEW.user_id, 
        JSON_OBJECT(
            'username', OLD.username,
            'email', OLD.email,
            'first_name', OLD.first_name,
            'last_name', OLD.last_name,
            'role', OLD.role,
            'is_active', OLD.is_active
        ),
        JSON_OBJECT(
            'username', NEW.username,
            'email', NEW.email,
            'first_name', NEW.first_name,
            'last_name', NEW.last_name,
            'role', NEW.role,
            'is_active', NEW.is_active
        )
    );
END//

CREATE TRIGGER attendance_audit_insert AFTER INSERT ON ATTENDANCE_RECORDS
FOR EACH ROW
BEGIN
    INSERT INTO AUDIT_LOG (user_id, action, table_name, record_id, new_values)
    VALUES (NEW.marked_by, 'INSERT', 'ATTENDANCE_RECORDS', NEW.attendance_id, JSON_OBJECT(
        'student_id', NEW.student_id,
        'course_id', NEW.course_id,
        'attendance_date', NEW.attendance_date,
        'status', NEW.status,
        'remarks', NEW.remarks
    ));
END//

DELIMITER ;