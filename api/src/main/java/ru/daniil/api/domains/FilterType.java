package ru.daniil.api.domains;

import lombok.Getter;

@Getter
public enum FilterType {
    HISTOGRAM("Improve image contrast"),
    KUWAHARA("Reduces noise"),
    SCHARR("Edge detection"),
    SEAM_CARVER("Content-aware image resizing");

    private final String description;

    FilterType(String description) {
        this.description = description;
    }
}
