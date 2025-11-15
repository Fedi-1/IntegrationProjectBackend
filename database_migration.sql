-- Database Migration for Dynamic Schedule Feature
-- Adds preparation_time_minutes field to students table
-- Date: 2025-11-15

-- Step 1: Add the new column
ALTER TABLE students 
ADD COLUMN preparation_time_minutes INT NULL;

-- Step 2: Set default value for existing students (30 minutes)
UPDATE students 
SET preparation_time_minutes = 30 
WHERE preparation_time_minutes IS NULL;

-- Step 3: Verify the migration
SELECT 
    id, 
    cin, 
    firstname, 
    lastname, 
    preparation_time_minutes 
FROM students 
LIMIT 5;

-- Expected result: All students should have preparation_time_minutes = 30

-- Rollback script (if needed):
-- ALTER TABLE students DROP COLUMN preparation_time_minutes;
