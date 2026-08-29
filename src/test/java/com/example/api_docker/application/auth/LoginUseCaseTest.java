package com.example.api_docker.application.auth;

import com.example.api_docker.UnitAbstractTests;
import com.example.api_docker.application.shared.LoginCommand;
import com.example.api_docker.application.shared.LoginResult;
import com.example.api_docker.domain.user.*;
import com.example.api_docker.domain.user.exception.InvalidCredentialsException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginUseCaseTest extends UnitAbstractTests {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenGenerator tokenGenerator;

    @InjectMocks
    private LoginUseCase loginUseCase;

    private static final String RAW_PASSWORD = "senha123";
    private static final String HASHED_PASSWORD = "hash-senha123";
    private static final String TOKEN = "jwt-token-gerado";

    @Test
    @DisplayName("Deve autenticar admin com credenciais válidas")
    void shouldAuthenticateAdminWithValidCredentials() {
        Email email = new Email("guilherme@email.com");
        User user = User.restore(
                new UserId(UUID.randomUUID()),
                new FullName("Guilherme", "Taliberti"),
                email,
                HASHED_PASSWORD,
                LocalDateTime.now()
        );

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(RAW_PASSWORD, HASHED_PASSWORD)).thenReturn(true);
        when(tokenGenerator.generate(user.getId(), email, UserRole.ADMIN)).thenReturn(TOKEN);

        LoginResult result = loginUseCase.execute(new LoginCommand("guilherme@email.com", RAW_PASSWORD));

        assertEquals(TOKEN, result.token());
        assertEquals("ADMIN", result.role());
        assertEquals("Guilherme Taliberti", result.fullName());
    }

    @Test
    @DisplayName("Deve lançar exceção quando senha incorreta para admin")
    void shouldThrowExceptionWhenPasswordIsIncorrectForAdmin() {
        Email email = new Email("guilherme@email.com");
        User user = User.restore(
                new UserId(UUID.randomUUID()),
                new FullName("Guilherme", "Taliberti"),
                email,
                HASHED_PASSWORD,
                LocalDateTime.now()
        );

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("senha-errada", HASHED_PASSWORD)).thenReturn(false);

        assertThrows(
                InvalidCredentialsException.class,
                () -> loginUseCase.execute(new LoginCommand("guilherme@email.com", "senha-errada"))
        );
    }

    @Test
    @DisplayName("Deve lançar exceção quando usuário não encontrado")
    void shouldThrowExceptionWhenUserNotFound() {
        Email email = new Email("guilherme@email.com");

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(
                InvalidCredentialsException.class,
                () -> loginUseCase.execute(new LoginCommand("guilherme@email.com", RAW_PASSWORD))
        );
    }

    @Test
    @DisplayName("Deve retornar token com role correta para cada tipo de usuário")
    void shouldReturnTokenWithCorrectRoleForEachUserType() {
        Email userEmail = new Email("guilherme.admin@email.com");

        User user = User.restore(
                new UserId(UUID.randomUUID()),
                new FullName("Guilherme", "Taliberti"),
                userEmail,
                HASHED_PASSWORD,
                LocalDateTime.now()
        );

        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(RAW_PASSWORD, HASHED_PASSWORD)).thenReturn(true);

        LoginResult userResult = loginUseCase.execute(
                new LoginCommand("guilherme.admin@email.com", RAW_PASSWORD)
        );
        assertEquals("ADMIN", userResult.role());
    }
}