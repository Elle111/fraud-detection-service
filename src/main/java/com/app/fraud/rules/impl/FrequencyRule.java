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

    private static final int HOURLY_FREQUENCY_SCORE = 250;
    private static final int DAILY_FREQUENCY_SCORE = 100;

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
    public int getPriority() {
        return 20;
    }

    @Override
    public FraudRuleResult evaluate(TransactionCreatedEvent event) {
        if (event == null || event.getAccountId() == null) {
            log.warn("Rule {} received invalid event or missing accountId", getName());
            return FraudRuleResult.builder()
                    .ruleName(getName())
                    .triggered(false)
                    .riskScore(0)
                    .reasons(List.of("Invalid event: missing accountId"))
                    .build();
        }

        log.debug("Evaluating frequency rule for account: {}", event.getAccountId());

        Instant oneHourAgo = Instant.now().minusSeconds(3600);
        Instant oneDayAgo = Instant.now().minusSeconds(86400);

        long transactionsLastHour = repository.countByAccountIdSince(event.getAccountId(), oneHourAgo);
        long transactionsLastDay = repository.countByAccountIdSince(event.getAccountId(), oneDayAgo);

        if (transactionsLastHour >= maxTransactionsPerHour) {
            return FraudRuleResult.builder()
                    .ruleName(getName())
                    .triggered(true)
                    .riskScore(HOURLY_FREQUENCY_SCORE)
                    .reasons(List.of("Transaction count in last hour is " + transactionsLastHour +
                            ", exceeding threshold " + maxTransactionsPerHour))
                    .build();
        }

        if (transactionsLastDay >= maxTransactionsPerDay) {
            return FraudRuleResult.builder()
                    .ruleName(getName())
                    .triggered(true)
                    .riskScore(DAILY_FREQUENCY_SCORE)
                    .reasons(List.of("Transaction count in last 24 hours is " + transactionsLastDay +
                            ", exceeding threshold " + maxTransactionsPerDay))
                    .build();
        }

        return FraudRuleResult.builder()
                .ruleName(getName())
                .triggered(false)
                .riskScore(0)
                .reasons(List.of())
                .build();
    }
}
