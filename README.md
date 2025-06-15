# Notification Service

A scalable, resilient backend service for sending SMS, Email, and Push notifications. Designed for high availability, asynchronous processing, robust error handling, and extensibility using proven design patterns.

---

## Table of Contents

- [Objective](#objective)
- [Features](#features)
- [Tech Stack & Design Choices](#tech-stack--design-choices)
- [Project Structure](#project-structure)
- [Setup Instructions](#setup-instructions)
- [API Documentation](#api-documentation)
- [Design Decisions & Trade-offs](#design-decisions--trade-offs)
- [Testing](#testing)
- [Future Improvements](#future-improvements)

---

## Objective

Build a core service responsible for delivering various notification types (SMS, Email, Push) asynchronously, ensuring reliability, observability, and extensibility for large-scale applications.

---

## Features

- **RESTful API**: Expose POST `/notifications` endpoint for notification requests.
- **Asynchronous Processing**: Uses Kafka (or mock) to enqueue requests and worker consumers for each notification type.
- **Rate Limiting**: Distributed (Redis-backed) per-user and per-template rate limiting.
- **Retry & Dead Letter Queue**: Exponential backoff strategy for transient errors, with failed requests sent to a DLQ after retries.
- **Idempotency**: Guarantees exactly-once delivery semantics via idempotency checks.
- **Observability**: Basic logging and a utility for metrics collection.
- **OpenAPI/Swagger Documentation**: API docs auto-generated.
- **Design Patterns**: Extensible via Strategy, Factory, and Adapter patterns.
- **Extensible Provider Integration**: Easily add new external notification providers.
- **Global Exception Handling**: Consistent API error responses.

---

## Tech Stack & Design Choices

- **Language/Framework**: Java 17, Spring Boot
- **Database**: Chosen as per deployment (e.g., PostgreSQL/MySQL/NoSQL); entity-centric design allows easy switching.
- **Queue**: Kafka for message brokering, can be mocked for demo/dev.
- **Distributed Cache**: Redis for rate limiting and idempotency.
- **Design Patterns**: Strategy (notification provider), Factory (provider selection), Adapter (external provider integration).
- **Documentation**: Swagger/OpenAPI via SpringFox.

---

## Project Structure

```
src/main/java/com/karboncard/assignment/notificationservice/
├── NotificationServiceApplication.java
├── config/
│   ├── KafkaConfig.java
│   ├── AsyncConfig.java
│   ├── RetryConfig.java
│   ├── RateLimitConfig.java
│   └── SwaggerConfig.java
├── controller/
│   └── NotificationController.java
├── model/
│   ├── enums/
│   ├── dto/
│   └── entity/
├── repository/
├── service/
│   ├── provider/
│   ├── factory/
│   ├── impl/
├── worker/
│   ├── impl/
│   └── adapter/
├── exception/
├── util/
```

---

## Setup Instructions

### Prerequisites

- Java 17+
- Maven/Gradle
- Kafka broker (can be run via Docker)
- Redis server (can be run via Docker)
- Database (e.g., PostgreSQL, MySQL)
- (Optional for dev/demo) Docker Compose for easy orchestration

### Steps

1. **Clone the repository**  
   `git clone https://github.com/shreyansgosalia18/notification-service.git`

2. **Configure environment variables**  
   Update `src/main/resources/application.yml` for DB, Kafka, and Redis connection details.

3. **Run dependencies (if using Docker Compose)**  
   ```
   docker-compose up -d
   ```

4. **Build and run the application**
   ```
   ./mvnw spring-boot:run
   ```

5. **Access Swagger UI**  
   Visit [http://localhost:8080/swagger-ui/](http://localhost:8080/swagger-ui/) for API docs.

---

## API Documentation

### POST /notifications

**Request:**
```json
{
  "userId": "string",
  "type": "SMS|EMAIL|PUSH",
  "templateId": "ORDER_SHIPPED_CONFIRMATION",
  "templateParams": {
    "orderId": "ABC123",
    "itemName": "Laptop"
  },
  "priority": "HIGH|MEDIUM|LOW",
  "correlationId": "optional-correlation-id"
}
```

**Response:**
- `202 Accepted` on successful enqueue.
- `429 Too Many Requests` if rate limit exceeded.
- `400 Bad Request` on validation errors.

See Swagger for detailed schemas, error responses, and examples.

---

## Design Decisions & Trade-offs

- **Monolithic service** for simplicity, but modules are decoupled for future microservices migration.
- **Kafka** chosen for message queue due to reliability, but abstracted for easy replacement.
- **Redis** for distributed rate limiting and idempotency; in-memory fallback for local/dev.
- **Strategy/Factory/Adapter Patterns** allow easy integration of new providers or notification types.
- **At-least-once delivery** via retries; **exactly-once** via idempotency checks.
- **Metrics** utility present; recommend Prometheus integration in production.
- **Dead Letter Queue**: Failed notifications after retries are captured for further analysis.

---

## Testing

- **Unit Tests**: Present for core service logic and components (see `src/test/`).
- **Manual Test Cases**:
  - Submit valid notification requests for each type.
  - Submit requests exceeding rate limits.
  - Simulate transient provider failure and observe retries.
  - Simulate permanent failure and verify DLQ handling.
- **Integration**: End-to-end flow from API to worker to provider is covered in integration tests.

---

## Future Improvements

- Add support for WhatsApp, Voice, or in-app notifications.
- Admin panel for monitoring and managing notifications.
- Enhanced analytics and reporting (notification open/click rates).
- CI/CD pipeline and Dockerization for production readiness.
- Full observability stack (Prometheus, Grafana, ELK).
- Security enhancements (auth, input sanitization, audit logs).
- Multi-tenancy support.

---

## Contact

For questions or contributions, please create an issue or contact the maintainer.
