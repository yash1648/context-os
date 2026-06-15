package com.grim.contextos.auth.service;

import com.grim.contextos.auth.model.RefreshToken;
import com.grim.contextos.auth.repository.RefreshTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public RefreshToken createRefreshToken(UUID userId) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUserId(userId);
        refreshToken.setToken(UUID.randomUUID().toString() + "-" + UUID.randomUUID().toString());
        refreshToken.setExpiresAt(LocalDateTime.now().plusDays(30));
        return refreshTokenRepository.save(refreshToken);
    }

    public RefreshToken validateRefreshToken(String token) {
        return refreshTokenRepository.findByTokenAndRevokedFalseAndExpiresAtAfter(token, LocalDateTime.now())
            .orElseThrow(() -> new RuntimeException("Invalid or expired refresh token"));
    }

    @Transactional
    public void revokeRefreshToken(String token) {
        refreshTokenRepository.findByToken(token).ifPresent(rt -> {
            rt.setRevoked(true);
            refreshTokenRepository.save(rt);
        });
    }
}
