-- Sample Data for Student Attendance System
-- This script populates the database with sample data for testing and demonstration

USE attendance_system;

-- Insert sample teachers
INSERT INTO USERS (username, password_hash, email, first_name, last_name, role) VALUES
('john.smith', '$2a$10$N9qo8uLOickgx2ZMRZoMye.Uo0ePPO4tyh/OpGrrabJefPCt/Nu/G', 'john.smith@university.edu', 'John', 'Smith', 'TEACHER'),
('mary.johnson', '$2a$10$N9qo8uLOickgx2ZMRZoMye.Uo0ePPO4tyh/OpGrrabJefPCt/Nu/G', 'mary.johnson@university.edu', 'Mary', 'Johnson', 'TEACHER'),
('david.brown', '$2a$10$N9qo8uLOickgx2ZMRZoMye.Uo0ePPO4tyh/OpGrrabJefPCt/Nu/G', 'david.brown@university.edu', 'David', 'Brown', 'TEACHER');

-- Insert sample students
INSERT INTO USERS (username, password_hash, email, first_name, last_name, role) VALUES
('alice.wilson', '$2a$10$N9qo8uLOickgx2ZMRZoMye.Uo0ePPO4tyh/OpGrrabJefPCt/Nu/G', 'alice.wilson@student.edu', 'Alice', 'Wilson', 'STUDENT'),
('bob.davis', '$2a$10$N9qo8uLOickgx2ZMRZoMye.Uo0ePPO4tyh/OpGrrabJefPCt/Nu/G', 'bob.davis@student.edu', 'Bob', 'Davis', 'STUDENT'),
('carol.miller', '$2a$10$N9qo8uLOickgx2ZMRZoMye.Uo0ePPO4tyh/OpGrrabJefPCt/Nu/G', 'carol.miller@student.edu', 'Carol', 'Miller', 'STUDENT'),
('daniel.garcia', '$2a$10$N9qo8uLOickgx2ZMRZoMye.Uo0ePPO4tyh/OpGrrabJefPCt/Nu/G', 'daniel.garcia@student.edu', 'Daniel', 'Garcia', 'STUDENT'),
('emma.rodriguez', '$2a$10$N9qo8uLOickgx2ZMRZoMye.Uo0ePPO4tyh/OpGrrabJefPCt/Nu/G', 'emma.rodriguez@student.edu', 'Emma', 'Rodriguez', 'STUDENT'),
('frank.martinez', '$2a$10$N9qo8uLOickgx2ZMRZoMye.Uo0ePPO4tyh/OpGrrabJefPCt/Nu/G', 'frank.martinez@student.edu', 'Frank', 'Martinez', 'STUDENT'),
('grace.anderson', '$2a$10$N9qo8uLOickgx2ZMRZoMye.Uo0ePPO4tyh/OpGrrabJefPCt/Nu/G', 'grace.anderson@student.edu', 'Grace', 'Anderson', 'STUDENT'),
('henry.taylor', '$2a$10$N9qo8uLOickgx2ZMRZoMye.Uo0ePPO4tyh/OpGrrabJefPCt/Nu/G', 'henry.taylor@student.edu', 'Henry', 'Taylor', 'STUDENT'),
('iris.thomas', '$2a$10$N9qo8uLOickgx2ZMRZoMye.Uo0ePPO4tyh/OpGrrabJefPCt/Nu/G', 'iris.thomas@student.edu', 'Iris', 'Thomas', 'STUDENT'),
('jack.hernandez', '$2a$10$N9qo8uLOickgx2ZMRZoMye.Uo0ePPO4tyh/OpGrrabJefPCt/Nu/G', 'jack.hernandez@student.edu', 'Jack', 'Hernandez', 'STUDENT');

-- Insert teacher profiles
INSERT INTO TEACHERS (user_id, employee_id, department, specialization) VALUES
((SELECT user_id FROM USERS WHERE username = 'john.smith'), 'EMP001', 'Computer Science', 'Software Engineering'),
((SELECT user_id FROM USERS WHERE username = 'mary.johnson'), 'EMP002', 'Mathematics', 'Statistics'),
((SELECT user_id FROM USERS WHERE username = 'david.brown'), 'EMP003', 'Computer Science', 'Database Systems');

-- Insert student profiles
INSERT INTO STUDENTS (user_id, student_number, program, year_level, enrollment_date) VALUES
((SELECT user_id FROM USERS WHERE username = 'alice.wilson'), 'STU2021001', 'Computer Science', 3, '2021-09-01'),
((SELECT user_id FROM USERS WHERE username = 'bob.davis'), 'STU2021002', 'Computer Science', 3, '2021-09-01'),
((SELECT user_id FROM USERS WHERE username = 'carol.miller'), 'STU2021003', 'Mathematics', 2, '2022-09-01'),
((SELECT user_id FROM USERS WHERE username = 'daniel.garcia'), 'STU2021004', 'Computer Science', 3, '2021-09-01'),
((SELECT user_id FROM USERS WHERE username = 'emma.rodriguez'), 'STU2022001', 'Mathematics', 2, '2022-09-01'),
((SELECT user_id FROM USERS WHERE username = 'frank.martinez'), 'STU2022002', 'Computer Science', 2, '2022-09-01'),
((SELECT user_id FROM USERS WHERE username = 'grace.anderson'), 'STU2022003', 'Mathematics', 2, '2022-09-01'),
((SELECT user_id FROM USERS WHERE username = 'henry.taylor'), 'STU2023001', 'Computer Science', 1, '2023-09-01'),
((SELECT user_id FROM USERS WHERE username = 'iris.thomas'), 'STU2023002', 'Mathematics', 1, '2023-09-01'),
((SELECT user_id FROM USERS WHERE username = 'jack.hernandez'), 'STU2023003', 'Computer Science', 1, '2023-09-01');

-- Insert sample courses
INSERT INTO COURSES (course_code, course_name, description, credits, teacher_id, semester, academic_year) VALUES
('CS101', 'Introduction to Programming', 'Basic programming concepts using Java', 3, 
 (SELECT teacher_id FROM TEACHERS WHERE employee_id = 'EMP001'), 'Fall', '2023-2024'),
('CS201', 'Data Structures and Algorithms', 'Advanced programming and algorithm design', 4, 
 (SELECT teacher_id FROM TEACHERS WHERE employee_id = 'EMP001'), 'Fall', '2023-2024'),
('CS301', 'Database Systems', 'Database design and management', 3, 
 (SELECT teacher_id FROM TEACHERS WHERE employee_id = 'EMP003'), 'Fall', '2023-2024'),
('MATH101', 'Calculus I', 'Differential and integral calculus', 4, 
 (SELECT teacher_id FROM TEACHERS WHERE employee_id = 'EMP002'), 'Fall', '2023-2024'),
('MATH201', 'Statistics', 'Probability and statistical analysis', 3, 
 (SELECT teacher_id FROM TEACHERS WHERE employee_id = 'EMP002'), 'Fall', '2023-2024');

-- Insert enrollments
-- CS101 enrollments (introductory course - all year levels)
INSERT INTO ENROLLMENTS (student_id, course_id, enrollment_date) VALUES
((SELECT student_id FROM STUDENTS WHERE student_number = 'STU2023001'), 
 (SELECT course_id FROM COURSES WHERE course_code = 'CS101'), '2023-09-01'),
((SELECT student_id FROM STUDENTS WHERE student_number = 'STU2023003'), 
 (SELECT course_id FROM COURSES WHERE course_code = 'CS101'), '2023-09-01'),
((SELECT student_id FROM STUDENTS WHERE student_number = 'STU2022002'), 
 (SELECT course_id FROM COURSES WHERE course_code = 'CS101'), '2023-09-01');

-- CS201 enrollments (intermediate course - year 2 and 3)
INSERT INTO ENROLLMENTS (student_id, course_id, enrollment_date) VALUES
((SELECT student_id FROM STUDENTS WHERE student_number = 'STU2021001'), 
 (SELECT course_id FROM COURSES WHERE course_code = 'CS201'), '2023-09-01'),
((SELECT student_id FROM STUDENTS WHERE student_number = 'STU2021002'), 
 (SELECT course_id FROM COURSES WHERE course_code = 'CS201'), '2023-09-01'),
((SELECT student_id FROM STUDENTS WHERE student_number = 'STU2021004'), 
 (SELECT course_id FROM COURSES WHERE course_code = 'CS201'), '2023-09-01'),
((SELECT student_id FROM STUDENTS WHERE student_number = 'STU2022002'), 
 (SELECT course_id FROM COURSES WHERE course_code = 'CS201'), '2023-09-01');

-- CS301 enrollments (advanced course - year 3)
INSERT INTO ENROLLMENTS (student_id, course_id, enrollment_date) VALUES
((SELECT student_id FROM STUDENTS WHERE student_number = 'STU2021001'), 
 (SELECT course_id FROM COURSES WHERE course_code = 'CS301'), '2023-09-01'),
((SELECT student_id FROM STUDENTS WHERE student_number = 'STU2021002'), 
 (SELECT course_id FROM COURSES WHERE course_code = 'CS301'), '2023-09-01'),
((SELECT student_id FROM STUDENTS WHERE student_number = 'STU2021004'), 
 (SELECT course_id FROM COURSES WHERE course_code = 'CS301'), '2023-09-01');

-- MATH101 enrollments (all math students and some CS students)
INSERT INTO ENROLLMENTS (student_id, course_id, enrollment_date) VALUES
((SELECT student_id FROM STUDENTS WHERE student_number = 'STU2021003'), 
 (SELECT course_id FROM COURSES WHERE course_code = 'MATH101'), '2023-09-01'),
((SELECT student_id FROM STUDENTS WHERE student_number = 'STU2022001'), 
 (SELECT course_id FROM COURSES WHERE course_code = 'MATH101'), '2023-09-01'),
((SELECT student_id FROM STUDENTS WHERE student_number = 'STU2022003'), 
 (SELECT course_id FROM COURSES WHERE course_code = 'MATH101'), '2023-09-01'),
((SELECT student_id FROM STUDENTS WHERE student_number = 'STU2023002'), 
 (SELECT course_id FROM COURSES WHERE course_code = 'MATH101'), '2023-09-01'),
((SELECT student_id FROM STUDENTS WHERE student_number = 'STU2023001'), 
 (SELECT course_id FROM COURSES WHERE course_code = 'MATH101'), '2023-09-01');

-- MATH201 enrollments (advanced math students)
INSERT INTO ENROLLMENTS (student_id, course_id, enrollment_date) VALUES
((SELECT student_id FROM STUDENTS WHERE student_number = 'STU2021003'), 
 (SELECT course_id FROM COURSES WHERE course_code = 'MATH201'), '2023-09-01'),
((SELECT student_id FROM STUDENTS WHERE student_number = 'STU2022001'), 
 (SELECT course_id FROM COURSES WHERE course_code = 'MATH201'), '2023-09-01'),
((SELECT student_id FROM STUDENTS WHERE student_number = 'STU2022003'), 
 (SELECT course_id FROM COURSES WHERE course_code = 'MATH201'), '2023-09-01');

-- Insert sample attendance records for the past month
-- This creates realistic attendance patterns with some absences

-- CS101 attendance (3 students, 20 classes over past month)
INSERT INTO ATTENDANCE_RECORDS (student_id, course_id, attendance_date, class_time, status, marked_by) 
SELECT 
    s.student_id,
    c.course_id,
    DATE_SUB(CURDATE(), INTERVAL (20 - day_num) DAY) as attendance_date,
    '09:00:00' as class_time,
    CASE 
        WHEN RAND() < 0.85 THEN 'PRESENT'
        WHEN RAND() < 0.95 THEN 'LATE'
        ELSE 'ABSENT'
    END as status,
    (SELECT user_id FROM USERS WHERE username = 'john.smith') as marked_by
FROM 
    (SELECT student_id FROM STUDENTS WHERE student_number IN ('STU2023001', 'STU2023003', 'STU2022002')) s
    CROSS JOIN (SELECT course_id FROM COURSES WHERE course_code = 'CS101') c
    CROSS JOIN (SELECT 1 as day_num UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 
                UNION SELECT 8 UNION SELECT 9 UNION SELECT 10 UNION SELECT 11 UNION SELECT 12
                UNION SELECT 15 UNION SELECT 16 UNION SELECT 17 UNION SELECT 18 UNION SELECT 19
                UNION SELECT 22 UNION SELECT 23 UNION SELECT 24 UNION SELECT 25 UNION SELECT 26) days;

-- CS201 attendance (4 students, 15 classes)
INSERT INTO ATTENDANCE_RECORDS (student_id, course_id, attendance_date, class_time, status, marked_by) 
SELECT 
    s.student_id,
    c.course_id,
    DATE_SUB(CURDATE(), INTERVAL (15 - day_num) DAY) as attendance_date,
    '11:00:00' as class_time,
    CASE 
        WHEN RAND() < 0.90 THEN 'PRESENT'
        WHEN RAND() < 0.97 THEN 'LATE'
        ELSE 'ABSENT'
    END as status,
    (SELECT user_id FROM USERS WHERE username = 'john.smith') as marked_by
FROM 
    (SELECT student_id FROM STUDENTS WHERE student_number IN ('STU2021001', 'STU2021002', 'STU2021004', 'STU2022002')) s
    CROSS JOIN (SELECT course_id FROM COURSES WHERE course_code = 'CS201') c
    CROSS JOIN (SELECT 1 as day_num UNION SELECT 2 UNION SELECT 3 UNION SELECT 5 UNION SELECT 6 
                UNION SELECT 8 UNION SELECT 9 UNION SELECT 10 UNION SELECT 12 UNION SELECT 13
                UNION SELECT 15 UNION SELECT 16 UNION SELECT 17 UNION SELECT 19 UNION SELECT 20) days;

-- Insert sample notifications
INSERT INTO NOTIFICATIONS (user_id, title, message, type) VALUES
((SELECT user_id FROM USERS WHERE username = 'alice.wilson'), 
 'Low Attendance Warning', 
 'Your attendance in CS201 has dropped below 75%. Please attend classes regularly to maintain good standing.', 
 'ATTENDANCE_WARNING'),
((SELECT user_id FROM USERS WHERE username = 'bob.davis'), 
 'Course Update', 
 'CS301 Database Systems: Assignment 3 has been posted. Due date: Next Friday.', 
 'COURSE_UPDATE'),
((SELECT user_id FROM USERS WHERE username = 'john.smith'), 
 'System Notification', 
 'Please remember to mark attendance for today\'s CS101 class.', 
 'REMINDER');

-- Display sample data summary
SELECT 'Sample data inserted successfully!' AS Status;
SELECT COUNT(*) as 'Total Users' FROM USERS;
SELECT COUNT(*) as 'Total Students' FROM STUDENTS;
SELECT COUNT(*) as 'Total Teachers' FROM TEACHERS;
SELECT COUNT(*) as 'Total Courses' FROM COURSES;
SELECT COUNT(*) as 'Total Enrollments' FROM ENROLLMENTS;
SELECT COUNT(*) as 'Total Attendance Records' FROM ATTENDANCE_RECORDS;
SELECT COUNT(*) as 'Total Notifications' FROM NOTIFICATIONS;

-- Show attendance summary for verification
SELECT 
    u.first_name, u.last_name, s.student_number, c.course_code,
    COUNT(ar.attendance_id) as total_classes,
    SUM(CASE WHEN ar.status IN ('PRESENT', 'LATE') THEN 1 ELSE 0 END) as attended,
    ROUND((SUM(CASE WHEN ar.status IN ('PRESENT', 'LATE') THEN 1 ELSE 0 END) / COUNT(ar.attendance_id)) * 100, 1) as percentage
FROM STUDENTS s
JOIN USERS u ON s.user_id = u.user_id
JOIN ENROLLMENTS e ON s.student_id = e.student_id
JOIN COURSES c ON e.course_id = c.course_id
LEFT JOIN ATTENDANCE_RECORDS ar ON s.student_id = ar.student_id AND c.course_id = ar.course_id
GROUP BY s.student_id, c.course_id
ORDER BY u.last_name, c.course_code;