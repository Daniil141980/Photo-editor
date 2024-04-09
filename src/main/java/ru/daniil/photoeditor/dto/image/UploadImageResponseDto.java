package ru.daniil.photoeditor.dto.image;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record UploadImageResponseDto(@NotNull
                                     UUID imageId) {
}
