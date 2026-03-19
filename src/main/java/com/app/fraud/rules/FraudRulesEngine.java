package com.app.fraud.rules;

import com.app.fraud.dto.event.TransactionCreatedEvent;
import com.app.fraud.dto.FraudDecision;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FraudRulesEngine {

    @Value("${fraud.engine.review-threshold:100}")
    private int reviewThreshold;

    @Value("${fraud.engine.decline-threshold:250}")
    private int declineThreshold;


    private final List<FraudRule> rules;
    
    public FraudRulesEngine(List<FraudRule> rules) {
        this.rules = rules;
        log.info("Initialized FraudRulesEngine with {} rules", rules.size());
    }
    
    public FraudEvaluationResult evaluate(TransactionCreatedEvent event) {
        if (event == null || event.getTransactionId() == null) {
            throw new IllegalArgumentException("Transaction event cannot be null or have null transaction ID");
        }
        log.info("Evaluating fraud rules for transaction: {}", event.getTransactionId());
        
        List<FraudRuleResult> results = rules.stream()
                .sorted(Comparator.comparingInt(FraudRule::getPriority))
                .peek(rule -> log.debug("Evaluating rule: {} (priority: {})", rule.getClass().getSimpleName(), rule.getPriority()))
                .map(rule -> {
                    FraudRuleResult result = rule.evaluate(event);
                    log.debug("Rule {} result: triggered={}, riskScore={}", 
                            rule.getClass().getSimpleName(), result.isTriggered(), result.getRiskScore());
                    return result;
                })
                .toList();
        
        // Determine final decision based on rule results
        int totalRiskScore = results.stream()
                .filter(FraudRuleResult::isTriggered)
                .mapToInt(result -> result.getRiskScore() != null ? result.getRiskScore() : 0)
                .sum();

        FraudDecision finalDecision = determineFinalDecision(totalRiskScore);

        List<String> allReasons = results.stream()
                .filter(FraudRuleResult:: isTriggered)
                .flatMap(result -> result.getReasons().stream())
                .collect(Collectors.toList());

        log.info("Fraud evaluation completed for transactionId={}, decision={}, riskScore={}",
                event.getTransactionId(), finalDecision, totalRiskScore);

        return FraudEvaluationResult.builder()
                .transactionId(event.getTransactionId())
                .decision(finalDecision)
                .riskScore(totalRiskScore)
                .reasons(allReasons)
                .ruleResults(results)
                .build();
    }
    
    private FraudDecision determineFinalDecision(int totalRiskScore) {
        if (totalRiskScore >= declineThreshold) {
            return FraudDecision.DECLINED;
        }
        if (totalRiskScore >= reviewThreshold) {
            return FraudDecision.REVIEW;
        }
        return FraudDecision.APPROVED;
    }
}
