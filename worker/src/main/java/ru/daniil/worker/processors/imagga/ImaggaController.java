package ru.daniil.worker.processors.imagga;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import ru.daniil.worker.dto.imagga.tag.ImageTagsResponseDto;
import ru.daniil.worker.dto.imagga.upload.ImageUploadResponseDto;

@Controller
@ConditionalOnProperty(prefix = "filter", name = "type", havingValue = "IMAGGA")
@RequiredArgsConstructor
public class ImaggaController {
    private final WebClient webClient;

    @Retry(name = "customRetry")
    @CircuitBreaker(name = "customCircuitBreaker")
    @RateLimiter(name = "customRateLimiter")
    public ImageUploadResponseDto postImage(Resource resource) {
        return webClient
                .post()
                .uri("/uploads")
                .body(BodyInserters.fromMultipartData("image", resource))
                .retrieve()
                .bodyToMono(ImageUploadResponseDto.class)
                .block();
    }

    @Retry(name = "customRetry")
    @CircuitBreaker(name = "customCircuitBreaker")
    @RateLimiter(name = "customRateLimiter")
    public ImageTagsResponseDto getTags(String uploadId) {
        return webClient
                .get()
                .uri("/tags?limit=3&image_upload_id=" + uploadId)
                .retrieve()
                .bodyToMono(ImageTagsResponseDto.class)
                .block();
    }
}
