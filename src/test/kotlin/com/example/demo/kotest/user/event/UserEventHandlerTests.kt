package com.example.demo.kotest.user.event

import com.example.demo.infrastructure.kafka.provider.KafkaTopicMetaProvider
import com.example.demo.infrastructure.mail.MailPayload
import com.example.demo.infrastructure.webhook.WebHookProvider
import com.example.demo.user.event.UserEvent
import com.example.demo.user.event.UserEventHandler
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@Tags("kotest-unit-test")
class UserEventHandlerTests :
	StringSpec({

		val kafkaTemplate = mockk<KafkaTemplate<String, MailPayload>>(relaxed = true)
		val webHookProvider = mockk<WebHookProvider>(relaxed = true)

		val handler = UserEventHandler(kafkaTemplate, webHookProvider)

		"should send kafka message on welcome event" {
			val event =
				UserEvent.WelcomeSignUpEvent(
					email = "awakelife93@gmail.com",
					name = "Tester"
				)

			handler.handleWelcomeSignUpEvent(event)

			verify {
				kafkaTemplate.send(
					KafkaTopicMetaProvider.MAIL_TOPIC,
					withArg {
						it.to shouldBe "awakelife93@gmail.com"
						it.subject shouldBe "Tester."
						it.body shouldBe "Welcome to our service!"
					}
				)
			}

			confirmVerified(kafkaTemplate)
		}

		"should send all message and rethrow if kafka send fails" {
			val event =
				UserEvent.WelcomeSignUpEvent(
					email = "fail@example.com",
					name = "Failer"
				)

			every {
				kafkaTemplate.send(any<String>(), any<MailPayload>())
			} throws RuntimeException("Kafka send failed")

			val exception =
				shouldThrow<RuntimeException> {
					handler.handleWelcomeSignUpEvent(event)
				}

			exception.message shouldBe "Kafka send failed"

			verify {
				webHookProvider.sendAll(
					match { it.contains("handleWelcomeSignUpEvent") },
					withArg {
						it.any { msg -> msg.contains("Kafka send failed") }
					}
				)
			}

			confirmVerified(webHookProvider)
		}
	})
