package com.grim.contextos.common.util;

import com.grim.contextos.auth.model.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;

public class SecurityUtil {

    public static Optional<UUID> getCurrentUserId() {
        return getCurrentUser().map(UserPrincipal::id);
    }

    public static Optional<String> getCurrentUserEmail() {
        return getCurrentUser().map(UserPrincipal::email);
    }

    public static Optional<UserPrincipal> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof UserPrincipal user) {
            return Optional.of(user);
        }
        return Optional.empty();
    }
}

