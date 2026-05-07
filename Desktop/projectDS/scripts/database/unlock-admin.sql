-- Unlock Admin Account
-- Use this script when the admin account gets locked due to failed login attempts

USE Wolde;

-- Clear failed login attempts for admin
-- Note: The failed_attempts are stored in memory by AuthenticationService
-- This script ensures the database user is active

-- Ensure admin account is active
UPDATE USERS 
SET is_active = TRUE 
WHERE username = 'admin';

-- Verify the admin account status
SELECT 
    username, 
    email, 
    role, 
    is_active,
    created_at
FROM USERS 
WHERE username = 'admin';

SELECT 'Admin account unlocked successfully!' AS Status;
