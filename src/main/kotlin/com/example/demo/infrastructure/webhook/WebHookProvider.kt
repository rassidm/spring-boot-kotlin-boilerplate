package com.example.demo.infrastructure.webhook

import com.example.demo.infrastructure.webhook.common.CommonWebHookMessage
import com.example.demo.infrastructure.webhook.common.WebHookMessage
import com.example.demo.infrastructure.webhook.constant.WebHookTarget
import com.example.demo.infrastructure.webhook.discord.DiscordEmbed
import com.example.demo.infrastructure.webhook.discord.DiscordMessage
import com.example.demo.infrastructure.webhook.discord.DiscordWebHookMessage
import com.example.demo.infrastructure.webhook.slack.SlackMessage
import com.example.demo.infrastructure.webhook.slack.SlackWebHookMessage
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class WebHookProvider(
	private val webhookRouter: WebHookRouter,
	private val webhookMessageConverter: WebHookMessageConverter,
	@Value("\${webhook.enabled}") private val enabled: Boolean
) {
	fun sendAll(
		title: String,
		lines: List<String>
	) {
		send(WebHookTarget.ALL, CommonWebHookMessage(title, lines))
	}

	fun sendSlack(
		title: String,
		lines: List<String>
	) {
		send(WebHookTarget.SLACK, SlackWebHookMessage(mutableListOf(SlackMessage.of(title, lines))))
	}

	fun sendDiscord(
		title: String,
		lines: List<String>,
		embeds: List<DiscordEmbed>? = null
	) {
		send(WebHookTarget.DISCORD, DiscordWebHookMessage(mutableListOf(DiscordMessage.of(title, lines, embeds))))
	}

	private fun send(
		target: WebHookTarget,
		message: WebHookMessage
	) {
		validateEnabled()

		when (target) {
			WebHookTarget.ALL -> {
				require(message is CommonWebHookMessage) {
					"When using WebHookTarget.ALL, message must be of type CommonWebHookMessage"
				}
				sendAll(message)
			}

			else -> sendToTarget(target, message)
		}
	}

	private fun sendToTarget(
		target: WebHookTarget,
		message: WebHookMessage
	) {
		val sender = webhookRouter.route(target)
		val converted = webhookMessageConverter.convert(target, message)
		sender.send(converted)
	}

	private fun sendAll(message: CommonWebHookMessage) {
		webhookRouter.all().forEach { sender ->
			val converted = webhookMessageConverter.convert(sender.target(), message)
			sender.send(converted)
		}
	}

	private fun validateEnabled() {
		check(enabled) { "Webhook is not enabled" }
	}
}
