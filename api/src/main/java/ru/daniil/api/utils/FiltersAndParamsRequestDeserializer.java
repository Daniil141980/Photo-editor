package ru.daniil.api.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import ru.daniil.api.domains.FilterType;
import ru.daniil.api.dto.filter.FiltersAndParamsRequestDto;
import ru.daniil.api.dto.processorparams.ProcessorParams;
import ru.daniil.api.exceptions.ConflictException;
import ru.daniil.api.exceptions.NotFoundException;

import java.io.IOException;

public class FiltersAndParamsRequestDeserializer extends StdDeserializer<FiltersAndParamsRequestDto> {

    public FiltersAndParamsRequestDeserializer() {
        this(null);
    }

    public FiltersAndParamsRequestDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public FiltersAndParamsRequestDto deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        JsonNode node = jp.getCodec().readTree(jp);
        var filter = node.get("filter").asText();
        try {
            FilterType.valueOf(filter);
        } catch (Exception e) {
            throw new NotFoundException("This filter does not exist");
        }

        var processorParamsNode = node.get("processor_params");
        var processorParamsClass = FilterType.valueOf(filter).getParamsDto();

        if (processorParamsClass == null) {
            return new FiltersAndParamsRequestDto(filter, null);
        }

        if (processorParamsClass.getDeclaredFields().length != processorParamsNode.size()) {
            throw new ConflictException("The number of required and transmitted parameters does not match");
        }

        ProcessorParams processorParams;

        try {
            processorParams = jp.getCodec().treeToValue(processorParamsNode, processorParamsClass);
        } catch (Exception e) {
            throw new ConflictException("The parameters could not be processed."
                    + " Check if the types are specified correctly");
        }
        return new FiltersAndParamsRequestDto(filter, processorParams);
    }
}