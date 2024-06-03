package ru.daniil.api.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.daniil.api.domains.FileProcessingStatus;
import ru.daniil.api.domains.FilterType;
import ru.daniil.api.domains.RequestEntity;
import ru.daniil.api.dto.kafka.ImageWipDto;
import ru.daniil.api.exceptions.ConflictException;
import ru.daniil.api.exceptions.NotFoundException;
import ru.daniil.api.kafka.ImageWipProducer;
import ru.daniil.api.repositories.requests.RequestRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RequestService {
    private final RequestRepository requestRepository;
    private final ImageService imageService;
    private final ImageWipProducer imageWipProducer;

    @Transactional
    public UUID saveRequest(UUID imageId, List<FilterType> filters) {
        imageService.getImage(imageId);
        var requestId = requestRepository.save(
                new RequestEntity(UUID.randomUUID(),
                        imageId,
                        null,
                        FileProcessingStatus.WIP)
        );
        imageWipProducer.sendMessage(new ImageWipDto(requestId, imageId, filters));
        return requestId;
    }

    public RequestEntity getRequest(UUID requestId, UUID imageId) {
        imageService.getImage(imageId);
        var requestEntity = requestRepository.get(requestId).orElseThrow(() ->
                new NotFoundException("Request with id:%s not found".formatted(requestId))
        );
        if (!requestEntity.imageId().equals(imageId)) {
            throw new ConflictException("Image wit id:%s does not apply to the request with id:%s".formatted(imageId,
                    requestId));
        }
        return requestEntity;
    }

    @Transactional
    public void updateRequest(UUID requestId, UUID imageId) {
        requestRepository.get(requestId).orElseThrow(() ->
                new NotFoundException("Request with id:%s not found".formatted(requestId))
        );
        requestRepository.updateIdModifiedAndStatus(requestId, imageId, FileProcessingStatus.DONE);
    }
}
