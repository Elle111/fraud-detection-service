package com.app.fraud.controller;

import com.app.fraud.entity.FraudEvaluation;
import com.app.fraud.repository.FraudEvaluationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/fraud-evaluations")
public class FraudEvaluationController {
    
    private final FraudEvaluationRepository repository;
    
    public FraudEvaluationController(FraudEvaluationRepository repository) {
        this.repository = repository;
    }
    
    @GetMapping("/{transactionId}")
    public ResponseEntity<List<FraudEvaluation>> getEvaluationsByTransactionId(@PathVariable UUID transactionId) {
        log.info("Fetching fraud evaluations for transaction: {}", transactionId);
        List<FraudEvaluation> evaluations = repository.findByTransactionId(transactionId);
        return ResponseEntity.ok(evaluations);
    }
    
    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<FraudEvaluation>> getEvaluationsByAccountId(@PathVariable UUID accountId) {
        log.info("Fetching fraud evaluations for account: {}", accountId);
        List<FraudEvaluation> evaluations = repository.findByAccountId(accountId);
        return ResponseEntity.ok(evaluations);
    }
    
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<FraudEvaluation>> getEvaluationsByCustomerId(@PathVariable UUID customerId) {
        log.info("Fetching fraud evaluations for customer: {}", customerId);
        List<FraudEvaluation> evaluations = repository.findByCustomerId(customerId);
        return ResponseEntity.ok(evaluations);
    }
    
    @GetMapping
    public ResponseEntity<List<FraudEvaluation>> getAllEvaluations() {
        log.info("Fetching all fraud evaluations");
        List<FraudEvaluation> evaluations = repository.findAll();
        return ResponseEntity.ok(evaluations);
    }
}
