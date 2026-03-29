package com.example.demo.user.application.impl

import com.example.demo.post.application.ChangePostService
import com.example.demo.security.component.provider.TokenProvider
import com.example.demo.user.application.ChangeUserService
import com.example.demo.user.application.UserService
import com.example.demo.user.constant.UserRole
import com.example.demo.user.dto.command.CreateUserCommand
import com.example.demo.user.dto.command.UpdateUserCommand
import com.example.demo.user.dto.response.CreateUserResponse
import com.example.demo.user.dto.response.UpdateMeResponse
import com.example.demo.user.dto.response.UpdateUserResponse
import com.example.demo.user.entity.User
import com.example.demo.user.event.UserEvent
import com.example.demo.user.exception.AlreadyUserExistException
import com.example.demo.user.repository.UserRepository
import org.springframework.context.ApplicationEventPublisher
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class ChangeUserServiceImpl(
	private val tokenProvider: TokenProvider,
	private val userService: UserService,
	private val changePostService: ChangePostService,
	private val userRepository: UserRepository,
	private val bCryptPasswordEncoder: BCryptPasswordEncoder,
	private val applicationEventPublisher: ApplicationEventPublisher
) : ChangeUserService {
	override fun createUser(command: CreateUserCommand): CreateUserResponse {
		userRepository
			.existsByEmail(command.email)
			.run {
				if (this) throw AlreadyUserExistException(command.email)
			}

		val user: User =
			userRepository.save(
				User(
					name = command.name,
					email = command.email,
					password = command.password,
					role = UserRole.USER
				).encodePassword(bCryptPasswordEncoder)
			)

		applicationEventPublisher.publishEvent(UserEvent.WelcomeSignUpEvent.from(user))

		return CreateUserResponse.from(
			user,
			tokenProvider.createFullTokens(user)
		)
	}

	override fun updateUser(
		userId: Long,
		command: UpdateUserCommand
	): UpdateUserResponse {
		val user: User =
			userService
				.validateReturnUser(userId)
				.update(name = command.name, role = UserRole.valueOf(command.role))

		return user.let(UpdateUserResponse::from)
	}

	override fun updateMe(
		userId: Long,
		command: UpdateUserCommand
	): UpdateMeResponse {
		val user: User =
			userService
				.validateReturnUser(userId)
				.update(name = command.name, role = UserRole.valueOf(command.role))

		return UpdateMeResponse.from(user, tokenProvider.createFullTokens(user))
	}

	override fun deleteUserById(userId: Long) {
		tokenProvider.deleteRefreshToken(userId)
		changePostService.deletePostByUserId(userId)
		userRepository.deleteById(userId)
	}

	override fun hardDeleteUserById(userId: Long) {
		userRepository.hardDeleteById(userId)
	}
}
