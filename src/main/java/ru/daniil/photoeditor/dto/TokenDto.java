package ru.daniil.photoeditor.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import ru.daniil.photoeditor.validation.constraints.ValidToken;

public record TokenDto(@Schema(description = "Рефреш токен")
                       @NotBlank(message = "Токен не должен быть пустым")
                       @ValidToken
                       String token) {
}
