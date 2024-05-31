package ru.daniil.worker.dto.imagga.tag;

import ru.daniil.worker.dto.imagga.StatusDto;

public record ImageTagsResponseDto(ResultTagsDto result, StatusDto status) {
}
