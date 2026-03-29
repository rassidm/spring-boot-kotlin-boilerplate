package com.example.demo.mockito.user.application

import com.example.demo.user.application.impl.UserServiceImpl
import com.example.demo.user.entity.User
import com.example.demo.user.exception.UserNotFoundException
import com.example.demo.user.exception.UserUnAuthorizedException
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
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@Tag("mockito-unit-test")
@DisplayName("Mockito Unit - User Service Test")
@ExtendWith(MockitoExtension::class)
class UserServiceTests {
	@InjectMocks
	private lateinit var userServiceImpl: UserServiceImpl

	@Mock
	private lateinit var userRepository: UserRepository

	@Mock
	private lateinit var bCryptPasswordEncoder: BCryptPasswordEncoder

	private val user: User = Instancio.create(User::class.java)

	@Nested
	@DisplayName("Validate And Return User Entity Test")
	inner class ValidateReturnUserTest {
		@Test
		@DisplayName("Success validate and get user entity")
		fun `should return user entity when given valid user id`() {
			val userId = user.id
			whenever(userRepository.findOneById(userId)) doReturn user

			val validateUser = userServiceImpl.validateReturnUser(userId)

			assertNotNull(validateUser)
			assertEquals(user.id, validateUser.id)
			assertEquals(user.email, validateUser.email)
			assertEquals(user.name, validateUser.name)
			assertEquals(user.role, validateUser.role)

			verify(userRepository).findOneById(userId)
			verifyNoMoreInteractions(userRepository)
		}

		@Test
		@DisplayName("validate and user entity is not found exception")
		fun `should throw UserNotFoundException when user not found`() {
			val userId = user.id
			whenever(userRepository.findOneById(userId)) doReturn null

			assertThrows<UserNotFoundException> {
				userServiceImpl.validateReturnUser(userId)
			}

			verify(userRepository).findOneById(userId)
		}
	}

	@Nested
	@DisplayName("Validate and authenticated Return User Entity")
	inner class ValidateAuthReturnUserTest {
		private val testEmail = "test@example.com"
		private val testPassword = "password123"

		@Test
		@DisplayName("Success validate and authenticated get user entity")
		fun `should return user entity when authentication successful`() {
			whenever(userRepository.findOneByEmail(testEmail)) doReturn user
			whenever(
				user.validatePassword(
					testPassword,
					bCryptPasswordEncoder
				)
			) doReturn true

			val validateAuthUser = userServiceImpl.validateAuthReturnUser(testEmail, testPassword)

			assertNotNull(validateAuthUser)
			assertEquals(user.id, validateAuthUser.id)
			assertEquals(user.email, validateAuthUser.email)
			assertEquals(user.name, validateAuthUser.name)
			assertEquals(user.role, validateAuthUser.role)

			verify(userRepository).findOneByEmail(testEmail)
			verifyNoMoreInteractions(userRepository)
		}

		@Test
		@DisplayName("validate and authenticated user is not found exception")
		fun `should throw UserNotFoundException when user not found by email`() {
			whenever(userRepository.findOneByEmail(testEmail)) doReturn null

			assertThrows<UserNotFoundException> {
				userServiceImpl.validateAuthReturnUser(testEmail, testPassword)
			}

			verify(userRepository).findOneByEmail(testEmail)
			verifyNoMoreInteractions(userRepository, bCryptPasswordEncoder)
		}

		@Test
		@DisplayName("validate and authenticated user is unauthorized exception")
		fun `should throw UserUnAuthorizedException when password validation fails`() {
			whenever(userRepository.findOneByEmail(testEmail)) doReturn user
			whenever(
				user.validatePassword(
					testPassword,
					bCryptPasswordEncoder
				)
			) doReturn false

			assertThrows<UserUnAuthorizedException> {
				userServiceImpl.validateAuthReturnUser(testEmail, testPassword)
			}

			verify(userRepository).findOneByEmail(testEmail)
		}
	}
}
