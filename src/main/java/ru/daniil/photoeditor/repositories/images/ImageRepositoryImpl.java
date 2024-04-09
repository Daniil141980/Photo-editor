package ru.daniil.photoeditor.repositories.images;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import ru.daniil.photoeditor.domains.ImageEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ImageRepositoryImpl implements ImageRepository {
    private final JdbcClient jdbcClient;

    @Override
    public ImageEntity save(@NotNull ImageEntity imageEntity) {
        return jdbcClient.sql("insert into images values (?, ?, ?, ?) returning *")
                .params(imageEntity.id(), imageEntity.filename(), imageEntity.size(), imageEntity.userId())
                .query(ImageEntity.class)
                .single();
    }

    @Override
    public List<ImageEntity> getByUserId(Long userId) {
        return jdbcClient.sql("select * from images where user_id = ?")
                .param(userId)
                .query(ImageEntity.class)
                .list();
    }

    @Override
    public Optional<ImageEntity> get(UUID id) {
        return jdbcClient.sql("select * from images where id = ?")
                .param(id)
                .query(ImageEntity.class)
                .optional();
    }

    @Override
    public void remove(UUID id) {
        jdbcClient.sql("delete from images where id = ?")
                .param(id)
                .update();
    }
}
