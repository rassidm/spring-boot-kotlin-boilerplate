package com.example.demo.kotest.infrastructure.mail

import com.example.demo.common.exception.CustomRuntimeException
import com.example.demo.infrastructure.mail.MailHelper
import com.example.demo.infrastructure.mail.MailPayload
import com.example.demo.infrastructure.webhook.WebHookProvider
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import org.springframework.mail.MailSender
import org.springframework.mail.SimpleMailMessage
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@Tags("kotest-unit-test")
class MailHelperTests :
	BehaviorSpec({
		Given("a valid mail payload") {
			val payload =
				MailPayload.of(
					to = "user@example.com",
					subject = "Test Subject",
					body = "Test Body"
				)

			val mailSender = mockk<MailSender>(relaxed = true)
			val webHookProvider = mockk<WebHookProvider>(relaxed = true)
			val mailHelper = MailHelper(mailSender, webHookProvider)

			When("sending an email") {

				Then("mailSender.send should be called with correct values") {
					mailHelper.sendEmail(payload)

					verify(exactly = 1) {
						mailSender.send(
							withArg {
								it.to shouldBe arrayOf("user@example.com")
								it.subject shouldBe "Test Subject"
								it.text shouldBe "Test Body"
							}
						)
					}
				}
			}
		}

		Given("an invalid email address") {
			val payload =
				MailPayload.of(
					to = "invalid-email",
					subject = "Subject",
					body = "Body"
				)

			val mailSender = mockk<MailSender>(relaxed = true)
			val webHookProvider = mockk<WebHookProvider>(relaxed = true)
			val mailHelper = MailHelper(mailSender, webHookProvider)

			When("sending an email") {

				Then("a CustomRuntimeException should be thrown") {
					val exception =
						shouldThrow<CustomRuntimeException> {
							mailHelper.sendEmail(payload)
						}

					exception.message shouldContain "Validation failed: to must be a valid email"
					verify(exactly = 0) { mailSender.send(any<SimpleMailMessage>()) }
				}
			}
		}

		Given("a mail sending failure") {
			val payload =
				MailPayload(
					to = "user@example.com",
					subject = "Test Subject",
					body = "Test Body"
				)

			val mailSender = mockk<MailSender>()
			val webHookProvider = mockk<WebHookProvider>(relaxed = true)
			val mailHelper = MailHelper(mailSender, webHookProvider)

			every { mailSender.send(any<SimpleMailMessage>()) } throws RuntimeException("SMTP error")

			justRun {
				webHookProvider.sendSlack(any<String>(), any<List<String>>())
			}

			When("sending an email") {

				Then("webHookProvider.sendSlack should be invoked") {
					val exception =
						shouldThrowExactly<CustomRuntimeException> {
							mailHelper.sendEmail(payload)
						}

					exception.message shouldContain "SMTP error"

					verify(exactly = 1) {
						webHookProvider.sendSlack(
							"Mail Sending Failed",
							mutableListOf(
								"Mail to: ${payload.to}",
								"Mail Subject: ${payload.subject}",
								"Mail Body: ${payload.body}",
								"Error: SMTP error"
							)
						)
					}
				}
			}
		}
	})
