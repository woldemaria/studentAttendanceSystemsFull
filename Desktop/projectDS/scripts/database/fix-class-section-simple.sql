-- Simple migration script to add class_section column
-- Run this if you get "Unknown column 's.class_section'" error

USE Wolde;

-- Add class_section column if it doesn't exist
ALTER TABLE STUDENTS 
ADD COLUMN IF NOT EXISTS class_section VARCHAR(10) DEFAULT 'A' AFTER year_level;

-- Add index for efficient queries
ALTER TABLE STUDENTS 
ADD INDEX IF NOT EXISTS idx_year_class (year_level, class_section);

-- Verify the change
DESCRIBE STUDENTS;

SELECT 'SUCCESS: class_section column added!' AS Status;
