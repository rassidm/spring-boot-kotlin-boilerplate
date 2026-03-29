package com.example.demo.user.dto.command

data class CreateUserCommand(
	val name: String,
	val email: String,
	val password: String
)
