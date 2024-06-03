package ru.daniil.worker.repositories.processorscheck;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import ru.daniil.worker.domains.ProcessorsCheckEntity;


@Repository
@RequiredArgsConstructor
public class ProcessorsCheckRepositoryImpl implements ProcessorsCheckRepository {
    private final JdbcClient jdbcClient;

    @Override
    public void save(@NotNull ProcessorsCheckEntity processorsCheckEntity) {
        jdbcClient.sql("insert into processors_check values (?, ?, ?) returning *")
                .params(processorsCheckEntity.imageId(),
                        processorsCheckEntity.requestId(),
                        processorsCheckEntity.filterType().name())
                .query(ProcessorsCheckEntity.class)
                .single();
    }

    @Override
    public Boolean exist(@NotNull ProcessorsCheckEntity processorsCheckEntity) {
        return jdbcClient.sql("select exists(select 1 from processors_check "
                        + "where image_id=? and request_id =? and filter_type=?)")
                .params(processorsCheckEntity.imageId(),
                        processorsCheckEntity.requestId(),
                        processorsCheckEntity.filterType().name())
                .query(Boolean.class)
                .single();
    }
}
