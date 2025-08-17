package com.example.demo.mockito.infrastructure.webhook

import com.example.demo.infrastructure.webhook.WebHookMessageConverter
import com.example.demo.infrastructure.webhook.WebHookProvider
import com.example.demo.infrastructure.webhook.WebHookRouter
import com.example.demo.infrastructure.webhook.common.CommonWebHookMessage
import com.example.demo.infrastructure.webhook.common.WebHookMessage
import com.example.demo.infrastructure.webhook.common.WebHookSender
import com.example.demo.infrastructure.webhook.constant.WebHookTarget
import com.example.demo.infrastructure.webhook.discord.DiscordWebHookMessage
import com.example.demo.infrastructure.webhook.slack.SlackWebHookMessage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@Tag("mockito-unit-test")
@DisplayName("Mockito Unit - WebHook Provider Test")
@ExtendWith(
	MockitoExtension::class
)
class WebHookProviderTests {
	private lateinit var router: WebHookRouter
	private lateinit var converter: WebHookMessageConverter
	private lateinit var sender: WebHookSender
	private lateinit var provider: WebHookProvider

	private val slackTarget = WebHookTarget.SLACK
	private val discordTarget = WebHookTarget.DISCORD

	private val slackMessage = SlackWebHookMessage(listOf())
	private val discordMessage = DiscordWebHookMessage(listOf())

	private val commonMessage = CommonWebHookMessage("Title", listOf("Line1", "Line2"))

	@BeforeEach
	fun setup() {
		router = mock(WebHookRouter::class.java)
		converter = mock(WebHookMessageConverter::class.java)
		sender = mock(WebHookSender::class.java)
		provider = WebHookProvider(router, converter, true)
	}

	@Nested
	internal inner class WhenWebhookEnabledTrue {
		@Test
		fun sendSlack_shouldConvertAndSendMessageForSlackTarget() {
			whenever(router.route(slackTarget)).thenReturn(sender)
			whenever(converter.convert(eq(slackTarget), any<WebHookMessage>())).thenReturn(slackMessage)

			provider.sendSlack("Title", listOf("Line1", "Line2"))

			verify(router).route(slackTarget)
			verify(converter).convert(eq(slackTarget), any<WebHookMessage>())
			verify(sender).send(slackMessage)
		}

		@Test
		fun sendDiscord_shouldConvertAndSendMessageForDiscordTarget() {
			whenever(router.route(discordTarget)).thenReturn(sender)
			whenever(converter.convert(eq(discordTarget), any<WebHookMessage>())).thenReturn(discordMessage)

			provider.sendDiscord("Title", listOf("Line1", "Line2"))

			verify(router).route(discordTarget)
			verify(converter).convert(eq(discordTarget), any<WebHookMessage>())
			verify(sender).send(discordMessage)
		}

		@Test
		fun sendAll_shouldConvertAndSendMessageToAllTargets() {
			val sender1: WebHookSender = mock(WebHookSender::class.java)
			val sender2: WebHookSender = mock(WebHookSender::class.java)

			whenever(router.all()).thenReturn(listOf(sender1, sender2))

			whenever(sender1.target()).thenReturn(WebHookTarget.SLACK)
			whenever(sender2.target()).thenReturn(WebHookTarget.DISCORD)

			whenever(converter.convert(eq(WebHookTarget.SLACK), any())).thenReturn(slackMessage)
			whenever(converter.convert(eq(WebHookTarget.DISCORD), any())).thenReturn(discordMessage)

			provider.sendAll("Title", listOf("Line1", "Line2"))

			verify(converter).convert(
				eq(WebHookTarget.SLACK),
				argThat { msg ->
					val commonMsg = msg as? CommonWebHookMessage ?: return@argThat false
					commonMsg.title == commonMessage.title && commonMsg.contents == commonMessage.contents
				}
			)

			verify(converter).convert(
				eq(WebHookTarget.DISCORD),
				argThat { msg ->
					val commonMsg = msg as? CommonWebHookMessage ?: return@argThat false
					commonMsg.title == commonMessage.title && commonMsg.contents == commonMessage.contents
				}
			)

			verify(sender1).send(slackMessage)
			verify(sender2).send(discordMessage)
		}
	}

	@Nested
	internal inner class WhenWebhookEnabledFalse {
		private lateinit var providerDisabled: WebHookProvider

		@BeforeEach
		fun setupDisabled() {
			providerDisabled = WebHookProvider(router, converter, false)
		}

		@Test
		fun shouldThrowExceptionIfEnabledIsFalse() {
			val exception: IllegalStateException =
				assertThrows(IllegalStateException::class.java) {
					providerDisabled.sendAll("Title", listOf("Line1"))
				}

			assertEquals("Webhook is not enabled", exception.message)
		}
	}
}
