package com.app.fraud.rules;

import com.app.fraud.dto.FraudDecision;
import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FraudRuleResult {
    private String ruleName;
    private FraudDecision decision;
    private Integer riskScore;
    private List<String> reasons;
    private boolean triggered;
}
