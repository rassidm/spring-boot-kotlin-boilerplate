package com.example.demo.auth.application

import com.example.demo.auth.dto.command.RefreshAccessTokenCommand
import com.example.demo.auth.dto.command.SignInCommand
import com.example.demo.auth.dto.response.RefreshAccessTokenResponse
import com.example.demo.auth.dto.response.SignInResponse
import com.example.demo.security.UserAdapter
import com.example.demo.security.component.provider.JWTProvider
import com.example.demo.security.component.provider.TokenProvider
import com.example.demo.user.application.UserService
import com.example.demo.user.entity.User
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service

@Service
class AuthService(
	private val userService: UserService,
	private val tokenProvider: TokenProvider,
	private val jwtProvider: JWTProvider
) {
	fun signIn(command: SignInCommand): SignInResponse {
		val user: User = userService.validateAuthReturnUser(command.email, command.password)

		return user.let {
			SignInResponse.from(it, tokenProvider.createFullTokens(it))
		}
	}

	fun signOut(userId: Long) {
		tokenProvider.deleteRefreshToken(userId)
		SecurityContextHolder.clearContext()
	}

	fun refreshAccessToken(command: RefreshAccessTokenCommand): RefreshAccessTokenResponse {
		val usernamePasswordAuthenticationToken = jwtProvider.getAuthentication(command.refreshToken, true)
		val userAdapter = usernamePasswordAuthenticationToken.principal as UserAdapter

		return RefreshAccessTokenResponse.from(
			tokenProvider.refreshAccessToken(
				userAdapter.securityUserItem
			)
		)
	}
}
