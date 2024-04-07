package ru.daniil.photoeditor.mappers;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.daniil.photoeditor.domains.ImageEntity;
import ru.daniil.photoeditor.dto.image.ImageResponseDto;

@Mapper(componentModel = "spring")
public interface ImageMapper {
    @Mapping(target = "imageId", source = "id")
    ImageResponseDto toImageResponseDto(ImageEntity imageEntity);
}
