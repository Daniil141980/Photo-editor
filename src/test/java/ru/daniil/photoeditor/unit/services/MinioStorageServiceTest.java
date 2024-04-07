package ru.daniil.photoeditor.unit.services;

import io.minio.GetObjectResponse;
import io.minio.MinioClient;
import io.minio.errors.ErrorResponseException;
import io.minio.messages.ErrorResponse;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import ru.daniil.photoeditor.config.StorageProperties;
import ru.daniil.photoeditor.exceptions.NotFoundException;
import ru.daniil.photoeditor.exceptions.StorageException;
import ru.daniil.photoeditor.services.MinioStorageService;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
        var storageProperties = new StorageProperties("", 9000, "", "", false, "test-bucket", 10485760L);

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
        var storageProperties = new StorageProperties("", 9000, "", "", false, "test-bucket", 10485760L);

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
        var storageProperties = new StorageProperties("", 9000, "", "", false, null, 10485760L);

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
        var storageProperties = new StorageProperties("", 9000, "", "", false, "test-bucket", 10485760L);

        var minioClient = mock(MinioClient.class);
        when(minioClient.putObject(any())).thenReturn(null);

        var minioStorageService = new MinioStorageService(storageProperties, minioClient);

        var multipartFile = new MockMultipartFile("image.png", "image.png", "image/png", "qweqwe".getBytes());
        minioStorageService.store(multipartFile);

        verify(minioClient, times(1)).putObject(any());
    }

    @Test
    @DisplayName("Test store empty file")
    @SneakyThrows
    void storeEmptyFile() {
        var storageProperties = new StorageProperties("", 9000, "", "", false, "test-bucket", 10485760L);

        var minioClient = mock(MinioClient.class);

        var minioStorageService = new MinioStorageService(storageProperties, minioClient);

        var multipartFile = new MockMultipartFile("image.png", new ByteArrayInputStream(new byte[0]));

        assertThrows(StorageException.class, () -> minioStorageService.store(multipartFile));
        verify(minioClient, times(0)).putObject(any());
    }

    @Test
    @DisplayName("Test store file failed")
    @SneakyThrows
    void storeFileFailed() {
        var storageProperties = new StorageProperties("", 9000, "", "", false, "test-bucket", 10485760L);

        var minioClient = mock(MinioClient.class);
        when(minioClient.putObject(any())).thenThrow(new IOException());

        var minioStorageService = new MinioStorageService(storageProperties, minioClient);

        var multipartFile = new MockMultipartFile("image.png", "image.png", "image/png", "qweqwe".getBytes());

        assertThrows(StorageException.class, () -> minioStorageService.store(multipartFile));
        verify(minioClient, times(1)).putObject(any());
    }

    @Test
    @DisplayName("Test remove file")
    @SneakyThrows
    void remove() {
        var storageProperties = new StorageProperties("", 9000, "", "", false, "test-bucket", 10485760L);

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
        var storageProperties = new StorageProperties("", 9000, "", "", false, "test-bucket", 10485760L);

        var minioClient = mock(MinioClient.class);
        doThrow(new IOException()).when(minioClient).removeObject(any());

        var minioStorageService = new MinioStorageService(storageProperties, minioClient);

        assertThrows(StorageException.class, () -> minioStorageService.remove(UUID.randomUUID().toString()));
        verify(minioClient, times(1)).removeObject(any());
    }

    @Test
    @SneakyThrows
    void loadAsResource() {
        var storageProperties = new StorageProperties("", 9000, "", "", false, "test-bucket", 10485760L);

        var getObjectResponse = mock(GetObjectResponse.class);
        when(getObjectResponse.readAllBytes()).thenReturn("qweqwe".getBytes());

        var minioClient = mock(MinioClient.class);
        when(minioClient.getObject(any())).thenReturn(getObjectResponse);

        var minioStorageService = new MinioStorageService(storageProperties, minioClient);
        var resource = minioStorageService.loadAsResource(UUID.randomUUID().toString());

        assertEquals("qweqwe", new String(resource.getInputStream().readAllBytes()));
        verify(minioClient, times(1)).getObject(any());
    }

    @Test
    @SneakyThrows
    void loadAsResourceNoSuchKey() {
        var storageProperties = new StorageProperties("", 9000, "", "", false, "test-bucket", 10485760L);

        var errorResponse = mock(ErrorResponse.class);
        when(errorResponse.code()).thenReturn("NoSuchKey");

        var errorResponseException = mock(ErrorResponseException.class);
        when(errorResponseException.errorResponse()).thenReturn(errorResponse);

        var minioClient = mock(MinioClient.class);
        when(minioClient.getObject(any())).thenThrow(errorResponseException);

        var minioStorageService = new MinioStorageService(storageProperties, minioClient);

        assertThrows(NotFoundException.class, () -> minioStorageService.loadAsResource(UUID.randomUUID().toString()));
        verify(minioClient, times(1)).getObject(any());
    }
}
