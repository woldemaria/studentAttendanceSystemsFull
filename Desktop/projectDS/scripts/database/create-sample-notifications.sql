-- Create Sample Notifications
-- Database: Wolde

USE Wolde;

-- Get user IDs
SET @admin_id = (SELECT user_id FROM USERS WHERE username = 'admin' LIMIT 1);
SET @teacher_id = (SELECT user_id FROM USERS WHERE username = 'teacher1' LIMIT 1);
SET @student_id = (SELECT user_id FROM USERS WHERE username = 'student1' LIMIT 1);

-- Create notifications for admin
INSERT INTO NOTIFICATIONS (user_id, title, message, type, is_read, created_at)
VALUES 
(@admin_id, 'System Update', 'System has been updated to version 1.0.0', 'SYSTEM_NOTIFICATION', 0, NOW()),
(@admin_id, 'New User Registration', 'A new student has registered: student1', 'SYSTEM_NOTIFICATION', 0, DATE_SUB(NOW(), INTERVAL 1 HOUR)),
(@admin_id, 'Low Attendance Alert', 'Student attendance below 75% threshold', 'ATTENDANCE_WARNING', 1, DATE_SUB(NOW(), INTERVAL 2 HOUR));

-- Create notifications for teacher
INSERT INTO NOTIFICATIONS (user_id, title, message, type, is_read, created_at)
VALUES 
(@teacher_id, 'Attendance Reminder', 'Please mark attendance for today''s classes', 'REMINDER', 0, NOW()),
(@teacher_id, 'Course Assignment', 'You have been assigned to a new course', 'COURSE_UPDATE', 0, DATE_SUB(NOW(), INTERVAL 30 MINUTE)),
(@teacher_id, 'Student Query', 'A student has a question about attendance', 'SYSTEM_NOTIFICATION', 1, DATE_SUB(NOW(), INTERVAL 1 DAY));

-- Create notifications for student
INSERT INTO NOTIFICATIONS (user_id, title, message, type, is_read, created_at)
VALUES 
(@student_id, 'Welcome!', 'Welcome to the Student Attendance System', 'SYSTEM_NOTIFICATION', 0, NOW()),
(@student_id, 'Attendance Marked', 'Your attendance has been marked for today', 'COURSE_UPDATE', 0, DATE_SUB(NOW(), INTERVAL 15 MINUTE)),
(@student_id, 'Low Attendance Warning', 'Your attendance is below 75%. Please improve your attendance.', 'ATTENDANCE_WARNING', 0, DATE_SUB(NOW(), INTERVAL 1 HOUR)),
(@student_id, 'Course Enrollment', 'You have been enrolled in a new course', 'COURSE_UPDATE', 1, DATE_SUB(NOW(), INTERVAL 2 HOUR));

-- Verify notifications were created
SELECT 
    u.username,
    n.title,
    n.type,
    n.is_read,
    n.created_at
FROM NOTIFICATIONS n
JOIN USERS u ON n.user_id = u.user_id
ORDER BY n.created_at DESC;

-- Show count by user
SELECT 
    u.username,
    COUNT(*) as notification_count,
    SUM(CASE WHEN n.is_read = 0 THEN 1 ELSE 0 END) as unread_count
FROM NOTIFICATIONS n
JOIN USERS u ON n.user_id = u.user_id
GROUP BY u.username;

SELECT 'Sample notifications created successfully!' AS Status;
