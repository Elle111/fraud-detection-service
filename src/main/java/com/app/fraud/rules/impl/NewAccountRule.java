package com.app.fraud.rules.impl;

import com.app.fraud.rules.FraudRule;
import com.app.fraud.rules.FraudRuleResult;
import com.app.fraud.dto.event.TransactionCreatedEvent;
import com.app.fraud.repository.FraudEvaluationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Slf4j
@Component
public class NewAccountRule implements FraudRule {

    private static final int NEW_ACCOUNT_RISK_SCORE = 120;

    private final FraudEvaluationRepository repository;

    @Value("${fraud.rules.account.max-age-hours:24}")
    private int maxAccountAgeHours;

    @Value("${fraud.rules.account.high-amount-threshold:5000}")
    private BigDecimal highAmountThreshold;

    public NewAccountRule(FraudEvaluationRepository repository) {
        this.repository = repository;
    }

    @Override
    public String getName() {
        return "NewAccountRule";
    }

    @Override
    public int getPriority() {
        return 40;
    }

    @Override
    public FraudRuleResult evaluate(TransactionCreatedEvent event) {
        if (event == null || event.getAccountId() == null || event.getAmount() == null) {
            log.warn("Rule {} received invalid event or missing accountId/amount", getName());
            return FraudRuleResult.builder()
                    .ruleName(getName())
                    .triggered(false)
                    .riskScore(0)
                    .reasons(List.of("Invalid event: missing accountId or amount"))
                    .build();
        }

        Instant firstTransactionTime = repository.getFirstTransactionTimeForAccount(event.getAccountId());
        
        if (firstTransactionTime == null) {
            log.debug("Account {} has no previous transactions, treating as new account", event.getAccountId());
            firstTransactionTime = Instant.now();
        }

        Duration accountAge = Duration.between(firstTransactionTime, Instant.now());
        long accountAgeHours = accountAge.toHours();

        log.debug("Evaluating {} for accountId={}, accountAgeHours={}, amount={}, threshold={}",
                getName(), event.getAccountId(), accountAgeHours, event.getAmount(), highAmountThreshold);

        if (accountAgeHours <= maxAccountAgeHours && event.getAmount().compareTo(highAmountThreshold) >= 0) {
            return FraudRuleResult.builder()
                    .ruleName(getName())
                    .triggered(true)
                    .riskScore(NEW_ACCOUNT_RISK_SCORE)
                    .reasons(List.of("High transaction on new account (age: " + accountAgeHours + " hours)"))
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
