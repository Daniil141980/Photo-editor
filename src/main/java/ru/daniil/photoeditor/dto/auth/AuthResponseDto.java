package ru.daniil.photoeditor.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

public record AuthResponseDto(@NotNull
                              String username,
                              @JsonProperty("refresh_token")
                              @NotNull
                              String refreshToken) {
}
