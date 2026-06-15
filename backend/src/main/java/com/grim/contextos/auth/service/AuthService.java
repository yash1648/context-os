package com.grim.contextos.auth.service;

import com.grim.contextos.auth.dto.request.LoginRequest;
import com.grim.contextos.auth.dto.request.RegisterRequest;
import com.grim.contextos.auth.dto.response.AuthResponse;
import com.grim.contextos.auth.dto.response.TokenRefreshResponse;
import com.grim.contextos.auth.model.UserPrincipal;
import com.grim.contextos.auth.security.JwtTokenProvider;
import com.grim.contextos.user.model.User;
import com.grim.contextos.user.repository.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider, RefreshTokenService refreshTokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.refreshTokenService = refreshTokenService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new RuntimeException("Email already registered");
        }

        User user = new User(request.email(), passwordEncoder.encode(request.password()), request.displayName());
        user = userRepository.save(user);

        UserPrincipal principal = new UserPrincipal(user.getId(), user.getEmail(), user.getPasswordHash(), user.getRole());
        String accessToken = jwtTokenProvider.generateAcessToken(principal);
        var refreshToken = refreshTokenService.createRefreshToken(user.getId());

        return new AuthResponse(
            user.getId(), user.getEmail(), user.getDisplayName(),
            accessToken, refreshToken.getToken(), 900000L
        );
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
            .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        UserPrincipal principal = new UserPrincipal(user.getId(), user.getEmail(), user.getPasswordHash(), user.getRole());
        String accessToken = jwtTokenProvider.generateAcessToken(principal);
        var refreshToken = refreshTokenService.createRefreshToken(user.getId());

        return new AuthResponse(
            user.getId(), user.getEmail(), user.getDisplayName(),
            accessToken, refreshToken.getToken(), 900000L
        );
    }

    public TokenRefreshResponse refresh(String refreshToken) {
        var token = refreshTokenService.validateRefreshToken(refreshToken);
        User user = userRepository.findById(token.getUserId())
            .orElseThrow(() -> new RuntimeException("User not found"));

        UserPrincipal principal = new UserPrincipal(user.getId(), user.getEmail(), user.getPasswordHash(), user.getRole());
        String accessToken = jwtTokenProvider.generateAcessToken(principal);

        return new TokenRefreshResponse(accessToken, 900000L);
    }

    @Transactional
    public void logout(String refreshToken) {
        refreshTokenService.revokeRefreshToken(refreshToken);
    }
}
