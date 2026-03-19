package com.app.fraud.rules.impl;

import com.app.fraud.rules.FraudRule;
import com.app.fraud.rules.FraudRuleResult;
import com.app.fraud.dto.event.TransactionCreatedEvent;
import com.app.fraud.repository.FraudEvaluationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class NewDeviceRule implements FraudRule {

    private static final int NEW_DEVICE_RISK_SCORE = 140;

    private final FraudEvaluationRepository repository;

    public NewDeviceRule(FraudEvaluationRepository repository) {
        this.repository = repository;
    }

    @Override
    public String getName() {
        return "NewDeviceRule";
    }

    @Override
    public int getPriority() {
        return 35;
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

        if (event.getDeviceFingerprint() == null || event.getDeviceFingerprint().trim().isEmpty()) {
            log.debug("No device fingerprint provided for accountId={}", event.getAccountId());
            return FraudRuleResult.builder()
                    .ruleName(getName())
                    .triggered(false)
                    .riskScore(0)
                    .reasons(List.of())
                    .build();
        }

        boolean deviceExists = repository.existsByAccountIdAndDevice(
                event.getAccountId(), event.getDeviceFingerprint());

        log.debug("Evaluating {} for accountId={}, deviceFingerprint={}, deviceExists={}",
                getName(), event.getAccountId(), event.getDeviceFingerprint(), deviceExists);

        if (!deviceExists) {
            return FraudRuleResult.builder()
                    .ruleName(getName())
                    .triggered(true)
                    .riskScore(NEW_DEVICE_RISK_SCORE)
                    .reasons(List.of("Transaction from new device"))
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
