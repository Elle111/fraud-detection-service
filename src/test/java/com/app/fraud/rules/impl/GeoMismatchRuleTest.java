package com.app.fraud.rules.impl;

import com.app.fraud.dto.event.TransactionCreatedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class GeoMismatchRuleTest {

    @InjectMocks
    private GeoMismatchRule rule;

    @Test
    void testRuleTriggered_WhenCountriesDiffer() {
        TransactionCreatedEvent event = createTestEvent("US", "GB");

        var result = rule.evaluate(event);

        assertTrue(result.isTriggered());
        assertEquals(130, result.getRiskScore());
        assertEquals("GeoMismatchRule", result.getRuleName());
        assertEquals(1, result.getReasons().size());
        assertEquals("IP country differs from billing country", result.getReasons().get(0));
    }

    @Test
    void testRuleTriggered_WhenCountriesDifferCaseInsensitive() {
        TransactionCreatedEvent event = createTestEvent("us", "GB");

        var result = rule.evaluate(event);

        assertTrue(result.isTriggered());
        assertEquals(130, result.getRiskScore());
    }

    @Test
    void testRuleNotTriggered_WhenCountriesMatch() {
        TransactionCreatedEvent event = createTestEvent("US", "US");

        var result = rule.evaluate(event);

        assertFalse(result.isTriggered());
        assertEquals(0, result.getRiskScore());
        assertTrue(result.getReasons().isEmpty());
    }

    @Test
    void testRuleNotTriggered_WhenIpCountryIsNull() {
        TransactionCreatedEvent event = createTestEvent(null, "US");

        var result = rule.evaluate(event);

        assertFalse(result.isTriggered());
        assertEquals(0, result.getRiskScore());
        assertTrue(result.getReasons().isEmpty());
    }

    @Test
    void testRuleNotTriggered_WhenIpCountryIsEmpty() {
        TransactionCreatedEvent event = createTestEvent("", "US");

        var result = rule.evaluate(event);

        assertFalse(result.isTriggered());
        assertEquals(0, result.getRiskScore());
        assertTrue(result.getReasons().isEmpty());
    }

    @Test
    void testRuleNotTriggered_WhenBillingCountryIsNull() {
        TransactionCreatedEvent event = createTestEvent("US", null);

        var result = rule.evaluate(event);

        assertFalse(result.isTriggered());
        assertEquals(0, result.getRiskScore());
        assertTrue(result.getReasons().isEmpty());
    }

    @Test
    void testRuleNotTriggered_WhenBillingCountryIsEmpty() {
        TransactionCreatedEvent event = createTestEvent("US", "");

        var result = rule.evaluate(event);

        assertFalse(result.isTriggered());
        assertEquals(0, result.getRiskScore());
        assertTrue(result.getReasons().isEmpty());
    }

    @Test
    void testRuleNotTriggered_WhenBothCountriesAreWhitespace() {
        TransactionCreatedEvent event = createTestEvent("   ", "   ");

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
                .ipCountry("US")
                .billingCountry("GB")
                .build();

        var result = rule.evaluate(event);

        assertFalse(result.isTriggered());
        assertEquals(0, result.getRiskScore());
        assertEquals("Invalid event: missing accountId", result.getReasons().get(0));
    }

    @Test
    void testGetName() {
        assertEquals("GeoMismatchRule", rule.getName());
    }

    @Test
    void testGetPriority() {
        assertEquals(50, rule.getPriority());
    }

    private TransactionCreatedEvent createTestEvent(String ipCountry, String billingCountry) {
        return TransactionCreatedEvent.builder()
                .transactionId(UUID.randomUUID())
                .accountId(UUID.randomUUID())
                .customerId(UUID.randomUUID())
                .ipCountry(ipCountry)
                .billingCountry(billingCountry)
                .build();
    }
}
