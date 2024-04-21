package ru.daniil.api.dto.image;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ImageResponseDto(@NotNull
                               UUID imageId,
                               @NotNull
                               String filename,
                               @NotNull
                               Integer size) {
}
