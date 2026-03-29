package com.example.demo.infrastructure.mail

data class MailPayload(
	val to: String,
	val subject: String,
	val body: String
)
