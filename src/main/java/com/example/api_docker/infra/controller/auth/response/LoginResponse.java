package com.example.api_docker.infra.controller.auth.response;

import com.example.api_docker.application.auth.result.LoginResult;

import java.util.UUID;

public record LoginResponse(
        String token,
        UUID userId,
        String fullName,
        String role
) {
    public static LoginResponse from(LoginResult result) {
        return new LoginResponse(
                result.token(),
                result.userId(),
                result.fullName(),
                result.role()
        );
    }
}
