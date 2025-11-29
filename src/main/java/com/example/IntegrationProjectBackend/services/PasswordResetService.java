package com.example.IntegrationProjectBackend.services;

import com.example.IntegrationProjectBackend.models.PasswordResetToken;
import com.example.IntegrationProjectBackend.models.User;
import com.example.IntegrationProjectBackend.repositories.PasswordResetTokenRepository;
import com.example.IntegrationProjectBackend.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

@Service
public class PasswordResetService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private SendGridEmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${app.frontend.url:http://localhost:4200}")
    private String frontendUrl;

    private static final int TOKEN_EXPIRY_MINUTES = 30;
    private static final int TOKEN_LENGTH = 32;

    @Transactional
    public void initiatePasswordReset(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            // For security, don't reveal if email exists
            System.out.println("⚠️ Password reset requested for non-existent email: " + email);
            return;
        }

        User user = userOpt.get();

        // Delete any existing tokens for this user
        tokenRepository.deleteByUser(user);

        // Generate secure token
        String token = generateSecureToken();

        // Create token entity
        LocalDateTime expiryDate = LocalDateTime.now().plusMinutes(TOKEN_EXPIRY_MINUTES);
        PasswordResetToken resetToken = new PasswordResetToken(token, user, expiryDate);

        tokenRepository.save(resetToken);

        // Send email
        try {
            String userName = user.getFirstName() + " " + user.getLastName();
            emailService.sendPasswordResetEmail(user.getEmail(), userName, token, frontendUrl);
            System.out.println("✅ Password reset email sent to: " + email);
        } catch (Exception e) {
            System.err.println("❌ Failed to send password reset email: " + e.getMessage());
            throw new RuntimeException("Erreur lors de l'envoi de l'email. Veuillez réessayer.");
        }
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        Optional<PasswordResetToken> tokenOpt = tokenRepository.findByToken(token);

        if (tokenOpt.isEmpty()) {
            throw new RuntimeException("Token invalide");
        }

        PasswordResetToken resetToken = tokenOpt.get();

        if (resetToken.isUsed()) {
            throw new RuntimeException("Ce lien a déjà été utilisé");
        }

        if (resetToken.isExpired()) {
            throw new RuntimeException("Ce lien a expiré. Demandez une nouvelle réinitialisation");
        }

        User user = resetToken.getUser();

        // Validate password strength
        if (newPassword == null || newPassword.length() < 6) {
            throw new RuntimeException("Le mot de passe doit contenir au moins 6 caractères");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Mark token as used
        resetToken.setUsed(true);
        tokenRepository.save(resetToken);

        // Send confirmation email
        try {
            String userName = user.getFirstName() + " " + user.getLastName();
            emailService.sendPasswordChangedConfirmation(user.getEmail(), userName);
        } catch (Exception e) {
            System.err.println("⚠️ Failed to send confirmation email: " + e.getMessage());
        }

        System.out.println("✅ Password reset successfully for user: " + user.getEmail());
    }

    public boolean validateToken(String token) {
        Optional<PasswordResetToken> tokenOpt = tokenRepository.findByToken(token);

        if (tokenOpt.isEmpty()) {
            return false;
        }

        PasswordResetToken resetToken = tokenOpt.get();
        return resetToken.isValid();
    }

    private String generateSecureToken() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[TOKEN_LENGTH];
        random.nextBytes(bytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

    // Cleanup expired tokens every day at 3 AM
    @Scheduled(cron = "0 0 3 * * ?")
    @Transactional
    public void cleanupExpiredTokens() {
        tokenRepository.deleteExpiredTokens(LocalDateTime.now());
        System.out.println("🧹 Cleaned up expired password reset tokens");
    }
}
