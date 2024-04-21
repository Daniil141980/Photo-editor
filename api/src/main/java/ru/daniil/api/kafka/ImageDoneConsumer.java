package ru.daniil.api.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;
import ru.daniil.api.dto.kafka.ImageDoneDto;
import ru.daniil.api.services.RequestService;

import java.net.SocketTimeoutException;


@Slf4j
@Component
@RequiredArgsConstructor
@KafkaListener(
        topics = "${app.images.done.topic}",
        groupId = "${app.images.done.group-id}",
        concurrency = "${app.images.done.partitions}",
        containerFactory = "multiTypeKafkaListenerContainerFactory"
)
public class ImageDoneConsumer {
    private final RequestService requestService;

    @KafkaHandler
    @RetryableTopic(
            backoff = @Backoff(value = 3000L),
            attempts = "5",
            autoCreateTopics = "false",
            include = SocketTimeoutException.class, exclude = NullPointerException.class)
    public void consume(ImageDoneDto imageDoneDto, Acknowledgment acknowledgment) {
        requestService.updateRequest(imageDoneDto.requestId(), imageDoneDto.imageId());
        acknowledgment.acknowledge();
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
