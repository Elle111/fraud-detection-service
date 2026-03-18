# Sample Transaction Created Event for Kafka Testing

```json
{
  "transactionId": "550e8400-e29b-41d4-a716-446655440000",
  "accountId": "550e8400-e29b-41d4-a716-446655440001",
  "customerId": "550e8400-e29b-41d4-a716-446655440002",
  "amount": 1500.00,
  "currency": "USD",
  "merchantId": "merchant_111",
  "merchantCategory": "5411",
  "paymentMethod": "credit_card",
  "cardLastFour": "1111",
  "timestamp": "2024-01-15T10:30:00Z",
  "ipAddress": "192.168.1.1",
  "deviceFingerprint": "fp_abc123",
  "location": "New York, NY"
}
```

# Expected Fraud Decision Event Output

```json
{
  "transactionId": "550e8400-e29b-41d4-a716-446655440000",
  "accountId": "550e8400-e29b-41d4-a716-446655440001",
  "customerId": "550e8400-e29b-41d4-a716-446655440002",
  "amount": 1500.00,
  "currency": "USD",
  "decision": "APPROVED",
  "riskScore": 10,
  "reasons": ["Transaction approved - low risk"],
  "timestamp": "2024-01-15T10:30:05Z",
  "evaluationId": "eval_550e8400-e29b-41d4-a716-446655440000",
  "version": "1.0"
}
```

# Kafka Commands for Testing

## Clean Start (Recommended)
```bash
# Stop and remove all data to start fresh
docker-compose down -v
docker-compose up -d

# Wait for services to be ready (about 30 seconds)
docker-compose logs -f kafka
```

## Create Topics
```bash
kafka-topics --create --topic transaction.created --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1
kafka-topics --create --topic fraud.decision --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1
```

## Produce Transaction Event
```bash
kafka-console-producer --topic transaction.created --bootstrap-server localhost:9092
```
(Paste the Transaction Created Event JSON above)

## Consume Fraud Decision Events
```bash
kafka-console-consumer --topic fraud.decision --bootstrap-server localhost:9092 --from-beginning
```

## List Topics
```bash
kafka-topics --list --bootstrap-server localhost:9092
```

## Describe Topics
```bash
kafka-topics --describe --topic transaction.created --bootstrap-server localhost:9092
kafka-topics --describe --topic fraud.decision --bootstrap-server localhost:9092
```

### Verify Message Consumption
```bash
# Check if messages are being consumed properly
kafka-console-consumer --topic transaction.created --bootstrap-server localhost:9092 --from-beginning --property print.key=true --property key.separator=":"
```
