```mermaid
flowchart TD
    A[Client/API] -->|POST /api/v1/notifications| B[NotificationController]
    B -->|Validate + Idempotency + Rate Limit| C[NotificationServiceImpl]
    C -->|Convert DTO to Entity| D[Notification]
    D -->|Save| E[NotificationRepository (DB)]
    E -->|Persisted| F[Notification Entity]
    C -->|Send to Kafka| G[KafkaProducerService]
    G -->|Enqueue| H[Kafka Topic]
    H -->|Consume| I[NotificationConsumer]
    I -->|Route by Type| J[NotificationProviderFactory]
    J -->|Select Provider| K[NotificationProvider]
    K -->|Use Adapter| L[ExternalProviderAdapter]
    L -->|Call Service| M[External Service]
    I -->|Update Status| E
    I -->|Metrics/Logging| N[MetricsUtil]
    B -->|Check Rate Limit| O[RateLimitingService]
    I -->|Idempotency| P[IdempotencyUtil]
    I -->|Retry Logic| Q[RetryConfig]
    Q -->|On Failure| R[DeadLetterQueue]
```
