package com.example.api_docker.application.user.usecase;

import com.example.api_docker.UnitAbstractTests;
import com.example.api_docker.application.user.command.CreateUserCommand;
import com.example.api_docker.domain.shared.DomainEventPublisher;
import com.example.api_docker.domain.user.Email;
import com.example.api_docker.domain.user.PasswordEncoder;
import com.example.api_docker.domain.user.User;
import com.example.api_docker.domain.user.UserRepository;
import com.example.api_docker.domain.user.event.UserCreatedEvent;
import com.example.api_docker.domain.user.exception.EmailAlreadyInUseException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateUserUseCaseTest extends UnitAbstractTests {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private DomainEventPublisher eventPublisher;

    @Spy
    @InjectMocks
    private CreateUserUseCase createUserUseCase;

    @Test
    @DisplayName("Deve criar user com dados válidos")
    void shouldCreateUserWithValidData() {
        CreateUserCommand command = new CreateUserCommand(
                "João", "Silva", "joao@email.com", "senha123"
        );

        when(userRepository.existsByEmail(new Email(command.email()))).thenReturn(false);
        when(passwordEncoder.encode(command.rawPassword())).thenReturn("hash-senha");

        createUserUseCase.execute(command);

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando email já está em uso")
    void shouldThrowExceptionWhenEmailAlreadyInUse() {
        CreateUserCommand command = new CreateUserCommand(
                "João", "Silva", "joao@email.com", "senha123"
        );

        when(userRepository.existsByEmail(new Email(command.email()))).thenReturn(true);

        assertThrows(
                EmailAlreadyInUseException.class,
                () -> createUserUseCase.execute(command)
        );

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Deve salvar o user no repositório após criação")
    void shouldSaveUserInRepository() {
        CreateUserCommand command = new CreateUserCommand(
                "João", "Silva", "joao@email.com", "senha123"
        );

        when(userRepository.existsByEmail(new Email(command.email()))).thenReturn(false);
        when(passwordEncoder.encode(command.rawPassword())).thenReturn("hash-senha");

        createUserUseCase.execute(command);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());

        User savedUser = captor.getValue();
        assertEquals("joao@email.com", savedUser.getEmail().value());
        assertEquals("João", savedUser.getName().firstName());
        assertEquals("Silva", savedUser.getName().lastName());
    }

    @Test
    @DisplayName("Deve publicar evento após criar o user")
    void shouldPublishEventAfterCreatingUser() {
        CreateUserCommand command = new CreateUserCommand(
                "João", "Silva", "joao@email.com", "senha123"
        );

        when(userRepository.existsByEmail(new Email(command.email()))).thenReturn(false);
        when(passwordEncoder.encode(command.rawPassword())).thenReturn("hash-senha");

        createUserUseCase.execute(command);

        verify(eventPublisher, times(1)).publish(any(UserCreatedEvent.class));
    }
}