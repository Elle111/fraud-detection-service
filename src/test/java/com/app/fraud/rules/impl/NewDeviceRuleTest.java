package com.app.fraud.rules.impl;

import com.app.fraud.dto.event.TransactionCreatedEvent;
import com.app.fraud.repository.FraudEvaluationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NewDeviceRuleTest {

    @Mock
    private FraudEvaluationRepository repository;

    @InjectMocks
    private NewDeviceRule rule;

    @Test
    void testRuleTriggered_WhenNewDevice() {
        TransactionCreatedEvent event = createTestEvent("new_device_123");
        when(repository.existsByAccountIdAndDevice(any(UUID.class), any(String.class)))
                .thenReturn(false);

        var result = rule.evaluate(event);

        assertTrue(result.isTriggered());
        assertEquals(140, result.getRiskScore());
        assertEquals("NewDeviceRule", result.getRuleName());
        assertEquals(1, result.getReasons().size());
        assertEquals("Transaction from new device", result.getReasons().get(0));
    }

    @Test
    void testRuleNotTriggered_WhenExistingDevice() {
        TransactionCreatedEvent event = createTestEvent("existing_device_456");
        when(repository.existsByAccountIdAndDevice(any(UUID.class), any(String.class)))
                .thenReturn(true);

        var result = rule.evaluate(event);

        assertFalse(result.isTriggered());
        assertEquals(0, result.getRiskScore());
        assertTrue(result.getReasons().isEmpty());
    }

    @Test
    void testRuleNotTriggered_WhenDeviceFingerprintIsNull() {
        TransactionCreatedEvent event = createTestEvent(null);

        var result = rule.evaluate(event);

        assertFalse(result.isTriggered());
        assertEquals(0, result.getRiskScore());
        assertTrue(result.getReasons().isEmpty());
    }

    @Test
    void testRuleNotTriggered_WhenDeviceFingerprintIsEmpty() {
        TransactionCreatedEvent event = createTestEvent("");

        var result = rule.evaluate(event);

        assertFalse(result.isTriggered());
        assertEquals(0, result.getRiskScore());
        assertTrue(result.getReasons().isEmpty());
    }

    @Test
    void testRuleNotTriggered_WhenDeviceFingerprintIsWhitespace() {
        TransactionCreatedEvent event = createTestEvent("   ");

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
                .deviceFingerprint("device_123")
                .build();

        var result = rule.evaluate(event);

        assertFalse(result.isTriggered());
        assertEquals(0, result.getRiskScore());
        assertEquals("Invalid event: missing accountId", result.getReasons().get(0));
    }

    @Test
    void testGetName() {
        assertEquals("NewDeviceRule", rule.getName());
    }

    @Test
    void testGetPriority() {
        assertEquals(35, rule.getPriority());
    }

    private TransactionCreatedEvent createTestEvent(String deviceFingerprint) {
        return TransactionCreatedEvent.builder()
                .transactionId(UUID.randomUUID())
                .accountId(UUID.randomUUID())
                .customerId(UUID.randomUUID())
                .deviceFingerprint(deviceFingerprint)
                .build();
    }
}
