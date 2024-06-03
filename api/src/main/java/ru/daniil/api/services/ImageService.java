package ru.daniil.api.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.daniil.api.domains.ImageEntity;
import ru.daniil.api.exceptions.NotFoundException;
import ru.daniil.api.repositories.images.ImageRepository;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageService {
    private final StorageService storageService;
    private final ImageRepository imageRepository;
    private final UserService userService;

    @Transactional
    public ImageEntity saveImage(MultipartFile file) {
        var id = storageService.store(file);
        return imageRepository.save(
                new ImageEntity(id,
                        file.getOriginalFilename(),
                        file.getSize(),
                        userService.loadCurrentUser().id()
                )
        );
    }

    @Transactional
    public void saveImage(UUID id, UUID oldImageId) {
        var oldImage = imageRepository.get(oldImageId).orElseThrow(() ->
                new NotFoundException("Image with id:%s not found".formatted(id))
        );
        imageRepository.save(
                new ImageEntity(id,
                        oldImage.filename(),
                        storageService.getSize(id.toString()),
                        oldImage.userId()
                )
        );
    }

    public List<ImageEntity> getAllByUser() {
        return imageRepository.getByUserId(userService.loadCurrentUser().id());
    }

    public ImageEntity getImage(UUID id) {
        var imageEntity = imageRepository.get(id).orElseThrow(() ->
                new NotFoundException("Image with id:%s not found".formatted(id))
        );
        checkRights(imageEntity);
        return imageEntity;
    }

    @Transactional
    public void removeImage(UUID id) {
        var imageEntity = imageRepository.get(id).orElseThrow(() ->
                new NotFoundException("Image with id:%s not found".formatted(id))
        );
        checkRights(imageEntity);
        imageRepository.remove(id);
        storageService.remove(id.toString());
    }

    public Boolean existImage(UUID id) {
        return imageRepository.exist(id);
    }

    void checkRights(ImageEntity imageEntity) {
        if (!Objects.equals(imageEntity.userId(), userService.loadCurrentUser().id())) {
            throw new NotFoundException("Image with id:%s not found".formatted(imageEntity.id()));
        }
    }
}

