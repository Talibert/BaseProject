package com.example.api_docker.domain.user;

import com.example.api_docker.RepositoryAbstractTests;
import com.example.api_docker.domain.shared.pagination.PageResult;
import com.example.api_docker.domain.shared.pagination.PaginationRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserRepositoryTest extends RepositoryAbstractTests {

    @Autowired
    private UserRepository userRepository;

    private User buildUser(String email) {
        return User.restore(
                new UserId(UUID.randomUUID()),
                new FullName(DEFAULT_FIRST_NAME, DEFAULT_LAST_NAME),
                new Email(email),
                DEFAULT_PASSWORD_HASH,
                LocalDateTime.now()
        );
    }

    @Test
    @DisplayName("Deve salvar e encontrar user pelo id")
    void shouldSaveAndFindUserById() {
        User user = buildUser(DEFAULT_EMAIL);
        userRepository.save(user);

        Optional<User> found = userRepository.findById(user.getId());

        assertTrue(found.isPresent());
        assertEquals(user.getId(), found.get().getId());
        assertEquals(DEFAULT_EMAIL, found.get().getEmail().value());
        assertEquals(DEFAULT_FIRST_NAME, found.get().getName().firstName());
        assertEquals(DEFAULT_LAST_NAME, found.get().getName().lastName());
    }

    @Test
    @DisplayName("Deve salvar e encontrar user pelo email")
    void shouldSaveAndFindUserByEmail() {
        User user = buildUser(DEFAULT_EMAIL);
        userRepository.save(user);

        Optional<User> found = userRepository.findByEmail(new Email(DEFAULT_EMAIL));

        assertTrue(found.isPresent());
        assertEquals(DEFAULT_EMAIL, found.get().getEmail().value());
    }

    @Test
    @DisplayName("Deve retornar vazio quando user não encontrado pelo id")
    void shouldReturnEmptyWhenUserNotFoundById() {
        Optional<User> found = userRepository.findById(new UserId(UUID.randomUUID()));

        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("Deve retornar vazio quando user não encontrado pelo email")
    void shouldReturnEmptyWhenUserNotFoundByEmail() {
        Optional<User> found = userRepository.findByEmail(new Email("naoexiste@email.com"));

        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("Deve atualizar user ao salvar com mesmo id")
    void shouldUpdateUserWhenSavingWithSameId() {
        User user = buildUser(DEFAULT_EMAIL);
        userRepository.save(user);

        User updated = User.restore(
                user.getId(),
                new FullName("Novo", "Nome"),
                new Email("novo@email.com"),
                "novo-hash",
                user.getCreatedAt()
        );
        userRepository.save(updated);

        Optional<User> found = userRepository.findById(user.getId());

        assertTrue(found.isPresent());
        assertEquals("Novo", found.get().getName().firstName());
        assertEquals("novo@email.com", found.get().getEmail().value());
    }

    @Test
    @DisplayName("Deve listar usuários de forma paginada")
    void shouldFindUsersPaginated() {
        for (int i = 1; i <= 5; i++) {
            userRepository.save(buildUser("paginated" + i + "@email.com"));
        }

        PageResult<User> page1 = userRepository.findAll(PaginationRequest.of(0, 2, "email", "ASC"));

        assertEquals(2, page1.items().size());
        assertEquals(0, page1.page());
        assertEquals(2, page1.size());
        assertTrue(page1.totalElements() >= 5);
        assertTrue(page1.totalPages() >= 3);
        assertTrue(page1.isFirst());
        assertFalse(page1.isLast());

        PageResult<User> page2 = userRepository.findAll(PaginationRequest.of(1, 2, "email", "ASC"));
        assertEquals(2, page2.items().size());
        assertEquals(1, page2.page());
        assertFalse(page2.isFirst());
    }
}