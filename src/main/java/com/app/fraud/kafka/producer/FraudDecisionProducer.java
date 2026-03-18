package com.app.fraud.kafka.producer;

import com.app.fraud.dto.event.FraudDecisionEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class FraudDecisionProducer {
    
    private final KafkaTemplate<String, FraudDecisionEvent> kafkaTemplate;
    
    @Value("${app.kafka.topics.fraud-decision:fraud.decision}")
    private String fraudDecisionTopic;
    
    public FraudDecisionProducer(KafkaTemplate<String, FraudDecisionEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }
    
    public void sendDecision(FraudDecisionEvent event) {
        try {
            kafkaTemplate.send(fraudDecisionTopic, event.getTransactionId().toString(), event)
                    .whenComplete((result, failure) -> {
                        if (failure != null) {
                            log.error("Failed to send fraud decision event for transaction: {}", event.getTransactionId(), failure);
                        } else {
                            log.info("Sent fraud decision event for transaction: {}", event.getTransactionId());
                        }
                    });
        } catch (Exception e) {
            log.error("Error sending fraud decision event for transaction: {}", event.getTransactionId(), e);
            throw new RuntimeException("Failed to send fraud decision event", e);
        }
    }
}
