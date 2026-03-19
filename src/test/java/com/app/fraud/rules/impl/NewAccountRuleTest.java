package com.app.fraud.rules.impl;

import com.app.fraud.dto.event.TransactionCreatedEvent;
import com.app.fraud.repository.FraudEvaluationRepository;
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
class NewAccountRuleTest {

    @Mock
    private FraudEvaluationRepository repository;

    @InjectMocks
    private NewAccountRule rule;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(rule, "maxAccountAgeHours", 24);
        ReflectionTestUtils.setField(rule, "highAmountThreshold", new BigDecimal("5000"));
    }

    @Test
    void testRuleTriggered_WhenNewAccountWithHighAmount() {
        TransactionCreatedEvent event = createTestEvent(new BigDecimal("6000"));
        when(repository.getFirstTransactionTimeForAccount(any(UUID.class)))
                .thenReturn(Instant.now().minusSeconds(3600)); // 1 hour ago

        var result = rule.evaluate(event);

        assertTrue(result.isTriggered());
        assertEquals(120, result.getRiskScore());
        assertEquals("NewAccountRule", result.getRuleName());
        assertEquals(1, result.getReasons().size());
        assertTrue(result.getReasons().get(0).contains("High transaction on new account"));
    }

    @Test
    void testRuleNotTriggered_WhenOldAccountWithHighAmount() {
        TransactionCreatedEvent event = createTestEvent(new BigDecimal("6000"));
        when(repository.getFirstTransactionTimeForAccount(any(UUID.class)))
                .thenReturn(Instant.now().minusSeconds(86400 * 2)); // 2 days ago

        var result = rule.evaluate(event);

        assertFalse(result.isTriggered());
        assertEquals(0, result.getRiskScore());
        assertTrue(result.getReasons().isEmpty());
    }

    @Test
    void testRuleNotTriggered_WhenNewAccountWithLowAmount() {
        TransactionCreatedEvent event = createTestEvent(new BigDecimal("3000"));
        when(repository.getFirstTransactionTimeForAccount(any(UUID.class)))
                .thenReturn(Instant.now().minusSeconds(3600)); // 1 hour ago

        var result = rule.evaluate(event);

        assertFalse(result.isTriggered());
        assertEquals(0, result.getRiskScore());
        assertTrue(result.getReasons().isEmpty());
    }

    @Test
    void testRuleTriggered_WhenAccountAgeExactlyAtThreshold() {
        TransactionCreatedEvent event = createTestEvent(new BigDecimal("5000"));
        when(repository.getFirstTransactionTimeForAccount(any(UUID.class)))
                .thenReturn(Instant.now().minusSeconds(86400)); // exactly 24 hours ago

        var result = rule.evaluate(event);

        assertTrue(result.isTriggered());
        assertEquals(120, result.getRiskScore());
    }

    @Test
    void testRuleTriggered_WhenAmountExactlyAtThreshold() {
        TransactionCreatedEvent event = createTestEvent(new BigDecimal("5000"));
        when(repository.getFirstTransactionTimeForAccount(any(UUID.class)))
                .thenReturn(Instant.now().minusSeconds(3600)); // 1 hour ago

        var result = rule.evaluate(event);

        assertTrue(result.isTriggered());
        assertEquals(120, result.getRiskScore());
    }

    @Test
    void testRuleNotTriggered_WhenNoPreviousTransactions() {
        TransactionCreatedEvent event = createTestEvent(new BigDecimal("3000"));
        when(repository.getFirstTransactionTimeForAccount(any(UUID.class)))
                .thenReturn(null); // No previous transactions

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
        assertEquals("NewAccountRule", rule.getName());
    }

    @Test
    void testGetPriority() {
        assertEquals(40, rule.getPriority());
    }

    private TransactionCreatedEvent createTestEvent(BigDecimal amount) {
        return TransactionCreatedEvent.builder()
                .transactionId(UUID.randomUUID())
                .accountId(UUID.randomUUID())
                .customerId(UUID.randomUUID())
                .amount(amount)
                .currency("USD")
                .timestamp(Instant.now())
                .build();
    }
}
