package com.example.demo.example

import com.example.demo.infrastructure.kafka.provider.KafkaTopicMetaProvider
import com.example.demo.infrastructure.mail.MailHelper
import com.example.demo.infrastructure.mail.MailPayload
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.mail.MailException
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

@Component
class WelcomeSignUpConsumer(
	private val mailHelper: MailHelper
) {
	@KafkaListener(
		topics = [KafkaTopicMetaProvider.MAIL_TOPIC],
		groupId = KafkaTopicMetaProvider.MAIL_GROUP,
		containerFactory = KafkaTopicMetaProvider.MAIL_CONTAINER_FACTORY
	)
	@Retryable(
		value = [MailException::class],
		maxAttempts = 3,
		backoff = Backoff(delay = 2000)
	)
	fun consume(payload: MailPayload) {
		runCatching {
			logger.info { "Received email payload: $payload" }
			mailHelper.sendEmail(payload)
		}.onFailure { exception ->
			logger.error { "Failed to send email: ${exception.message}" }

			when (exception) {
				is MailException -> throw exception
				else -> logger.warn { "Unexpected error during email sending: ${exception.javaClass.simpleName}" }
			}
		}
	}
}
