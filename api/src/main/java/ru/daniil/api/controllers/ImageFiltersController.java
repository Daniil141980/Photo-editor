package ru.daniil.api.controllers;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.daniil.api.domains.FilterType;
import ru.daniil.api.dto.SuccessResponseDto;
import ru.daniil.api.dto.filter.ApplyImageFiltersResponseDto;
import ru.daniil.api.dto.filter.GetModifiedImageByRequestIdResponseDto;
import ru.daniil.api.mappers.RequestMapper;
import ru.daniil.api.services.RequestService;
import ru.daniil.api.validation.constraints.ValidFilterType;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping
@RequiredArgsConstructor
@Tag(name = "Image Filters Controller",
        description = "Базовый CRUD API для работы с пользовательскими запросами на редактирование картинок")
public class ImageFiltersController {
    private final RequestService requestService;
    private final RequestMapper requestMapper;

    @ApiResponses({
            @ApiResponse(responseCode = "200", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "404",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = SuccessResponseDto.class)
                    )),
            @ApiResponse(responseCode = "500",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = SuccessResponseDto.class)
                    ))
    })
    @PostMapping(value = "/image/{image-id}/filters/apply", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApplyImageFiltersResponseDto applyImageFilters(@PathVariable("image-id")
                                                          UUID imageId,
                                                          //TODO то ли я указал?
                                                          @RequestParam(value = "filters")
                                                          @ValidFilterType
                                                          List<String> filters) {
        return new ApplyImageFiltersResponseDto(requestService.saveRequest(imageId,
                filters.stream().map(FilterType::valueOf).toList()));
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "404",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = SuccessResponseDto.class)
                    )),
            @ApiResponse(responseCode = "500",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = SuccessResponseDto.class)
                    ))
    })
    @GetMapping(value = "/image/{image-id}/filters/{request-id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public GetModifiedImageByRequestIdResponseDto getModifiedImageByRequestId(@PathVariable("image-id")
                                                                              UUID imageId,
                                                                              @PathVariable("request-id")
                                                                              UUID requestId) {
        return requestMapper.toGetModifiedImageByRequestIdResponseDto(requestService.getRequest(requestId, imageId));
    }
}
