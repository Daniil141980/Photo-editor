package ru.daniil.photoeditor.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AuthRequestDto(@Schema(description = "Имя пользователя", example = "user")
                             @NotBlank(message = "Имя пользователя не должно быть пустым")
                             @Size(min = 3, max = 100, message = "Имя должно быть больше 3, но меньше 100 символов")
                             String username,
                             @Schema(description = "Пароль пользователя", example = "12345")
                             @NotBlank(message = "Пароль не должен быть пустым")
                             @Size(min = 5, max = 100, message = "Пароль должен быть больше 5, но меньше 100 символов")
                             String password) {
}
