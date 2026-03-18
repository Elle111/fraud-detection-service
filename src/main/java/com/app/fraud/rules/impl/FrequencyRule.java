package com.app.fraud.rules.impl;

import com.app.fraud.rules.FraudRule;
import com.app.fraud.rules.FraudRuleResult;
import com.app.fraud.dto.event.TransactionCreatedEvent;
import com.app.fraud.dto.FraudDecision;
import com.app.fraud.repository.FraudEvaluationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Slf4j
@Component
public class FrequencyRule implements FraudRule {
    
    private final FraudEvaluationRepository repository;
    
    @Value("${fraud.rules.frequency.max-transactions-per-hour:10}")
    private int maxTransactionsPerHour;
    
    @Value("${fraud.rules.frequency.max-transactions-per-day:50}")
    private int maxTransactionsPerDay;
    
    public FrequencyRule(FraudEvaluationRepository repository) {
        this.repository = repository;
    }
    
    @Override
    public String getName() {
        return "FrequencyRule";
    }
    
    @Override
    public FraudRuleResult evaluate(TransactionCreatedEvent event) {
        log.debug("Evaluating frequency rule for account: {}", event.getAccountId());
        
        Instant oneHourAgo = Instant.now().minusSeconds(3600);
        Instant oneDayAgo = Instant.now().minusSeconds(86400);
        
        long transactionsLastHour = repository.countByAccountIdSince(event.getAccountId(), oneHourAgo);
        long transactionsLastDay = repository.countByAccountIdSince(event.getAccountId(), oneDayAgo);
        
        if (transactionsLastHour >= maxTransactionsPerHour) {
            return FraudRuleResult.builder()
                    .ruleName(getName())
                    .decision(FraudDecision.DECLINED)
                    .riskScore(250)
                    .reasons(List.of("Too many transactions in the last hour: " + transactionsLastHour))
                    .triggered(true)
                    .build();
        } else if (transactionsLastDay >= maxTransactionsPerDay) {
            return FraudRuleResult.builder()
                    .ruleName(getName())
                    .decision(FraudDecision.REVIEW)
                    .riskScore(100)
                    .reasons(List.of("High transaction frequency in the last 24 hours: " + transactionsLastDay))
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
