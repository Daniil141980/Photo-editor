package ru.daniil.photoeditor.domains;

import java.util.UUID;

public record ImageEntity(UUID id, String filename, Long size, Long userId) {
}
