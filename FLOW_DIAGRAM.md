```mermaid
flowchart TD
    A[Client/API] -->|POST /api/v1/notifications| B[NotificationController]
    B -->|Validate + Idempotency + Rate Limit| C[NotificationServiceImpl]
    C -->|Convert DTO to Entity & Save| D[NotificationRepository (DB)]
    C -->|Send to Kafka| E[KafkaProducerService]
    E -->|Enqueue| F[Kafka Topic]
    F -->|Consume| G[NotificationConsumer]
    G -->|Route by Type| H[NotificationProviderFactory]
    H -->|Select Provider| I[NotificationProvider]
    I -->|Use Adapter| J[ExternalProviderAdapter]
    J -->|Call Service| K[External Service]
    G -->|Update Status| D
    G -->|Metrics/Logging| L[MetricsUtil]
    B -->|Check Rate Limit| M[RateLimitingService]
    G -->|Idempotency| N[IdempotencyUtil]
    G -->|Retry Logic| O[RetryConfig]
    O -->|On Failure| P[DeadLetterQueue]
```
