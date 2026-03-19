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

    private static final int REVIEW_SCORE = 150;
    private static final int HIGH_RISK_SCORE = 300;

    @Value("${fraud.rules.amount.threshold:10000}")
    private BigDecimal thresholdAmount;

    @Value("${fraud.rules.amount.high-threshold:50000}")
    private BigDecimal highThresholdAmount;

    @Override
    public String getName() {
        return "AmountThresholdRule";
    }

    @Override
    public int getPriority() {
        return 10;
    }

    @Override
    public FraudRuleResult evaluate(TransactionCreatedEvent event) {
        if (event == null || event.getAmount() == null) {
            log.warn("Rule {} received invalid event or null amount", getName());
            return FraudRuleResult.builder()
                    .ruleName(getName())
                    .triggered(false)
                    .riskScore(0)
                    .reasons(List.of("Invalid event: missing transaction amount"))
                    .build();
        }

        BigDecimal amount = event.getAmount();

        log.debug("Evaluating {} for transactionId={}, amount={}",
                getName(), event.getTransactionId(), amount);

        if (amount.compareTo(highThresholdAmount) >= 0) {
            return FraudRuleResult.builder()
                    .ruleName(getName())
                    .triggered(true)
                    .riskScore(HIGH_RISK_SCORE)
                    .reasons(List.of("Transaction amount exceeds high threshold of " + highThresholdAmount))
                    .build();
        }

        if (amount.compareTo(thresholdAmount) >= 0) {
            return FraudRuleResult.builder()
                    .ruleName(getName())
                    .triggered(true)
                    .riskScore(REVIEW_SCORE)
                    .reasons(List.of("Transaction amount exceeds threshold of " + thresholdAmount))
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
