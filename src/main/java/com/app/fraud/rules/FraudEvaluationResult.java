package com.app.fraud.rules;

import com.app.fraud.dto.FraudDecision;
import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FraudEvaluationResult {
    private UUID transactionId;
    private FraudDecision decision;
    private Integer riskScore;
    private List<String> reasons;
    private List<FraudRuleResult> ruleResults;
}
