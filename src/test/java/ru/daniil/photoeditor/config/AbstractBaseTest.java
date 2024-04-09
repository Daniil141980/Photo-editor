package ru.daniil.photoeditor.config;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@DirtiesContext
@ActiveProfiles("test")
@ContextConfiguration(initializers = {PostgresTestConfig.Initializer.class, MinIOTestConfig.Initializer.class})
@Testcontainers
public abstract class AbstractBaseTest {
    @Container
    public GenericContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:14.5-alpine");

    @Container
    public GenericContainer<?> minioContainer = new MinIOContainer("minio/minio");
}
