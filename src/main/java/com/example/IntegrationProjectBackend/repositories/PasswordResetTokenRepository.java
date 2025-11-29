package com.example.IntegrationProjectBackend.repositories;

import com.example.IntegrationProjectBackend.models.PasswordResetToken;
import com.example.IntegrationProjectBackend.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    
    Optional<PasswordResetToken> findByToken(String token);
    
    Optional<PasswordResetToken> findByUser(User user);
    
    @Modifying
    @Query("DELETE FROM PasswordResetToken p WHERE p.expiryDate < ?1")
    void deleteExpiredTokens(LocalDateTime now);
    
    @Modifying
    @Query("DELETE FROM PasswordResetToken p WHERE p.user = ?1")
    void deleteByUser(User user);
}
