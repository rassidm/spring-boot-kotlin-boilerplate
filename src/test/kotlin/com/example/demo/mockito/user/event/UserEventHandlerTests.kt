package com.example.demo.mockito.user.event

import com.example.demo.infrastructure.kafka.provider.KafkaTopicMetaProvider
import com.example.demo.infrastructure.mail.MailPayload
import com.example.demo.infrastructure.webhook.WebHookProvider
import com.example.demo.user.event.UserEvent
import com.example.demo.user.event.UserEventHandler
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@Tag("mockito-unit-test")
@DisplayName("Mockito Unit - User Event Handler Test")
@ExtendWith(MockitoExtension::class)
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
		val email = "awakelife93@gmail.com"
		val name = "Tester"
		val event = UserEvent.WelcomeSignUpEvent(email, name)

		handler.handleWelcomeSignUpEvent(event)

		argumentCaptor<MailPayload> {
			verify(kafkaTemplate).send(
				eq(KafkaTopicMetaProvider.MAIL_TOPIC),
				capture()
			)

			firstValue.let { payload ->
				assertEquals(email, payload.to)
				assertEquals("$name.", payload.subject)
				assertEquals("Welcome to our service!", payload.body)
			}
		}

		verifyNoInteractions(webHookProvider)
	}

	@Test
	fun `should send all message and rethrow if kafka send fails`() {
		val email = "fail@example.com"
		val name = "Failer"
		val event = UserEvent.WelcomeSignUpEvent(email, name)
		val exception = RuntimeException("Kafka send failed")

		whenever(kafkaTemplate.send(any<String>(), any<MailPayload>())) doThrow exception

		val thrown =
			assertThrows<RuntimeException> {
				handler.handleWelcomeSignUpEvent(event)
			}

		assertEquals("Kafka send failed", thrown.message)

		verify(kafkaTemplate).send(
			eq(KafkaTopicMetaProvider.MAIL_TOPIC),
			any<MailPayload>()
		)

		verify(webHookProvider).sendAll(
			argThat { contains("handleWelcomeSignUpEvent") },
			argThat { any { it.contains("Kafka send failed") } }
		)

		verifyNoMoreInteractions(kafkaTemplate, webHookProvider)
	}
}
