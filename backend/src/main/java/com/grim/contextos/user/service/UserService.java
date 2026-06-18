package com.grim.contextos.user.service;

import com.grim.contextos.user.dto.UpdateUserRequest;
import com.grim.contextos.user.model.User;
import com.grim.contextos.user.repository.UserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getUserById(UUID id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<Map<String, Object>> listAllUsers() {
        return userRepository.findAll().stream()
            .map(user -> Map.<String, Object>of(
                "id", user.getId(),
                "email", user.getEmail(),
                "displayName", user.getDisplayName(),
                "avatarUrl", user.getAvatarUrl() != null ? user.getAvatarUrl() : "",
                "role", user.getRole().name(),
                "createdAt", user.getCreatedAt() != null ? user.getCreatedAt().toString() : ""
            ))
            .toList();
    }

    @Transactional
    public Map<String, Object> updateProfile(UUID userId, UpdateUserRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        if (request.displayName() != null) {
            user.setDisplayName(request.displayName());
        }
        if (request.avatarUrl() != null) {
            user.setAvatarUrl(request.avatarUrl());
        }
        if (request.settings() != null) {
            user.setSettings(request.settings());
        }

        userRepository.save(user);

        return Map.of(
            "id", user.getId(),
            "email", user.getEmail(),
            "displayName", user.getDisplayName(),
            "avatarUrl", user.getAvatarUrl() != null ? user.getAvatarUrl() : "",
            "role", user.getRole().name()
        );
    }
}
