package com.example.api_docker.application.user.result;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserResult(UUID userId, String fullName, String email, LocalDateTime createdAt) {}
