package ru.daniil.api.domains;

import lombok.Getter;

@Getter
public enum FilterType {
    REVERS_COLORS("Поменять цвета"),
    CROP("Обрезать"),
    REMOVE_BACKGROUND("Поменять фон");

    private final String description;

    FilterType(String description) {
        this.description = description;
    }
}
