package com.app.fraud.controller;

import com.app.fraud.dto.TransactionRequest;
import com.app.fraud.dto.TransactionResponse;
import com.app.fraud.dto.event.TransactionCreatedEvent;
import com.app.fraud.mapper.TransactionMapper;
import com.app.fraud.service.TransactionProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@Slf4j
@Validated
public class TransactionController {

    private final TransactionProducer transactionProducer;
    private final TransactionMapper transactionMapper;
    
    @Value("${app.kafka.topics.transaction-created:transaction.created}")
    private String transactionTopic;

    @PostMapping("/send")
    public ResponseEntity<TransactionResponse> sendTransaction(
            @Valid @RequestBody TransactionRequest request) {
        
        log.info("Received transaction request for transactionId: {}", request.getTransactionId());
        
        try {
            // Convert request to event
            TransactionCreatedEvent event = transactionMapper.toEvent(request);
            
            // Send to Kafka
            transactionProducer.sendTransactionEvent(event);
            
            // Return success response
            UUID transactionId = request.getTransactionId();
            TransactionResponse response = TransactionResponse.success(
                transactionId,
                transactionTopic
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            UUID transactionId = request.getTransactionId();
            log.error("Error processing transaction request for transactionId: {}", transactionId, e);
            
            TransactionResponse response = TransactionResponse.error(
                "Failed to process transaction: " + e.getMessage(),
                transactionId
            );
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/send-sync")
    public ResponseEntity<TransactionResponse> sendTransactionSync(
            @Valid @RequestBody TransactionRequest request) {
        
        log.info("Received sync transaction request for transactionId: {}", request.getTransactionId());
        
        try {
            // Convert request to event
            TransactionCreatedEvent event = transactionMapper.toEvent(request);
            
            // Send to Kafka synchronously and wait for result
            var sendResult = transactionProducer.sendTransactionEventSync(event);
            
            // Return success response with partition and offset info
            UUID transactionId = request.getTransactionId();
            TransactionResponse response = TransactionResponse.success(
                transactionId,
                transactionTopic,
                sendResult.getRecordMetadata().partition(),
                sendResult.getRecordMetadata().offset()
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            UUID transactionId = request.getTransactionId();
            log.error("Error processing sync transaction request for transactionId: {}", transactionId, e);
            
            TransactionResponse response = TransactionResponse.error(
                "Failed to process transaction: " + e.getMessage(),
                transactionId
            );
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Transaction service is healthy");
    }
}
