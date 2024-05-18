package ru.daniil.worker.services;

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
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import ru.daniil.worker.config.StorageProperties;
import ru.daniil.worker.exceptions.NotFoundException;
import ru.daniil.worker.exceptions.StorageException;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;
import javax.imageio.ImageIO;

@Service
@RequiredArgsConstructor
public class MinioStorageService implements StorageService {
    private final StorageProperties properties;
    private final MinioClient client;


    @Override
    @SneakyThrows
    public UUID store(BufferedImage image, String contentType) {
        try {
            if (image == null) {
                throw new StorageException("Failed to store empty file.");
            }

            byte[] imageBytes;
            try (var baos = new ByteArrayOutputStream()) {
                ImageIO.write(image, contentType.split("/")[1], baos);
                baos.flush();
                imageBytes = baos.toByteArray();
            }

            var fileId = UUID.randomUUID();
            client.putObject(
                    PutObjectArgs.builder()
                            .bucket(properties.getBucket())
                            .object(fileId.toString())
                            .stream(new ByteArrayInputStream(imageBytes),
                                    imageBytes.length,
                                    properties.getImageSize())
                            .contentType(contentType)
                            .build()
            );
            return fileId;
        } catch (IOException e) {
            throw new StorageException("Failed to store file.", e);
        }
    }

    @Override
    public Pair<BufferedImage, String> loadImageAndContentType(String fileId) {
        try {
            var contentType = client.statObject(
                    StatObjectArgs.builder()
                            .bucket(properties.getBucket())
                            .object(fileId)
                            .build()
            ).contentType();

            var content = client.getObject(
                    GetObjectArgs.builder()
                            .bucket(properties.getBucket())
                            .object(fileId)
                            .build()
            ).readAllBytes();

            var image = ImageIO.read(new ByteArrayInputStream(content));

            return Pair.of(image, contentType);
        } catch (Exception e) {
            throw new NotFoundException("Could not read file: " + fileId);
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
