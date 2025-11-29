-- ============================================
-- QUICK FIX: Update Your Test Users' Passwords
-- ============================================
-- Run this on your database (MySQL or PostgreSQL)

-- ============================================
-- STEP 1: Update your test users
-- ============================================
-- Replace 'your-email@example.com' with YOUR actual email!

-- For password "12345678":
UPDATE users 
SET password = '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi'
WHERE email = 'your-email@example.com';

-- If you have multiple users, repeat for each:
-- UPDATE users 
-- SET password = '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi'
-- WHERE email = 'another-user@example.com';

-- ============================================
-- STEP 2: Verify it worked
-- ============================================
SELECT email, password FROM users;
-- Passwords should now start with: $2a$10$

-- ============================================
-- DONE! Now you can login with "12345678"
-- ============================================
