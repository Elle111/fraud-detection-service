# Fraud Detection Service

A Spring Boot 3 microservice for real-time fraud detection using Kafka and PostgreSQL.

## Overview

The fraud detection service processes transaction events from Kafka, evaluates fraud risk using configurable rules, persists evaluation results in PostgreSQL, and publishes fraud decision events.

## Technology Stack

- **Java 21**
- **Spring Boot 3.2.5**
- **Spring Web**
- **Spring Kafka**
- **Spring Data JPA**
- **PostgreSQL**
- **H2 (for testing)**
- **Lombok**
- **Validation**
- **Actuator**
- **Testcontainers**

## Architecture

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   Transaction   │───▶│  Fraud Detection │───▶│  Fraud Decision │
│   Created Event │    │     Service      │    │    Event        │
└─────────────────┘    └──────────────────┘    └─────────────────┘
                               │
                               ▼
                       ┌─────────────────┐
                       │   PostgreSQL    │
                       │   Database      │
                       └─────────────────┘
```

## Package Structure

```
com.app.fraud/
├── config/                 # Configuration classes
├── dto.event/             # Event DTOs
├── entity/                # JPA entities
├── repository/            # Data repositories
├── rules/                 # Fraud detection rules
│   └── impl/             # Rule implementations
├── service/               # Business logic
├── kafka.consumer/        # Kafka consumers
├── kafka.producer/        # Kafka producers
└── controller/            # REST controllers
```

## Features

### Fraud Detection Rules

1. **Amount Threshold Rule**
   - High amount threshold: $10,000 (configurable)
   - Medium amount threshold: $5,000 (configurable)

2. **Frequency Rule**
   - Maximum transactions per hour: 10 (configurable)
   - Maximum transactions per day: 50 (configurable)

### Risk Scoring

- **LOW**: No rules triggered
- **MEDIUM**: Medium risk rules triggered
- **HIGH**: High risk rules triggered
- **CRITICAL**: Multiple high risk rules triggered

### Decision Types

- **APPROVED**: Transaction passes all checks
- **REJECTED**: Transaction fails critical checks
- **MANUAL_REVIEW**: Transaction requires manual investigation

## Configuration

### Application Properties

Key configuration options in `application.yml`:

```yaml
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/fraud_detection
spring.datasource.username=postgres
spring.datasource.password=password

# Kafka
spring.kafka.bootstrap-servers=localhost:9092

# Fraud Rules
fraud.rules.amount.threshold.high=10000.00
fraud.rules.amount.threshold.medium=5000.00
fraud.rules.frequency.max.transactions.per.hour=10
fraud.rules.frequency.max.transactions.per.day=50

# Kafka Topics
app.kafka.topics.transaction-created=transaction.created
app.kafka.topics.fraud-decision=fraud.decision
```

## API Endpoints

### Fraud Evaluation Endpoints

- `GET /api/v1/fraud-evaluations/{transactionId}` - Get evaluation by transaction ID
- `GET /api/v1/fraud-evaluations/account/{accountId}` - Get evaluations by account ID
- `GET /api/v1/fraud-evaluations/decision/{decision}` - Get evaluations by decision
- `GET /api/v1/fraud-evaluations/risk-score/{riskScore}` - Get evaluations by risk score
- `GET /api/v1/fraud-evaluations/date-range` - Get evaluations by date range
- `GET /api/v1/fraud-evaluations/account/{accountId}/stats` - Get account statistics
- `GET /api/v1/fraud-evaluations/stats` - Get system statistics

### Actuator Endpoints

- `GET /actuator/health` - Health check
- `GET /actuator/info` - Application info
- `GET /actuator/metrics` - Metrics
- `GET /actuator/prometheus` - Prometheus metrics

## Kafka Events

### Transaction Created Event

```json
{
  "transactionId": "txn_12345",
  "accountId": "acc_67890",
  "amount": 1500.00,
  "currency": "USD",
  "merchantId": "merchant_111",
  "merchantCategoryCode": "5411",
  "cardNumber": "4111111111111111",
  "cardType": "VISA",
  "ipAddress": "192.168.1.1",
  "deviceFingerprint": "fp_abc123",
  "location": "New York, NY",
  "timestamp": "2024-01-15T10:30:00Z"
}
```

### Fraud Decision Event

```json
{
  "transactionId": "txn_12345",
  "accountId": "acc_67890",
  "amount": 1500.00,
  "currency": "USD",
  "decision": "APPROVED",
  "riskScore": "LOW",
  "reason": "Transaction approved",
  "evaluationTimestamp": "2024-01-15T10:30:05Z",
  "originalTransactionTimestamp": "2024-01-15T10:30:00Z"
}
```

## Running the Application

### Prerequisites

- Java 21
- Maven 3.8+
- Docker & Docker Compose

### Quick Start with Docker Compose

1. **Start all services**
   ```bash
   docker-compose up -d
   ```

2. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

### Local Development

1. **Start PostgreSQL**
   ```bash
   docker run -d --name postgres-fraud \
     -e POSTGRES_DB=fraud_detection \
     -e POSTGRES_USER=postgres \
     -e POSTGRES_PASSWORD=password \
     -p 5432:5432 postgres:14
   ```

2. **Start Kafka**
   ```bash
   docker run -d --name kafka-fraud \
     -e KAFKA_ZOOKEEPER_CONNECT=zookeeper:2181 \
     -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9092 \
     -e KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=1 \
     -p 9092:9092 \
     confluentinc/cp-kafka:latest
   ```

3. **Create Kafka Topics**
   ```bash
   kafka-topics --create --topic transaction.created --bootstrap-server localhost:9092
   kafka-topics --create --topic fraud.decision --bootstrap-server localhost:9092
   ```

4. **Run the Application**
   ```bash
   mvn spring-boot:run
   ```

### Testing

Run the test suite:
```bash
mvn test
```

The application uses H2 in-memory database for testing and Testcontainers for integration tests with Kafka.

### API Testing with Postman

A Postman collection is provided in `postman-collection.json` with all available endpoints:

1. **Import the collection** into Postman
2. **Set environment variables**:
   - `baseUrl`: http://localhost:8080
   - `transactionId`: Sample UUID for transaction testing
   - `accountId`: Sample UUID for account testing  
   - `customerId`: Sample UUID for customer testing

**Available Endpoints:**
- `GET /actuator/health` - Health check
- `GET /actuator/info` - Application info
- `GET /actuator/metrics` - Application metrics
- `GET /actuator/prometheus` - Prometheus metrics
- `GET /api/fraud-evaluations` - Get all evaluations
- `GET /api/fraud-evaluations/{transactionId}` - Get by transaction ID
- `GET /api/fraud-evaluations/account/{accountId}` - Get by account ID
- `GET /api/fraud-evaluations/customer/{customerId}` - Get by customer ID

### Kafka Testing

Sample events and Kafka commands are provided in `kafka-test-data.md` for testing message flow.

### Docker Services

The `docker-compose.yml` file provides:
- **PostgreSQL**: Database service on port 5432
- **Kafka**: Message broker on port 9092 (with KRaft mode)
- **Zookeeper**: Coordination service for Kafka

To stop all services:
```bash
docker-compose down
```

To view logs:
```bash
docker-compose logs -f
```

## Monitoring

The service exposes metrics for monitoring:

- **Health**: Service health status
- **Metrics**: JVM and application metrics
- **Prometheus**: Metrics in Prometheus format

## Extending Fraud Rules

To add new fraud detection rules:

1. Implement the `FraudRule` interface
2. Add the `@Component` annotation
3. The rule will be automatically discovered and applied

Example:
```java
@Component
public class CustomFraudRule implements FraudRule {
    @Override
    public String getRuleName() {
        return "CustomFraudRule";
    }
    
    @Override
    public FraudRuleResult evaluate(TransactionCreatedEvent transaction) {
        // Implementation here
    }
    
    @Override
    public int getPriority() {
        return 3;
    }
}
```[.gitignore](.gitignore)

## License

This project is licensed under the MIT License.
