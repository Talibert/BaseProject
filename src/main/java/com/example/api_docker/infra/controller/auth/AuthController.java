package com.example.api_docker.infra.controller.auth;

import com.example.api_docker.application.auth.usecase.LoginUseCase;
import com.example.api_docker.infra.controller.auth.request.LoginRequest;
import com.example.api_docker.infra.controller.auth.response.LoginResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final LoginUseCase loginUseCase;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
        var result = loginUseCase.execute(request.toCommand());
        return ResponseEntity.ok(LoginResponse.from(result));
    }
}
