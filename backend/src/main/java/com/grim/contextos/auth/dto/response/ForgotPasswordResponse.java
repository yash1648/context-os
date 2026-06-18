package com.grim.contextos.auth.dto.response;

public record ForgotPasswordResponse(
    String message,
    String token
) {
    public static ForgotPasswordResponse of(String token) {
        return new ForgotPasswordResponse(
            "If the email exists, a reset link has been sent",
            token
        );
    }
}
