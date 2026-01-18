package com.example.demo.security.component.provider

import com.example.demo.common.config.CorsConfig
import com.example.demo.security.component.SecurityErrorResponseWriter
import com.example.demo.security.component.filter.JWTAuthFilter
import jakarta.servlet.DispatcherType
import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configurers.CorsConfigurer
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer
import org.springframework.security.config.annotation.web.configurers.HttpBasicConfigurer
import org.springframework.security.config.annotation.web.configurers.SessionManagementConfigurer
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.stereotype.Component

@Component
class AuthProvider(
	private val corsConfig: CorsConfig,
	private val jwtProvider: JWTProvider,
	private val securityErrorResponseWriter: SecurityErrorResponseWriter,
	@Value("\${auth.x-api-key}") private val apiKey: String
) {
	fun ignoreListDefaultEndpoints(): Array<String> =
		arrayOf(
			"/api-docs/**",
			"/swagger-ui/**",
			"/swagger.html",
			"/actuator/**"
		)

	fun whiteListDefaultEndpoints(): Array<String> =
		arrayOf(
			"/api/v1/auth/signIn",
			"/api/v1/auth/refresh",
			"/api/v1/users/register"
		)

	fun generateRequestAPIKey(request: HttpServletRequest): String? = request.getHeader("X-API-KEY")

	fun validateApiKey(requestAPIKey: String): Boolean = apiKey == requestAPIKey

	fun defaultSecurityFilterChain(httpSecurity: HttpSecurity): HttpSecurity =
		httpSecurity
			.csrf { csrf: CsrfConfigurer<HttpSecurity?> -> csrf.disable() }
			.httpBasic { httpBasic: HttpBasicConfigurer<HttpSecurity?> -> httpBasic.disable() }
			.formLogin { formLogin: FormLoginConfigurer<HttpSecurity?> -> formLogin.disable() }
			.cors { cors: CorsConfigurer<HttpSecurity?> ->
				cors.configurationSource(
					corsConfig.corsConfigurationSource()
				)
			}.sessionManagement { httpSecuritySessionManagementConfigurer: SessionManagementConfigurer<HttpSecurity?> ->
				httpSecuritySessionManagementConfigurer.sessionCreationPolicy(
					SessionCreationPolicy.STATELESS
				)
			}.addFilterBefore(
				JWTAuthFilter(jwtProvider, securityErrorResponseWriter),
				UsernamePasswordAuthenticationFilter::class.java
			).authorizeHttpRequests { request ->
				request
					.dispatcherTypeMatchers(DispatcherType.ERROR)
					.permitAll()
			}
}
