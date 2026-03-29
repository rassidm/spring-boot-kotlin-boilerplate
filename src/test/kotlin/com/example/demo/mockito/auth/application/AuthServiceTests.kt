package com.example.demo.mockito.auth.application

import com.example.demo.auth.application.AuthService
import com.example.demo.auth.dto.command.RefreshAccessTokenCommand
import com.example.demo.auth.dto.command.SignInCommand
import com.example.demo.security.SecurityUserItem
import com.example.demo.security.UserAdapter
import com.example.demo.security.component.provider.JWTProvider
import com.example.demo.security.component.provider.TokenProvider
import com.example.demo.security.exception.RefreshTokenNotFoundException
import com.example.demo.user.application.impl.UserServiceImpl
import com.example.demo.user.entity.User
import com.example.demo.user.exception.UserNotFoundException
import com.example.demo.user.exception.UserUnAuthorizedException
import org.instancio.Instancio
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@Tag("mockito-unit-test")
@DisplayName("Mockito Unit - Auth Service Test")
@ExtendWith(
	MockitoExtension::class
)
class AuthServiceTests {
	@Mock
	private lateinit var jwtProvider: JWTProvider

	@Mock
	private lateinit var tokenProvider: TokenProvider

	@Mock
	private lateinit var userServiceImpl: UserServiceImpl

	@InjectMocks
	private lateinit var authService: AuthService

	private val defaultAccessToken =
		"""
    eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.
    eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c
    """

	private val user: User = Instancio.create(User::class.java)

	@Nested
	@DisplayName("Sign In Test")
	inner class SignInTest {
		private val signInCommand: SignInCommand = Instancio.create(SignInCommand::class.java)

		@Test
		@DisplayName("Success sign in")
		fun should_AssertSignInResponse_when_GivenSignInCommand() {
			whenever(userServiceImpl.validateAuthReturnUser(any<String>(), any<String>()))
				.thenReturn(user)

			whenever(tokenProvider.createFullTokens(any<User>()))
				.thenReturn(defaultAccessToken)

			val signInResponse = authService.signIn(signInCommand)

			assertNotNull(signInResponse)
			assertEquals(user.email, signInResponse.email)
			assertEquals(user.name, signInResponse.name)
			assertEquals(defaultAccessToken, signInResponse.accessToken)
		}

		@Test
		@DisplayName("User not found")
		fun should_AssertUserNotFoundException_when_GivenSignInCommand() {
			whenever(userServiceImpl.validateAuthReturnUser(any<String>(), any<String>()))
				.thenThrow(UserNotFoundException(user.id))

			Assertions.assertThrows(
				UserNotFoundException::class.java
			) { authService.signIn(signInCommand) }
		}

		@Test
		@DisplayName("User unauthorized")
		fun should_AssertUserUnAuthorizedException_when_GivenSignInCommand() {
			whenever(userServiceImpl.validateAuthReturnUser(any<String>(), any<String>()))
				.thenThrow(UserUnAuthorizedException(user.email))

			Assertions.assertThrows(
				UserUnAuthorizedException::class.java
			) { authService.signIn(signInCommand) }
		}
	}

	@Nested
	@DisplayName("Sign Out Test")
	inner class SignOutTest {
		@Test
		@DisplayName("Success sign out")
		fun should_VerifyCallDeleteRefreshToken_when_GivenUserId() {
			authService.signOut(user.id)

			verify(tokenProvider, times(1)).deleteRefreshToken(any<Long>())
		}
	}

	@Nested
	@DisplayName("Refresh Access Token Test")
	inner class RefreshTokenTest {
		private val refreshAccessTokenCommand: RefreshAccessTokenCommand =
			Instancio.create(
				RefreshAccessTokenCommand::class.java
			)

		private val usernamePasswordAuthenticationToken =
			UsernamePasswordAuthenticationToken(
				UserAdapter(SecurityUserItem.from(user)),
				null,
				UserAdapter(SecurityUserItem.from(user)).authorities
			)

		@Test
		@DisplayName("Success refresh access token")
		fun should_AssertRefreshAccessTokenResponse_when_GivenSecurityUserItem() {
			whenever(jwtProvider.getAuthentication(any<String>(), any<Boolean>()))
				.thenReturn(usernamePasswordAuthenticationToken)

			whenever(tokenProvider.refreshAccessToken(any<SecurityUserItem>()))
				.thenReturn(defaultAccessToken)

			val refreshAccessTokenResponse =
				authService.refreshAccessToken(
					refreshAccessTokenCommand
				)

			assertNotNull(refreshAccessTokenResponse)
			assertEquals(
				defaultAccessToken,
				refreshAccessTokenResponse.accessToken
			)
		}

		@Test
		@DisplayName("Refresh token is not found")
		fun should_AssertRefreshTokenNotFoundException_when_GivenSecurityUserItem() {
			whenever(jwtProvider.getAuthentication(any<String>(), any<Boolean>()))
				.thenReturn(usernamePasswordAuthenticationToken)

			whenever(tokenProvider.refreshAccessToken(any<SecurityUserItem>()))
				.thenThrow(RefreshTokenNotFoundException(user.id))

			Assertions.assertThrows(
				RefreshTokenNotFoundException::class.java
			) { authService.refreshAccessToken(refreshAccessTokenCommand) }
		}
	}
}
