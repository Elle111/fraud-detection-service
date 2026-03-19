package com.app.fraud.rules.impl;

import com.app.fraud.rules.FraudRule;
import com.app.fraud.rules.FraudRuleResult;
import com.app.fraud.dto.event.TransactionCreatedEvent;
import com.app.fraud.repository.FraudEvaluationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Slf4j
@Component
public class AmountVelocityRule implements FraudRule {

    private static final int VELOCITY_RISK_SCORE = 180;

    private final FraudEvaluationRepository repository;

    @Value("${fraud.rules.velocity.max-amount-per-hour:20000}")
    private BigDecimal maxAmountPerHour;

    public AmountVelocityRule(FraudEvaluationRepository repository) {
        this.repository = repository;
    }

    @Override
    public String getName() {
        return "AmountVelocityRule";
    }

    @Override
    public int getPriority() {
        return 30;
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

        Instant oneHourAgo = Instant.now().minusSeconds(3600);
        
        BigDecimal totalAmount = repository.sumAmountByAccountIdSince(event.getAccountId(), oneHourAgo);

        log.debug("Evaluating {} for accountId={}, totalAmountInLastHour={}, threshold={}",
                getName(), event.getAccountId(), totalAmount, maxAmountPerHour);

        if (totalAmount.compareTo(maxAmountPerHour) >= 0) {
            return FraudRuleResult.builder()
                    .ruleName(getName())
                    .triggered(true)
                    .riskScore(VELOCITY_RISK_SCORE)
                    .reasons(List.of("Total transaction amount in last hour is " + totalAmount +
                            ", exceeding threshold " + maxAmountPerHour))
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
