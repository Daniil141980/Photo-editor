package ru.daniil.api.controllers;

import io.micrometer.core.annotation.Counted;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ru.daniil.api.dto.SuccessResponseDto;
import ru.daniil.api.dto.image.ListImageResponseDto;
import ru.daniil.api.dto.image.UploadImageResponseDto;
import ru.daniil.api.exceptions.NotFoundException;
import ru.daniil.api.mappers.ImageMapper;
import ru.daniil.api.services.ImageService;
import ru.daniil.api.services.StorageService;
import ru.daniil.api.validation.constraints.FileFormat;

import java.util.UUID;

@RestController
@RequestMapping
@RequiredArgsConstructor
@Tag(name = "Image Controller", description = "Базовый CRUD API для работы с картинками")
public class ImageController {
    private final ImageService imageService;
    private final StorageService storageService;
    private final ImageMapper imageMapper;


    @ApiResponses({
            @ApiResponse(responseCode = "200", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "400",
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
    @Counted(value = "processed_messages")
    @PostMapping(value = "/image",
            consumes = {MediaType.MULTIPART_FORM_DATA_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public UploadImageResponseDto loadImage(@RequestParam("file")
                                            @NotNull
                                            @FileFormat("image/jpeg,image/png")
                                            MultipartFile file) {
        return new UploadImageResponseDto(imageService.saveImage(file).id());
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "500",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = SuccessResponseDto.class)
                    ))
    })
    @GetMapping(value = "/images", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ListImageResponseDto getImages() {
        return new ListImageResponseDto(
                imageService.getAllByUser().stream().map(imageMapper::toImageResponseDto).toList()
        );
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
    @GetMapping(value = "/image/{image-id}")
    public ResponseEntity<Resource> getImageById(@PathVariable("image-id") UUID imageId) {
        var imageEntity = imageService.getImage(imageId);
        var file = storageService.loadAsResource(imageId.toString());
        if (file == null) {
            throw new NotFoundException("Image:%s not found".formatted(imageId));
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"%s\"".formatted(imageEntity.filename()))
                .body(file);
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
    @DeleteMapping(value = "/image/{image-id}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public SuccessResponseDto deleteImage(@PathVariable("image-id") UUID imageId) {
        imageService.removeImage(imageId);
        return new SuccessResponseDto(true, "Image:%s has been deleted".formatted(imageId));
    }
}
