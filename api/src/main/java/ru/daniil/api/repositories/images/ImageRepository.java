package ru.daniil.api.repositories.images;

import ru.daniil.api.domains.ImageEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ImageRepository {

    ImageEntity save(ImageEntity imageEntity);

    List<ImageEntity> getByUserId(Long userId);

    Optional<ImageEntity> get(UUID id);

    void remove(UUID id);
}
