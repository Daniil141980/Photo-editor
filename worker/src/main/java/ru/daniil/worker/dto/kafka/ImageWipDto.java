package ru.daniil.worker.dto.kafka;


import ru.daniil.worker.domains.FilterType;

import java.util.List;
import java.util.UUID;

public record ImageWipDto(UUID requestId, UUID imageId, List<FilterType> filters) {
}
