package com.example.api_docker.infra.controller.auth.request;

import com.example.api_docker.application.auth.command.LoginCommand;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "Email não pode ser vazio")
        @Email(message = "Email inválido")
        String email,

        @NotBlank(message = "Senha não pode ser vazia")
        String password
) {
    public LoginCommand toCommand() {
        return new LoginCommand(email, password);
    }
}
