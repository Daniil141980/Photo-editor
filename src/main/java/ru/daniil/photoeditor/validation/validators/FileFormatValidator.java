package ru.daniil.photoeditor.validation.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import ru.daniil.photoeditor.validation.constraints.FileFormat;

import java.util.List;

public class FileFormatValidator implements ConstraintValidator<FileFormat, MultipartFile> {
    List<MediaType> mediaTypes;

    @Override
    public void initialize(FileFormat constraintAnnotation) {
        mediaTypes = MediaType.parseMediaTypes(constraintAnnotation.value());
    }

    @Override
    public boolean isValid(MultipartFile multipartFile, ConstraintValidatorContext constraintValidatorContext) {
        String contentType = multipartFile.getContentType();
        if (contentType == null) {
            return false;
        }
        MediaType fileMediaType = MediaType.parseMediaType(contentType);
        return mediaTypes.stream().anyMatch(mediaType ->
                mediaType.includes(fileMediaType)
        );
    }
}
