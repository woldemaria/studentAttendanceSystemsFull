-- Migration script to add class_section column to STUDENTS table
-- Run this if you get "Unknown column 's.class_section'" error

USE Wolde;

-- Check if column exists and add it if it doesn't
SET @dbname = 'Wolde';
SET @tablename = 'STUDENTS';
SET @columnname = 'class_section';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (table_name = @tablename)
      AND (table_schema = @dbname)
      AND (column_name = @columnname)
  ) > 0,
  "SELECT 1",
  CONCAT("ALTER TABLE ", @tablename, " ADD COLUMN ", @columnname, " VARCHAR(10) DEFAULT 'A' AFTER year_level")
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- Add index if it doesn't exist
SET @indexname = 'idx_year_class';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
    WHERE
      (table_name = @tablename)
      AND (table_schema = @dbname)
      AND (index_name = @indexname)
  ) > 0,
  "SELECT 1",
  CONCAT("ALTER TABLE ", @tablename, " ADD INDEX ", @indexname, " (year_level, class_section)")
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- Verify the column was added
SELECT 
    COLUMN_NAME, 
    DATA_TYPE, 
    COLUMN_DEFAULT, 
    IS_NULLABLE
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'Wolde' 
  AND TABLE_NAME = 'STUDENTS' 
  AND COLUMN_NAME = 'class_section';

-- Show success message
SELECT 'Migration completed successfully! class_section column added to STUDENTS table.' AS Status;
