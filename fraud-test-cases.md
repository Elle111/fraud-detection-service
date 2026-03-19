# Fraud Detection Test Cases

This file contains JSON examples that trigger different fraud detection rules to test the system.

## 1. Normal Transaction (Should be APPROVED)
```json
{
  "transactionId": "550e8400-e29b-41d4-a716-446655440000",
  "accountId": "550e8400-e29b-41d4-a716-446655440001",
  "customerId": "550e8400-e29b-41d4-a716-446655440002",
  "amount": 100.00,
  "currency": "USD",
  "merchantId": "merchant_safe",
  "merchantCategory": "5411",
  "paymentMethod": "credit_card",
  "cardLastFour": "1234",
  "timestamp": "2024-01-15T10:30:00Z",
  "ipAddress": "192.168.1.1",
  "ipCountry": "US",
  "billingCountry": "US",
  "deviceFingerprint": "fp_known_device_001",
  "location": "New York, NY"
}
```

## 2. High Amount Transaction (Triggers AmountThresholdRule - REVIEW)
```json
{
  "transactionId": "550e8400-e29b-41d4-a716-446655440010",
  "accountId": "550e8400-e29b-41d4-a716-446655440011",
  "customerId": "550e8400-e29b-41d4-a716-446655440012",
  "amount": 15000.00,
  "currency": "USD",
  "merchantId": "merchant_high_value",
  "merchantCategory": "5411",
  "paymentMethod": "credit_card",
  "cardLastFour": "5678",
  "timestamp": "2024-01-15T11:00:00Z",
  "ipAddress": "192.168.1.2",
  "ipCountry": "US",
  "billingCountry": "US",
  "deviceFingerprint": "fp_known_device_002",
  "location": "Los Angeles, CA"
}
```

## 3. Very High Amount Transaction (Triggers AmountThresholdRule - DECLINED)
```json
{
  "transactionId": "550e8400-e29b-41d4-a716-446655440020",
  "accountId": "550e8400-e29b-41d4-a716-446655440021",
  "customerId": "550e8400-e29b-41d4-a716-446655440022",
  "amount": 60000.00,
  "currency": "USD",
  "merchantId": "merchant_very_high",
  "merchantCategory": "5541",
  "paymentMethod": "credit_card",
  "cardLastFour": "9999",
  "timestamp": "2024-01-15T11:15:00Z",
  "ipAddress": "192.168.1.3",
  "ipCountry": "US",
  "billingCountry": "US",
  "deviceFingerprint": "fp_known_device_003",
  "location": "Chicago, IL"
}
```

## 4. High Frequency Account (Triggers FrequencyRule - REVIEW)
```json
{
  "transactionId": "550e8400-e29b-41d4-a716-446655440030",
  "accountId": "550e8400-e29b-41d4-a716-446655440031",
  "customerId": "550e8400-e29b-41d4-a716-446655440032",
  "amount": 200.00,
  "currency": "USD",
  "merchantId": "merchant_freq_test",
  "merchantCategory": "5812",
  "paymentMethod": "credit_card",
  "cardLastFour": "1111",
  "timestamp": "2024-01-15T12:00:00Z",
  "ipAddress": "192.168.1.4",
  "ipCountry": "US",
  "billingCountry": "US",
  "deviceFingerprint": "fp_freq_device",
  "location": "Miami, FL"
}
```
*(Send this multiple times within an hour to trigger the frequency rule)*

## 5. High Velocity Amount (Triggers AmountVelocityRule - REVIEW)
```json
{
  "transactionId": "550e8400-e29b-41d4-a716-446655440040",
  "accountId": "550e8400-e29b-41d4-a716-446655440041",
  "customerId": "550e8400-e29b-41d4-a716-446655440042",
  "amount": 8000.00,
  "currency": "USD",
  "merchantId": "merchant_velocity",
  "merchantCategory": "5311",
  "paymentMethod": "credit_card",
  "cardLastFour": "2222",
  "timestamp": "2024-01-15T13:00:00Z",
  "ipAddress": "192.168.1.5",
  "ipCountry": "US",
  "billingCountry": "US",
  "deviceFingerprint": "fp_velocity_device",
  "location": "Seattle, WA"
}
```
*(Send multiple transactions to the same account within an hour totaling over $20,000)*

## 6. New Account with High Amount (Triggers NewAccountRule - REVIEW)
```json
{
  "transactionId": "550e8400-e29b-41d4-a716-446655440050",
  "accountId": "550e8400-e29b-41d4-a716-446655440051",
  "customerId": "550e8400-e29b-41d4-a716-446655440052",
  "amount": 6000.00,
  "currency": "USD",
  "merchantId": "merchant_new_account",
  "merchantCategory": "5712",
  "paymentMethod": "credit_card",
  "cardLastFour": "3333",
  "timestamp": "2024-01-15T14:00:00Z",
  "ipAddress": "192.168.1.6",
  "ipCountry": "US",
  "billingCountry": "US",
  "deviceFingerprint": "fp_new_account_device",
  "location": "Boston, MA"
}
```

## 7. New Device Transaction (Triggers NewDeviceRule - REVIEW)
```json
{
  "transactionId": "550e8400-e29b-41d4-a716-446655440060",
  "accountId": "550e8400-e29b-41d4-a716-446655440061",
  "customerId": "550e8400-e29b-41d4-a716-446655440062",
  "amount": 500.00,
  "currency": "USD",
  "merchantId": "merchant_new_device",
  "merchantCategory": "5912",
  "paymentMethod": "credit_card",
  "cardLastFour": "4444",
  "timestamp": "2024-01-15T15:00:00Z",
  "ipAddress": "192.168.1.7",
  "ipCountry": "US",
  "billingCountry": "US",
  "deviceFingerprint": "fp_brand_new_device_12345",
  "location": "Denver, CO"
}
```

## 8. Geographic Mismatch (Triggers GeoMismatchRule - REVIEW)
```json
{
  "transactionId": "550e8400-e29b-41d4-a716-446655440070",
  "accountId": "550e8400-e29b-41d4-a716-446655440071",
  "customerId": "550e8400-e29b-41d4-a716-446655440072",
  "amount": 300.00,
  "currency": "USD",
  "merchantId": "merchant_geo_mismatch",
  "merchantCategory": "4111",
  "paymentMethod": "credit_card",
  "cardLastFour": "5555",
  "timestamp": "2024-01-15T16:00:00Z",
  "ipAddress": "192.168.1.8",
  "ipCountry": "CA",
  "billingCountry": "US",
  "deviceFingerprint": "fp_geo_device",
  "location": "Toronto, ON"
}
```

## 9. Multiple Risk Factors (Should trigger DECLINED)
```json
{
  "transactionId": "550e8400-e29b-41d4-a716-446655440080",
  "accountId": "550e8400-e29b-41d4-a716-446655440081",
  "customerId": "550e8400-e29b-41d4-a716-446655440082",
  "amount": 25000.00,
  "currency": "USD",
  "merchantId": "merchant_high_risk",
  "merchantCategory": "7995",
  "paymentMethod": "credit_card",
  "cardLastFour": "6666",
  "timestamp": "2024-01-15T17:00:00Z",
  "ipAddress": "192.168.1.9",
  "ipCountry": "RU",
  "billingCountry": "US",
  "deviceFingerprint": "fp_suspicious_new_device",
  "location": "Moscow, RU"
}
```

## 10. Failed Attempts Follow-up (Triggers FailedAttemptsRule - DECLINED)
```json
{
  "transactionId": "550e8400-e29b-41d4-a716-446655440090",
  "accountId": "550e8400-e29b-41d4-a716-446655440091",
  "customerId": "550e8400-e29b-41d4-a716-446655440092",
  "amount": 1000.00,
  "currency": "USD",
  "merchantId": "merchant_failed_attempts",
  "merchantCategory": "6011",
  "paymentMethod": "credit_card",
  "cardLastFour": "7777",
  "timestamp": "2024-01-15T18:00:00Z",
  "ipAddress": "192.168.1.10",
  "ipCountry": "US",
  "billingCountry": "US",
  "deviceFingerprint": "fp_failed_device",
  "location": "Las Vegas, NV"
}
```
*(Send this after multiple declined transactions to the same account within 15 minutes)*

## Testing Instructions

### Setup
```bash
# Start services
docker-compose up -d

# Create topics
kafka-topics --create --topic transaction.created --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1
kafka-topics --create --topic fraud.decision --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1

# Start consumer to watch results
kafka-console-consumer --topic fraud.decision --bootstrap-server localhost:9092 --from-beginning
```

### Send Test Transactions
```bash
# Start producer
kafka-console-producer --topic transaction.created --bootstrap-server localhost:9092

# Copy and paste each JSON example above
```

### Expected Results
- **Normal Transaction**: APPROVED, risk score 0
- **Single Risk Factor**: REVIEW, risk score 100-249
- **High Risk Single Factor**: REVIEW, risk score 150-300
- **Multiple Risk Factors**: DECLINED, risk score 250+
- **Failed Attempts**: DECLINED, risk score 220+

### Priority Order (Lower number = higher priority)
1. AmountThresholdRule (10)
2. FrequencyRule (20) 
3. FailedAttemptsRule (25)
4. AmountVelocityRule (30)
5. NewDeviceRule (35)
6. NewAccountRule (40)
7. GeoMismatchRule (50)
