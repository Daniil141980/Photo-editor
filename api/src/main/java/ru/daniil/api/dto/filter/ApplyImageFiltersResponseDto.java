package ru.daniil.api.dto.filter;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ApplyImageFiltersResponseDto(@NotNull
                                           @Schema(description = "ИД запроса в системе")
                                           UUID requestId) {
}