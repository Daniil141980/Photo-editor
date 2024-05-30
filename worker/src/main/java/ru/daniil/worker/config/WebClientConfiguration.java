package ru.daniil.worker.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import io.github.resilience4j.reactor.ratelimiter.operator.RateLimiterOperator;
import io.github.resilience4j.reactor.retry.RetryOperator;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.springframework.web.reactive.function.client.ExchangeFilterFunctions.basicAuthentication;

@Configuration
public class WebClientConfiguration {
    public static final int TIMEOUT = 5000;
    private static final String BASE_URL = "https://api.imagga.com/v2";
    @Value("${api.key}")
    private String key;
    @Value("${api.secret}")
    private String secret;

    @Bean
    public WebClient webClientWithTimeout() {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, TIMEOUT)
                .responseTimeout(Duration.ofMillis(TIMEOUT))
                .doOnConnected(conn ->
                        conn.addHandlerLast(new ReadTimeoutHandler(TIMEOUT, TimeUnit.MILLISECONDS))
                                .addHandlerLast(new WriteTimeoutHandler(TIMEOUT, TimeUnit.MILLISECONDS)));

        return WebClient.builder()
                .baseUrl(BASE_URL)
                .filter(basicAuthentication(key, secret))
                .filter((request, next) -> Mono.just(request)
                        .transformDeferred(CircuitBreakerOperator.of(circuitBreakerConf()))
                        .transformDeferred(RetryOperator.of(retryConf()))
                        .transformDeferred(RateLimiterOperator.of(rateLimiterConf()))
                        .flatMap(next::exchange))
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    private CircuitBreaker circuitBreakerConf() {
        var circuitBreakerConfig = CircuitBreakerConfig.custom()
                .failureRateThreshold(75)
                .waitDurationInOpenState(Duration.ofMillis(TIMEOUT))
                .slidingWindowSize(4)
                .build();
        var circuitBreakerRegistry = CircuitBreakerRegistry.of(circuitBreakerConfig);
        return circuitBreakerRegistry.circuitBreaker("myCircuitBreaker");
    }

    private Retry retryConf() {
        var retryConfig = RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofMillis(1000))
                .retryOnException(throwable -> {
                    if (throwable instanceof WebClientResponseException ex) {
                        return ex.getStatusCode().is5xxServerError() || ex.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS;
                    }
                    return false;
                })
                .build();
        var retryRegistry = RetryRegistry.of(retryConfig);
        return retryRegistry.retry("myRetry");
    }

    private RateLimiter rateLimiterConf() {
        var rateLimiterConfig = RateLimiterConfig.custom()
                .limitForPeriod(4)
                .limitRefreshPeriod(Duration.ofHours(1))
                .timeoutDuration(Duration.ofMinutes(30))
                .build();
        var rateLimiterRegistry = RateLimiterRegistry.of(rateLimiterConfig);
        return rateLimiterRegistry.rateLimiter("myRateLimiter");
    }
}
