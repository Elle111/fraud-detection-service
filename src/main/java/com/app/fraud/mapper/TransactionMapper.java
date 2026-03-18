package com.app.fraud.mapper;

import com.app.fraud.dto.TransactionRequest;
import com.app.fraud.dto.event.TransactionCreatedEvent;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {
    
    public TransactionCreatedEvent toEvent(TransactionRequest request) {
        return TransactionCreatedEvent.builder()
                .transactionId(request.getTransactionId())
                .accountId(request.getAccountId())
                .customerId(request.getCustomerId())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .merchantId(request.getMerchantId())
                .merchantCategory(request.getMerchantCategory())
                .paymentMethod(request.getPaymentMethod())
                .cardLastFour(request.getCardLastFour())
                .timestamp(request.getTimestamp())
                .ipAddress(request.getIpAddress())
                .deviceFingerprint(request.getDeviceFingerprint())
                .location(request.getLocation())
                .build();
    }
}
