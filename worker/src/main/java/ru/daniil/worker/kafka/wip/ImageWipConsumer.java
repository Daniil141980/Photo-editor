package ru.daniil.worker.kafka.wip;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;
import ru.daniil.worker.domains.FilterType;
import ru.daniil.worker.dto.kafka.ImageWipDto;
import ru.daniil.worker.processors.ProcessorParams;
import ru.daniil.worker.services.ProcessorsService;

import java.net.SocketTimeoutException;
import java.util.LinkedHashMap;
import java.util.Map;


@Slf4j
@Component
@RequiredArgsConstructor
@KafkaListener(
        topics = "${app.images.wip.topic}",
        groupId = "${app.images.wip.group-id}",
        concurrency = "${app.images.wip.partitions}",
        containerFactory = "multiTypeKafkaListenerContainerFactory"
)
public class ImageWipConsumer {
    private final ProcessorsService processorsService;
    @Value("${filter.type}")
    private String typeProcessor;

    @KafkaHandler
    @RetryableTopic(
            backoff = @Backoff(value = 3000L),
            attempts = "5",
            autoCreateTopics = "false",
            include = SocketTimeoutException.class, exclude = NullPointerException.class)
    public void consume(ImageWipDto imageWipDto, Acknowledgment acknowledgment) {
        log.info(imageWipDto.toString());

        if (imageWipDto.filters().isEmpty()) {
            processorsService.sendMessageToImageDone(imageWipDto.requestId(), imageWipDto.imageId());
        }

        var firstKey = canDoProcess(imageWipDto.filters());
        if (firstKey != null) {
            if (!processorsService.doProcessEarly(imageWipDto.imageId(), imageWipDto.requestId(), firstKey)) {
                processorsService.startProcess(imageWipDto);
                acknowledgment.acknowledge();
            }
        }
    }

    private FilterType canDoProcess(LinkedHashMap<FilterType, ProcessorParams> filters) {
        var firstKey = filters.entrySet().stream().findFirst().map(Map.Entry::getKey).orElse(null);
        if (firstKey.equals(FilterType.valueOf(typeProcessor))) {
            return firstKey;
        }
        return null;
    }


    @KafkaHandler(isDefault = true)
    @RetryableTopic(
            backoff = @Backoff(value = 3000L),
            attempts = "5",
            autoCreateTopics = "false",
            include = SocketTimeoutException.class, exclude = NullPointerException.class)
    public void unknown(Object object) {
        log.warn("Unknown type received: {}", object);
    }
}
