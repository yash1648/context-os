package com.grim.contextos.user.controller;

import com.grim.contextos.auth.model.UserPrincipal;
import com.grim.contextos.common.response.ApiResponse;
import com.grim.contextos.user.model.User;
import com.grim.contextos.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getProfile(@AuthenticationPrincipal UserPrincipal principal) {
        User user = userService.getUserById(principal.id());
        Map<String, Object> profile = Map.of(
            "id", user.getId(),
            "email", user.getEmail(),
            "displayName", user.getDisplayName(),
            "avatarUrl", user.getAvatarUrl() != null ? user.getAvatarUrl() : "",
            "role", user.getRole().name()
        );
        return ResponseEntity.ok(ApiResponse.ok(profile));
    }
}
