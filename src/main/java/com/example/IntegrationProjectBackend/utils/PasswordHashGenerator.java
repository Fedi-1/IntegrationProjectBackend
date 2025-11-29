package com.example.IntegrationProjectBackend.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Utility to generate BCrypt hashes for existing passwords
 * Run this once to get hashed passwords for your database
 */
public class PasswordHashGenerator {

    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        System.out.println("=== BCrypt Password Hash Generator ===\n");

        // Common test passwords
        String[] passwords = {
                "12345678",
                "password",
                "123456",
                "test1234",
                "admin123"
        };

        for (String password : passwords) {
            String hash = encoder.encode(password);
            System.out.println("Password: " + password);
            System.out.println("BCrypt Hash: " + hash);
            System.out.println("---");
        }

        System.out.println("\n✅ Copy the hashes above and use them in your SQL UPDATE statement!");
        System.out.println("Example:");
        System.out.println("UPDATE users SET password = '<hash>' WHERE email = 'your-email@example.com';");
    }
}
