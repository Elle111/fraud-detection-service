package com.app.fraud.rules.impl;

import com.app.fraud.rules.FraudRule;
import com.app.fraud.rules.FraudRuleResult;
import com.app.fraud.dto.event.TransactionCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class GeoMismatchRule implements FraudRule {

    private static final int GEO_MISMATCH_RISK_SCORE = 130;

    @Override
    public String getName() {
        return "GeoMismatchRule";
    }

    @Override
    public int getPriority() {
        return 50;
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

        if (event.getIpCountry() == null || event.getIpCountry().trim().isEmpty() ||
            event.getBillingCountry() == null || event.getBillingCountry().trim().isEmpty()) {
            log.debug("Missing country information for accountId={}", event.getAccountId());
            return FraudRuleResult.builder()
                    .ruleName(getName())
                    .triggered(false)
                    .riskScore(0)
                    .reasons(List.of())
                    .build();
        }

        log.debug("Evaluating {} for accountId={}, ipCountry={}, billingCountry={}",
                getName(), event.getAccountId(), event.getIpCountry(), event.getBillingCountry());

        log.debug("IP country: '{}' (null: {}, empty: '{}'), Billing country: '{}' (null: {}, empty: '{}')",
                event.getIpCountry(), 
                event.getIpCountry() == null,
                event.getIpCountry() == null ? "N/A" : event.getIpCountry().trim().isEmpty(),
                event.getBillingCountry(),
                event.getBillingCountry() == null,
                event.getBillingCountry() == null ? "N/A" : event.getBillingCountry().trim().isEmpty());

        if (!event.getIpCountry().equalsIgnoreCase(event.getBillingCountry())) {
            log.info("Geo mismatch detected for accountId={}: IP country '{}' vs billing country '{}'",
                    event.getAccountId(), event.getIpCountry(), event.getBillingCountry());
            return FraudRuleResult.builder()
                    .ruleName(getName())
                    .triggered(true)
                    .riskScore(GEO_MISMATCH_RISK_SCORE)
                    .reasons(List.of("IP country differs from billing country"))
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
