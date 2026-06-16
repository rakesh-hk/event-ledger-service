# Event Ledger

## Architecture

Event Ledger is a two-service microservice system composed of:

- `event-gateway`: public-facing API for event ingestion, persistence, validation, idempotency, trace generation, and resilient forwarding to account service.
- `account-service`: independent account balance and transaction service with its own H2 database and trace propagation support.

## Design Decisions

- Multi-module Maven project for independent service lifecycle.
- Spring Boot 3.x with Spring Web, Spring Data JPA, OpenFeign, and Resilience4j.
- H2 in-memory databases for service independence and easy local startup.
- JSON structured logs via Logstash encoder.
- Trace propagation through `X-Trace-Id` and MDC.
- Prometheus metrics exposed through `/actuator/prometheus`.

## Setup

Requirements:

- Java 21
- Maven 3.9+
- Docker + Docker Compose (optional)

### Build locally

```bash
mvn clean package -DskipTests=false
```

### Run locally

```bash
cd event-ledger
mvn -pl account-service spring-boot:run &
mvn -pl event-gateway spring-boot:run
```

### Run with Docker

```bash
docker compose up --build
```

## Service URLs

- Event Gateway: `http://localhost:8080`
- Account Service: `http://localhost:8081`
- Prometheus metrics:
  - `http://localhost:8080/actuator/prometheus`
  - `http://localhost:8081/actuator/prometheus`

## APIs

### Event Gateway

- `POST /events`
- `GET /events/{id}`
- `GET /events?account={accountId}`
- `GET /health`

### Account Service

- `POST /accounts/{accountId}/transactions`
- `GET /accounts/{accountId}/balance`
- `GET /accounts/{accountId}`
- `GET /health`

## Sample Usage

### Create event

```bash
curl -X POST http://localhost:8080/events \
  -H 'Content-Type: application/json' \
  -d '{
    "eventId": "evt-001",
    "accountId": "acct-100",
    "type": "DEPOSIT",
    "amount": 100.00,
    "currency": "USD",
    "eventTimestamp": "2026-06-16T12:00:00Z"
  }'
```

### Query event

```bash
curl http://localhost:8080/events/evt-001
```

### Query account balance

```bash
curl http://localhost:8081/accounts/acct-100/balance
```

## Resiliency

The gateway uses Resilience4j with:

- Circuit Breaker
- Retry with exponential backoff
- Timeouts

If the account service is unavailable, event POST requests return `503 Service Unavailable` and read-only GET endpoints continue to work.

## Trace Propagation

- Gateway generates `X-Trace-Id` when missing.
- The header is propagated to Account Service.
- Both services log the trace identifier in JSON logs using MDC.

## Tests

- Unit tests for service behavior, validation, idempotency.
- Integration tests for gateway-account flow, duplicate events, out-of-order ordering, and resiliency.

```bash
mvn test
```
