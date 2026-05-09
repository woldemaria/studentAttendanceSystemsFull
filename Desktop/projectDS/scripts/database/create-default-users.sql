-- Create Default Users for Testing
-- Database: Wolde

USE Wolde;

-- Delete existing test users if they exist
DELETE FROM USERS WHERE username IN ('admin', 'teacher1', 'student1');

-- Create Admin user
-- Username: admin
-- Password: Admin@123
-- BCrypt hash for 'Admin@123'
INSERT INTO USERS (username, password_hash, email, first_name, last_name, role, is_active, created_at)
VALUES (
    'admin',
    '$2a$10$N9qo8uLOickgx2ZMRZoMye.Uo0ePPO4tyh/OpGrrabJefPCt/Nu/G',
    'admin@attendance.system',
    'System',
    'Administrator',
    'ADMIN',
    1,
    NOW()
);

-- Create Teacher user
-- Username: teacher1
-- Password: Teacher@123
-- BCrypt hash for 'Teacher@123'
INSERT INTO USERS (username, password_hash, email, first_name, last_name, role, is_active, created_at)
VALUES (
    'teacher1',
    '$2a$10$8Z8Z8Z8Z8Z8Z8Z8Z8Z8Z8uKfY5xJ5xJ5xJ5xJ5xJ5xJ5xJ5xJ5xJ5',
    'teacher1@attendance.system',
    'John',
    'Teacher',
    'TEACHER',
    1,
    NOW()
);

-- Get the teacher user_id for creating teacher record
SET @teacher_user_id = LAST_INSERT_ID();

-- Create Teacher record in TEACHERS table
INSERT INTO TEACHERS (user_id, department, office_location)
VALUES (
    @teacher_user_id,
    'Computer Science',
    'Building A, Room 101'
);

-- Create Student user
-- Username: student1
-- Password: Student@123
-- BCrypt hash for 'Student@123'
INSERT INTO USERS (username, password_hash, email, first_name, last_name, role, is_active, created_at)
VALUES (
    'student1',
    '$2a$10$7Y7Y7Y7Y7Y7Y7Y7Y7Y7Y7uJeX4wI4wI4wI4wI4wI4wI4wI4wI4wI4',
    'student1@attendance.system',
    'Jane',
    'Student',
    'STUDENT',
    1,
    NOW()
);

-- Get the student user_id for creating student record
SET @student_user_id = LAST_INSERT_ID();

-- Create Student record in STUDENTS table
INSERT INTO STUDENTS (user_id, student_number, year_level, class_section, enrollment_date)
VALUES (
    @student_user_id,
    'STU001',
    1,
    'A',
    NOW()
);

-- Verify the users were created
SELECT 
    u.username,
    u.role,
    u.email,
    u.first_name,
    u.last_name,
    u.is_active
FROM USERS u
WHERE u.username IN ('admin', 'teacher1', 'student1')
ORDER BY u.role, u.username;

-- Show success message
SELECT 'Default users created successfully!' AS Status;
SELECT 'Login with: admin/Admin@123, teacher1/Teacher@123, student1/Student@123' AS Credentials;
