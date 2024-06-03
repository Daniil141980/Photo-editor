package ru.daniil.worker.dto.kafka;

import java.util.UUID;

public record ImageDoneDto(UUID requestId, UUID imageId) {
}
