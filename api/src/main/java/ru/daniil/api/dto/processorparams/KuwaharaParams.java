package ru.daniil.api.dto.processorparams;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record KuwaharaParams(@NotNull
                             @Size(min = 1)
                             Integer size) implements ProcessorParams {
}
