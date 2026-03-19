package com.app.fraud.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRequest {
    
    @NotNull(message = "Transaction ID is required")
    private UUID transactionId;
    
    @NotNull(message = "Account ID is required")
    private UUID accountId;
    
    @NotNull(message = "Customer ID is required")
    private UUID customerId;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;
    
    @NotBlank(message = "Currency is required")
    private String currency;
    
    private String merchantId;
    private String merchantCategory;
    private String paymentMethod;
    private String cardLastFour;
    private String ipAddress;
    private String ipCountry;
    private String billingCountry;
    private String deviceFingerprint;
    private String location;
    
    @Builder.Default
    private Instant timestamp = Instant.now();
}
