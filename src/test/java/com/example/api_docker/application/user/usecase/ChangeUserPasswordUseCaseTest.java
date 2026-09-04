package com.example.api_docker.application.user.usecase;

import com.example.api_docker.UnitAbstractTests;
import com.example.api_docker.application.user.command.ChangeUserPasswordCommand;
import com.example.api_docker.domain.shared.DomainEventPublisher;
import com.example.api_docker.domain.shared.exception.DomainException;
import com.example.api_docker.domain.user.*;
import com.example.api_docker.domain.user.event.UserPasswordChangedEvent;
import com.example.api_docker.domain.user.exception.InvalidCredentialsException;
import com.example.api_docker.domain.user.exception.UserNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChangeUserPasswordUseCaseTest extends UnitAbstractTests {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private DomainEventPublisher eventPublisher;

    @Spy
    @InjectMocks
    private ChangeUserPasswordUseCase changeUserPasswordUseCase;

    @Test
    @DisplayName("Deve alterar senha com sucesso e publicar evento")
    void shouldChangePasswordSuccessfully() {
        User user = User.create(new FullName("Guilherme", "Taliberti"),
                new Email("user@email.com"), "hash-antigo");
        user.pullDomainEvents();

        ChangeUserPasswordCommand command = new ChangeUserPasswordCommand(
                user.getId(), "senhaAtual", "novaSenha"
        );

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("senhaAtual", "hash-antigo")).thenReturn(true);
        when(passwordEncoder.matches("novaSenha", "hash-antigo")).thenReturn(false);
        when(passwordEncoder.encode("novaSenha")).thenReturn("hash-novo");

        changeUserPasswordUseCase.execute(command);

        assertEquals("hash-novo", user.getPasswordHash());
        verify(userRepository, times(1)).save(user);
        verify(eventPublisher, times(1)).publish(any(UserPasswordChangedEvent.class));
    }

    @Test
    @DisplayName("Deve lançar InvalidCredentialsException quando a senha atual for incorreta")
    void shouldThrowExceptionWhenCurrentPasswordIsWrong() {
        User user = User.create(new FullName("Guilherme", "Taliberti"),
                new Email("user@email.com"), "hash-antigo");

        ChangeUserPasswordCommand command = new ChangeUserPasswordCommand(
                user.getId(), "senhaIncorreta", "novaSenha"
        );

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("senhaIncorreta", "hash-antigo")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> changeUserPasswordUseCase.execute(command));

        verify(userRepository, never()).save(any());
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    @DisplayName("Deve lançar DomainException quando a nova senha for igual à senha atual")
    void shouldThrowExceptionWhenNewPasswordIsSameAsCurrent() {
        User user = User.create(new FullName("Guilherme", "Taliberti"),
                new Email("user@email.com"), "hash-antigo");

        ChangeUserPasswordCommand command = new ChangeUserPasswordCommand(
                user.getId(), "senhaAtual", "senhaAtual"
        );

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("senhaAtual", "hash-antigo")).thenReturn(true);

        DomainException exception = assertThrows(DomainException.class,
                () -> changeUserPasswordUseCase.execute(command));
        assertEquals("A nova senha não pode ser igual à senha atual", exception.getMessage());

        verify(userRepository, never()).save(any());
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    @DisplayName("Deve lançar UserNotFoundException quando o usuário não existir")
    void shouldThrowExceptionWhenUserNotFound() {
        UserId userId = UserId.generate();
        ChangeUserPasswordCommand command = new ChangeUserPasswordCommand(
                userId, "senhaAtual", "novaSenha"
        );

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> changeUserPasswordUseCase.execute(command));

        verify(userRepository, never()).save(any());
        verify(eventPublisher, never()).publish(any());
    }
}
