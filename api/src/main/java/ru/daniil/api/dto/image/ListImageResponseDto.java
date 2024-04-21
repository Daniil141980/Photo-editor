package ru.daniil.api.dto.image;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ListImageResponseDto(@NotNull
                                   List<ImageResponseDto> images) {
}
