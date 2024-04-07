package ru.daniil.photoeditor.repositories.tokens;


import org.springframework.data.util.Pair;

import java.util.Optional;

public interface TokenRepository {
    String save(String token, Long userId);

    Boolean existsByToken(String token);

    Optional<Pair<Long, Long>> getUserIdByToken(String token);

    String updateToken(Long id, String token);

    void removeByToken(String token);
}
