package ru.daniil.worker.services;

import io.minio.MinioClient;
import io.minio.errors.ErrorResponseException;
import io.minio.messages.ErrorResponse;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.daniil.worker.config.StorageProperties;
import ru.daniil.worker.exceptions.NotFoundException;
import ru.daniil.worker.exceptions.StorageException;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import javax.imageio.ImageIO;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MinioStorageServiceTest {

    @Test
    @DisplayName("Test init minio, create bucket")
    @SneakyThrows
    void init() {
        var storageProperties = new StorageProperties("", 9000, "", "", false, "test-bucket", 10485760L, 0);

        var minioClient = mock(MinioClient.class);
        when(minioClient.bucketExists(any())).thenReturn(false);
        doAnswer(invocation -> null).when(minioClient).makeBucket(any());

        var minioStorageService = new MinioStorageService(storageProperties, minioClient);
        minioStorageService.init();

        verify(minioClient, times(1)).bucketExists(any());
        verify(minioClient, times(1)).makeBucket(any());
    }

    @Test
    @DisplayName("Test init minio, bucket exists")
    @SneakyThrows
    void initBucketExists() {
        var storageProperties = new StorageProperties("", 9000, "", "", false, "test-bucket", 10485760L, 0);

        var minioClient = mock(MinioClient.class);
        when(minioClient.bucketExists(any())).thenReturn(true);
        doAnswer(invocation -> null).when(minioClient).makeBucket(any());

        var minioStorageService = new MinioStorageService(storageProperties, minioClient);
        minioStorageService.init();

        verify(minioClient, times(1)).bucketExists(any());
        verify(minioClient, times(0)).makeBucket(any());
    }

    @Test
    @DisplayName("Test init minio, bad bucket name")
    @SneakyThrows
    void initBadBucketName() {
        var storageProperties = new StorageProperties("", 9000, "", "", false, null, 10485760L, 0);

        var minioClient = mock(MinioClient.class);

        var minioStorageService = new MinioStorageService(storageProperties, minioClient);

        assertThrows(StorageException.class, minioStorageService::init);
        verify(minioClient, times(0)).bucketExists(any());
        verify(minioClient, times(0)).makeBucket(any());
    }

    @Test
    @DisplayName("Test store file")
    @SneakyThrows
    void store() {
        var storageProperties = new StorageProperties("", 9000, "", "", false, "test-bucket", 10485760L, 15);

        var minioClient = mock(MinioClient.class);
        when(minioClient.putObject(any())).thenReturn(null);

        var minioStorageService = new MinioStorageService(storageProperties, minioClient);

        var bufferedImage = ImageIO.read(new File("src/test/resources/files/image.jpg"));
        minioStorageService.store(bufferedImage, "image/jpg", true);
        verify(minioClient, times(1)).putObject(any());
    }

    @Test
    @DisplayName("Test store empty file")
    @SneakyThrows
    void storeEmptyFile() {
        var storageProperties = new StorageProperties("", 9000, "", "", false, "test-bucket", 10485760L, 15);

        var minioClient = mock(MinioClient.class);

        var minioStorageService = new MinioStorageService(storageProperties, minioClient);


        assertThrows(StorageException.class, () -> minioStorageService.store(null, "image/jpg", true));
        verify(minioClient, times(0)).putObject(any());
    }

    @Test
    @DisplayName("Test store file failed")
    @SneakyThrows
    void storeFileFailed() {
        var storageProperties = new StorageProperties("", 9000, "", "", false, "test-bucket", 10485760L, 0);

        var minioClient = mock(MinioClient.class);
        when(minioClient.putObject(any())).thenThrow(new IOException());

        var minioStorageService = new MinioStorageService(storageProperties, minioClient);

        var bufferedImage = ImageIO.read(new File("src/test/resources/files/image.jpg"));

        assertThrows(StorageException.class, () -> minioStorageService.store(bufferedImage, "image/jpg", true));
        verify(minioClient, times(1)).putObject(any());
    }

    @Test
    @DisplayName("Test remove file")
    @SneakyThrows
    void remove() {
        var storageProperties = new StorageProperties("", 9000, "", "", false, "test-bucket", 10485760L, 0);

        var minioClient = mock(MinioClient.class);
        doAnswer(invocation -> null).when(minioClient).removeObject(any());

        var minioStorageService = new MinioStorageService(storageProperties, minioClient);

        minioStorageService.remove(UUID.randomUUID().toString());

        verify(minioClient, times(1)).removeObject(any());
    }

    @Test
    @DisplayName("Test remove file failed")
    @SneakyThrows
    void removeFailed() {
        var storageProperties = new StorageProperties("", 9000, "", "", false, "test-bucket", 10485760L, 0);

        var minioClient = mock(MinioClient.class);
        doThrow(new IOException()).when(minioClient).removeObject(any());

        var minioStorageService = new MinioStorageService(storageProperties, minioClient);

        assertThrows(StorageException.class, () -> minioStorageService.remove(UUID.randomUUID().toString()));
        verify(minioClient, times(1)).removeObject(any());
    }

    @Test
    @SneakyThrows
    void loadAsResourceNoSuchKey() {
        var storageProperties = new StorageProperties("", 9000, "", "", false, "test-bucket", 10485760L, 0);

        var errorResponse = mock(ErrorResponse.class);
        when(errorResponse.code()).thenReturn("NoSuchKey");

        var errorResponseException = mock(ErrorResponseException.class);
        when(errorResponseException.errorResponse()).thenReturn(errorResponse);

        var minioClient = mock(MinioClient.class);
        when(minioClient.getObject(any())).thenThrow(errorResponseException);
        when(minioClient.statObject(any())).thenThrow(errorResponseException);

        var minioStorageService = new MinioStorageService(storageProperties, minioClient);

        assertThrows(NotFoundException.class, () -> minioStorageService.loadImageAndContentType(UUID.randomUUID().toString()));
    }
}
