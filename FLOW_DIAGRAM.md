```mermaid
flowchart TD
    A[Client / API Consumer] -->|POST /notifications| B(NotificationController)
    B -->|Validate & Build Request| C[KafkaProducerService]
    C -->|Enqueue Notification| D>Kafka Topic / Queue]
    D -->|Consume Message| E[NotificationConsumer(s)]
    E -->|Route by Type| F[NotificationProviderFactory]
    F -->|Select Provider| G[NotificationProvider (Strategy)]
    G -->|Use Adapter| H[ExternalProviderAdapter]
    H -->|Call External Service| I[SMS/Email/Push Service<br/>(e.g. Twilio, SendGrid, Firebase)]
    E -->|Update Status| J[NotificationRepository (DB)]
    E -->|Metrics & Logging| K[MetricsUtil / Observability]
    B -->|Rate Limit Check| L[RateLimitingService (Redis)]
    E -->|Idempotency Check| M[IdempotencyUtil (Redis/DB)]
    E -->|Retry Logic| N[RetryConfig/Resilience4j]
    N -->|DLQ on Failure| O[Dead Letter Queue/DB]

    style D fill:#fff,stroke:#333,stroke-width:2px
    style I fill:#fff,stroke:#333,stroke-width:2px
    style O fill:#fff,stroke:#f66,stroke-width:2px
```
