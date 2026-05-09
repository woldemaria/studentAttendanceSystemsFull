-- Add profile fields to USERS table
-- Phone number, gender, and photo (profile image path)

USE Wolde;

-- Add new columns to USERS table
ALTER TABLE USERS 
ADD COLUMN IF NOT EXISTS phone_number VARCHAR(20),
ADD COLUMN IF NOT EXISTS gender ENUM('MALE', 'FEMALE', 'OTHER'),
ADD COLUMN IF NOT EXISTS photo_path VARCHAR(255);

-- Add index for phone number lookups
ALTER TABLE USERS ADD INDEX IF NOT EXISTS idx_phone_number (phone_number);

-- Update STUDENTS table to ensure class_section column exists
ALTER TABLE STUDENTS 
ADD COLUMN IF NOT EXISTS class_section VARCHAR(1) DEFAULT 'A';

-- Display confirmation
SELECT 'Profile fields added successfully!' AS Status;
SELECT 'Added: phone_number, gender, photo_path to USERS table' AS Details;
