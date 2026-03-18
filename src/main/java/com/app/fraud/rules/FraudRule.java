package com.app.fraud.rules;

import com.app.fraud.dto.event.TransactionCreatedEvent;

public interface FraudRule {
    String getName();
    FraudRuleResult evaluate(TransactionCreatedEvent event);
}
