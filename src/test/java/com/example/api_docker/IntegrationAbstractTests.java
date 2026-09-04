package com.example.api_docker;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@EmbeddedKafka(partitions = 1)
@TestPropertySource(locations = "classpath:application-test-integration.properties")
public abstract class IntegrationAbstractTests {
}