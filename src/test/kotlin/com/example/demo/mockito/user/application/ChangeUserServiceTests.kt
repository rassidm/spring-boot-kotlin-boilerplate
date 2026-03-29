package com.example.demo.mockito.user.application

import com.example.demo.post.application.impl.ChangePostServiceImpl
import com.example.demo.security.component.provider.TokenProvider
import com.example.demo.user.application.impl.ChangeUserServiceImpl
import com.example.demo.user.application.impl.UserServiceImpl
import com.example.demo.user.constant.UserRole
import com.example.demo.user.dto.command.CreateUserCommand
import com.example.demo.user.dto.command.UpdateUserCommand
import com.example.demo.user.entity.User
import com.example.demo.user.exception.AlreadyUserExistException
import com.example.demo.user.exception.UserNotFoundException
import com.example.demo.user.repository.UserRepository
import org.instancio.Instancio
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.springframework.context.ApplicationEventPublisher
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@Tag("mockito-unit-test")
@DisplayName("Mockito Unit - Post / Put / Delete / Patch User Service Test")
@ExtendWith(MockitoExtension::class)
class ChangeUserServiceTests {
	@Mock
	private lateinit var userRepository: UserRepository

	@Mock
	private lateinit var bCryptPasswordEncoder: BCryptPasswordEncoder

	@Mock
	private lateinit var tokenProvider: TokenProvider

	@Mock
	private lateinit var postServiceImpl: ChangePostServiceImpl

	@Mock
	private lateinit var userServiceImpl: UserServiceImpl

	@Suppress("unused")
	@Mock
	private lateinit var applicationEventPublisher: ApplicationEventPublisher

	@InjectMocks
	private lateinit var changeUserServiceImpl: ChangeUserServiceImpl

	private val defaultUserEncodePassword =
		"$2a$10\$T44NRNpbxkQ9qHbCtqQZ7O3gYfipzC0cHvOIJ/aV4PTlvJjtDl7x2\n" + //
			""
	private val defaultAccessToken =
		"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c\n" + //
			""

	private val user: User = Instancio.create(User::class.java)

	@Nested
	@DisplayName("Delete User Test")
	inner class DeleteTest {
		@Test
		@DisplayName("Success delete user")
		fun `should delete user and related data when given valid user id`() {
			val userId = user.id

			changeUserServiceImpl.deleteUserById(userId)

			inOrder(tokenProvider, postServiceImpl, userRepository) {
				verify(tokenProvider).deleteRefreshToken(userId)
				verify(postServiceImpl).deletePostByUserId(userId)
				verify(userRepository).deleteById(userId)
			}
			verifyNoMoreInteractions(tokenProvider, postServiceImpl, userRepository)
		}
	}

	@Nested
	@DisplayName("Update User Test")
	inner class UpdateTest {
		private val updateUserCommand: UpdateUserCommand =
			Instancio
				.create(UpdateUserCommand::class.java)
				.copy(role = UserRole.USER.name)

		@Test
		@DisplayName("Success update user")
		fun `should return updated user response when update successful`() {
			val userId = user.id
			whenever(userServiceImpl.validateReturnUser(userId)) doReturn user

			val updateUserResponse =
				changeUserServiceImpl.updateUser(
					userId,
					updateUserCommand
				)

			assertNotNull(updateUserResponse)
			assertEquals(user.name, updateUserResponse.name)
			assertEquals(user.role, updateUserResponse.role)

			verify(userServiceImpl).validateReturnUser(userId)
			verifyNoMoreInteractions(userServiceImpl)
		}

		@Test
		@DisplayName("Not found user")
		fun `should throw UserNotFoundException when user not found`() {
			val userId = user.id
			whenever(userServiceImpl.validateReturnUser(userId)) doThrow UserNotFoundException(userId)

			assertThrows<UserNotFoundException> {
				changeUserServiceImpl.updateUser(userId, updateUserCommand)
			}

			verify(userServiceImpl).validateReturnUser(userId)
		}
	}

	@Nested
	@DisplayName("Create User Test")
	inner class RegisterTest {
		private val createUserCommand: CreateUserCommand =
			Instancio.create(CreateUserCommand::class.java)

		@Test
		@DisplayName("Success create user")
		fun `should return created user response when creation successful`() {
			whenever(userRepository.existsByEmail(createUserCommand.email)) doReturn false
			whenever(bCryptPasswordEncoder.encode(createUserCommand.password)) doReturn defaultUserEncodePassword
			whenever(userRepository.save(any<User>())) doReturn user
			whenever(tokenProvider.createFullTokens(user)) doReturn defaultAccessToken

			val createUserResponse = changeUserServiceImpl.createUser(createUserCommand)

			assertNotNull(createUserResponse)
			assertEquals(user.email, createUserResponse.email)
			assertEquals(user.name, createUserResponse.name)

			inOrder(userRepository, bCryptPasswordEncoder, tokenProvider) {
				verify(userRepository).existsByEmail(createUserCommand.email)
				verify(bCryptPasswordEncoder).encode(createUserCommand.password)
				verify(userRepository).save(any())
				verify(tokenProvider).createFullTokens(user)
			}
		}

		@Test
		@DisplayName("Already user exist")
		fun `should throw AlreadyUserExistException when email already exists`() {
			whenever(userRepository.existsByEmail(createUserCommand.email)) doReturn true

			assertThrows<AlreadyUserExistException> {
				changeUserServiceImpl.createUser(createUserCommand)
			}

			verify(userRepository).existsByEmail(createUserCommand.email)
			verifyNoInteractions(bCryptPasswordEncoder, tokenProvider)
			verifyNoMoreInteractions(userRepository)
		}
	}
}
