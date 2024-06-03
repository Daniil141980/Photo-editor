package ru.daniil.api.repositories.requests;

import ru.daniil.api.domains.FileProcessingStatus;
import ru.daniil.api.domains.RequestEntity;

import java.util.Optional;
import java.util.UUID;


public interface RequestRepository {

    UUID save(RequestEntity imageEntity);

    Optional<RequestEntity> get(UUID id);

    void updateIdModifiedAndStatus(UUID id,
                                   UUID imageModifiedId,
                                   FileProcessingStatus fileProcessingStatus);

    UUID getOldImageId(UUID requestId);
}
