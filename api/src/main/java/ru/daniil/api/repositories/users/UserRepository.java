package ru.daniil.api.repositories.users;

import ru.daniil.api.domains.UserEntity;

import java.util.Optional;

public interface UserRepository {
    UserEntity save(String username, String password);

    Boolean existsByUsername(String username);

    Optional<UserEntity> getByUsername(String username);

    Optional<UserEntity> getById(Long id);
}
