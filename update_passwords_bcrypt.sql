-- 🔐 Update Existing Users with BCrypt Encrypted Passwords
-- Run this SQL script on your database (MySQL or PostgreSQL)

-- ====================================
-- For password: "12345678"
-- ====================================
-- Copy this encrypted password: $2a$10$OXjEEeGmpsLiw1YvWW4ohefCkoAa2x60xw1rIfcVoHgK/qRdEDPcW

-- Update ALL users who have "12345678" as password:
UPDATE users 
SET password = '$2a$10$OXjEEeGmpsLiw1YvWW4ohefCkoAa2x60xw1rIfcVoHgK/qRdEDPcW'
WHERE password = '12345678';

-- Or update specific user by email:
-- UPDATE users 
-- SET password = '$2a$10$OXjEEeGmpsLiw1YvWW4ohefCkoAa2x60xw1rIfcVoHgK/qRdEDPcW'
-- WHERE email = 'your-email@example.com';

-- ====================================
-- For password: "password123"
-- ====================================
-- UPDATE users 
-- SET password = '$2a$10$MVy7MDoAD5eL7ca6vCsjjeBc76YlrBvvaLcbT2fBq8gvPgfSUGBu2'
-- WHERE password = 'password123';

-- ====================================
-- For password: "admin123"
-- ====================================
-- UPDATE users 
-- SET password = '$2a$10$l6le1Gf.GZXNwtYAY8Fb.O./s4nQ46BWsYbKyFmR7zd6X16.4t2yC'
-- WHERE password = 'admin123';

-- ====================================
-- For password: "parent123"
-- ====================================
-- UPDATE users 
-- SET password = '$2a$10$oI8uqtZLL55d7PfUDLmcJOTpVuHsss11tpcIbhonHfZycrOiCjP0i'
-- WHERE password = 'parent123';

-- ====================================
-- For password: "student123"
-- ====================================
-- UPDATE users 
-- SET password = '$2a$10$/6aCmMPmqYf6F19b/PmXzeVkDpT4YqLKPHNA0/Gf8atx9aE0pQiZG'
-- WHERE password = 'student123';

-- ====================================
-- Verify Update
-- ====================================
-- Check that passwords are now encrypted:
SELECT id, email, LEFT(password, 20) as encrypted_password_preview, role 
FROM users 
LIMIT 10;

-- You should see passwords starting with "$2a$10$" instead of plain text

-- ====================================
-- Notes
-- ====================================
-- ✅ After running this:
--    1. All existing users can still login (same plain password, just encrypted in DB)
--    2. New signups will automatically use BCrypt
--    3. Password reset will use BCrypt
--    4. Login will check BCrypt passwords

-- ⚠️ Important:
--    - Run this script ONCE
--    - Don't run it multiple times (it will encrypt already encrypted passwords!)
--    - If you have plain text passwords, update them NOW
