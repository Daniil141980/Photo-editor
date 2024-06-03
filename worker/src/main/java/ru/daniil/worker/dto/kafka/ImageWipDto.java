package ru.daniil.worker.dto.kafka;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import ru.daniil.worker.domains.FilterType;
import ru.daniil.worker.processors.ProcessorParams;
import ru.daniil.worker.utils.ImageWipDtoDeserializer;

import java.util.LinkedHashMap;
import java.util.UUID;

@JsonDeserialize(using = ImageWipDtoDeserializer.class)
public record ImageWipDto(UUID requestId, UUID imageId, LinkedHashMap<FilterType, ProcessorParams> filters) {
}
