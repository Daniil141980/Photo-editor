package ru.daniil.api.dto.processorparams;

import jakarta.validation.constraints.NotBlank;

public record ScharrParams(@NotBlank
                           String type) implements ProcessorParams {
}
