# kotlin-spring-boot-playground

A playground project for exploring various Spring Boot features with Kotlin, including database integrations (JPA,
Exposed, R2DBC), Server-Sent Events (SSE), Content Negotiation, and Serialization.

## 🛠 Tech Stack

- **Language:** [Kotlin](https://kotlinlang.org/) (2.3.0)
- **Framework:** [Spring Boot](https://spring.io/projects/spring-boot) (4.0.1)
- **JDK:** 25
- **Build Tool:** Gradle (Kotlin DSL)
- **Database Integrations:** Exposed (JDBC/R2DBC), Spring Data JPA
- **Messaging:** Redis Streams, Apache Kafka (used in SSE module)
- **Serialization:** Jackson, kotlinx.serialization, Gson, Protobuf, MessagePack

## 📂 Project Structure

This is a multi-module Gradle project:

- **[`content-negotiation`](./content-negotiation)**: Experimenting with different content types (JSON, MessagePack,
  Protobuf) and HTTP compression (Gzip). Includes automated compression ratio reports.
- **[`http-exchange`](./http-exchange)**: Demonstration of Spring's HTTP Interface (introduced in Spring 6).
- **[`jdbc-exposed`](./jdbc-exposed)**: Integration of [JetBrains Exposed](https://github.com/JetBrains/Exposed) with
  JDBC in a Spring Boot environment.
- **[`jdbc-jpa`](./jdbc-jpa)**: Standard Spring Data JPA integration.
- **[`r2dbc-exposed`](./r2dbc-exposed)**: Reactive database access using Exposed with R2DBC.
- **[`serialization`](./serialization)**: Comparison and tests for various serialization libraries (Jackson,
  kotlinx.serialization, Gson).
- **[`sse`](./sse)**: Real-time event streaming using Server-Sent Events (SSE), backed by Redis Streams and Kafka.
  Includes a Docker Compose setup for infrastructure.

## 🚀 Getting Started

### Requirements

- **JDK 25** or higher
- **Docker** (required for `sse` and potentially other modules using `spring-boot-docker-compose`)

### Setup

Clone the repository:

```bash
git clone https://github.com/bossm0n5t3r/kotlin-spring-boot-playground.git
cd kotlin-spring-boot-playground
```

### Running Modules

You can run individual modules using the Gradle wrapper. For example, to run the `sse` module:

```bash
./gradlew :sse:bootRun
```

Or the `content-negotiation` module:

```bash
./gradlew :content-negotiation:bootRun
```

Refer to individual module READMEs (where available) for specific details.

## 📜 Common Scripts

- **Build all modules:**
  ```bash
  ./gradlew build
  ```
- **Run all tests:**
  ```bash
  ./gradlew test
  ```
- **Check code style (ktlint):**
  ```bash
  ./gradlew ktlintCheck
  ```
- **Format code (ktlint):**
  ```bash
  ./gradlew ktlintFormat
  ```

## 🧪 Testing

Each module contains its own set of tests.

- **Unit & Integration tests:** Run with `./gradlew test`.
- **Module-specific tests:** Run with `./gradlew :<module-name>:test`.

## ⚙️ Environment Variables

Most configurations are handled via `application.yaml` in each module. Some common overrides include:

- `SERVER_PORT`: Set the port for the web server (defaults to 8080).
- `SPRING_DOCKER_COMPOSE_ENABLED`: Enable/disable Docker Compose integration (true/false).

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🔗 References

- [Exposed Spring Sample](https://github.com/JetBrains/Exposed/tree/main/samples/exposed-spring)
