package ru.daniil.api.dto.filter;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import ru.daniil.api.dto.processorparams.ProcessorParams;
import ru.daniil.api.utils.FiltersAndParamsRequestDeserializer;


@JsonDeserialize(using = FiltersAndParamsRequestDeserializer.class)
public record FiltersAndParamsRequestDto(String filter,
                                         @JsonProperty("processor_params")
                                         ProcessorParams processorParams) {
}
