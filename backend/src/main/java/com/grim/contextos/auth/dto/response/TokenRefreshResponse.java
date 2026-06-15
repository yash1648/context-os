package com.grim.contextos.auth.dto.response;

public record TokenRefreshResponse(String accessToken, long expiresIn) {}
