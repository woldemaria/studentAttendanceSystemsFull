-- Add department column to STUDENTS table
-- This allows students to have a department field just like teachers

USE Wolde;

-- Add department column if it doesn't exist
ALTER TABLE STUDENTS 
ADD COLUMN IF NOT EXISTS department VARCHAR(100) AFTER program;

-- Add index for department
CREATE INDEX IF NOT EXISTS idx_student_department ON STUDENTS(department);

-- Update existing students with default department
UPDATE STUDENTS 
SET department = 'General' 
WHERE department IS NULL;

-- Verify the change
DESCRIBE STUDENTS;

SELECT 'Department column added to STUDENTS table successfully!' AS Status;
