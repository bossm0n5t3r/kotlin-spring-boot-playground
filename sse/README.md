# SSE module

This module demonstrates Server-Sent Events (SSE) in Kotlin with Spring Boot WebFlux, backed by Redis Streams (Valkey)
and Apache Kafka. It exposes three streaming endpoints and uses scheduled publishers to emit demo messages every 5
seconds.

## Overview

- Language/Build: Kotlin, Gradle (Kotlin DSL)
- Frameworks/Libraries: Spring Boot 4 (WebFlux, Data Redis), Spring for Apache Kafka, Reactor Kafka,
  kotlinx.serialization
- Backends:
  - Redis Streams via Valkey container
  - Kafka via Spring Kafka and Reactor Kafka
- Entry point: src/main/kotlin/me/bossm0n5t3r/sse/SseApplication.kt
- Endpoints (produces text/event-stream):
  - GET `/sse/redis` — stream from Redis Streams
  - GET `/sse/kafka` — stream from Kafka (Spring Kafka based)
  - GET `/sse/kafka/reactor` — stream from Kafka (Reactor Kafka based)
- Demo client: src/main/resources/client.html (see Run section)

## Event payload (JSON data in SSE)

```json
{
  "streamId": "<backend-specific stream id>",
  "ulid": "<ULID>",
  "message": "<string>",
  "createdAt": "<ISO-8601 instant>"
}
```

SSE id header is set to streamId so clients can resume with Last-Event-ID.

## Requirements

- JDK 24 (managed by Gradle toolchain)
- Docker and Docker Compose (for Valkey/Redis, Kafka, Kafka UI)
- Gradle wrapper

## Setup and Run

### Option A — Start services manually, then run app

1) Start Compose services
   ```bash
   docker compose -f sse/compose.yaml up -d
   ```
   Starts:

- Valkey on localhost:6379
- Kafka on localhost:9092 (Zookeeper on 2181)
- Kafka UI on http://localhost:8085

2) Run the application
   ```bash
   ./gradlew :sse:bootRun
   ```

### Option B — Let Spring Boot start services

Spring Boot Docker Compose integration is enabled in src/main/resources/application.yaml:

```yaml
spring:
  docker:
    compose:
      enabled: true
      file: sse/compose.yaml
```

Run the app and Spring Boot will start sse/compose.yaml:

```bash
./gradlew :sse:bootRun
```

To disable, set SPRING_DOCKER_COMPOSE_ENABLED=false or adjust config.

## Verify streaming

- Redis backend:
  ```bash
  curl -N http://localhost:8080/sse/redis
  ```
- Kafka backend (Spring Kafka):
  ```bash
  curl -N http://localhost:8080/sse/kafka
  ```
- Kafka backend (Reactor Kafka):
  ```bash
  curl -N http://localhost:8080/sse/kafka/reactor
  ```
- Resume from a specific SSE id using Last-Event-ID header:
  ```bash
  curl -N -H 'Last-Event-ID: 42' http://localhost:8080/sse/kafka
  ```

## Demo client

The file src/main/resources/client.html can be opened directly in a browser. It is not auto-served because Spring Boot
serves static content from classpath:/static or classpath:/public by default.

- Optionally, move it to src/main/resources/static/client.html to be served at http://localhost:8080/client.html. (TODO)

## Scripts / Gradle tasks

- Run app:
  ```bash
  ./gradlew :sse:bootRun
  ```
- Run tests:
  ```bash
  ./gradlew :sse:test
  ```
- Lint (ktlint):
  ```bash
  ./gradlew :sse:ktlintCheck
  ```
- Kafka UI: http://localhost:8085 when Compose is up

## Environment variables and configuration

### Defaults (src/main/resources/application.yaml)

```yaml
spring:
  docker:
    compose:
      enabled: true
      file: sse/compose.yaml
  data:
    redis:
      host: localhost
      port: 6379
  kafka:
    bootstrap-servers: localhost:9092
    consumer:
      auto-offset-reset: earliest
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.apache.kafka.common.serialization.StringDeserializer
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.apache.kafka.common.serialization.StringSerializer
```

### Overridable via env vars, for example

- SPRING_DATA_REDIS_HOST, SPRING_DATA_REDIS_PORT
- SPRING_KAFKA_BOOTSTRAP_SERVERS
- SPRING_DOCKER_COMPOSE_ENABLED (true/false)
- SERVER_PORT (defaults to 8080)

## Tests

- Location: src/test/kotlin
- Run: ./gradlew :sse:test

## Project structure (module)

- build.gradle.kts — module build (WebFlux, Redis, Kafka, Reactor Kafka, ktlint)
- compose.yaml — Docker Compose for Valkey, Kafka, Kafka UI
- src/main/kotlin/me/bossm0n5t3r/sse/SseApplication.kt — Spring Boot app entry point
- src/main/kotlin/me/bossm0n5t3r/sse/controller/* — SSE controllers
  - RedisSseController -> /sse/redis
  - KafkaSseController -> /sse/kafka
  - ReactorKafkaSseController -> /sse/kafka/reactor
- src/main/kotlin/me/bossm0n5t3r/sse/service/* — Event stores
  - RedisStreamEventStore
  - KafkaEventStore
  - ReactorKafkaEventStore
  - EventStore interface
- src/main/kotlin/me/bossm0n5t3r/sse/publisher/* — Scheduled publishers (tick every 5s)
  - RedisPublisher, KafkaPublisher
- src/main/kotlin/me/bossm0n5t3r/sse/configuration/* — Kafka/Redis/Reactor Kafka configuration, logger helper
- src/main/kotlin/me/bossm0n5t3r/sse/dto/SseEvent.kt — Event DTO and JSON helper
- src/main/resources/application.yaml — Spring Boot config
- src/main/resources/client.html — Demo client (see note above)
- src/test/kotlin/... — Tests

## License

- TODO: Add license details (no LICENSE file detected in repo at the time of writing).

## Notes

- Kafka topic used: sse-events (auto-created in Compose with KAFKA_AUTO_CREATE_TOPICS_ENABLE=true)
- Redis Stream key used: sse:events
- SSE reconnect: endpoints set retry: 3000 so clients reconnect ~3s after disconnect.

## Last updated

- 2025-11-30
