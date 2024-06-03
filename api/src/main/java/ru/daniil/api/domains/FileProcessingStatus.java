package ru.daniil.api.domains;

import lombok.Getter;

@Getter
public enum FileProcessingStatus {
    WIP("Работа в процессе"),
    DONE("Готово");

    private final String description;

    FileProcessingStatus(String description) {
        this.description = description;
    }
}
