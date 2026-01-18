package com.example.demo.security.component.filter

import com.example.demo.security.component.SecurityErrorResponseWriter
import com.example.demo.security.component.provider.AuthProvider
import com.example.demo.security.exception.APIKeyNotFoundException
import io.micrometer.common.lang.NonNull
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.filter.OncePerRequestFilter

class APIKeyAuthFilter(
	private val authProvider: AuthProvider,
	private val securityErrorResponseWriter: SecurityErrorResponseWriter
) : OncePerRequestFilter() {
	override fun doFilterInternal(
		@NonNull httpServletRequest: HttpServletRequest,
		@NonNull httpServletResponse: HttpServletResponse,
		@NonNull filterChain: FilterChain
	) {
		runCatching {
			authProvider.generateRequestAPIKey(httpServletRequest)?.let {
				if (!authProvider.validateApiKey(it)) {
					throw APIKeyNotFoundException(httpServletRequest.requestURI)
				}
			} ?: throw APIKeyNotFoundException(httpServletRequest.requestURI)
		}.onSuccess { filterChain.doFilter(httpServletRequest, httpServletResponse) }
			.onFailure {
				securityErrorResponseWriter.writeErrorResponse(
					httpServletRequest,
					httpServletResponse,
					it
				)
			}
	}
}
