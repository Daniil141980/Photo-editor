package ru.daniil.worker.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;
import ru.daniil.worker.dto.kafka.ImageDoneDto;
import ru.daniil.worker.dto.kafka.ImageWipDto;

import java.net.SocketTimeoutException;


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
    private final ImageDoneProducer imageDoneProducer;

    @KafkaHandler
    @RetryableTopic(
            backoff = @Backoff(value = 3000L),
            attempts = "5",
            autoCreateTopics = "false",
            include = SocketTimeoutException.class, exclude = NullPointerException.class)
    public void consume(ImageWipDto imageWipDto, Acknowledgment acknowledgment) throws InterruptedException {
        log.info(imageWipDto.toString());
        Thread.sleep(30000);
        imageDoneProducer.sendMessage(new ImageDoneDto(imageWipDto.requestId(), imageWipDto.imageId()));
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
