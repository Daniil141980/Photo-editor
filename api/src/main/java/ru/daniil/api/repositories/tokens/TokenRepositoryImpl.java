package ru.daniil.api.repositories.tokens;

import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TokenRepositoryImpl implements TokenRepository {
    private final JdbcClient jdbcClient;

    @Override
    public String save(String token, Long userId) {
        return jdbcClient.sql("insert into tokens values (default, ?, ?) returning token")
                .params(token, userId)
                .query(String.class)
                .single();
    }

    @Override
    public Boolean existsByToken(String token) {
        return jdbcClient.sql("select exists(select 1 from tokens where token = ?)")
                .param(token)
                .query(Boolean.class)
                .single();
    }

    @Override
    public Optional<Pair<Long, Long>> getUserIdByToken(String token) {
        return jdbcClient.sql("select id, user_id from tokens where token=?")
                .param(token)
                .query((rs, rowNum) -> Pair.of(rs.getLong("id"), rs.getLong("user_id")))
                .optional();
    }

    @Override
    public String updateToken(Long id, String token) {
        return jdbcClient.sql("update tokens set token=? where id=? returning token")
                .params(token, id)
                .query(String.class)
                .single();
    }

    @Override
    public void removeByToken(String token) {
        jdbcClient.sql("delete from tokens where token = ?")
                .param(token)
                .update();
    }
}
