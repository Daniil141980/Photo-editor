package ru.daniil.api.repositories.users;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import ru.daniil.api.domains.UserEntity;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {
    private final JdbcClient jdbcClient;

    @Override
    public UserEntity save(String username, String password) {
        return jdbcClient.sql("insert into users values (default, ?, ?) returning *")
                .params(username, password)
                .query(UserEntity.class)
                .single();
    }

    @Override
    public Boolean existsByUsername(String username) {
        return jdbcClient.sql("select exists(select 1 from users where username = ?)")
                .param(username)
                .query(Boolean.class)
                .single();
    }

    @Override
    public Optional<UserEntity> getByUsername(String username) {
        return jdbcClient.sql("select * from users where username = ?")
                .param(username)
                .query(UserEntity.class)
                .optional();
    }

    @Override
    public Optional<UserEntity> getById(Long id) {
        return jdbcClient.sql("select * from users where id = ?")
                .param(id)
                .query(UserEntity.class)
                .optional();
    }
}
