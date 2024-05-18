package ru.daniil.worker.dto.kafka;


import ru.daniil.worker.domains.FilterType;
import ru.daniil.worker.processors.ProcessorParams;

import java.util.LinkedHashMap;
import java.util.UUID;

public record ImageWipDto(UUID requestId, UUID imageId, LinkedHashMap<FilterType, ProcessorParams> filters) {
}
