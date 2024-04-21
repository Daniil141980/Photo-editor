package ru.daniil.api.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.daniil.api.domains.RequestEntity;
import ru.daniil.api.dto.filter.GetModifiedImageByRequestIdResponseDto;

@Mapper(componentModel = "spring")
public interface RequestMapper {
    @Mapping(target = "imageId", expression = "java(requestEntity.imageModifiedId() != null ?"
             + " requestEntity.imageModifiedId() : requestEntity.imageId())")
    GetModifiedImageByRequestIdResponseDto toGetModifiedImageByRequestIdResponseDto(RequestEntity requestEntity);
}