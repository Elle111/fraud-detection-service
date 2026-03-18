package com.app.fraud.dto.event;

import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransactionCreatedEvent {
    private UUID transactionId;
    private UUID accountId;
    private UUID customerId;
    private BigDecimal amount;
    private String currency;
    private String merchantId;
    private String merchantCategory;
    private String paymentMethod;
    private String cardLastFour;
    private Instant timestamp;
    private String ipAddress;
    private String deviceFingerprint;
    private String location;
}
