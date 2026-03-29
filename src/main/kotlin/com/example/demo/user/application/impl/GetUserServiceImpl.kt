package com.example.demo.user.application.impl

import com.example.demo.user.application.GetUserService
import com.example.demo.user.dto.response.GetUserResponse
import com.example.demo.user.entity.User
import com.example.demo.user.exception.UserNotFoundException
import com.example.demo.user.repository.UserRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class GetUserServiceImpl(
	private val userRepository: UserRepository
) : GetUserService {
	override fun getUserById(userId: Long): GetUserResponse {
		val user: User =
			userRepository
				.findOneById(userId) ?: throw UserNotFoundException(userId)

		return user.let(GetUserResponse::from)
	}

	override fun getUserByEmail(email: String): GetUserResponse {
		val user = userRepository.findOneByEmail(email) ?: throw UserNotFoundException(email)

		return user.let(GetUserResponse::from)
	}

	override fun getUserList(pageable: Pageable): Page<GetUserResponse> =
		userRepository
			.findAll(pageable)
			.map(GetUserResponse::from)
}
