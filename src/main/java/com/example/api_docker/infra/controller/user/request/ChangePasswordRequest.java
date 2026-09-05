package com.example.api_docker.infra.controller.user.request;

import com.example.api_docker.application.user.command.ChangeUserPasswordCommand;
import com.example.api_docker.domain.user.UserId;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @NotBlank(message = "A senha atual é obrigatória")
        String currentPassword,

        @NotBlank(message = "A nova senha é obrigatória")
        @Size(min = 6, message = "A nova senha deve ter no mínimo 6 caracteres")
        String newPassword
) {
    public ChangeUserPasswordCommand toCommand(UserId userId) {
        return new ChangeUserPasswordCommand(userId, currentPassword, newPassword);
    }
}
