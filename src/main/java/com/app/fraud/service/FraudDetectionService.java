package com.app.fraud.service;

import com.app.fraud.dto.event.TransactionCreatedEvent;
import com.app.fraud.dto.event.FraudDecisionEvent;
import com.app.fraud.entity.FraudEvaluation;
import com.app.fraud.rules.FraudEvaluationResult;
import com.app.fraud.rules.FraudRulesEngine;
import com.app.fraud.repository.FraudEvaluationRepository;
import com.app.fraud.kafka.producer.FraudDecisionProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
public class FraudDetectionService {
    
    private final FraudRulesEngine rulesEngine;
    private final FraudEvaluationRepository repository;
    private final FraudDecisionProducer decisionProducer;
    
    public FraudDetectionService(FraudRulesEngine rulesEngine, 
                                FraudEvaluationRepository repository,
                                FraudDecisionProducer decisionProducer) {
        this.rulesEngine = rulesEngine;
        this.repository = repository;
        this.decisionProducer = decisionProducer;
    }
    
    @Transactional
    public void processTransaction(TransactionCreatedEvent event) {
        log.info("Processing fraud detection for transaction: {}", event.getTransactionId());
        
        // Evaluate fraud rules
        FraudEvaluationResult evaluationResult = rulesEngine.evaluate(event);
        
        // Save evaluation result
        FraudEvaluation evaluation = saveEvaluation(event, evaluationResult);
        
        // Publish decision event
        publishDecisionEvent(event, evaluationResult, evaluation.getEvaluationId());
        
        log.info("Completed fraud detection for transaction: {}, decision: {}, riskScore: {}", 
                event.getTransactionId(), evaluationResult.getDecision(), evaluationResult.getRiskScore());
    }
    
    private FraudEvaluation saveEvaluation(TransactionCreatedEvent event, FraudEvaluationResult result) {
        FraudEvaluation evaluation = FraudEvaluation.builder()
                .transactionId(event.getTransactionId())
                .accountId(event.getAccountId())
                .customerId(event.getCustomerId())
                .amount(event.getAmount())
                .currency(event.getCurrency())
                .decision(result.getDecision())
                .riskScore(result.getRiskScore())
                .reasons(result.getReasons())
                .timestamp(event.getTimestamp())
                .evaluationId(UUID.randomUUID().toString())
                .version("1.0")
                .build();
        
        return repository.save(evaluation);
    }
    
    private void publishDecisionEvent(TransactionCreatedEvent transactionEvent, 
                                    FraudEvaluationResult evaluationResult,
                                    String evaluationId) {
        FraudDecisionEvent decisionEvent = FraudDecisionEvent.builder()
                .transactionId(transactionEvent.getTransactionId())
                .accountId(transactionEvent.getAccountId())
                .customerId(transactionEvent.getCustomerId())
                .amount(transactionEvent.getAmount())
                .currency(transactionEvent.getCurrency())
                .decision(evaluationResult.getDecision())
                .riskScore(evaluationResult.getRiskScore())
                .reasons(evaluationResult.getReasons())
                .timestamp(transactionEvent.getTimestamp())
                .evaluationId(evaluationId)
                .version("1.0")
                .build();
        
        decisionProducer.sendDecision(decisionEvent);
    }
}
