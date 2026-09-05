package com.example.api_docker.infra.controller.user;

import com.example.api_docker.application.user.query.GetUserQuery;
import com.example.api_docker.application.user.usecase.ChangeUserPasswordUseCase;
import com.example.api_docker.application.user.usecase.CreateUserUseCase;
import com.example.api_docker.application.user.usecase.GetUserUseCase;
import com.example.api_docker.domain.user.UserId;
import com.example.api_docker.infra.controller.user.request.ChangePasswordRequest;
import com.example.api_docker.infra.controller.user.request.CreateUserRequest;
import com.example.api_docker.infra.controller.user.response.UserResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final CreateUserUseCase createUserUseCase;
    private final GetUserUseCase getUserUseCase;
    private final ChangeUserPasswordUseCase changeUserPasswordUseCase;

    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody @Valid CreateUserRequest request) {
        createUserUseCase.execute(request.toCommand());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(@AuthenticationPrincipal UserId userId) {
        var result = getUserUseCase.execute(new GetUserQuery(userId));
        return ResponseEntity.ok(UserResponse.from(result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> findById(@PathVariable UUID id) {
        var result = getUserUseCase.execute(new GetUserQuery(new UserId(id)));
        return ResponseEntity.ok(UserResponse.from(result));
    }

    @PatchMapping("/password")
    public ResponseEntity<Void> changePassword(@AuthenticationPrincipal UserId userId,
                                               @RequestBody @Valid ChangePasswordRequest request) {
        changeUserPasswordUseCase.execute(request.toCommand(userId));
        return ResponseEntity.noContent().build();
    }
}
