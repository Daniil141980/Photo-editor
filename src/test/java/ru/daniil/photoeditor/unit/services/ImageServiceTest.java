package ru.daniil.photoeditor.unit.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import ru.daniil.photoeditor.domains.ImageEntity;
import ru.daniil.photoeditor.domains.UserEntity;
import ru.daniil.photoeditor.exceptions.NotFoundException;
import ru.daniil.photoeditor.repositories.images.ImageRepository;
import ru.daniil.photoeditor.services.ImageService;
import ru.daniil.photoeditor.services.StorageService;
import ru.daniil.photoeditor.services.UserService;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ImageServiceTest {
    @Test
    @DisplayName("Test save image")
    void saveImage() throws IOException {
        var uuid = UUID.fromString("16fd2706-8baf-433b-82eb-8c7fada847da");
        var userEntity = new UserEntity(1L, "user", "password");

        var storageService = mock(StorageService.class);
        when(storageService.store(any())).thenReturn(uuid);

        var imageRepository = mock(ImageRepository.class);
        when(imageRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var userService = mock(UserService.class);
        when(userService.loadCurrentUser()).thenReturn(userEntity);

        var imageService = new ImageService(storageService, imageRepository, userService);
        var file = new MockMultipartFile("image.png", "image.png", "image/png", InputStream.nullInputStream());
        var imageEntity = imageService.saveImage(file);

        verify(storageService, times(1)).store(file);
        verify(imageRepository, times(1)).save(imageEntity);
        assertEquals(uuid, imageEntity.id());
        assertEquals(userEntity.id(), imageEntity.userId());
        assertEquals(file.getOriginalFilename(), imageEntity.filename());
    }

    @Test
    @DisplayName("Test load all images of current user")
    void getAll() {
        var userEntity = new UserEntity(1L, "user", "password");

        var imageRepository = mock(ImageRepository.class);
        when(imageRepository.getByUserId(userEntity.id())).thenReturn(List.of(
                new ImageEntity(UUID.randomUUID(), "image.png", 128000L, 1L),
                new ImageEntity(UUID.randomUUID(), "image1.png", 128000L, 1L),
                new ImageEntity(UUID.randomUUID(), "files/image.jpeg", 128000L, 1L),
                new ImageEntity(UUID.randomUUID(), "image2.jpeg", 128000L, 1L)
        ));

        var userService = mock(UserService.class);
        when(userService.loadCurrentUser()).thenReturn(userEntity);

        var imageService = new ImageService(mock(StorageService.class), imageRepository, userService);
        var imageEntities = imageService.getAllByUser();

        verify(imageRepository, times(1)).getByUserId(userEntity.id());
        assertThat(imageEntities).hasSize(4);
        assertTrue(imageEntities.stream().allMatch(it -> it.userId().equals(userEntity.id())));
    }

    @Test
    @DisplayName("Test get image by UUID")
    void getById() {
        var uuid = UUID.fromString("16fd2706-8baf-433b-82eb-8c7fada847da");
        var userEntity = new UserEntity(1L, "user", "password");
        var imageEntity = new ImageEntity(uuid, "image.png", 128000L, userEntity.id());

        var imageRepository = mock(ImageRepository.class);
        when(imageRepository.get(uuid)).thenReturn(Optional.of(imageEntity));

        var userService = mock(UserService.class);
        when(userService.loadCurrentUser()).thenReturn(userEntity);

        var imageService = new ImageService(mock(StorageService.class), imageRepository, userService);
        var actualImageEntity = imageService.getImage(uuid);

        verify(imageRepository, times(1)).get(uuid);
        assertEquals(imageEntity, actualImageEntity);
    }

    @Test
    @DisplayName("Test get not existing image by UUID")
    void getByIdNotFound() {
        var uuid = UUID.fromString("16fd2706-8baf-433b-82eb-8c7fada847da");
        var userEntity = new UserEntity(1L, "user", "password");

        var imageRepository = mock(ImageRepository.class);
        when(imageRepository.get(uuid)).thenReturn(Optional.empty());

        var userService = mock(UserService.class);
        when(userService.loadCurrentUser()).thenReturn(userEntity);

        var imageService = new ImageService(mock(StorageService.class), imageRepository, userService);

        assertThrows(NotFoundException.class, () -> imageService.getImage(uuid));
        verify(imageRepository, times(1)).get(uuid);
    }

    @Test
    @DisplayName("Test get image by UUID, but owned by other user")
    void getByIdForbidden() {
        var uuid = UUID.fromString("16fd2706-8baf-433b-82eb-8c7fada847da");
        var userEntity = new UserEntity(1L, "user", "password");
        var imageEntity = new ImageEntity(uuid, "image.png", 128000L, userEntity.id() + 100);

        var imageRepository = mock(ImageRepository.class);
        when(imageRepository.get(uuid)).thenReturn(Optional.of(imageEntity));

        var userService = mock(UserService.class);
        when(userService.loadCurrentUser()).thenReturn(userEntity);

        var imageService = new ImageService(mock(StorageService.class), imageRepository, userService);

        assertThrows(NotFoundException.class, () -> imageService.getImage(uuid));
        verify(imageRepository, times(1)).get(uuid);
    }

    @Test
    @DisplayName("Test delete image by UUID")
    void deleteById() {
        var uuid = UUID.fromString("16fd2706-8baf-433b-82eb-8c7fada847da");
        var userEntity = new UserEntity(1L, "user", "password");
        var imageEntity = new ImageEntity(uuid, "image.png", 128000L, userEntity.id());

        var imageRepository = mock(ImageRepository.class);
        when(imageRepository.get(uuid)).thenReturn(Optional.of(imageEntity));
        doAnswer(invocation -> null).when(imageRepository).remove(uuid);

        var userService = mock(UserService.class);
        when(userService.loadCurrentUser()).thenReturn(userEntity);

        var imageService = new ImageService(mock(StorageService.class), imageRepository, userService);
        imageService.removeImage(uuid);

        verify(imageRepository, times(1)).get(uuid);
        verify(imageRepository, times(1)).remove(uuid);
    }

    @Test
    @DisplayName("Test delete not existing image by UUID")
    void deleteByIdNotFound() {
        var uuid = UUID.fromString("16fd2706-8baf-433b-82eb-8c7fada847da");
        var userEntity = new UserEntity(1L, "user", "password");

        var imageRepository = mock(ImageRepository.class);
        when(imageRepository.get(uuid)).thenReturn(Optional.empty());
        doAnswer(invocation -> null).when(imageRepository).remove(uuid);

        var userService = mock(UserService.class);
        when(userService.loadCurrentUser()).thenReturn(userEntity);

        var imageService = new ImageService(mock(StorageService.class), imageRepository, userService);

        assertThrows(NotFoundException.class, () -> imageService.removeImage(uuid));
        verify(imageRepository, times(1)).get(uuid);
        verify(imageRepository, times(0)).remove(uuid);
    }

    @Test
    @DisplayName("Test delete image by UUID, but owned by other user")
    void deleteByIdForbidden() {
        var uuid = UUID.fromString("16fd2706-8baf-433b-82eb-8c7fada847da");
        var userEntity = new UserEntity(1L, "user", "password");
        var imageEntity = new ImageEntity(uuid, "image.png", 128000L, userEntity.id() + 100);

        var imageRepository = mock(ImageRepository.class);
        when(imageRepository.get(uuid)).thenReturn(Optional.of(imageEntity));
        doAnswer(invocation -> null).when(imageRepository).remove(uuid);

        var userService = mock(UserService.class);
        when(userService.loadCurrentUser()).thenReturn(userEntity);

        var imageService = new ImageService(mock(StorageService.class), imageRepository, userService);

        assertThrows(NotFoundException.class, () -> imageService.getImage(uuid));
        verify(imageRepository, times(1)).get(uuid);
        verify(imageRepository, times(0)).remove(uuid);
    }
}
