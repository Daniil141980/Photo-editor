package ru.daniil.api.dto.kafka;

import ru.daniil.api.domains.FilterType;
import ru.daniil.api.dto.processorparams.ProcessorParams;

import java.util.LinkedHashMap;
import java.util.UUID;

public record ImageWipDto(UUID requestId, UUID imageId, LinkedHashMap<FilterType, ProcessorParams> filters) {
}
