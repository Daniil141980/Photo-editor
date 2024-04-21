package ru.daniil.api.dto.filter;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record GetModifiedImageByRequestIdResponseDto(@NotNull
                                                     @Schema(description = "ИД модифицированного или оригинального"
                                                             + " файла в случае отсутствия первого")
                                                     UUID imageId,
                                                     @NotNull
                                                     @Schema(description = "Статус обработки файла")
                                                     String status
) {
}