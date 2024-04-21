package ru.daniil.api.mappers;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.daniil.api.domains.ImageEntity;
import ru.daniil.api.dto.image.ImageResponseDto;

@Mapper(componentModel = "spring")
public interface ImageMapper {
    @Mapping(target = "imageId", source = "id")
    ImageResponseDto toImageResponseDto(ImageEntity imageEntity);
}
