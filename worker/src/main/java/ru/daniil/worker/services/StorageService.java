package ru.daniil.worker.services;


import org.springframework.data.util.Pair;

import java.awt.image.BufferedImage;
import java.util.UUID;

public interface StorageService {

    void init();

    UUID store(BufferedImage image, String contentType);

    Pair<BufferedImage, String> loadImageAndContentType(String fileId);

    void remove(String fileId);
}
