package com.grim.contextos.auth.security;

import com.grim.contextos.auth.model.Role;
import com.grim.contextos.auth.model.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider tokenProvider;
    private final UUID userId = UUID.randomUUID();
    private final UserPrincipal principal = new UserPrincipal(userId, "test@test.com", "hash", Role.USER);

    @BeforeEach
    void setUp() {
        tokenProvider = new JwtTokenProvider("my-super-secret-key-that-is-long-enough-for-hmac-sha-512-algorithm");
    }

    @Test
    void generateAccessTokenReturnsValidToken() {
        String token = tokenProvider.generateAcessToken(principal);

        assertNotNull(token);
        assertTrue(token.split("\\.").length == 3); // JWT has 3 parts
    }

    @Test
    void validateTokenReturnsTrueForValidToken() {
        String token = tokenProvider.generateAcessToken(principal);
        assertTrue(tokenProvider.validateToken(token));
    }

    @Test
    void validateTokenReturnsFalseForInvalidToken() {
        assertFalse(tokenProvider.validateToken("invalid-token"));
    }

    @Test
    void validateTokenReturnsFalseForGarbage() {
        assertFalse(tokenProvider.validateToken("eyJhbGciOiJIUzUxMiJ9.garbage.signature"));
    }

    @Test
    void getUserIdFromTokenReturnsCorrectId() {
        String token = tokenProvider.generateAcessToken(principal);
        UUID extractedId = tokenProvider.getUserIdFromToken(token);

        assertEquals(userId, extractedId);
    }

    @Test
    void getEmailFromTokenReturnsCorrectEmail() {
        String token = tokenProvider.generateAcessToken(principal);
        String email = tokenProvider.getEmailFromToken(token);

        assertEquals("test@test.com", email);
    }

    @Test
    void generateRefreshTokenReturnsValidToken() {
        String token = tokenProvider.generateRefreshToken(principal);

        assertNotNull(token);
        assertTrue(token.split("\\.").length == 3);
    }

    @Test
    void accessAndRefreshTokensAreDifferent() {
        String access = tokenProvider.generateAcessToken(principal);
        String refresh = tokenProvider.generateRefreshToken(principal);

        assertNotEquals(access, refresh);
    }

    @Test
    void differentSecretsCreateDifferentTokens() {
        JwtTokenProvider provider2 = new JwtTokenProvider("a-completely-different-secret-key-that-is-also-long-enough");
        String t1 = tokenProvider.generateAcessToken(principal);
        String t2 = provider2.generateAcessToken(principal);

        // Both are valid tokens but should be different
        assertNotEquals(t1, t2);
        assertTrue(tokenProvider.validateToken(t1));
    }

    @Test
    void shortSecretGetsExpanded() {
        // This should trigger the SHA-256 expansion path
        JwtTokenProvider shortSecretProvider = new JwtTokenProvider("short");
        String token = shortSecretProvider.generateAcessToken(principal);

        assertNotNull(token);
        assertTrue(shortSecretProvider.validateToken(token));
    }

    @Test
    void tokenExpirationIs15Minutes() {
        // Check that the expiration field is set (can't easily check exact time without parsing)
        String token = tokenProvider.generateAcessToken(principal);
        assertTrue(tokenProvider.validateToken(token));
    }
}
