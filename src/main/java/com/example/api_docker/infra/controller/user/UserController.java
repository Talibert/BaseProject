package com.example.api_docker.infra.controller.user;

import com.example.api_docker.application.user.command.CreateUserCommand;
import com.example.api_docker.application.user.query.GetUserQuery;
import com.example.api_docker.application.user.result.UserResult;
import com.example.api_docker.application.user.usecase.CreateUserUseCase;
import com.example.api_docker.application.user.usecase.GetUserUseCase;
import com.example.api_docker.domain.user.UserId;
import com.example.api_docker.infra.controller.user.request.CreateUserRequest;
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

    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody @Valid CreateUserRequest request) {
        CreateUserCommand command = new CreateUserCommand(
                request.firstName(),
                request.lastName(),
                request.email(),
                request.password()
        );
        createUserUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/me")
    public ResponseEntity<UserResult> me(@AuthenticationPrincipal UserId userId) {
        var result = getUserUseCase.execute(new GetUserQuery(userId));
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResult> findById(@PathVariable UUID id) {
        var result = getUserUseCase.execute(new GetUserQuery(new UserId(id)));
        return ResponseEntity.ok(result);
    }
}
