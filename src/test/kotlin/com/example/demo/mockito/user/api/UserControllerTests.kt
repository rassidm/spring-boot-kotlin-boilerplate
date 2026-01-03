package com.example.demo.mockito.user.api

import com.example.demo.security.SecurityUserItem
import com.example.demo.user.api.UserController
import com.example.demo.user.application.impl.ChangeUserServiceImpl
import com.example.demo.user.application.impl.GetUserServiceImpl
import com.example.demo.user.dto.serve.request.CreateUserRequest
import com.example.demo.user.dto.serve.request.UpdateUserRequest
import com.example.demo.user.dto.serve.response.CreateUserResponse.Companion.from
import com.example.demo.user.dto.serve.response.GetUserResponse
import com.example.demo.user.dto.serve.response.UpdateMeResponse
import com.example.demo.user.dto.serve.response.UpdateUserResponse
import com.example.demo.user.entity.User
import org.assertj.core.api.Assertions.assertThat
import org.instancio.Instancio
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@Tag("mockito-unit-test")
@DisplayName("Mockito Unit - User Controller Test")
@ExtendWith(MockitoExtension::class)
class UserControllerTests {
	@InjectMocks
	private lateinit var userController: UserController

	@Mock
	private lateinit var getUserServiceImpl: GetUserServiceImpl

	@Mock
	private lateinit var changeUserServiceImpl: ChangeUserServiceImpl

	private val defaultAccessToken =
		"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c\n" + //
			""
	private val defaultPageable = Pageable.ofSize(1)

	private val user: User = Instancio.create(User::class.java)

	@Test
	@DisplayName("Get user by id")
	fun `should return user response when getting user by id`() {
		val userId = user.id
		val expectedResponse = GetUserResponse.from(user)
		whenever(getUserServiceImpl.getUserById(userId)) doReturn expectedResponse

		val response = userController.getUserById(userId)

		assertNotNull(response)
		assertNotNull(response.body)
		assertEquals(HttpStatus.OK, response.statusCode)

		response.body?.let { body ->
			assertEquals(user.id, body.userId)
			assertEquals(user.email, body.email)
			assertEquals(user.name, body.name)
			assertEquals(user.role, body.role)
		}

		verify(getUserServiceImpl).getUserById(userId)
		verifyNoMoreInteractions(getUserServiceImpl)
	}

	@Test
	@DisplayName("Get me")
	fun `should return current user response when getting me`() {
		val securityUserItem = Instancio.create(SecurityUserItem::class.java)
		val expectedResponse = GetUserResponse.from(user)
		whenever(getUserServiceImpl.getUserById(securityUserItem.userId)) doReturn expectedResponse

		val response = userController.getMe(securityUserItem)

		assertNotNull(response)
		assertNotNull(response.body)
		assertEquals(HttpStatus.OK, response.statusCode)

		response.body?.let { body ->
			assertEquals(user.id, body.userId)
			assertEquals(user.email, body.email)
			assertEquals(user.name, body.name)
			assertEquals(user.role, body.role)
		}

		verify(getUserServiceImpl).getUserById(securityUserItem.userId)
		verifyNoMoreInteractions(getUserServiceImpl)
	}

	@Test
	@DisplayName("Get user list")
	fun `should return page of users when getting user list`() {
		val userResponse = GetUserResponse.from(user)
		val expectedPage = PageImpl(listOf(userResponse), defaultPageable, 1)
		whenever(getUserServiceImpl.getUserList(defaultPageable)) doReturn expectedPage

		val response = userController.getUserList(defaultPageable)

		assertNotNull(response)
		assertNotNull(response.body)
		assertEquals(HttpStatus.OK, response.statusCode)

		response.body?.let { body ->
			assertThat(body).isNotEmpty()
			assertEquals(user.id, body.content[0].userId)
			assertEquals(user.email, body.content[0].email)
			assertEquals(user.name, body.content[0].name)
			assertEquals(user.role, body.content[0].role)
		}

		verify(getUserServiceImpl).getUserList(defaultPageable)
		verifyNoMoreInteractions(getUserServiceImpl)
	}

	@Test
	@DisplayName("Create user")
	fun `should return created user response when creating user`() {
		val createUserRequest = Instancio.create(CreateUserRequest::class.java)
		val expectedResponse = from(user, defaultAccessToken)
		whenever(changeUserServiceImpl.createUser(createUserRequest)) doReturn expectedResponse

		val response = userController.createUser(createUserRequest)

		assertNotNull(response)
		assertNotNull(response.body)
		assertEquals(HttpStatus.CREATED, response.statusCode)

		response.body?.let { body ->
			assertEquals(user.email, body.email)
			assertEquals(user.name, body.name)
			assertEquals(defaultAccessToken, body.accessToken)
		}

		verify(changeUserServiceImpl).createUser(createUserRequest)
		verifyNoMoreInteractions(changeUserServiceImpl)
	}

	@Test
	@DisplayName("Update user")
	fun `should return updated user response when updating user`() {
		val userId = user.id
		val updateUserRequest = Instancio.create(UpdateUserRequest::class.java)
		val expectedResponse = UpdateUserResponse.from(user)
		whenever(changeUserServiceImpl.updateUser(userId, updateUserRequest)) doReturn expectedResponse

		val response = userController.updateUser(updateUserRequest, userId)

		assertNotNull(response)
		assertNotNull(response.body)
		assertEquals(HttpStatus.OK, response.statusCode)

		response.body?.let { body ->
			assertEquals(user.email, body.email)
			assertEquals(user.name, body.name)
			assertEquals(user.role, body.role)
		}

		verify(changeUserServiceImpl).updateUser(userId, updateUserRequest)
		verifyNoMoreInteractions(changeUserServiceImpl)
	}

	@Test
	@DisplayName("Update me")
	fun `should return updated me response when updating current user`() {
		val updateUserRequest = Instancio.create(UpdateUserRequest::class.java)
		val securityUserItem = Instancio.create(SecurityUserItem::class.java)
		val expectedResponse = UpdateMeResponse.from(user, defaultAccessToken)
		whenever(changeUserServiceImpl.updateMe(securityUserItem.userId, updateUserRequest)) doReturn expectedResponse

		val response = userController.updateMe(updateUserRequest, securityUserItem)

		assertNotNull(response)
		assertNotNull(response.body)
		assertEquals(HttpStatus.OK, response.statusCode)

		response.body?.let { body ->
			assertEquals(user.email, body.email)
			assertEquals(user.name, body.name)
			assertEquals(user.role, body.role)
			assertEquals(defaultAccessToken, body.accessToken)
		}

		verify(changeUserServiceImpl).updateMe(securityUserItem.userId, updateUserRequest)
		verifyNoMoreInteractions(changeUserServiceImpl)
	}

	@Test
	@DisplayName("Delete user")
	fun `should return no content when deleting user`() {
		val userId = user.id
		doNothing().whenever(changeUserServiceImpl).deleteUserById(userId)

		val response = userController.deleteUser(userId)

		assertNotNull(response)
		assertNull(response.body)
		assertEquals(HttpStatus.NO_CONTENT, response.statusCode)

		verify(changeUserServiceImpl).deleteUserById(userId)
		verifyNoMoreInteractions(changeUserServiceImpl)
	}
}
