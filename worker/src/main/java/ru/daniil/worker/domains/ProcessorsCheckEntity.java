package ru.daniil.worker.domains;

import java.util.UUID;

public record ProcessorsCheckEntity(UUID imageId, UUID requestId, FilterType filterType) {
}
