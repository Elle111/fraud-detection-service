package com.app.fraud.rules.impl;

import com.app.fraud.dto.event.TransactionCreatedEvent;
import com.app.fraud.repository.FraudEvaluationRepository;
import com.app.fraud.dto.FraudDecision;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FailedAttemptsRuleTest {

    @Mock
    private FraudEvaluationRepository repository;

    @InjectMocks
    private FailedAttemptsRule rule;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(rule, "maxFailedPer15Min", 5);
    }

    @Test
    void testRuleTriggered_WhenFailedAttemptsExceedThreshold() {
        TransactionCreatedEvent event = createTestEvent();
        when(repository.countByAccountIdSinceAndDecision(any(UUID.class), any(Instant.class), any(FraudDecision.class)))
                .thenReturn(7L);

        var result = rule.evaluate(event);

        assertTrue(result.isTriggered());
        assertEquals(220, result.getRiskScore());
        assertEquals("FailedAttemptsRule", result.getRuleName());
        assertEquals(1, result.getReasons().size());
        assertTrue(result.getReasons().get(0).contains("Failed transaction attempts"));
    }

    @Test
    void testRuleTriggered_WhenFailedAttemptsEqualThreshold() {
        TransactionCreatedEvent event = createTestEvent();
        when(repository.countByAccountIdSinceAndDecision(any(UUID.class), any(Instant.class), any(FraudDecision.class)))
                .thenReturn(5L);

        var result = rule.evaluate(event);

        assertTrue(result.isTriggered());
        assertEquals(220, result.getRiskScore());
    }

    @Test
    void testRuleNotTriggered_WhenFailedAttemptsBelowThreshold() {
        TransactionCreatedEvent event = createTestEvent();
        when(repository.countByAccountIdSinceAndDecision(any(UUID.class), any(Instant.class), any(FraudDecision.class)))
                .thenReturn(3L);

        var result = rule.evaluate(event);

        assertFalse(result.isTriggered());
        assertEquals(0, result.getRiskScore());
        assertTrue(result.getReasons().isEmpty());
    }

    @Test
    void testRuleReturnsError_WhenEventIsNull() {
        var result = rule.evaluate(null);

        assertFalse(result.isTriggered());
        assertEquals(0, result.getRiskScore());
        assertEquals("Invalid event: missing accountId", result.getReasons().get(0));
    }

    @Test
    void testRuleReturnsError_WhenAccountIdIsNull() {
        TransactionCreatedEvent event = TransactionCreatedEvent.builder()
                .transactionId(UUID.randomUUID())
                .accountId(null)
                .build();

        var result = rule.evaluate(event);

        assertFalse(result.isTriggered());
        assertEquals(0, result.getRiskScore());
        assertEquals("Invalid event: missing accountId", result.getReasons().get(0));
    }

    @Test
    void testGetName() {
        assertEquals("FailedAttemptsRule", rule.getName());
    }

    @Test
    void testGetPriority() {
        assertEquals(25, rule.getPriority());
    }

    private TransactionCreatedEvent createTestEvent() {
        return TransactionCreatedEvent.builder()
                .transactionId(UUID.randomUUID())
                .accountId(UUID.randomUUID())
                .customerId(UUID.randomUUID())
                .timestamp(Instant.now())
                .build();
    }
}
