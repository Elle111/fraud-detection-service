package com.app.fraud.service;

import com.app.fraud.dto.event.TransactionCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionProducer {

    private final KafkaTemplate<String, TransactionCreatedEvent> kafkaTemplate;
    
    @Value("${app.kafka.topics.transaction-created:transaction.created}")
    private String transactionTopic;

    public void sendTransactionEvent(TransactionCreatedEvent event) {
        try {
            log.info("Sending transaction event for transactionId: {}", event.getTransactionId());
            var result = kafkaTemplate.send(transactionTopic, event.getTransactionId().toString(), event);
            result.whenComplete((sendResult, ex) -> {
                if (ex != null) {
                    log.error("Failed to send transaction event for transactionId: {}", event.getTransactionId(), ex);
                } else {
                    log.info("Successfully sent transaction event for transactionId: {} to partition {} with offset {}",
                            event.getTransactionId(),
                            sendResult.getRecordMetadata().partition(),
                            sendResult.getRecordMetadata().offset());
                }
            });
            kafkaTemplate.flush();
        } catch (Exception e) {
            log.error("Error sending transaction event for transactionId: {}", event.getTransactionId(), e);
            throw new RuntimeException("Failed to send transaction event", e);
        }
    }
    
    public SendResult<String, TransactionCreatedEvent> sendTransactionEventSync(TransactionCreatedEvent event) {
        try {
            log.info("Sending transaction event synchronously for transactionId: {}", event.getTransactionId());
            var result = kafkaTemplate.send(transactionTopic, event.getTransactionId().toString(), event);
            kafkaTemplate.flush();
            
            SendResult<String, TransactionCreatedEvent> sendResult = result.get(10, TimeUnit.SECONDS);
            
            log.info("Successfully sent transaction event for transactionId: {} to partition {} with offset {}",
                    event.getTransactionId(),
                    sendResult.getRecordMetadata().partition(),
                    sendResult.getRecordMetadata().offset());
                    
            return sendResult;
            
        } catch (Exception e) {
            log.error("Error sending transaction event synchronously for transactionId: {}", event.getTransactionId(), e);
            throw new RuntimeException("Failed to send transaction event", e);
        }
    }
}
