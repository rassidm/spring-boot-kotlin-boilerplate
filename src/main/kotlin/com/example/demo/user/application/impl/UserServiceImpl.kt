package com.example.demo.user.application.impl

import com.example.demo.user.application.UserService
import com.example.demo.user.entity.User
import com.example.demo.user.exception.UserNotFoundException
import com.example.demo.user.exception.UserUnAuthorizedException
import com.example.demo.user.repository.UserRepository
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserServiceImpl(
	private val userRepository: UserRepository,
	private val bCryptPasswordEncoder: BCryptPasswordEncoder
) : UserService {
	@Transactional(readOnly = true)
	override fun validateReturnUser(userId: Long): User {
		val user: User =
			userRepository
				.findOneById(userId) ?: throw UserNotFoundException(userId)

		return user
	}

	@Transactional(readOnly = true)
	override fun validateAuthReturnUser(
		email: String,
		password: String
	): User {
		val user: User =
			userRepository
				.findOneByEmail(email) ?: throw UserNotFoundException(email)

		user
			.validatePassword(
				password,
				bCryptPasswordEncoder
			).run {
				check(this) { throw UserUnAuthorizedException(email) }
			}

		return user
	}
}
