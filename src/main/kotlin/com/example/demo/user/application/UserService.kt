package com.example.demo.user.application

import com.example.demo.user.entity.User

interface UserService {
	fun validateReturnUser(userId: Long): User

	fun validateAuthReturnUser(
		email: String,
		password: String
	): User
}
