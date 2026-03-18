package com.app.fraud.rules.impl;

import com.app.fraud.rules.FraudRule;
import com.app.fraud.rules.FraudRuleResult;
import com.app.fraud.dto.event.TransactionCreatedEvent;
import com.app.fraud.dto.FraudDecision;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Component
public class AmountThresholdRule implements FraudRule {
    
    @Value("${fraud.rules.amount.threshold:10000}")
    private BigDecimal thresholdAmount;
    
    @Value("${fraud.rules.amount.high-threshold:50000}")
    private BigDecimal highThresholdAmount;
    
    @Override
    public String getName() {
        return "AmountThresholdRule";
    }
    
    @Override
    public FraudRuleResult evaluate(TransactionCreatedEvent event) {
        log.debug("Evaluating amount threshold rule for transaction: {}, amount: {}", 
                 event.getTransactionId(), event.getAmount());
        
        if (event.getAmount().compareTo(highThresholdAmount) >= 0) {
            return FraudRuleResult.builder()
                    .ruleName(getName())
                    .decision(FraudDecision.DECLINED)
                    .riskScore(300)
                    .reasons(List.of("Transaction amount exceeds high threshold of " + highThresholdAmount))
                    .triggered(true)
                    .build();
        } else if (event.getAmount().compareTo(thresholdAmount) >= 0) {
            return FraudRuleResult.builder()
                    .ruleName(getName())
                    .decision(FraudDecision.REVIEW)
                    .riskScore(150)
                    .reasons(List.of("Transaction amount exceeds threshold of " + thresholdAmount))
                    .triggered(true)
                    .build();
        }
        
        return FraudRuleResult.builder()
                .ruleName(getName())
                .decision(FraudDecision.APPROVED)
                .riskScore(0)
                .reasons(List.of())
                .triggered(false)
                .build();
    }
}
