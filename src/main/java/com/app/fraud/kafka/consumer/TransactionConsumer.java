package com.app.fraud.kafka.consumer;

import com.app.fraud.dto.event.TransactionCreatedEvent;
import com.app.fraud.service.FraudDetectionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TransactionConsumer {
    
    private final FraudDetectionService fraudDetectionService;
    
    public TransactionConsumer(FraudDetectionService fraudDetectionService) {
        this.fraudDetectionService = fraudDetectionService;
    }
    
    @KafkaListener(
        topics = "${app.kafka.topics.transaction-created:transaction.created}",
        groupId = "${app.kafka.consumer-group:fraud-detection-service}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleTransactionCreated(@Payload TransactionCreatedEvent event) {
        log.info("Received transaction created event: {}", event.getTransactionId());
        
        try {
            fraudDetectionService.processTransaction(event);
        } catch (Exception e) {
            log.error("Error processing transaction: {}", event.getTransactionId(), e);
            throw e; // Re-throw to trigger Kafka retry mechanism
        }
    }
}
