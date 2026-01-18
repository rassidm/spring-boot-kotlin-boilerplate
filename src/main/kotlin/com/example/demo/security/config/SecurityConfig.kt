package com.example.demo.security.config

import com.example.demo.security.component.CustomAccessDeniedHandler
import com.example.demo.security.component.CustomAuthenticationEntryPoint
import com.example.demo.security.component.SecurityErrorResponseWriter
import com.example.demo.security.component.filter.APIKeyAuthFilter
import com.example.demo.security.component.provider.AuthProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configurers.ExceptionHandlingConfigurer
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Profile("dev", "prod")
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfig(
	private val authProvider: AuthProvider,
	private val customAuthenticationEntryPoint: CustomAuthenticationEntryPoint,
	private val customAccessDeniedHandler: CustomAccessDeniedHandler,
	private val securityErrorResponseWriter: SecurityErrorResponseWriter
) {
	@Bean
	@Throws(Exception::class)
	fun authenticationManager(authenticationConfiguration: AuthenticationConfiguration): AuthenticationManager = authenticationConfiguration.authenticationManager

	@Bean
	@Throws(Exception::class)
	fun filterChain(httpSecurity: HttpSecurity): SecurityFilterChain =
		authProvider
			.defaultSecurityFilterChain(httpSecurity)
			.authorizeHttpRequests { request ->
				request
					.requestMatchers(*authProvider.whiteListDefaultEndpoints(), *authProvider.ignoreListDefaultEndpoints())
					.permitAll()
					.anyRequest()
					.authenticated()
			}.addFilterAfter(
				APIKeyAuthFilter(authProvider, securityErrorResponseWriter),
				UsernamePasswordAuthenticationFilter::class.java
			).exceptionHandling { exceptionHandling: ExceptionHandlingConfigurer<HttpSecurity?> ->
				exceptionHandling
					.authenticationEntryPoint(customAuthenticationEntryPoint)
					.accessDeniedHandler(customAccessDeniedHandler)
			}.build()
}
