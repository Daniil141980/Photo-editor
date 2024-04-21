package ru.daniil.api.services;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface StorageService {

    void init();

    UUID store(MultipartFile file);

    Resource loadAsResource(String fileId);

    void remove(String fileId);
}
