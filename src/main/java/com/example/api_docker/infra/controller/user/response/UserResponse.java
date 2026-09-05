package com.example.api_docker.infra.controller.user.response;

import com.example.api_docker.application.user.result.UserResult;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserResponse(
        UUID userId,
        String fullName,
        String email,
        LocalDateTime createdAt
) {
    public static UserResponse from(UserResult result) {
        return new UserResponse(
                result.userId(),
                result.fullName(),
                result.email(),
                result.createdAt()
        );
    }
}
