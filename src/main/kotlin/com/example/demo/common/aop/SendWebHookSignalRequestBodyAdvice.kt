package com.example.demo.common.aop

import com.example.demo.common.annotaction.SendWebHookSignalRequest
import com.example.demo.infrastructure.webhook.WebHookProvider
import org.springframework.core.MethodParameter
import org.springframework.http.HttpInputMessage
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter
import java.lang.reflect.Type

@ControllerAdvice
class SendWebHookSignalRequestBodyAdvice(
	private val webHookProvider: WebHookProvider
) : RequestBodyAdviceAdapter() {
	override fun supports(
		methodParameter: MethodParameter,
		targetType: Type,
		converterType: Class<out HttpMessageConverter<*>>
	): Boolean = methodParameter.method
		?.isAnnotationPresent(SendWebHookSignalRequest::class.java)
		?: false

	override fun afterBodyRead(
		body: Any,
		inputMessage: HttpInputMessage,
		parameter: MethodParameter,
		targetType: Type,
		converterType: Class<out HttpMessageConverter<*>>
	): Any {
		webHookProvider.sendAll("Subscription request received from method ${parameter.method?.name}.", mutableListOf("Request Body: $body"))

		return super.afterBodyRead(body, inputMessage, parameter, targetType, converterType)
	}
}
