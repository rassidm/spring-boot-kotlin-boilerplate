package com.example.demo.user.event

import com.example.demo.infrastructure.kafka.provider.KafkaTopicMetaProvider
import com.example.demo.infrastructure.mail.MailPayload
import com.example.demo.infrastructure.webhook.WebHookProvider
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

private val logger = KotlinLogging.logger {}

@Component
class UserEventHandler(
	private val mailKafkaTemplate: KafkaTemplate<String, MailPayload>,
	private val webHookProvider: WebHookProvider
) {
	@Async
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	fun handleWelcomeSignUpEvent(welcomeSignUpEvent: UserEvent.WelcomeSignUpEvent) {
		runCatching {
			val payload =
				MailPayload(
					to = welcomeSignUpEvent.email,
					subject = "${welcomeSignUpEvent.name}.",
					body = "Welcome to our service!"
				)

			mailKafkaTemplate.send(KafkaTopicMetaProvider.MAIL_TOPIC, payload)
		}.onFailure { exception ->
			logger.error(exception) { "Failed to send message to Kafka" }

			webHookProvider.sendAll(
				title = "Failed to send message to Kafka (handleWelcomeSignUpEvent)",
				lines = mutableListOf("Failed to send message to Kafka: ${exception.message} / $welcomeSignUpEvent")
			)

			throw exception
		}
	}
}
