package ru.daniil.api.services;

import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.daniil.api.config.StorageProperties;
import ru.daniil.api.exceptions.NotFoundException;
import ru.daniil.api.exceptions.StorageException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MinioStorageService implements StorageService {
    private final StorageProperties properties;
    private final MinioClient client;


    @Override
    @SneakyThrows
    public UUID store(MultipartFile file) {
        try {
            if (file.isEmpty()) {
                throw new StorageException("Failed to store empty file.");
            }

            var fileId = UUID.randomUUID();
            client.putObject(
                    PutObjectArgs.builder()
                            .bucket(properties.getBucket())
                            .object(fileId.toString())
                            .stream(new ByteArrayInputStream(file.getBytes()),
                                    file.getSize(),
                                    properties.getImageSize())
                            .contentType(file.getContentType())
                            .build()
            );
            return fileId;
        } catch (IOException e) {
            throw new StorageException("Failed to store file.", e);
        }
    }

    @Override
    public Resource loadAsResource(String fileId) {
        try {
            return new ByteArrayResource(client.getObject(
                    GetObjectArgs.builder()
                            .bucket(properties.getBucket())
                            .object(fileId)
                            .build()).readAllBytes());
        } catch (Exception e) {
            throw new NotFoundException("Could not read file: " + fileId);
        }
    }

    @Override
    public Long getSize(String fileId) {
        try {
            return client.statObject(
                    StatObjectArgs.builder()
                            .bucket(properties.getBucket())
                            .object(fileId)
                            .build()
            ).size();
        } catch (Exception e) {
            throw new NotFoundException("Could not get size: " + fileId);
        }
    }

    @Override
    public void remove(String fileId) {
        try {
            client.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(properties.getBucket())
                            .object(fileId)
                            .build()
            );
        } catch (Exception e) {
            throw new StorageException("Failed to remove file.", e);
        }
    }

    @Override
    @SneakyThrows
    @PostConstruct
    public void init() {
        var bucketName = properties.getBucket();
        if (Objects.isNull(bucketName) || bucketName.isBlank()) {
            throw new StorageException("You should specify bucket name to use storage");
        }
        if (!client.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())) {
            client.makeBucket(MakeBucketArgs.builder()
                    .bucket(bucketName)
                    .build()
            );
        }
    }
}
