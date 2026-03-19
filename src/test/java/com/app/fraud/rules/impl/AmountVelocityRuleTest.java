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

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AmountVelocityRuleTest {

    @Mock
    private FraudEvaluationRepository repository;

    @InjectMocks
    private AmountVelocityRule rule;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(rule, "maxAmountPerHour", new BigDecimal("20000"));
    }

    @Test
    void testRuleTriggered_WhenVelocityExceedsThreshold() {
        TransactionCreatedEvent event = createTestEvent();
        when(repository.sumAmountByAccountIdSince(any(UUID.class), any(Instant.class)))
                .thenReturn(new BigDecimal("25000"));

        var result = rule.evaluate(event);

        assertTrue(result.isTriggered());
        assertEquals(180, result.getRiskScore());
        assertEquals("AmountVelocityRule", result.getRuleName());
        assertEquals(1, result.getReasons().size());
        assertTrue(result.getReasons().get(0).contains("exceeding threshold"));
    }

    @Test
    void testRuleNotTriggered_WhenVelocityBelowThreshold() {
        TransactionCreatedEvent event = createTestEvent();
        when(repository.sumAmountByAccountIdSince(any(UUID.class), any(Instant.class)))
                .thenReturn(new BigDecimal("15000"));

        var result = rule.evaluate(event);

        assertFalse(result.isTriggered());
        assertEquals(0, result.getRiskScore());
        assertTrue(result.getReasons().isEmpty());
    }

    @Test
    void testRuleNotTriggered_WhenVelocityEqualsThreshold() {
        TransactionCreatedEvent event = createTestEvent();
        when(repository.sumAmountByAccountIdSince(any(UUID.class), any(Instant.class)))
                .thenReturn(new BigDecimal("20000"));

        var result = rule.evaluate(event);

        assertTrue(result.isTriggered());
        assertEquals(180, result.getRiskScore());
    }

    @Test
    void testRuleReturnsError_WhenEventIsNull() {
        var result = rule.evaluate(null);

        assertFalse(result.isTriggered());
        assertEquals(0, result.getRiskScore());
        assertEquals("Invalid event: missing accountId or amount", result.getReasons().get(0));
    }

    @Test
    void testRuleReturnsError_WhenAccountIdIsNull() {
        TransactionCreatedEvent event = TransactionCreatedEvent.builder()
                .transactionId(UUID.randomUUID())
                .accountId(null)
                .amount(new BigDecimal("1000"))
                .build();

        var result = rule.evaluate(event);

        assertFalse(result.isTriggered());
        assertEquals(0, result.getRiskScore());
        assertEquals("Invalid event: missing accountId or amount", result.getReasons().get(0));
    }

    @Test
    void testRuleReturnsError_WhenAmountIsNull() {
        TransactionCreatedEvent event = TransactionCreatedEvent.builder()
                .transactionId(UUID.randomUUID())
                .accountId(UUID.randomUUID())
                .amount(null)
                .build();

        var result = rule.evaluate(event);

        assertFalse(result.isTriggered());
        assertEquals(0, result.getRiskScore());
        assertEquals("Invalid event: missing accountId or amount", result.getReasons().get(0));
    }

    @Test
    void testGetName() {
        assertEquals("AmountVelocityRule", rule.getName());
    }

    @Test
    void testGetPriority() {
        assertEquals(30, rule.getPriority());
    }

    private TransactionCreatedEvent createTestEvent() {
        return TransactionCreatedEvent.builder()
                .transactionId(UUID.randomUUID())
                .accountId(UUID.randomUUID())
                .customerId(UUID.randomUUID())
                .amount(new BigDecimal("1000"))
                .currency("USD")
                .timestamp(Instant.now())
                .build();
    }
}
