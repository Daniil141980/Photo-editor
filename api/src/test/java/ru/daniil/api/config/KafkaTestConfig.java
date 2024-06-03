package ru.daniil.api.config;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.utility.DockerImageName;

import java.util.LinkedHashMap;
import java.util.Map;


public class KafkaTestConfig {
    private static final KafkaContainer kafka1Container;
    private static final KafkaContainer kafka2Container;
    private static final KafkaContainer kafka3Container;
    private static final GenericContainer<?> zookeeperContainer;

    static {
        var network = Network.newNetwork();

        zookeeperContainer = initZookeeperContainer(network);
        kafka1Container = initKafkaBrokerContainer(1, network);
        kafka2Container = initKafkaBrokerContainer(2, network);
        kafka3Container = initKafkaBrokerContainer(3, network);

        zookeeperContainer.start();
        kafka1Container.start();
        kafka2Container.start();
        kafka3Container.start();
    }

    private static KafkaContainer initKafkaBrokerContainer(int brokerId, Network network) {
        final Map<String, String> env = new LinkedHashMap<>();
        env.put("KAFKA_LISTENER_SECURITY_PROTOCOL_MAP", "BROKER:PLAINTEXT,PLAINTEXT:SASL_PLAINTEXT");

        env.put("KAFKA_LISTENER_NAME_PLAINTEXT_SASL_ENABLED_MECHANISMS", "PLAIN");

        env.put("KAFKA_LISTENER_NAME_PLAINTEXT_PLAIN_SASL_JAAS_CONFIG",
                "org.apache.kafka.common.security.plain.PlainLoginModule required "
                        + "username=\"admin\" "
                        + "password=\"admin-secret\" "
                        + "user_admin=\"admin-secret\" "
                        + "user_producer=\"producer-secret\" "
                        + "user_consumer=\"consumer-secret\";");

        env.put("KAFKA_SASL_JAAS_CONFIG", "org.apache.kafka.common.security.plain.PlainLoginModule required "
                + "username=\"admin\" "
                + "password=\"admin-secret\";");

        return new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:latest"))
                .withNetwork(network)
                .withNetworkAliases("kafka-" + brokerId)
                .withEnv("KAFKA_BROKER_ID", Integer.toString(brokerId))
                .withEnv(env)
                .withExternalZookeeper("zookeeper:2181")
                .dependsOn(zookeeperContainer)
                .withStartupAttempts(3)
                .withReuse(true);
    }

    private static GenericContainer<?> initZookeeperContainer(Network network) {
        return new GenericContainer<>(DockerImageName.parse("confluentinc/cp-zookeeper:latest"))
                .withNetwork(network)
                .withNetworkAliases("zookeeper")
                .withEnv("ZOOKEEPER_CLIENT_PORT", "2181")
                .withStartupAttempts(3)
                .withReuse(true);
    }

    @Component("KafkaInitializer")
    public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {

            String boostrapServers = kafka1Container.getBootstrapServers()
                    + "," + kafka2Container.getBootstrapServers()
                    + "," + kafka3Container.getBootstrapServers();
            TestPropertyValues.of(
                    "spring.kafka.bootstrap-servers=" + boostrapServers
            ).applyTo(applicationContext.getEnvironment());
        }
    }
}
