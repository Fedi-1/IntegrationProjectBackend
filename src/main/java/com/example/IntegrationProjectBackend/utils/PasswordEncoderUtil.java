package com.example.IntegrationProjectBackend.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Utility to generate BCrypt encoded passwords for database insertion
 * Run this as a standalone Java application to get encrypted passwords
 */
public class PasswordEncoderUtil {

    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        // List of passwords you want to encode
        String[] passwords = {
                "12345678",
                "password123",
                "admin123",
                "parent123",
                "student123"
        };

        System.out.println("=".repeat(80));
        System.out.println("BCrypt Password Encoder - For Database Manual Insertion");
        System.out.println("=".repeat(80));
        System.out.println();

        for (String plainPassword : passwords) {
            String encodedPassword = encoder.encode(plainPassword);
            System.out.println("Plain Password:    " + plainPassword);
            System.out.println("Encoded Password:  " + encodedPassword);
            System.out.println();
            System.out.println("SQL UPDATE Example:");
            System.out.println(
                    "UPDATE users SET password = '" + encodedPassword + "' WHERE email = 'your-email@example.com';");
            System.out.println();
            System.out.println("-".repeat(80));
            System.out.println();
        }

        System.out.println("Usage Instructions:");
        System.out.println("1. Copy the encoded password for your test password (e.g., '12345678')");
        System.out.println("2. Run SQL UPDATE on your database (MySQL/PostgreSQL)");
        System.out.println("3. All users with that encrypted password can now login");
        System.out.println("4. Password reset will also use BCrypt encryption");
        System.out.println();
    }
}
