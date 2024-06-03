package ru.daniil.api.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.daniil.api.dto.kafka.ImageWipDto;
import ru.daniil.api.exceptions.InternalServerException;


@Slf4j
@Service
@RequiredArgsConstructor
public class ImageWipProducer {

    private final KafkaTemplate<String, Object> transactionalTemplate;

    @Value("${app.images.wip.topic}")
    private String topicWip;

    public void sendMessage(ImageWipDto message) {
        try {
            transactionalTemplate.executeInTransaction(it -> transactionalTemplate.send(topicWip, message));
        } catch (KafkaException e) {
            throw new InternalServerException("Kafka: " + e.getMessage());
        }
    }
}
