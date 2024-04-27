package ru.daniil.api.config;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@DirtiesContext
@ActiveProfiles("test")
@ContextConfiguration(initializers = {PostgresTestConfig.Initializer.class,
        MinIOTestConfig.Initializer.class,
        KafkaTestConfig.Initializer.class})
@Testcontainers
public abstract class AbstractBaseTest {
}
