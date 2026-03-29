package com.example.demo.kotest.auth.api

import com.example.demo.auth.api.AuthController
import com.example.demo.auth.application.AuthService
import com.example.demo.auth.dto.command.SignInCommand
import com.example.demo.auth.dto.request.SignInRequest
import com.example.demo.auth.dto.response.SignInResponse
import com.example.demo.security.SecurityUserItem
import com.example.demo.user.entity.User
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import org.instancio.Instancio
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@Tags("kotest-unit-test")
class AuthControllerTests :
	FunSpec({
		val authController = mockk<AuthController>()
		val authService = mockk<AuthService>()

		val user: User = Instancio.create(User::class.java)
		val defaultAccessToken =
			"""
      eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.
      eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c
      """

		test("Sign In") {
			val signInRequest = Instancio.create(SignInRequest::class.java)

			every { authService.signIn(any<SignInCommand>()) } returns SignInResponse.from(user, defaultAccessToken)

			every { authController.signIn(any<SignInRequest>()) } returns
				ResponseEntity.ok(
					SignInResponse.from(
						user,
						defaultAccessToken
					)
				)

			val response = authController.signIn(signInRequest)

			response shouldNotBeNull {
				statusCode shouldBe HttpStatus.OK
				body shouldNotBeNull {
					userId shouldBe user.id
					email shouldBe user.email
					name shouldBe user.name
					role shouldBe user.role
					accessToken shouldBe defaultAccessToken
				}
			}
		}

		test("Sign Out") {
			val securityUserItem = Instancio.create(SecurityUserItem::class.java)

			justRun { authService.signOut(any<Long>()) }

			every { authController.signOut(any<SecurityUserItem>()) } returns ResponseEntity.ok().build()

			val response = authController.signOut(securityUserItem)

			response shouldNotBeNull {
				statusCode shouldBe HttpStatus.OK
				body.shouldBeNull()
			}
		}
	})
