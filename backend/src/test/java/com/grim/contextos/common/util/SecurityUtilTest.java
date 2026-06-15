package com.grim.contextos.common.util;

import com.grim.contextos.auth.model.Role;
import com.grim.contextos.auth.model.UserPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SecurityUtilTest {

    private final UUID userId = UUID.randomUUID();
    private final UserPrincipal principal = new UserPrincipal(userId, "test@test.com", "hash", Role.USER);
    private Authentication authentication;
    private SecurityContext securityContext;

    @BeforeEach
    void setUp() {
        authentication = mock(Authentication.class);
        securityContext = mock(SecurityContext.class);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentUserReturnsPrincipalWhenAuthenticated() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(principal);
        SecurityContextHolder.setContext(securityContext);

        Optional<UserPrincipal> result = SecurityUtil.getCurrentUser();

        assertTrue(result.isPresent());
        assertEquals(userId, result.get().id());
        assertEquals("test@test.com", result.get().email());
    }

    @Test
    void getCurrentUserReturnsEmptyWhenNotAuthenticated() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);
        SecurityContextHolder.setContext(securityContext);

        assertTrue(SecurityUtil.getCurrentUser().isEmpty());
    }

    @Test
    void getCurrentUserReturnsEmptyWhenNoAuth() {
        when(securityContext.getAuthentication()).thenReturn(null);
        SecurityContextHolder.setContext(securityContext);

        assertTrue(SecurityUtil.getCurrentUser().isEmpty());
    }

    @Test
    void getCurrentUserReturnsEmptyWhenNotUserPrincipal() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("anonymousUser");
        SecurityContextHolder.setContext(securityContext);

        assertTrue(SecurityUtil.getCurrentUser().isEmpty());
    }

    @Test
    void getCurrentUserIdReturnsId() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(principal);
        SecurityContextHolder.setContext(securityContext);

        Optional<UUID> result = SecurityUtil.getCurrentUserId();
        assertTrue(result.isPresent());
        assertEquals(userId, result.get());
    }

    @Test
    void getCurrentUserIdReturnsEmptyWhenNoUser() {
        SecurityContextHolder.clearContext();
        assertTrue(SecurityUtil.getCurrentUserId().isEmpty());
    }

    @Test
    void getCurrentUserEmailReturnsEmail() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(principal);
        SecurityContextHolder.setContext(securityContext);

        Optional<String> result = SecurityUtil.getCurrentUserEmail();
        assertTrue(result.isPresent());
        assertEquals("test@test.com", result.get());
    }

    @Test
    void getCurrentUserEmailReturnsEmptyWhenNoUser() {
        SecurityContextHolder.clearContext();
        assertTrue(SecurityUtil.getCurrentUserEmail().isEmpty());
    }
}
