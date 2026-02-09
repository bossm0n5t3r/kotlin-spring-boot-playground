# kotlin-spring-boot-playground

A playground project for exploring various Spring Boot features with Kotlin, including database integrations (JPA,
Exposed, R2DBC), Server-Sent Events (SSE), Content Negotiation, and Serialization.

## 🛠 Tech Stack

- **Language:** [Kotlin](https://kotlinlang.org/) (2.3.10)
- **Framework:** [Spring Boot](https://spring.io/projects/spring-boot) (4.0.2)
- **JDK:** 25
- **Build Tool:** Gradle (Kotlin DSL)
- **Database Integrations:** Exposed (JDBC/R2DBC), Spring Data JPA, R2DBC, H2, PostgreSQL
- **Messaging:** Redis Streams, Apache Kafka
- **Serialization:** Jackson, kotlinx.serialization, Gson, Protobuf, MessagePack
- **Security:** Spring Security (MVC & WebFlux), JWT

## 📂 Project Structure

This is a multi-module Gradle project:

- **[`account`](./account)**: Implementation of an account management system using R2DBC and Spring Boot.
- **[`content-negotiation`](./content-negotiation)**: Experimenting with different content types (JSON, MessagePack,
  Protobuf) and HTTP compression (Gzip). Includes automated compression ratio reports.
- **[`http-exchange`](./http-exchange)**: Demonstration of Spring's HTTP Interface (introduced in Spring 6).
- **[`jdbc-exposed`](./jdbc-exposed)**: Integration of [JetBrains Exposed](https://github.com/JetBrains/Exposed) with
  JDBC in a Spring Boot environment.
- **[`jdbc-jpa`](./jdbc-jpa)**: Standard Spring Data JPA integration.
- **[`r2dbc-exposed`](./r2dbc-exposed)**: Reactive database access using Exposed with R2DBC.
- **[`security-mvc`](./security-mvc)**: Spring Security implementation for Spring MVC applications, including JWT
  authentication.
- **[`security-webflux`](./security-webflux)**: Spring Security implementation for Spring WebFlux (Reactive)
  applications, including JWT authentication.
- **[`serialization`](./serialization)**: Comparison and tests for various serialization libraries (Jackson,
  kotlinx.serialization, Gson).
- **[`sse`](./sse)**: Real-time event streaming using Server-Sent Events (SSE), backed by Redis Streams and Kafka.
  Includes a Docker Compose setup for infrastructure.
- **[`tx-routing-datasource`](./tx-routing-datasource)**: Dynamic data source routing based on transaction read-only
  status (Master/Slave routing).

## 🛠 Project Entry Points

Each module has its own `main` function:

- `account`: `me.bossm0n5t3r.account.AccountApplicationKt`
- `content-negotiation`: `me.bossm0n5t3r.contentnegotiation.ContentNegotiationApplicationKt`
- `http-exchange`: `me.bossm0n5t3r.HttpExchangeApplicationKt`
- `jdbc-exposed`: `me.bossm0n5t3r.JdbcExposedApplicationKt`
- `jdbc-jpa`: `me.bossm0n5t3r.JdbcJpaApplicationKt`
- `r2dbc-exposed`: `me.bossm0n5t3r.R2dbcExposedApplicationKt`
- `security-mvc`: `me.bossm0n5t3r.security.mvc.SecurityMvcApplicationKt`
- `security-webflux`: `me.bossm0n5t3r.security.webflux.SecurityWebfluxApplicationKt`
- `sse`: `me.bossm0n5t3r.sse.SseApplicationKt`
- `tx-routing-datasource`: `me.bossm0n5t3r.txroutingdatasource.TxRoutingDatasourceApplicationKt`

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
- **Test reports:** After running tests, reports can be found in `build/reports/tests/test/index.html` within each
  module.

## ⚙️ Environment Variables

Most configurations are handled via `application.yaml` in each module. Common overrides include:

- `SERVER_PORT`: Set the port for the web server (e.g., `8080`, `1019`).
- `SPRING_DOCKER_COMPOSE_ENABLED`: Enable/disable Docker Compose integration (`true`/`false`).
- `SPRING_PROFILES_ACTIVE`: Set the active Spring profile (e.g., `local`, `test`).
- `JWT_SECRET`: Secret key for JWT signing (used in security modules).
- `TODO`: Add module-specific environment variables as they are identified.

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🔗 References

- [Exposed Spring Sample](https://github.com/JetBrains/Exposed/tree/main/samples/exposed-spring)
