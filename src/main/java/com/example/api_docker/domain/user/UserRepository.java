package com.example.api_docker.domain.user;

import java.util.Optional;

public interface UserRepository {
    boolean existsByEmail(Email email);
    void save(User admin);
    Optional<User> findById(UserId id);
    Optional<User> findByEmail(Email email);
}
