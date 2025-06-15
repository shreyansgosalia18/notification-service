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

- **Language/Platform:** Java 21, Spring Boot 3.4.6
- **Core Frameworks:** Spring Web, Spring Data JPA, Spring Data Redis, Spring Kafka, Spring Boot Actuator, Spring Boot Validation
- **Resiliency:** Resilience4j for retries, circuit breakers, and robust error handling
- **Database:** PostgreSQL (production), Testcontainers (integration tests)
- **Queue:** Kafka for asynchronous processing and decoupling between API and workers
- **Caching/Rate Limiting:** Redis for distributed caching and rate limiting
- **API Documentation:** OpenAPI/Swagger via Springdoc
- **Metrics/Monitoring:** Micrometer with Prometheus; Spring Boot Actuator for health and metrics endpoints
- **Migrations:** Flyway for database version control
- **Containerization:** Docker images built using Jib plugin for seamless CI/CD
- **Testing:** JUnit 5, Spring Boot test starter, Testcontainers (Kafka, PostgreSQL)
- **Utilities:** Lombok for boilerplate reduction, Hibernate Types for advanced DB mapping

All dependencies and plugins are managed via Gradle for consistent builds and reproducibility.

---

## Project Structure

```
src/main/java/com/karboncard/assignment/notificationservice/
├── NotificationServiceApplication.java            # Main Spring Boot application
├── config/                                       # Application and framework-level configs
│   ├── KafkaConfig.java                          # Message queue configuration
│   ├── AsyncConfig.java                          # Thread pool/executor configuration
│   ├── RetryConfig.java                          # Exponential backoff and retry settings
│   ├── RateLimitConfig.java                      # Distributed rate limiting setup
│   └── SwaggerConfig.java                        # API documentation config (OpenAPI)
├── controller/
│   └── NotificationController.java               # REST API endpoints for notifications
├── model/
│   ├── enums/                                    # Enum types for notification
│   │   ├── NotificationType.java                 # SMS, EMAIL, PUSH
│   │   ├── NotificationPriority.java             # HIGH, MEDIUM, LOW
│   │   └── NotificationStatus.java               # State: PENDING, SENT, FAILED, etc.
│   ├── dto/
│   │   ├── request/
│   │   │   └── NotificationRequestDTO.java       # Incoming request DTO (Builder pattern)
│   │   └── response/
│   │       └── NotificationResponseDTO.java      # API response DTO (Builder pattern)
│   └── entity/
│       └── Notification.java                     # JPA entity for database persistence
├── repository/
│   └── NotificationRepository.java               # Data access repository
├── service/
│   ├── NotificationService.java                  # Service interface
│   ├── impl/
│   │   └── NotificationServiceImpl.java          # Service implementation
│   ├── RateLimitingService.java                  # Rate limiting interface
│   ├── impl/
│   │   └── RedisRateLimitingService.java         # Redis-backed rate limiting
│   ├── KafkaProducerService.java                 # Message producer interface
│   ├── impl/
│   │   └── KafkaProducerServiceImpl.java         # Kafka producer implementation
│   ├── provider/                                # Strategy Pattern for notification methods
│   │   ├── NotificationProvider.java             # Provider interface
│   │   ├── impl/
│   │   │   ├── EmailNotificationProvider.java    # Email notification logic
│   │   │   ├── SmsNotificationProvider.java      # SMS notification logic
│   │   │   └── PushNotificationProvider.java     # Push notification logic
│   └── factory/
│       └── NotificationProviderFactory.java      # Factory Pattern for provider selection
├── worker/
│   ├── NotificationConsumer.java                 # Queue consumer interface
│   ├── impl/
│   │   ├── EmailNotificationConsumer.java        # Email worker
│   │   ├── SmsNotificationConsumer.java          # SMS worker
│   │   └── PushNotificationConsumer.java         # Push worker
│   └── adapter/                                 # Adapter Pattern for 3rd-party integrations
│       ├── ExternalProviderAdapter.java
│       └── impl/
│           ├── TwilioSmsAdapter.java
│           ├── SendGridEmailAdapter.java
│           └── FirebasePushAdapter.java
├── exception/
│   ├── RateLimitExceededException.java           # Exception for rate limit violation
│   └── GlobalExceptionHandler.java               # Centralized error handler
└── util/
    ├── IdempotencyUtil.java                      # Exactly-once delivery helper
    └── MetricsUtil.java                          # Metrics and observability helpers
```
---

## Setup Instructions

### Prerequisites

- **Java 21** (ensure `JAVA_HOME` points to JDK 21)
- **Docker & Docker Compose** (for running dependencies)
- **Gradle** (or use the provided Gradle wrapper `./gradlew`)
- **Git**

---

### 1. Clone the Repository

```bash
git clone https://github.com/shreyansgosalia18/notification-service.git
cd notification-service
```

---

### 2. Start Dependencies (PostgreSQL, Redis, Kafka, Zookeeper)

You can start all dependencies with Docker Compose:

```bash
docker-compose up -d
```

This will spin up:
- PostgreSQL (database)
- Redis (for rate limiting/idempotency)
- Kafka & Zookeeper (for asynchronous messaging)

> Default ports exposed: PostgreSQL (5432), Redis (6379), Kafka (9092)

---

### 3. Configure Application Properties

Edit `src/main/resources/application.yml` as needed to match your environment (DB/Kafka/Redis connection details).  
Defaults match the `docker-compose.yml` configuration.

---

### 4. Run Database Migrations

Flyway migrations will run automatically on application startup.

---

### 5. Build and Run the Application

**Using Gradle Wrapper:**

```bash
./gradlew bootRun
```

Or, to build a JAR:

```bash
./gradlew clean build
java -jar build/libs/notification-service.jar
```

---

### 6. Access the API Documentation

Visit [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) or [http://localhost:8080/swagger-ui/](http://localhost:8080/swagger-ui/) for interactive API documentation.

---

### 7. Running Tests

```bash
./gradlew test
```

Integration tests use Testcontainers to spin up ephemeral PostgreSQL and Kafka instances.

---

### 8. Build a Docker Image (Optional)

To build a Docker image using Jib:

```bash
./gradlew jibDockerBuild
```

This will produce a local Docker image named `notification-service:0.0.1-SNAPSHOT`.

---

### 9. Stopping Services

To stop and remove all Docker containers:

```bash
docker-compose down
```

---

**You're ready to develop and test the Notification Service!**
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
