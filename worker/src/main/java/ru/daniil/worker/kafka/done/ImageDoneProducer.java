package ru.daniil.worker.kafka.done;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.daniil.worker.dto.kafka.ImageDoneDto;
import ru.daniil.worker.exceptions.InternalServerException;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageDoneProducer {

    private final KafkaTemplate<String, Object> transactionalTemplate;

    @Value("${app.images.done.topic}")
    private String topicDone;

    public void sendMessage(ImageDoneDto message) {
        log.info("Send message: {}", message);
        try {
            transactionalTemplate.executeInTransaction(it -> transactionalTemplate.send(topicDone, message));
        } catch (KafkaException e) {
            throw new InternalServerException("Kafka: " + e.getMessage());
        }
    }
}
