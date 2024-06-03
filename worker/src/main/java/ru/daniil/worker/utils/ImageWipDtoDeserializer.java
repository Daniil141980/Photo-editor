package ru.daniil.worker.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import ru.daniil.worker.domains.FilterType;
import ru.daniil.worker.dto.kafka.ImageWipDto;
import ru.daniil.worker.processors.ProcessorParams;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.UUID;

public class ImageWipDtoDeserializer extends StdDeserializer<ImageWipDto> {

    public ImageWipDtoDeserializer() {
        this(null);
    }

    public ImageWipDtoDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public ImageWipDto deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        JsonNode node = jp.getCodec().readTree(jp);
        var requestId = UUID.fromString(node.get("requestId").asText());
        var imageId = UUID.fromString(node.get("imageId").asText());

        var filtersNode = node.get("filters");
        var filters = new LinkedHashMap<FilterType, ProcessorParams>();
        var fields = filtersNode.fields();

        ProcessorParams params;
        while (fields.hasNext()) {
            var field = fields.next();
            var filterType = FilterType.valueOf(field.getKey());
            params = null;
            if (filterType.getParamsDto() != null) {
                params = jp.getCodec().treeToValue(field.getValue(), filterType.getParamsDto());
            }
            filters.put(filterType, params);
        }

        return new ImageWipDto(requestId, imageId, filters);
    }
}