package ru.daniil.api.dto.kafka;

import ru.daniil.api.domains.FilterType;

import java.util.List;
import java.util.UUID;

public record ImageWipDto(UUID requestId, UUID imageId, List<FilterType> filters) {
}
