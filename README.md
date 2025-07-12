# Spring Boot Boilerplate (Kotlin)

## Environment & Skills

- Application
	- Kotlin 2.0
	- Jdk 21
	- Spring boot 3.5.3
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
	- Sentry
	- Kotlin Logging
	- Flyway
	- Webhook
		- Slack
		- Discord
	- Kafka
	- WebClient

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
	- Pgadmin
	- Ktlint
	- Detekt
	- Mailhog
	- Netty resolver dns native macos

## Project Guide

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
	- application.yml
		- prod, dev, local, common, test, secret-{environment}
		- common: Write common variables for the project.
		- test: Create the variables needed for your test environment.
		- secret-{environment}: your secret variables for each environment.

## Local Installation

To use the application, the following two services must be installed and running:

- kafka
- redis
- mailhog

## Description

1. webhook
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

2. mailhog
	- mailhog is a tool for testing email sending.
	- [If you want to use MailHog, the default SMTP port is 1025.
		Of course, if you already have your own preferred setup, you can freely adjust the port as needed.](docker-compose.yml)
	- dashboard: http://localhost:8025
	- Please check the settings in application-local.yml and application-secret-local.yml.


3. lint
	- ktlint
		- [using the official lint rules by default.](gradle.properties)
			- [Please refer to the lint rules for this project here.](.editorconfig)
		- report output
			- build/reports/ktlint
	- detekt
		- [using rules](detekt.yml)
		- report output
			- build/reports/detekt


4. docker-compose
	- If you plan to use it, you need to check the environment variables.


5. create spring batch metadata table (localhost, development and production environments.)
	- Run your ddl script or Please refer
		to [github - spring batch](https://github.com/spring-projects/spring-batch/blob/5.0.x/spring-batch-core/src/main/resources/org/springframework/batch/core/schema-postgresql.sql)
		- Since this project uses postgresql, the spring.batch.jdbc.initialize-schema: always option does not work.
		- localhost & test environment,
			generating [batch-postgresql-metadata-schema.sql](src/main/resources/db/sql/batch-postgresql-metadata-schema.sql).
			- [application-test.yml](src/main/resources/application-test.yml)


6. two types of tests
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

7. kafka
	- [KafkaTopicMetaProvider](src/main/kotlin/com/example/demo/infrastructure/kafka/provider/KafkaTopicMetaProvider.kt)
		- Manage metadata related to topics
	- DLQ
		- [DLQs are dynamically created in this project.](src/main/kotlin/com/example/demo/infrastructure/kafka/provider/KafkaConsumerFactoryProvider.kt)
		- [default fallback partition: 1](src/main/kotlin/com/example/demo/infrastructure/kafka/DlqHelper.kt) (if the topic
			partition does not exist)


8. [example](src/main/kotlin/com/example/demo/example/WelcomeSignUpConsumer.kt)
	- [When a user signs up, an event is generated to send an email to the recipient.](src/main/kotlin/com/example/demo/user/event/UserEventHandler.kt)
		- You can test this flow by referring to the MailHog and Kafka sections.

## Author

Hyunwoo Park
