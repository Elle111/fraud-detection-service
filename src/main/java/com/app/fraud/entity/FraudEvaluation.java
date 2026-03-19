package com.app.fraud.entity;

import com.app.fraud.dto.FraudDecision;
import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "fraud_evaluations")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FraudEvaluation {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(nullable = false)
    private UUID transactionId;
    
    @Column(nullable = false)
    private UUID accountId;
    
    @Column(nullable = false)
    private UUID customerId;
    
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;
    
    @Column(nullable = false)
    private String currency;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FraudDecision decision;
    
    @Column
    private Integer riskScore;
    
    @ElementCollection
    @CollectionTable(name = "fraud_evaluation_reasons", joinColumns = @JoinColumn(name = "evaluation_id"))
    @Column(name = "reason")
    private List<String> reasons;
    
    @Column(nullable = false)
    private Instant timestamp;
    
    @Column
    private String deviceFingerprint;
    
    @Column(nullable = false, unique = true)
    private String evaluationId;
    
    @Column
    private String version;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
    
    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;
}
