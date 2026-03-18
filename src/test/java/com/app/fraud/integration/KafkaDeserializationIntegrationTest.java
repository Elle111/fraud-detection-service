package com.app.fraud.integration;

import com.app.fraud.dto.event.TransactionCreatedEvent;
import com.app.fraud.config.KafkaTestConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.ContextConfiguration;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = KafkaTestConfig.class)
@EmbeddedKafka(partitions = 1, topics = {"transaction.created.test"})
@ActiveProfiles("test")
public class KafkaDeserializationIntegrationTest {

    @Autowired
    private KafkaTemplate<String, TransactionCreatedEvent> kafkaTemplate;

    private static final String TEST_TOPIC = "transaction.created.test";

    @Test
    void testValidTransactionEventSerialization() throws Exception {
        // Create valid test event matching the exact JSON structure from kafka-test-data.md
        TransactionCreatedEvent testEvent = TransactionCreatedEvent.builder()
                .transactionId(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"))
                .accountId(UUID.fromString("550e8400-e29b-41d4-a716-446655440001"))
                .customerId(UUID.fromString("550e8400-e29b-41d4-a716-446655440002"))
                .amount(new BigDecimal("1500.00"))
                .currency("USD")
                .merchantId("merchant_111")
                .merchantCategory("5411")
                .paymentMethod("credit_card")
                .cardLastFour("1111")
                .timestamp(Instant.parse("2024-01-15T10:30:00Z"))
                .ipAddress("192.168.1.1")
                .deviceFingerprint("fp_abc123")
                .location("New York, NY")
                .build();

        // Send event to Kafka - this tests serialization
        var result = kafkaTemplate.send(TEST_TOPIC, testEvent.getTransactionId().toString(), testEvent);
        kafkaTemplate.flush();

        // Verify the message was sent successfully
        assertNotNull(result);
        var sendResult = result.get(10, TimeUnit.SECONDS);
        assertNotNull(sendResult);
        assertNotNull(sendResult.getRecordMetadata());
    }

    @Test
    void testMinimalTransactionEventSerialization() throws Exception {
        // Create minimal test event (only required fields)
        TransactionCreatedEvent testEvent = TransactionCreatedEvent.builder()
                .transactionId(UUID.randomUUID())
                .accountId(UUID.randomUUID())
                .customerId(UUID.randomUUID())
                .amount(new BigDecimal("100.00"))
                .currency("USD")
                .timestamp(Instant.now())
                .build();

        // Send event to Kafka
        var result = kafkaTemplate.send(TEST_TOPIC, testEvent.getTransactionId().toString(), testEvent);
        kafkaTemplate.flush();

        // Verify the message was sent successfully
        assertNotNull(result);
        var sendResult = result.get(10, TimeUnit.SECONDS);
        assertNotNull(sendResult.getRecordMetadata());
    }

    @Test
    void testKafkaConfiguration() {
        // Verify that Kafka components are properly configured
        assertNotNull(kafkaTemplate, "KafkaTemplate should be configured");
        assertNotNull(kafkaTemplate.getProducerFactory(), "Producer factory should be available");
    }
}
