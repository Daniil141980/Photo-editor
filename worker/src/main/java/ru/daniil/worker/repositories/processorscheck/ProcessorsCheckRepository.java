package ru.daniil.worker.repositories.processorscheck;

import ru.daniil.worker.domains.ProcessorsCheckEntity;

public interface ProcessorsCheckRepository {
    void save(ProcessorsCheckEntity processorsCheckEntity);

    Boolean exist(ProcessorsCheckEntity processorsCheckEntity);
}
