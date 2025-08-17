package com.example.demo.mockito.infrastructure.webhook.slack

import com.example.demo.infrastructure.webhook.slack.SlackFactoryProvider
import com.example.demo.infrastructure.webhook.slack.SlackMessage
import com.slack.api.Slack
import com.slack.api.webhook.Payload
import com.slack.api.webhook.WebhookResponse
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.MockedStatic
import org.mockito.Mockito.mockStatic
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@Tag("mockito-unit-test")
@DisplayName("Mockito Unit - Slack Factory Provider Test")
@ExtendWith(
	MockitoExtension::class
)
class SlackFactoryProviderTests {
	@Mock
	private lateinit var slackMock: Slack

	@Mock
	private lateinit var responseMock: WebhookResponse

	private lateinit var slackStaticMock: MockedStatic<Slack>
	private lateinit var provider: SlackFactoryProvider

	private val url = "https://hooks.slack.com/services/test"

	@BeforeEach
	fun setup() {
		slackStaticMock = mockStatic(Slack::class.java)
		slackStaticMock.`when`<Slack> { Slack.getInstance() }.thenReturn(slackMock)

		provider = SlackFactoryProvider(url)
	}

	@AfterEach
	fun tearDown() {
		slackStaticMock.close()
	}

	@Test
	fun `should send message successfully when Slack responds with 200`() {
		whenever(responseMock.code).thenReturn(200)
		whenever(slackMock.send(eq(url), any<Payload>())).thenReturn(responseMock)

		val slackMessage =
			listOf(
				SlackMessage("Test Title", mutableListOf("Line1", "Line2"))
			)

		provider.send(slackMessage)

		verify(slackMock).send(eq(url), any<Payload>())
	}

	@Test
	fun `should throw exception when Slack responds with error code`() {
		whenever(responseMock.code).thenReturn(500)
		whenever(responseMock.message).thenReturn("Internal Server Error")
		whenever(slackMock.send(eq(url), any<Payload>())).thenReturn(responseMock)

		val slackMessage =
			listOf(
				SlackMessage("Test Title", mutableListOf("Line1"))
			)

		val exception =
			kotlin
				.runCatching {
					provider.send(slackMessage)
				}.exceptionOrNull()

		assert(exception is IllegalArgumentException)
		assert(exception?.message == "Slack response code: 500, message: Internal Server Error")

		verify(slackMock).send(eq(url), any<Payload>())
	}
}
