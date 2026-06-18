package com.grim.contextos.auth.service;

import com.grim.contextos.auth.dto.request.LoginRequest;
import com.grim.contextos.auth.dto.request.RegisterRequest;
import com.grim.contextos.auth.dto.request.ResetPasswordRequest;
import com.grim.contextos.auth.dto.response.AuthResponse;
import com.grim.contextos.auth.dto.response.TokenRefreshResponse;
import com.grim.contextos.auth.model.PasswordResetToken;
import com.grim.contextos.auth.model.RefreshToken;
import com.grim.contextos.auth.model.Role;
import com.grim.contextos.auth.model.UserPrincipal;
import com.grim.contextos.auth.repository.PasswordResetTokenRepository;
import com.grim.contextos.auth.security.JwtTokenProvider;
import com.grim.contextos.user.model.User;
import com.grim.contextos.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    private AuthService authService;
    private final UUID userId = UUID.randomUUID();
    private User testUser;
    private RefreshToken testRefreshToken;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, passwordEncoder, jwtTokenProvider, refreshTokenService, passwordResetTokenRepository);

        testUser = new User("test@test.com", "encodedPassword", "Test User");
        testUser.setId(userId);

        testRefreshToken = new RefreshToken();
        testRefreshToken.setToken("refresh-token-value");
        testRefreshToken.setUserId(userId);
    }

    @Test
    void registerCreatesUserAndReturnsAuthResponse() {
        var request = new RegisterRequest("test@test.com", "Password123!", "Test User");

        when(userRepository.existsByEmail("test@test.com")).thenReturn(false);
        when(passwordEncoder.encode("Password123!")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtTokenProvider.generateAcessToken(any(UserPrincipal.class))).thenReturn("access-token");
        when(refreshTokenService.createRefreshToken(userId)).thenReturn(testRefreshToken);

        AuthResponse response = authService.register(request);

        assertEquals(userId, response.userId());
        assertEquals("test@test.com", response.email());
        assertEquals("Test User", response.displayName());
        assertEquals("access-token", response.accessToken());
        assertEquals("refresh-token-value", response.refreshToken());
        assertEquals(900000L, response.expiresIn());

        verify(userRepository).save(any(User.class));
        verify(refreshTokenService).createRefreshToken(userId);
    }

    @Test
    void registerThrowsWhenEmailExists() {
        var request = new RegisterRequest("test@test.com", "Password123!", "Test User");

        when(userRepository.existsByEmail("test@test.com")).thenReturn(true);

        var ex = assertThrows(RuntimeException.class, () -> authService.register(request));
        assertEquals("Email already registered", ex.getMessage());

        verify(userRepository, never()).save(any());
    }

    @Test
    void loginWithValidCredentialsReturnsAuthResponse() {
        var request = new LoginRequest("test@test.com", "Password123!");

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("Password123!", "encodedPassword")).thenReturn(true);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtTokenProvider.generateAcessToken(any(UserPrincipal.class))).thenReturn("access-token");
        when(refreshTokenService.createRefreshToken(userId)).thenReturn(testRefreshToken);

        AuthResponse response = authService.login(request);

        assertEquals(userId, response.userId());
        assertEquals("test@test.com", response.email());
        assertEquals("access-token", response.accessToken());
    }

    @Test
    void loginWithInvalidEmailThrowsBadCredentials() {
        var request = new LoginRequest("wrong@test.com", "Password123!");

        when(userRepository.findByEmail("wrong@test.com")).thenReturn(Optional.empty());

        assertThrows(BadCredentialsException.class, () -> authService.login(request));
    }

    @Test
    void loginWithWrongPasswordThrowsBadCredentials() {
        var request = new LoginRequest("test@test.com", "wrongPassword");

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        assertThrows(BadCredentialsException.class, () -> authService.login(request));
    }

    @Test
    void loginUpdatesLastLoginAt() {
        var request = new LoginRequest("test@test.com", "Password123!");

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("Password123!", "encodedPassword")).thenReturn(true);
        when(jwtTokenProvider.generateAcessToken(any(UserPrincipal.class))).thenReturn("access-token");
        when(refreshTokenService.createRefreshToken(userId)).thenReturn(testRefreshToken);

        authService.login(request);

        assertNotNull(testUser.getLastLoginAt());
    }

    @Test
    void refreshWithValidTokenReturnsNewAccessToken() {
        when(refreshTokenService.validateRefreshToken("valid-refresh-token")).thenReturn(testRefreshToken);
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(jwtTokenProvider.generateAcessToken(any(UserPrincipal.class))).thenReturn("new-access-token");

        TokenRefreshResponse response = authService.refresh("valid-refresh-token");

        assertEquals("new-access-token", response.accessToken());
        assertEquals(900000L, response.expiresIn());
    }

    @Test
    void refreshWithInvalidTokenThrows() {
        when(refreshTokenService.validateRefreshToken("bad-token"))
            .thenThrow(new RuntimeException("Invalid or expired refresh token"));

        assertThrows(RuntimeException.class, () -> authService.refresh("bad-token"));
    }

    @Test
    void refreshWithNonExistentUserThrows() {
        when(refreshTokenService.validateRefreshToken("valid-token")).thenReturn(testRefreshToken);
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> authService.refresh("valid-token"));
    }

    @Test
    void logoutRevokesRefreshToken() {
        authService.logout("refresh-token");

        verify(refreshTokenService).revokeRefreshToken("refresh-token");
    }

    @Test
    void forgotPasswordGeneratesToken() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));

        String token = authService.forgotPassword("test@test.com");

        assertNotNull(token);
        verify(passwordResetTokenRepository).deleteByEmail("test@test.com");
        verify(passwordResetTokenRepository).save(any(PasswordResetToken.class));
    }

    @Test
    void forgotPasswordThrowsWhenEmailNotFound() {
        when(userRepository.findByEmail("nonexistent@test.com")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
            () -> authService.forgotPassword("nonexistent@test.com"));
        verify(passwordResetTokenRepository, never()).save(any());
    }

    @Test
    void resetPasswordWithValidTokenUpdatesPassword() {
        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken(token, "test@test.com",
            LocalDateTime.now().plusHours(1));

        when(passwordResetTokenRepository.findByToken(token)).thenReturn(Optional.of(resetToken));
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode("NewPassword123!")).thenReturn("newEncodedPassword");

        authService.resetPassword(new ResetPasswordRequest(token, "NewPassword123!"));

        assertEquals("newEncodedPassword", testUser.getPasswordHash());
        assertTrue(resetToken.isUsed());
        verify(userRepository).save(testUser);
        verify(passwordResetTokenRepository).save(resetToken);
    }

    @Test
    void resetPasswordWithInvalidTokenThrows() {
        when(passwordResetTokenRepository.findByToken("bad-token")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
            () -> authService.resetPassword(new ResetPasswordRequest("bad-token", "NewPassword123!")));
    }

    @Test
    void resetPasswordWithUsedTokenThrows() {
        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken(token, "test@test.com",
            LocalDateTime.now().plusHours(1));
        resetToken.setUsed(true);

        when(passwordResetTokenRepository.findByToken(token)).thenReturn(Optional.of(resetToken));

        assertThrows(RuntimeException.class,
            () -> authService.resetPassword(new ResetPasswordRequest(token, "NewPassword123!")));
    }

    @Test
    void resetPasswordWithExpiredTokenThrows() {
        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken(token, "test@test.com",
            LocalDateTime.now().minusHours(1));

        when(passwordResetTokenRepository.findByToken(token)).thenReturn(Optional.of(resetToken));

        assertThrows(RuntimeException.class,
            () -> authService.resetPassword(new ResetPasswordRequest(token, "NewPassword123!")));
    }
}
