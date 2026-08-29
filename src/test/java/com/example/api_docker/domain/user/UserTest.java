package com.example.api_docker.domain.user;

import com.example.api_docker.UnitAbstractTests;
import com.example.api_docker.domain.shared.DomainEvent;
import com.example.api_docker.domain.shared.exception.DomainException;
import com.example.api_docker.domain.user.event.UserCreatedEvent;
import com.example.api_docker.domain.user.event.UserPasswordChangedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserTest extends UnitAbstractTests {

    @Test
    @DisplayName("Deve criar user com sucesso e registrar evento UserCreatedEvent")
    void shouldCreateUserSuccessfullyWithEvent() {
        FullName name = new FullName("Guilherme", "Taliberti");
        Email email = new Email("user@email.com");
        String passwordHash = "hash123";

        User user = User.create(name, email, passwordHash);

        assertNotNull(user.getId());
        assertEquals(name, user.getName());
        assertEquals(email, user.getEmail());
        assertEquals(passwordHash, user.getPasswordHash());
        assertNotNull(user.getCreatedAt());

        List<DomainEvent> events = user.pullDomainEvents();
        assertEquals(1, events.size());

        UserCreatedEvent event = (UserCreatedEvent) events.getFirst();
        assertEquals(user.getId(), event.userId());
        assertEquals(email, event.email());

        assertTrue(user.pullDomainEvents().isEmpty());
    }

    @Test
    @DisplayName("Deve restaurar user sem registrar eventos de domínio")
    void shouldRestoreUserWithoutDomainEvents() {
        UserId id = UserId.generate();
        FullName name = new FullName("Guilherme", "Taliberti");
        Email email = new Email("user@email.com");
        String passwordHash = "hash123";
        LocalDateTime createdAt = LocalDateTime.now().minusDays(1);

        User user = User.restore(id, name, email, passwordHash, createdAt);

        assertEquals(id, user.getId());
        assertEquals(name, user.getName());
        assertEquals(email, user.getEmail());
        assertEquals(passwordHash, user.getPasswordHash());
        assertEquals(createdAt, user.getCreatedAt());
        assertTrue(user.pullDomainEvents().isEmpty());
    }

    @Test
    @DisplayName("Deve alterar senha do user com sucesso e registrar evento UserPasswordChangedEvent")
    void shouldChangePasswordSuccessfullyWithEvent() {
        User user = User.create(
                new FullName("Guilherme", "Taliberti"),
                new Email("user@email.com"),
                "hash-antigo"
        );
        user.pullDomainEvents();

        user.changePassword("hash-novo");

        assertEquals("hash-novo", user.getPasswordHash());

        List<DomainEvent> events = user.pullDomainEvents();
        assertEquals(1, events.size());

        UserPasswordChangedEvent event = (UserPasswordChangedEvent) events.getFirst();
        assertEquals(user.getId(), event.userId());
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar alterar senha para valor nulo ou em branco")
    void shouldThrowExceptionWhenChangingPasswordToInvalidValue() {
        User user = User.create(
                new FullName("Guilherme", "Taliberti"),
                new Email("user@email.com"),
                "hash-antigo"
        );

        DomainException nullException = assertThrows(
                DomainException.class,
                () -> user.changePassword(null)
        );
        assertEquals("Hash da senha não pode ser vazio", nullException.getMessage());

        DomainException blankException = assertThrows(
                DomainException.class,
                () -> user.changePassword("   ")
        );
        assertEquals("Hash da senha não pode ser vazio", blankException.getMessage());
    }
}
