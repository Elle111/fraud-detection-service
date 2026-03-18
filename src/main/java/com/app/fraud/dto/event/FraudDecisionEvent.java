package com.app.fraud.dto.event;

import com.app.fraud.dto.FraudDecision;
import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FraudDecisionEvent {
    @NotNull
    private UUID transactionId;
    
    @NotNull
    private UUID accountId;
    
    @NotNull
    private UUID customerId;
    
    @NotNull
    private BigDecimal amount;
    
    @NotNull
    private String currency;
    
    @NotNull
    private FraudDecision decision;
    
    @Min(0)
    @Max(1000)
    private Integer riskScore;
    
    private List<String> reasons;
    
    @NotNull
    private Instant timestamp;
    
    @NotNull
    private String evaluationId;
    
    private String version;
}
