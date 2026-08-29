package com.example.api_docker.application.user.usecase;

import com.example.api_docker.UnitAbstractTests;
import com.example.api_docker.application.user.query.GetUserQuery;
import com.example.api_docker.application.user.result.UserResult;
import com.example.api_docker.domain.user.*;
import com.example.api_docker.domain.user.exception.UserNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetUserUseCaseTest extends UnitAbstractTests {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private GetUserUseCase getUserUseCase;

    @Test
    @DisplayName("Deve retornar user quando encontrado pelo id")
    void shouldReturnUserWhenFoundById() {
        UserId userId = new UserId(UUID.randomUUID());
        LocalDateTime createdAt = LocalDateTime.now();
        User user = User.restore(
                userId,
                new FullName("Guilherme", "Taliberti"),
                new Email("guilherme@email.com"),
                "hash-senha",
                createdAt
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        UserResult result = getUserUseCase.execute(new GetUserQuery(userId));

        assertNotNull(result);
        assertEquals(userId.value(), result.userId());

        assertEquals(userId.value(), result.userId());
        assertEquals("Guilherme Taliberti", result.fullName());
        assertEquals("guilherme@email.com", result.email());
        assertEquals(createdAt, result.createdAt());
    }

    @Test
    @DisplayName("Deve lançar exceção quando user não encontrado")
    void shouldThrowExceptionWhenUserNotFound() {
        UserId userId = new UserId(UUID.randomUUID());

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> getUserUseCase.execute(new GetUserQuery(userId))
        );
    }
}