-- Fix discriminator values for Parent and Student
-- The database might have "Parent" and "Student" but Hibernate expects "PARENT" and "STUDENT"

UPDATE users SET dtype = 'PARENT' WHERE dtype = 'Parent' OR role = 'PARENT';
UPDATE users SET dtype = 'STUDENT' WHERE dtype = 'Student' OR role = 'ETUDIANT';
UPDATE users SET dtype = 'ADMINISTRATOR' WHERE role = 'ADMINISTRATOR';

-- Verify the update
SELECT id, cin, email, dtype, role FROM users;
