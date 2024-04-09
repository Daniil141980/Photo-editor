package ru.daniil.photoeditor.dto.image;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ListImageResponseDto(@NotNull
                                   List<ImageResponseDto> images) {
}
