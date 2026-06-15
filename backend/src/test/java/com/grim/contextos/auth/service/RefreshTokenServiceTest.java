package com.grim.contextos.auth.service;

import com.grim.contextos.auth.model.RefreshToken;
import com.grim.contextos.auth.repository.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    private RefreshTokenService refreshTokenService;
    private final UUID userId = UUID.randomUUID();
    private RefreshToken testToken;

    @BeforeEach
    void setUp() {
        refreshTokenService = new RefreshTokenService(refreshTokenRepository);

        testToken = new RefreshToken();
        testToken.setId(UUID.randomUUID());
        testToken.setUserId(userId);
        testToken.setToken("token-value");
        testToken.setExpiresAt(LocalDateTime.now().plusDays(30));
        testToken.setRevoked(false);
    }

    @Test
    void createRefreshTokenSavesAndReturns() {
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(testToken);

        RefreshToken result = refreshTokenService.createRefreshToken(userId);

        assertNotNull(result);
        assertEquals("token-value", result.getToken());
        assertEquals(userId, result.getUserId());
        assertFalse(result.isRevoked());
        assertNotNull(result.getExpiresAt());

        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void createRefreshTokenSetsProperExpiration() {
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(i -> i.getArgument(0));

        RefreshToken result = refreshTokenService.createRefreshToken(userId);

        // Should expire ~30 days from now
        assertTrue(result.getExpiresAt().isAfter(LocalDateTime.now().plusDays(29)));
        assertTrue(result.getExpiresAt().isBefore(LocalDateTime.now().plusDays(31)));
    }

    @Test
    void createRefreshTokenGeneratesUniqueToken() {
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(i -> i.getArgument(0));

        RefreshToken t1 = refreshTokenService.createRefreshToken(userId);
        RefreshToken t2 = refreshTokenService.createRefreshToken(userId);

        assertNotEquals(t1.getToken(), t2.getToken());
    }

    @Test
    void validateRefreshTokenReturnsTokenWhenValid() {
        when(refreshTokenRepository.findByTokenAndRevokedFalseAndExpiresAtAfter(eq("valid-token"), any(LocalDateTime.class)))
            .thenReturn(Optional.of(testToken));

        RefreshToken result = refreshTokenService.validateRefreshToken("valid-token");

        assertEquals(testToken.getToken(), result.getToken());
    }

    @Test
    void validateRefreshTokenThrowsWhenExpired() {
        when(refreshTokenRepository.findByTokenAndRevokedFalseAndExpiresAtAfter(eq("expired-token"), any(LocalDateTime.class)))
            .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> refreshTokenService.validateRefreshToken("expired-token"));
    }

    @Test
    void revokeRefreshTokenSetsRevoked() {
        when(refreshTokenRepository.findByToken("valid-token")).thenReturn(Optional.of(testToken));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(testToken);

        refreshTokenService.revokeRefreshToken("valid-token");

        assertTrue(testToken.isRevoked());
        verify(refreshTokenRepository).save(testToken);
    }

    @Test
    void revokeRefreshTokenDoesNothingWhenTokenNotFound() {
        when(refreshTokenRepository.findByToken("unknown-token")).thenReturn(Optional.empty());

        refreshTokenService.revokeRefreshToken("unknown-token");

        verify(refreshTokenRepository, never()).save(any());
    }
}
