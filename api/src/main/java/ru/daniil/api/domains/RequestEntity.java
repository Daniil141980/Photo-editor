package ru.daniil.api.domains;

import java.util.UUID;

public record RequestEntity(UUID id, UUID imageId, UUID imageModifiedId, FileProcessingStatus status) {
}

