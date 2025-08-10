package com.example.demo.mockito.user.event

import com.example.demo.infrastructure.kafka.provider.KafkaTopicMetaProvider
import com.example.demo.infrastructure.mail.MailPayload
import com.example.demo.infrastructure.webhook.WebHookProvider
import com.example.demo.user.event.UserEvent
import com.example.demo.user.event.UserEventHandler
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.ArgumentMatchers.contains
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@Tag("mockito-unit-test")
@DisplayName("Mockito Unit - User Event Handler Test")
@ExtendWith(
	MockitoExtension::class
)
class UserEventHandlerTests {
	@Mock
	private lateinit var kafkaTemplate: KafkaTemplate<String, MailPayload>

	@Mock
	private lateinit var webHookProvider: WebHookProvider
	private lateinit var handler: UserEventHandler

	@BeforeEach
	fun setUp() {
		handler = UserEventHandler(kafkaTemplate, webHookProvider)
	}

	@Test
	fun `should send kafka message on welcome event`() {
		val event = UserEvent.WelcomeSignUpEvent("awakelife93@gmail.com", "Tester")

		handler.handleWelcomeSignUpEvent(event)

		val captor = argumentCaptor<MailPayload>()
		verify(kafkaTemplate).send(eq(KafkaTopicMetaProvider.MAIL_TOPIC), captor.capture())

		val payload = captor.firstValue
		assertEquals("awakelife93@gmail.com", payload.to)
		assertEquals("Tester.", payload.subject)
		assertEquals("Welcome to our service!", payload.body)
	}

	@Test
	fun `should send all message and rethrow if kafka send fails`() {
		val event = UserEvent.WelcomeSignUpEvent("fail@example.com", "Failer")
		val exception = RuntimeException("Kafka send failed")

		whenever(kafkaTemplate.send(anyString(), any<MailPayload>())).thenThrow(exception)

		val thrown =
			assertThrows(RuntimeException::class.java) {
				handler.handleWelcomeSignUpEvent(event)
			}

		assertEquals("Kafka send failed", thrown.message)

		verify(webHookProvider).sendAll(
			contains("handleWelcomeSignUpEvent"),
			argThat { any { it.contains("Kafka send failed") } }
		)
	}
}
