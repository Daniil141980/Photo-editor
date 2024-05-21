package ru.daniil.worker.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.daniil.worker.domains.FilterType;
import ru.daniil.worker.domains.ProcessorsCheckEntity;
import ru.daniil.worker.dto.kafka.ImageDoneDto;
import ru.daniil.worker.dto.kafka.ImageWipDto;
import ru.daniil.worker.kafka.done.ImageDoneProducer;
import ru.daniil.worker.kafka.wip.ImageWipProducer;
import ru.daniil.worker.processors.Processor;
import ru.daniil.worker.processors.ProcessorParams;

import java.util.LinkedHashMap;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProcessorsService {
    private final Processor processor;
    private final ImageDoneProducer imageDoneProducer;
    private final ImageWipProducer imageWipProducer;
    private final ProcessorsCheckService processorsCheckService;
    private final StorageService storageService;

    public void startProcess(ImageWipDto imageWipDto) {
        var oldImage = storageService.loadImageAndContentType(imageWipDto.imageId().toString());
        var processorEntry = imageWipDto.filters().pollFirstEntry();
        var newImage = processor.doProcess(oldImage.getFirst(), processorEntry.getValue());
        var newImageId = storageService.store(newImage, oldImage.getSecond());

        processorsCheckService.save(new ProcessorsCheckEntity(
                imageWipDto.imageId(), imageWipDto.requestId(), processorEntry.getKey()));

        if (imageWipDto.filters().isEmpty()) {
            sendMessageToImageDone(imageWipDto.requestId(), newImageId);
        } else {
            sendMessageToImageWip(imageWipDto.requestId(), newImageId, imageWipDto.filters());
        }
    }

    public void sendMessageToImageDone(UUID requestId, UUID imageId) {
        imageDoneProducer.sendMessage(new ImageDoneDto(requestId, imageId));
    }

    private void sendMessageToImageWip(UUID requestId,
                                       UUID imageId,
                                       LinkedHashMap<FilterType, ProcessorParams> filters) {
        imageWipProducer.sendMessage(new ImageWipDto(requestId, imageId, filters));
    }

    public boolean doProcessEarly(UUID imageId, UUID requestId, FilterType filterType) {
        return processorsCheckService.exist(new ProcessorsCheckEntity(imageId, requestId, filterType));
    }
}