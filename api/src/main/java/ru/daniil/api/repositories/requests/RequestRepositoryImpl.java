package ru.daniil.api.repositories.requests;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import ru.daniil.api.domains.FileProcessingStatus;
import ru.daniil.api.domains.RequestEntity;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class RequestRepositoryImpl implements RequestRepository {
    private final JdbcClient jdbcClient;

    @Override
    public UUID save(RequestEntity requestEntity) {
        return jdbcClient.sql("insert into requests values (?, ?, default, ?) returning id")
                .params(requestEntity.id(), requestEntity.imageId(), requestEntity.status().name())
                .query(UUID.class)
                .single();
    }

    @Override
    public Optional<RequestEntity> get(UUID id) {
        return jdbcClient.sql("select * from requests where id = ?")
                .param(id)
                .query(RequestEntity.class)
                .optional();
    }

    @Override
    public void updateIdModifiedAndStatus(UUID id,
                                          UUID imageModifiedId,
                                          FileProcessingStatus fileProcessingStatus) {
        jdbcClient.sql("update requests set image_modified_id = ?, status = ? where id = ? returning *")
                .params(imageModifiedId, fileProcessingStatus.name(), id)
                .query(RequestEntity.class)
                .single();
    }

    @Override
    public UUID getOldImageId(UUID requestId) {
        return jdbcClient.sql("select image_id from requests where id = ?")
                .param(requestId)
                .query(UUID.class)
                .single();
    }
}
