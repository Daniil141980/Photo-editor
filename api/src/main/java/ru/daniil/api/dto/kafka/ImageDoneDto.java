package ru.daniil.api.dto.kafka;

import java.util.UUID;

public record ImageDoneDto(UUID requestId, UUID imageId) {
}
