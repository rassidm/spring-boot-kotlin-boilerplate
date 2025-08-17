package com.example.demo.mockito.auth.api

import com.example.demo.auth.api.AuthController
import com.example.demo.auth.application.AuthService
import com.example.demo.auth.dto.serve.request.RefreshAccessTokenRequest
import com.example.demo.auth.dto.serve.request.SignInRequest
import com.example.demo.auth.dto.serve.response.RefreshAccessTokenResponse
import com.example.demo.auth.dto.serve.response.SignInResponse
import com.example.demo.security.SecurityUserItem
import com.example.demo.user.entity.User
import org.instancio.Instancio
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@Tag("mockito-unit-test")
@DisplayName("Mockito Unit - Auth Controller Test")
@ExtendWith(
	MockitoExtension::class
)
class AuthControllerTests {
	@InjectMocks
	private lateinit var authController: AuthController

	@Mock
	private lateinit var authService: AuthService

	private val defaultAccessToken =
		"""
    eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.
    eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c
    """

	private val user: User = Instancio.create(User::class.java)

	@Test
	@DisplayName("Sign in")
	fun should_AssertSignInResponse_when_GivenSignInRequest() {
		val signInRequest = Instancio.create(SignInRequest::class.java)

		whenever(authService.signIn(any<SignInRequest>()))
			.thenReturn(SignInResponse.from(user, defaultAccessToken))

		val response =
			authController.signIn(
				signInRequest
			)

		assertNotNull(response)
		assertNotNull(response.body)
		assertEquals(HttpStatus.OK, response.statusCode)

		val body =
			requireNotNull(response.body) {
				"Response body must not be null"
			}

		assertEquals(user.id, body.userId)
		assertEquals(user.email, body.email)
		assertEquals(user.name, body.name)
		assertEquals(user.role, body.role)
		assertEquals(defaultAccessToken, body.accessToken)
	}

	@Test
	@DisplayName("Sign out")
	fun should_AssertSignOutVoidResponse_when_GivenSecurityUserItem() {
		val securityUserItem =
			Instancio.create(
				SecurityUserItem::class.java
			)

		val response = authController.signOut(securityUserItem)

		assertNotNull(response)
		assertNull(response.body)
		assertEquals(HttpStatus.OK, response.statusCode)
	}

	@Test
	@DisplayName("Refresh access token")
	fun should_AssertRefreshAccessTokenResponse_when_GivenSecurityUserItem() {
		val refreshAccessTokenRequest =
			Instancio.create(
				RefreshAccessTokenRequest::class.java
			)

		whenever(authService.refreshAccessToken(any<RefreshAccessTokenRequest>()))
			.thenReturn(RefreshAccessTokenResponse.of(defaultAccessToken))

		val response =
			authController.refreshAccessToken(
				refreshAccessTokenRequest
			)

		assertNotNull(response)
		assertNotNull(response.body)
		assertEquals(HttpStatus.CREATED, response.statusCode)

		val body =
			requireNotNull(response.body) {
				"Response body must not be null"
			}

		assertEquals(defaultAccessToken, body.accessToken)
	}
}
