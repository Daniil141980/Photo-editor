package ru.daniil.worker.dto.imagga.upload;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ResultUploadDto(@JsonProperty("upload_id")
                              String uploadId) {
}
