package com.trading.userservice;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;

/**
 * Context-load smoke test for CI.
 *
 * Strategy:
 *  - H2 replaces PostgreSQL (see application-test.properties)
 *  - ConnectionFactory / RabbitTemplate are mocked so no broker is needed
 *  - A REAL SimpleRabbitListenerContainerFactory (autoStartup=false) is provided
 *    via @TestConfiguration so that RabbitListenerEndpointRegistry can call
 *    factory.createListenerContainer() and get a non-null result, preventing NPE
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class UserServiceApplicationTests {

    /** Mock the AMQP connection – no RabbitMQ broker required in CI */
    @MockBean
    ConnectionFactory connectionFactory;

    /** Mock the template so any injected RabbitTemplate reference resolves */
    @MockBean
    RabbitTemplate rabbitTemplate;

    /**
     * Provide a real container factory (not a mock!) that is wired to the
     * mocked ConnectionFactory with auto-startup disabled.
     * This prevents RabbitListenerEndpointRegistry from receiving null
     * when it calls factory.createListenerContainer(endpoint).
     */
    @TestConfiguration
    static class RabbitTestConfig {
        @Bean
        SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
                ConnectionFactory connectionFactory) {
            SimpleRabbitListenerContainerFactory factory =
                    new SimpleRabbitListenerContainerFactory();
            factory.setConnectionFactory(connectionFactory);
            factory.setAutoStartup(false);  // never actually connects
            return factory;
        }
    }

    @Test
    void contextLoads() {
        // passes if Spring context assembles without errors
    }
}
