# Spring Boot Boilerplate (Kotlin)

A production-ready Spring Boot 3.5.5 + Kotlin 2.0 starter project, offering a unified package with essential backend
features pre-integrated and real-world examples. Includes a complete **OpenTelemetry-based observability stack** for
unified monitoring, distributed tracing, and log aggregation.

## Environment & Skills

- Application
	- Kotlin 2.0
	- Jdk 21
	- Spring boot 3.5.5
		- mvc
		- webflux
	- Gradle 8.10
	- Spring Security
	- Spring Batch
	- Springdoc OpenAPI
	- Postgresql
	- h2 database (PostgreSQL mode) - localhost environment
	- Jpa
	- QueryDSL
	- Redis
	- Jwt
	- Validation
	- Kotlin Logging
	- Logback
	- Flyway
	- Webhook
		- Slack
		- Discord
	- Kafka
	- WebClient
	- Spring Actuator


- Test
	- Spring Boot Starter Test
	- Spring Security
	- Spring Batch
	- Junit 5
	- Mockito Kotlin
	- Mockito Inline
	- Kotest
	- Mockk
	- Instancio
	- h2 database (PostgreSQL mode)
	- Flyway


- Etc
	- Docker
	- Pgadmin
	- Ktlint
	- Detekt
	- Mailhog
	- Netty resolver dns native macos
	- Kafka UI


- Monitoring
	- Prometheus
	- Grafana
	- Tempo
  - OpenTelemetry Collector
  - Loki
  - Sentry

## Project Guide

- monitoring
- docker
- src
	- common
	- domain (post, user, auth)
	- example
		- WelcomeSignUpConsumer: Kafka Consumer(SignUp Event) Example
	- infrastructure (kafka, redis, webhook, mail)
	- security
		- spring security + jwt logic
	- utils
	- resources
		- db
			- migration: flyway sql
			- sql: spring batch postgresql metadata sql
		- logback-spring.xml
			- Logback configuration with environment-specific settings
			- Profiles: prod, dev, local
		- application.yml
			- prod, dev, local, common, test, secret-{environment}
			- common: Write common variables for the project.
			- test: Create the variables needed for your test environment.
			- secret-{environment}: your secret variables for each environment.

## Clean Architecture + Hexagonal Architecture Version

If you're interested, check this out as well:

- **Repository**: [kotlin-clean-architecture-multimodule](https://github.com/awakelife93/kotlin-clean-architecture-multimodule)

## Local Installation

To use the application, the following two services must be installed and running:

- kafka
- redis
- mailhog
- grafana
- prometheus
- opentelemetry collector
- tempo
- loki

## Description

1. Security & CVE Management
	- The project manages dependency vulnerabilities centrally through the `applyCveFixes()` function in [build.gradle.kts](build.gradle.kts)
	- All known CVE fixes are automatically applied during dependency resolution

```kotlin
// Example: CVE fix implementation
requested.group == "org.apache.commons" && requested.name == "commons-lang3" -> {
	useVersion("3.18.0")
	because("CVE-2025-48924")
}
```

2. Database DDL Management
	- This project uses Flyway for DDL management instead of JPA auto-generation.
	- Migration scripts are located in [src/main/resources/db/migration](src/main/resources/db/migration)
	- If you prefer not to use Flyway, entity synchronization is configured - you can use JPA DDL auto-generation instead.
		- JPA DDL configuration: [src/main/resources/application-common.yml](src/main/resources/application-common.yml) (
			spring.jpa.generate-ddl)
		- Set `spring.jpa.hibernate.ddl-auto` property for each environment (local, dev, prod) as needed


3. Webhook
	- [enable & route endpoint](src/main/resources/application-common.yml)
		- default enable true
	- [types](src/main/kotlin/com/example/demo/infrastructure/webhook)
		- slack
		- discord

```kotlin
// example

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

4. Mailhog
	- mailhog is a tool for testing email sending.
	- [If you want to use MailHog, the default SMTP port is 1025.
		Of course, if you already have your own preferred setup, you can freely adjust the port as needed.](docker/base/docker-compose.mailhog.yml)
	- Please check the settings in application-local.yml and application-secret-local.yml.


5. Lint
	- ktlint
		- [using the official lint rules by default.](gradle.properties)
			- [Please refer to the lint rules for this project here.](.editorconfig)
		- report output
			- build/reports/ktlint
	- detekt
		- [using rules](detekt.yml)
		- report output
			- build/reports/detekt


6. Docker & Infrastructure Services
	- The project includes Docker Compose configurations for all required services
	- For detailed setup, port information, and service management, see [Docker Setup Guide](docker/README.md)


7. Create Spring Batch metadata table (localhost, development and production environments.)
	- Run your ddl script or Please refer
		to [github - spring batch](https://github.com/spring-projects/spring-batch/blob/5.0.x/spring-batch-core/src/main/resources/org/springframework/batch/core/schema-postgresql.sql)
		- Since this project uses postgresql, the spring.batch.jdbc.initialize-schema: always option does not work.
		- localhost & test environment,
			generating [batch-postgresql-metadata-schema.sql](src/main/resources/db/sql/batch-postgresql-metadata-schema.sql).
			- [application-test.yml](src/main/resources/application-test.yml)


8. Two types of tests
	- [mockito](src/test/kotlin/com/example/demo/mockito)
		- [BaseIntegrationController](src/test/kotlin/com/example/demo/mockito/common/BaseIntegrationController.kt)
	- [kotest & mockk](src/test/kotlin/com/example/demo/kotest)
		- **if you want to bypass Spring Security authentication issues.**
			- [SecurityListenerFactory](src/test/kotlin/com/example/demo/kotest/common/security/SecurityListenerFactory.kt)
			- [BaseIntegrationController](src/test/kotlin/com/example/demo/kotest/common/BaseIntegrationController.kt)
				```kotlin
				// example
				listeners(SecurityListenerFactory())

				Then("Call DELETE /api/v1/users/{userId}").config(tags = setOf(SecurityListenerFactory.NonSecurityOption)) {
					// ...
				}
				```

9. Kafka
	- [KafkaTopicMetaProvider](src/main/kotlin/com/example/demo/infrastructure/kafka/provider/KafkaTopicMetaProvider.kt)
		- Manage metadata related to topics
	- DLQ
		- [DLQs are dynamically created in this project.](src/main/kotlin/com/example/demo/infrastructure/kafka/provider/KafkaConsumerFactoryProvider.kt)
		- [default fallback partition: 1](src/main/kotlin/com/example/demo/infrastructure/kafka/DlqHelper.kt) (if the topic
			partition does not exist)


10. [Example](src/main/kotlin/com/example/demo/example/WelcomeSignUpConsumer.kt)
	- [When a user signs up, an event is generated to send an email to the recipient.](src/main/kotlin/com/example/demo/user/event/UserEventHandler.kt)
		- [You can test this flow by referring to the MailHog and Kafka sections.](src/main/kotlin/com/example/demo/example/WelcomeSignUpConsumer.kt)
	- [Accounts are hard deleted after one year.](src/main/kotlin/com/example/demo/user/batch/writer/UserDeleteItemWriter.kt)
		- [You can test this flow by referring to the Kafka sections.](src/main/kotlin/com/example/demo/example/UserDeleteConsumer.kt)


11. OpenTelemetry Stack Configuration (Monitoring & Observability)
	- All observability data (metrics, traces, logs) are now collected through OpenTelemetry Collector
	- **OpenTelemetry Collector Endpoints**:
		- OTLP gRPC: localhost:4317
		- OTLP HTTP: localhost:4318
	- **Grafana**: Unified dashboard for metrics, traces, and logs
		- Prometheus: Metrics collection and visualization
			- [Configuration](monitoring/prometheus.yml)
		- Tempo: Distributed tracing
			- [Configuration](monitoring/tempo.yml)
		- Loki: Log aggregation and analysis
			- [Configuration](monitoring/loki.yml)
	- **OpenTelemetry Collector**:
		- [Configuration](monitoring/otel-collector-config.yml)


12. Service Access URLs (When services are running)

	### Application
	- **API Documentation (Swagger)**: http://localhost:8085/swagger-ui/index.html
	- **H2 Console** (local environment): http://localhost:8085/h2-console
	- **Application Server**: http://localhost:8085

	### Infrastructure Services
	- **MailHog** (Email Testing): http://localhost:8025
	- **PgAdmin** (PostgreSQL Management): http://localhost:8088
	- **Kafka UI** (Kafka Management): http://localhost:9000
	- **Redis** (CLI/Client access): localhost:6379
	- **PostgreSQL** (Database connection): localhost:5432
	- **Kafka** (Broker connection): localhost:9092
	- **Zookeeper** (Coordination service): localhost:2181

	### Observability
	- **Grafana** (Unified Observability Dashboard): http://localhost:3000
		- Username: `demo`
		- Password: `demo`
		- Metrics (Prometheus), Traces (Tempo), Logs (Loki) visualization
		- **Data Source Configuration** (use Docker internal network addresses):
			- Prometheus: `http://prometheus:9090`
			- Tempo: `http://tempo:3200`
			- Loki: `http://loki:3100`
	- **Prometheus** (Metrics Collection): http://localhost:9090
	- **Tempo** (Distributed Tracing): http://localhost:3200
	- **Loki** (Log Aggregation): http://localhost:3100
	- **OpenTelemetry Collector**:
		- gRPC: localhost:4317
		- HTTP: localhost:4318

## Author

Hyunwoo Park
