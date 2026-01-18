package com.example.demo.security.component.filter

import com.example.demo.security.component.SecurityErrorResponseWriter
import com.example.demo.security.component.provider.JWTProvider
import io.micrometer.common.lang.NonNull
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter

class JWTAuthFilter(
	private val jwtProvider: JWTProvider,
	private val securityErrorResponseWriter: SecurityErrorResponseWriter
) : OncePerRequestFilter() {
	override fun doFilterInternal(
		@NonNull httpServletRequest: HttpServletRequest,
		@NonNull httpServletResponse: HttpServletResponse,
		@NonNull filterChain: FilterChain
	) {
		runCatching {
			jwtProvider.generateRequestToken(httpServletRequest)?.let {
				jwtProvider.validateToken(it)

				val usernamePasswordAuthenticationToken: UsernamePasswordAuthenticationToken =
					jwtProvider.getAuthentication(
						it
					)

				SecurityContextHolder
					.getContext()
					.authentication = usernamePasswordAuthenticationToken
			} ?: SecurityContextHolder.clearContext()
		}.onSuccess { filterChain.doFilter(httpServletRequest, httpServletResponse) }
			.onFailure {
				SecurityContextHolder.clearContext()

				securityErrorResponseWriter.writeErrorResponse(
					httpServletRequest,
					httpServletResponse,
					it,
					it.message ?: "Invalid Token"
				)
			}
	}
}
