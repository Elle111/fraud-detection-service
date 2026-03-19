package com.app.fraud.rules;

import com.app.fraud.dto.event.TransactionCreatedEvent;

public interface FraudRule {
    String getName();

    //Lower number = higher priority
    int getPriority();
    FraudRuleResult evaluate(TransactionCreatedEvent event);
}
