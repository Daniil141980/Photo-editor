package ru.daniil.api.dto.processorparams;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record SeamCarverParams(@NotNull
                               @Min(1)
                               Integer width,
                               @NotNull
                               @Min(1)
                               Integer height) implements ProcessorParams {
}
