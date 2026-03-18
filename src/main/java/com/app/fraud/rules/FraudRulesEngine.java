package com.app.fraud.rules;

import com.app.fraud.dto.event.TransactionCreatedEvent;
import com.app.fraud.dto.FraudDecision;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FraudRulesEngine {
    
    private final List<FraudRule> rules;
    
    public FraudRulesEngine(List<FraudRule> rules) {
        this.rules = rules;
        log.info("Initialized FraudRulesEngine with {} rules", rules.size());
    }
    
    public FraudEvaluationResult evaluate(TransactionCreatedEvent event) {
        log.info("Evaluating fraud rules for transaction: {}", event.getTransactionId());
        
        List<FraudRuleResult> results = rules.stream()
                .map(rule -> rule.evaluate(event))
                .collect(Collectors.toList());
        
        // Determine final decision based on rule results
        FraudDecision finalDecision = determineFinalDecision(results);
        int totalRiskScore = results.stream()
                .mapToInt(result -> result.getRiskScore() != null ? result.getRiskScore() : 0)
                .sum();
        
        List<String> allReasons = results.stream()
                .flatMap(result -> result.getReasons().stream())
                .collect(Collectors.toList());
        
        return FraudEvaluationResult.builder()
                .transactionId(event.getTransactionId())
                .decision(finalDecision)
                .riskScore(totalRiskScore)
                .reasons(allReasons)
                .ruleResults(results)
                .build();
    }
    
    private FraudDecision determineFinalDecision(List<FraudRuleResult> results) {
        boolean hasDeclined = results.stream()
                .anyMatch(result -> result.getDecision() == FraudDecision.DECLINED);
        
        if (hasDeclined) {
            return FraudDecision.DECLINED;
        }
        
        boolean hasReview = results.stream()
                .anyMatch(result -> result.getDecision() == FraudDecision.REVIEW);
        
        return hasReview ? FraudDecision.REVIEW : FraudDecision.APPROVED;
    }
}
