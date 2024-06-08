package ru.daniil.api.config;

import io.micrometer.core.aop.CountedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@EnableAspectJAutoProxy
@Configuration
public class CustomMetricsConfiguration {

    @Bean
    public CountedAspect countedAspect(MeterRegistry meterRegistry) {
        return new CountedAspect(meterRegistry);
    }
}
