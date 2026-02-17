# Spring Boot Boilerplate (Kotlin)

A production-ready Spring Boot 3.5.5 + Kotlin 2.0 starter project, offering a unified package with essential backend
features pre-integrated and real-world examples. Includes a complete **OpenTelemetry-based observability stack** for
unified monitoring, distributed tracing, and log aggregation.

> **Looking for a multi-module version?** See [kotlin-clean-architecture-multimodule](https://github.com/awakelife93/kotlin-clean-architecture-multimodule) for the Hexagonal Architecture + Multi-Module version of this project.

## Table of Contents

- [Quick Start](#quick-start)
- [Environment & Skills](#environment--skills)
- [Project Guide](#project-guide)
- [Features](#features)
  - [Security & Quality](#security--quality)
  - [Data Management](#data-management)
  - [Integration & Messaging](#integration--messaging)
  - [Testing](#testing)
  - [Monitoring & Observability](#monitoring--observability)
- [Service Access URLs](#service-access-urls)

## Quick Start

### Prerequisites

- Java 21+
- Docker & Docker Compose

### Run

```bash
# 1. Start infrastructure services
cd docker && ./setup.sh

# 2. Run application
./gradlew bootRun
```

Application is running at http://localhost:8085

See [Service Access URLs](#service-access-urls) for all available services.

**Infrastructure services started by setup.sh:**
- Kafka & Zookeeper
- Redis
- PostgreSQL & PgAdmin
- MailHog
- Prometheus
- Grafana
- Tempo
- Loki
- OpenTelemetry Collector

## Environment & Skills

### Application

| Category | Technologies |
|----------|-------------|
| Language & Runtime | Kotlin 2.0, JDK 21 |
| Framework | Spring Boot 3.5.5 (MVC, WebFlux) |
| Build | Gradle 8.10 |
| Security | Spring Security, JWT |
| Database | PostgreSQL, H2 (PostgreSQL mode, local), JPA, QueryDSL, Flyway |
| Cache | Redis |
| Messaging | Kafka |
| Batch | Spring Batch |
| API Docs | Springdoc OpenAPI |
| HTTP Client | WebClient |
| Webhook | Slack, Discord |
| Logging | Kotlin Logging, Logback |
| Validation | Jakarta Validation |
| Monitoring | Spring Actuator |

### Test

| Category | Technologies |
|----------|-------------|
| Framework | Spring Boot Starter Test, JUnit 5, Kotest |
| Mocking | Mockito Kotlin, Mockito Inline, MockK |
| Data | Instancio |
| Database | H2 (PostgreSQL mode), Flyway |
| Security | Spring Security Test |
| Batch | Spring Batch Test |

### Infrastructure & Tooling

| Category | Technologies |
|----------|-------------|
| Container | Docker, Docker Compose |
| DB Management | PgAdmin |
| Code Quality | Ktlint, Detekt |
| Email Testing | MailHog |
| Messaging UI | Kafka UI |
| DNS | Netty Resolver DNS Native macOS |

### Monitoring & Observability

| Category | Technologies |
|----------|-------------|
| Metrics | Prometheus |
| Dashboard | Grafana |
| Tracing | Tempo |
| Log Aggregation | Loki |
| Collector | OpenTelemetry Collector |
| Error Tracking | Sentry |

## Project Guide

```
├── monitoring/                          # Observability configurations
├── docker/                              # Docker Compose files & setup script
└── src/
    ├── main/
    │   ├── kotlin/.../demo/
    │   │   ├── DemoApplication.kt       # Application entry point
    │   │   ├── common/                  # Shared utilities & base classes
    │   │   ├── post/                    # Post domain
    │   │   ├── user/                    # User domain
    │   │   ├── auth/                    # Auth domain
    │   │   ├── example/                 # Usage examples
    │   │   │   ├── UserDeleteConsumer   # Kafka Consumer (User Delete Event)
    │   │   │   └── WelcomeSignUpConsumer # Kafka Consumer (SignUp Event)
    │   │   ├── infrastructure/          # External integrations
    │   │   │   ├── kafka/
    │   │   │   ├── redis/
    │   │   │   ├── webhook/
    │   │   │   └── mail/
    │   │   ├── security/                # Spring Security + JWT
    │   │   └── utils/
    │   └── resources/
    │       ├── db/
    │       │   ├── migration/           # Flyway SQL scripts
    │       │   └── sql/                 # Spring Batch metadata SQL
    │       ├── logback-*.xml            # Logback configs (spring, prod, dev, local)
    │       ├── kotest.properties        # Kotest configuration
    │       └── application-*.yml        # Profile configs
    │           ├── common               # Shared variables
    │           ├── prod / dev / local   # Environment-specific
    │           ├── test                 # Test environment
    │           └── secret-{env}         # Secret variables per environment
    └── test/
        └── kotlin/.../demo/
            ├── common/                  # Shared test configuration
            ├── mockito/                 # Mockito-based tests
            └── kotest/                  # Kotest + MockK tests
```

## Features

### Security & Quality

**CVE Management**
- All dependency vulnerabilities centrally managed via `applyCveFixes()` in [build.gradle.kts](build.gradle.kts)
- CVE fixes automatically applied during dependency resolution

```kotlin
// Example: CVE fix implementation
when {
    requested.group == "org.apache.commons" && requested.name == "commons-lang3" -> {
        useVersion("3.18.0")
        because("CVE-2025-48924")
    }
}
```

**Code Quality**
- Ktlint: [Official lint rules](.editorconfig)
  - Report: `build/reports/ktlint`
- Detekt: [Custom rules](detekt.yml)
  - Report: `build/reports/detekt`

### Data Management

**Database DDL**
- Uses Flyway for DDL management instead of JPA auto-generation
- Migration scripts: [src/main/resources/db/migration](src/main/resources/db/migration)
- Alternative: JPA DDL auto-generation via [application-common.yml](src/main/resources/application-common.yml)

**Spring Batch**
- Metadata table required for all environments
- PostgreSQL schema: [batch-postgresql-metadata-schema.sql](src/main/resources/db/sql/batch-postgresql-metadata-schema.sql)
- Reference: [Spring Batch Schema](https://github.com/spring-projects/spring-batch/blob/5.0.x/spring-batch-core/src/main/resources/org/springframework/batch/core/schema-postgresql.sql)

### Integration & Messaging

**Webhook**
- Configuration: [application-common.yml](src/main/resources/application-common.yml) (default enabled)
- Types: Slack, Discord

```kotlin
// 1. all
webHookProvider.sendAll(
	"Subscription request received from method ${parameter.method?.name}.",
	mutableListOf("Request Body: $body")
)

// 2. target slack
webHookProvider.sendSlack(
	"Failed to send message to Kafka (foo)",
	mutableListOf("Failed to send message to Kafka: ${exception.message} / $foo")
)

// 3. target discord
webHookProvider.sendDiscord(
	"Failed to send message to Kafka (bar)",
	mutableListOf("Failed to send message to Kafka: ${exception.message} / $bar")
)
```

**Kafka**
- Topic metadata: [KafkaTopicMetaProvider](src/main/kotlin/com/example/demo/infrastructure/kafka/provider/KafkaTopicMetaProvider.kt)
- DLQ: Dynamically created, [default partition: 1](src/main/kotlin/com/example/demo/infrastructure/kafka/DlqHelper.kt)

**Email Testing**
- MailHog integration (SMTP port 1025)
- Configuration: [docker-compose.mailhog.yml](docker/base/docker-compose.mailhog.yml), [application-local.yml](src/main/resources/application-local.yml)

**Examples**
- [User signup → Email event via Kafka](src/main/kotlin/com/example/demo/user/event/UserEventHandler.kt)
  - Consumer: [WelcomeSignUpConsumer](src/main/kotlin/com/example/demo/example/WelcomeSignUpConsumer.kt)
- [User deletion after 1 year (Batch)](src/main/kotlin/com/example/demo/user/batch/writer/UserDeleteItemWriter.kt)
  - Consumer: [UserDeleteConsumer](src/main/kotlin/com/example/demo/example/UserDeleteConsumer.kt)

### Testing

**Mockito**
- Base: [BaseIntegrationController](src/test/kotlin/com/example/demo/mockito/common/BaseIntegrationController.kt)

**Kotest & MockK**
- Base: [BaseIntegrationController](src/test/kotlin/com/example/demo/kotest/common/BaseIntegrationController.kt)
- Security bypass: [SecurityListenerFactory](src/test/kotlin/com/example/demo/kotest/common/security/SecurityListenerFactory.kt)

```kotlin
// Bypass Spring Security in tests
listeners(SecurityListenerFactory())

Then("Call DELETE /api/v1/users/{userId}").config(tags = setOf(SecurityListenerFactory.NonSecurityOption)) {
	// ...
}
```

### Monitoring & Observability

**OpenTelemetry Stack**
- All metrics, traces, logs collected via OpenTelemetry Collector
- Collector endpoints:
  - OTLP gRPC: localhost:4317
  - OTLP HTTP: localhost:4318

**Configuration**
- [OpenTelemetry Collector](monitoring/otel-collector-config.yml)
- [Prometheus](monitoring/prometheus.yml)
- [Tempo](monitoring/tempo.yml)
- [Loki](monitoring/loki.yml)

## Service Access URLs

### Application

| Service | URL | Description |
|---------|-----|-------------|
| API Documentation | http://localhost:8085/swagger-ui/index.html | Swagger UI |
| H2 Console | http://localhost:8085/h2-console | Database console (local) |
| Application Server | http://localhost:8085 | Main endpoint |

### Infrastructure

| Service | URL | Description |
|---------|-----|-------------|
| MailHog | http://localhost:8025 | Email testing |
| PgAdmin | http://localhost:8088 | PostgreSQL management |
| Kafka UI | http://localhost:9000 | Kafka management |
| Redis | localhost:6379 | Redis connection |
| PostgreSQL | localhost:5432 | Database connection |
| Kafka Broker | localhost:9092 | Kafka connection |
| Zookeeper | localhost:2181 | Coordination service |

### Observability

| Service | URL | Credentials | Description |
|---------|-----|-------------|-------------|
| Grafana | http://localhost:3000 | demo / demo | Unified dashboard |
| Prometheus | http://localhost:9090 | - | Metrics collection |
| Tempo | http://localhost:3200 | - | Distributed tracing |
| Loki | http://localhost:3100 | - | Log aggregation |
| OTLP (gRPC) | localhost:4317 | - | OpenTelemetry endpoint |
| OTLP (HTTP) | localhost:4318 | - | OpenTelemetry endpoint |

**Grafana Data Sources** (Docker internal network):
- Prometheus: `http://prometheus:9090`
- Tempo: `http://tempo:3200`
- Loki: `http://loki:3100`

## Author

Hyunwoo Park
