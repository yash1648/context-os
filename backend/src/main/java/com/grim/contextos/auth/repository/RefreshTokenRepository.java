package com.grim.contextos.auth.repository;

import com.grim.contextos.auth.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    Optional<RefreshToken> findByTokenAndRevokedFalseAndExpiresAtAfter(String token, LocalDateTime now);
    Optional<RefreshToken> findByToken(String token);
}
