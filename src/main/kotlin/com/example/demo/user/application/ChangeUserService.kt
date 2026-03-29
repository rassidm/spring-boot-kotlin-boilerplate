package com.example.demo.user.application

import com.example.demo.user.dto.command.CreateUserCommand
import com.example.demo.user.dto.command.UpdateUserCommand
import com.example.demo.user.dto.response.CreateUserResponse
import com.example.demo.user.dto.response.UpdateMeResponse
import com.example.demo.user.dto.response.UpdateUserResponse

interface ChangeUserService {
	fun updateUser(
		userId: Long,
		command: UpdateUserCommand
	): UpdateUserResponse

	fun updateMe(
		userId: Long,
		command: UpdateUserCommand
	): UpdateMeResponse

	fun createUser(command: CreateUserCommand): CreateUserResponse

	fun deleteUserById(userId: Long)

	fun hardDeleteUserById(userId: Long)
}
