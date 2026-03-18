package com.app.fraud.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {
    
    private String status;
    private String message;
    private UUID transactionId;
    private String topic;
    private Integer partition;
    private Long offset;
    
    public static TransactionResponse success(UUID transactionId, String topic, Integer partition, Long offset) {
        return TransactionResponse.builder()
                .status("SUCCESS")
                .message("Transaction event sent successfully")
                .transactionId(transactionId)
                .topic(topic)
                .partition(partition)
                .offset(offset)
                .build();
    }
    
    public static TransactionResponse success(UUID transactionId, String topic) {
        return TransactionResponse.builder()
                .status("SUCCESS")
                .message("Transaction event sent successfully")
                .transactionId(transactionId)
                .topic(topic)
                .build();
    }
    
    public static TransactionResponse error(String message, UUID transactionId) {
        return TransactionResponse.builder()
                .status("ERROR")
                .message(message)
                .transactionId(transactionId)
                .build();
    }
}
