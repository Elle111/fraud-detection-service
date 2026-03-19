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
public class FailedAttemptsRule implements FraudRule {

    private static final int FAILED_ATTEMPTS_RISK_SCORE = 220;

    private final FraudEvaluationRepository repository;

    @Value("${fraud.rules.failed.max-failed-per-15min:5}")
    private int maxFailedPer15Min;

    public FailedAttemptsRule(FraudEvaluationRepository repository) {
        this.repository = repository;
    }

    @Override
    public String getName() {
        return "FailedAttemptsRule";
    }

    @Override
    public int getPriority() {
        return 25;
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

        Instant fifteenMinutesAgo = Instant.now().minusSeconds(900);
        
        long failedAttempts = repository.countByAccountIdSinceAndDecision(
                event.getAccountId(), fifteenMinutesAgo, FraudDecision.DECLINED);

        log.debug("Evaluating {} for accountId={}, failedAttemptsInLast15Min={}, threshold={}",
                getName(), event.getAccountId(), failedAttempts, maxFailedPer15Min);

        if (failedAttempts >= maxFailedPer15Min) {
            return FraudRuleResult.builder()
                    .ruleName(getName())
                    .triggered(true)
                    .riskScore(FAILED_ATTEMPTS_RISK_SCORE)
                    .reasons(List.of("Failed transaction attempts in last 15 minutes: " + failedAttempts))
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
