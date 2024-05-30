package ru.daniil.worker.dto.imagga.upload;

import ru.daniil.worker.dto.imagga.StatusDto;

public record ImageUploadResponseDto(ResultUploadDto result, StatusDto status) {
}
