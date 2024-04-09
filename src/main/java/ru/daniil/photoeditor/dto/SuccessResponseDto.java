package ru.daniil.photoeditor.dto;


import jakarta.validation.constraints.NotNull;

public record SuccessResponseDto(@NotNull
                                 boolean success,
                                 String message) {
}
