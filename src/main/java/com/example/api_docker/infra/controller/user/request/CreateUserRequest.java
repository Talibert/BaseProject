package com.example.api_docker.infra.controller.user.request;

import com.example.api_docker.application.user.command.CreateUserCommand;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @NotBlank(message = "Nome não pode ser vazio")
        String firstName,

        @NotBlank(message = "Sobrenome não pode ser vazio")
        String lastName,

        @NotBlank(message = "Email não pode ser vazio")
        @Email(message = "Email inválido")
        String email,

        @NotBlank(message = "Senha não pode ser vazia")
        @Size(min = 8, message = "Senha deve ter no mínimo 8 caracteres")
        String password
) {
    public CreateUserCommand toCommand() {
        return new CreateUserCommand(firstName, lastName, email, password);
    }
}
