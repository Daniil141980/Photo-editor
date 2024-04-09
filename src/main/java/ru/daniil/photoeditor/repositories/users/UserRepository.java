package ru.daniil.photoeditor.repositories.users;

import ru.daniil.photoeditor.domains.UserEntity;

import java.util.Optional;

public interface UserRepository {
    UserEntity save(String username, String password);

    Boolean existsByUsername(String username);

    Optional<UserEntity> getByUsername(String username);

    Optional<UserEntity> getById(Long id);
}
