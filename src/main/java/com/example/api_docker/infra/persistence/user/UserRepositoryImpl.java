package com.example.api_docker.infra.persistence.user;

import com.example.api_docker.domain.user.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class UserRepositoryImpl implements UserRepository {

    private final UserJpaRepository userJpaRepository;

    @Override
    public boolean existsByEmail(Email email) {
        return userJpaRepository.existsByEmail(email.value());
    }

    @Override
    public void save(User admin) {
        userJpaRepository.save(toJpaEntity(admin));
    }

    @Override
    public Optional<User> findById(UserId id) {
        return userJpaRepository.findById(id.value()).map(this::toDomain);
    }

    @Override
    public Optional<User> findByEmail(Email email) {
        return userJpaRepository.findByEmail(email.value()).map(this::toDomain);
    }

    private UserJpaEntity toJpaEntity(User user) {
        UserJpaEntity entity = new UserJpaEntity();
        entity.setId(user.getId().value());
        entity.setFirstName(user.getName().firstName());
        entity.setLastName(user.getName().lastName());
        entity.setEmail(user.getEmail().value());
        entity.setPasswordHash(user.getPasswordHash());
        entity.setCreatedAt(user.getCreatedAt());
        return entity;
    }

    private User toDomain(UserJpaEntity entity) {
        return User.restore(
                new UserId(entity.getId()),
                new FullName(entity.getFirstName(), entity.getLastName()),
                new Email(entity.getEmail()),
                entity.getPasswordHash(),
                entity.getCreatedAt()
        );
    }
}
