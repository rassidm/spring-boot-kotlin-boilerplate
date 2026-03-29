package com.example.demo.mockito.user.repository

import com.example.demo.common.config.JpaAuditConfig
import com.example.demo.common.config.QueryDslConfig
import com.example.demo.user.constant.UserRole
import com.example.demo.user.dto.request.UpdateUserRequest
import com.example.demo.user.entity.User
import com.example.demo.user.repository.UserRepository
import org.instancio.Instancio
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@Tag("mockito-unit-test")
@DisplayName("Mockito Unit - User Repository Test")
@Import(value = [QueryDslConfig::class, JpaAuditConfig::class])
@DataJpaTest
class UserRepositoryTests(
	@Autowired private val userRepository: UserRepository
) {
	private val defaultUserEmail = "user@example.com"
	private val defaultUserPassword = "test_password_123!@"
	private val defaultUserName = "username"
	private val defaultUserRole = UserRole.USER

	private lateinit var userEntity: User

	@BeforeEach
	@Throws(Exception::class)
	fun setUp() {
		userEntity =
			User(
				defaultUserEmail,
				defaultUserName,
				defaultUserPassword,
				defaultUserRole
			)
	}

	@Test
	@DisplayName("Create user")
	fun should_AssertCreatedUserEntity_when_GivenUserEntity() {
		val createUser = userRepository.save(userEntity)

		assertEquals(createUser.id, userEntity.id)
		assertEquals(createUser.email, userEntity.email)
		assertEquals(createUser.name, userEntity.name)
		assertEquals(createUser.role, userEntity.role)
	}

	@Test
	@DisplayName("Update user")
	fun should_AssertUpdatedUserEntity_when_GivenUserIdAndUpdateUserRequest() {
		val updateUserRequest =
			Instancio
				.create(UpdateUserRequest::class.java)
				.copy(role = UserRole.USER.name)

		val beforeUpdateUser = userRepository.save(userEntity)
		val userRole = UserRole.valueOf(updateUserRequest.role)

		userRepository.save(
			beforeUpdateUser.update(
				updateUserRequest.name,
				userRole
			)
		)

		val afterUpdateUser: User =
			requireNotNull(
				userRepository
					.findOneById(beforeUpdateUser.id)
			) {
				"User must not be null"
			}

		assertEquals(afterUpdateUser.name, updateUserRequest.name)
		assertEquals(afterUpdateUser.role, userRole)
	}

	@Test
	@DisplayName("Delete user")
	fun should_AssertDeletedUserEntity_when_GivenUserId() {
		val beforeDeleteUser = userRepository.save(userEntity)

		userRepository.deleteById(beforeDeleteUser.id)

		val afterDeleteUser: User? =
			userRepository
				.findOneById(beforeDeleteUser.id)

		assertNull(afterDeleteUser)
	}

	@Test
	@DisplayName("Find user by id")
	fun should_AssertFindUserEntity_when_GivenUserId() {
		val beforeFindUser = userRepository.save(userEntity)

		val afterFindUser: User =
			requireNotNull(
				userRepository
					.findOneById(beforeFindUser.id)
			) {
				"User must not be null"
			}

		assertEquals(beforeFindUser.id, afterFindUser.id)
		assertEquals(beforeFindUser.email, afterFindUser.email)
		assertEquals(beforeFindUser.name, afterFindUser.name)
		assertEquals(beforeFindUser.role, afterFindUser.role)
	}

	@Test
	@DisplayName("Find user by email")
	fun should_AssertFindUserEntity_when_GivenUserEmail() {
		val beforeFindUser = userRepository.save(userEntity)

		val afterFindUser: User =
			requireNotNull(
				userRepository
					.findOneByEmail(beforeFindUser.email)
			) {
				"User must not be null"
			}

		assertEquals(beforeFindUser.id, afterFindUser.id)
		assertEquals(beforeFindUser.email, afterFindUser.email)
		assertEquals(beforeFindUser.name, afterFindUser.name)
		assertEquals(beforeFindUser.role, afterFindUser.role)
	}
}
