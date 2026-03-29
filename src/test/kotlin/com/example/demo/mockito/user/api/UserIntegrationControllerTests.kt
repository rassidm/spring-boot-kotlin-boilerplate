package com.example.demo.mockito.user.api

import com.example.demo.mockito.common.BaseIntegrationController
import com.example.demo.mockito.common.security.WithMockCustomUser
import com.example.demo.user.api.UserController
import com.example.demo.user.application.impl.ChangeUserServiceImpl
import com.example.demo.user.application.impl.GetUserServiceImpl
import com.example.demo.user.constant.UserRole
import com.example.demo.user.dto.command.CreateUserCommand
import com.example.demo.user.dto.command.UpdateUserCommand
import com.example.demo.user.dto.request.CreateUserRequest
import com.example.demo.user.dto.request.UpdateUserRequest
import com.example.demo.user.dto.response.CreateUserResponse.Companion.from
import com.example.demo.user.dto.response.GetUserResponse
import com.example.demo.user.dto.response.UpdateMeResponse
import com.example.demo.user.dto.response.UpdateUserResponse
import com.example.demo.user.entity.User
import com.example.demo.user.exception.AlreadyUserExistException
import com.example.demo.user.exception.UserNotFoundException
import org.instancio.Instancio
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders

@ActiveProfiles("test")
@Tag("mockito-integration-test")
@DisplayName("Mockito Integration - User Controller Test")
@WebMvcTest(
	UserController::class
)
@ExtendWith(MockitoExtension::class)
class UserIntegrationControllerTests : BaseIntegrationController() {
	@MockitoBean
	private lateinit var getUserServiceImpl: GetUserServiceImpl

	@MockitoBean
	private lateinit var changeUserServiceImpl: ChangeUserServiceImpl

	private val defaultUserEmail = "user@example.com"
	private val defaultUserPassword = "test_password_123!@"
	private val defaultAccessToken =
		"""
    eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.
    eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c
    """
	private val defaultPageable = Pageable.ofSize(1)

	private val user: User = Instancio.create(User::class.java)

	@BeforeEach
	fun setup() {
		mockMvc =
			MockMvcBuilders
				.webAppContextSetup(webApplicationContext)
				.apply<DefaultMockMvcBuilder>(SecurityMockMvcConfigurers.springSecurity())
				.alwaysDo<DefaultMockMvcBuilder>(MockMvcResultHandlers.print())
				.build()
	}

	@Nested
	@DisplayName("GET /api/v1/users/{userId} Test")
	inner class GetUserByIdTest {
		@Test
		@DisplayName("GET /api/v1/users/{userId} Response")
		@WithMockCustomUser
		@Throws(Exception::class)
		fun should_ExpectOKResponseToGetUserResponse_when_GivenUserIdAndUserIsAuthenticated() {
			Mockito.`when`(getUserServiceImpl.getUserById(any<Long>())).thenReturn(GetUserResponse.from(user))
			mockMvc
				.perform(
					MockMvcRequestBuilders
						.get(
							"/api/v1/users/{userId}",
							user.id
						).with(SecurityMockMvcRequestPostProcessors.csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON)
				).andExpect(
					MockMvcResultMatchers.status().isOk
				).andExpect(
					MockMvcResultMatchers.jsonPath("$.message").value(commonMessage)
				).andExpect(
					MockMvcResultMatchers.jsonPath("$.data.userId").value(user.id)
				).andExpect(
					MockMvcResultMatchers.jsonPath("$.data.email").value(user.email)
				).andExpect(MockMvcResultMatchers.jsonPath("$.data.name").value(user.name))
				.andExpect(MockMvcResultMatchers.jsonPath("$.data.role").value(user.role.name))
		}

		@Test
		@DisplayName("Not Found Exception GET /api/v1/users/{userId} Response")
		@WithMockCustomUser
		@Throws(Exception::class)
		fun should_ExpectErrorResponseToUserNotFoundException_when_GivenUserIdAndUserIsAuthenticated() {
			val userNotFoundException = UserNotFoundException(user.id)
			Mockito.`when`(getUserServiceImpl.getUserById(any<Long>())).thenThrow(userNotFoundException)
			mockMvc
				.perform(
					MockMvcRequestBuilders
						.get(
							"/api/v1/users/{userId}",
							user.id
						).with(SecurityMockMvcRequestPostProcessors.csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON)
				).andExpect(
					MockMvcResultMatchers.status().isNotFound
				).andExpect(MockMvcResultMatchers.jsonPath("$.message").value(userNotFoundException.message))
				.andExpect(MockMvcResultMatchers.jsonPath("$.errors").isEmpty)
		}

		@Test
		@DisplayName("Unauthorized Exception GET /api/v1/users/{userId} Response")
		@Throws(Exception::class)
		fun should_ExpectErrorResponseToUnauthorizedException_when_GivenUserIdAndUserIsNotAuthenticated() {
			mockMvc
				.perform(
					MockMvcRequestBuilders
						.get(
							"/api/v1/users/{userId}",
							user.id
						).with(SecurityMockMvcRequestPostProcessors.csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON)
				).andExpect(MockMvcResultMatchers.status().isUnauthorized)
		}
	}

	@Nested
	@DisplayName("GET /api/v1/users/me Test")
	inner class GetMeTest {
		@Test
		@DisplayName("GET /api/v1/users/me Response")
		@WithMockCustomUser
		@Throws(Exception::class)
		fun should_ExpectOKResponseToGetUserResponse_when_UserIsAuthenticated() {
			Mockito.`when`(getUserServiceImpl.getUserById(any<Long>())).thenReturn(GetUserResponse.from(user))
			mockMvc
				.perform(
					MockMvcRequestBuilders
						.get(
							"/api/v1/users/me"
						).with(SecurityMockMvcRequestPostProcessors.csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON)
				).andExpect(
					MockMvcResultMatchers.status().isOk
				).andExpect(
					MockMvcResultMatchers.jsonPath("$.message").value(commonMessage)
				).andExpect(
					MockMvcResultMatchers.jsonPath("$.data.userId").value(user.id)
				).andExpect(
					MockMvcResultMatchers.jsonPath("$.data.email").value(user.email)
				).andExpect(MockMvcResultMatchers.jsonPath("$.data.name").value(user.name))
				.andExpect(MockMvcResultMatchers.jsonPath("$.data.role").value(user.role.name))
		}

		@Test
		@DisplayName("Not Found Exception GET /api/v1/users/me Response")
		@WithMockCustomUser
		@Throws(Exception::class)
		fun should_ExpectErrorResponseToUserNotFoundException_when_UserIsAuthenticated() {
			val userNotFoundException = UserNotFoundException(user.id)
			Mockito.`when`(getUserServiceImpl.getUserById(any<Long>())).thenThrow(userNotFoundException)
			mockMvc
				.perform(
					MockMvcRequestBuilders
						.get(
							"/api/v1/users/me"
						).with(SecurityMockMvcRequestPostProcessors.csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON)
				).andExpect(
					MockMvcResultMatchers.status().isNotFound
				).andExpect(MockMvcResultMatchers.jsonPath("$.message").value(userNotFoundException.message))
				.andExpect(MockMvcResultMatchers.jsonPath("$.errors").isEmpty)
		}

		@Test
		@DisplayName("Unauthorized Exception GET /api/v1/users/me Response")
		@Throws(Exception::class)
		fun should_ExpectErrorResponseToUnauthorizedException_when_UserIsNotAuthenticated() {
			mockMvc
				.perform(
					MockMvcRequestBuilders
						.get(
							"/api/v1/users/me"
						).with(SecurityMockMvcRequestPostProcessors.csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON)
				).andExpect(MockMvcResultMatchers.status().isUnauthorized)
		}
	}

	@Nested
	@DisplayName("GET /api/v1/users Test")
	inner class GetUserListTest {
		@Test
		@DisplayName("GET /api/v1/users Response")
		@WithMockCustomUser
		@Throws(Exception::class)
		fun should_ExpectOKResponseToPageOfGetUserResponse_when_GivenDefaultPageableAndUserIsAuthenticated() {
			Mockito.`when`(getUserServiceImpl.getUserList(any<Pageable>())).thenReturn(PageImpl(listOf(GetUserResponse.from(user)), defaultPageable, 1))
			mockMvc
				.perform(
					MockMvcRequestBuilders
						.get(
							"/api/v1/users"
						).with(SecurityMockMvcRequestPostProcessors.csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON)
				).andExpect(
					MockMvcResultMatchers.status().isOk
				).andExpect(
					MockMvcResultMatchers.jsonPath("$.message").value(commonMessage)
				).andExpect(
					MockMvcResultMatchers.jsonPath("$.data.content[0].userId").value(user.id)
				).andExpect(
					MockMvcResultMatchers.jsonPath("$.data.content[0].email").value(user.email)
				).andExpect(
					MockMvcResultMatchers.jsonPath("$.data.content[0].name").value(user.name)
				).andExpect(MockMvcResultMatchers.jsonPath("$.data.content[0].role").value(user.role.name))
		}

		@Test
		@DisplayName("Empty GET /api/v1/users Response")
		@WithMockCustomUser
		@Throws(Exception::class)
		fun should_ExpectOKResponseToPageOfGetUserResponseIsEmpty_when_GivenDefaultPageableAndUserIsAuthenticated() {
			Mockito.`when`(getUserServiceImpl.getUserList(any<Pageable>())).thenReturn(PageImpl(listOf(), defaultPageable, 0))
			mockMvc
				.perform(
					MockMvcRequestBuilders
						.get(
							"/api/v1/users"
						).with(SecurityMockMvcRequestPostProcessors.csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON)
				).andExpect(
					MockMvcResultMatchers.status().isOk
				).andExpect(MockMvcResultMatchers.jsonPath("$.message").value(commonMessage))
				.andExpect(MockMvcResultMatchers.jsonPath("$.data.content").isEmpty)
		}

		@Test
		@DisplayName("Unauthorized Exception GET /api/v1/users Response")
		@Throws(Exception::class)
		fun should_ExpectErrorResponseToUnauthorizedException_when_GivenDefaultPageableAndUserIsNotAuthenticated() {
			mockMvc
				.perform(
					MockMvcRequestBuilders
						.get(
							"/api/v1/users"
						).with(SecurityMockMvcRequestPostProcessors.csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON)
				).andExpect(MockMvcResultMatchers.status().isUnauthorized)
		}
	}

	@Nested
	@DisplayName("POST /api/v1/users/register Test")
	inner class CreateUserTest {
		private val mockCreateUserRequest: CreateUserRequest = Instancio.create(CreateUserRequest::class.java)
		private val createUserRequest: CreateUserRequest = mockCreateUserRequest.copy(email = defaultUserEmail, password = defaultUserPassword)

		@Test
		@DisplayName("POST /api/v1/users/register Response")
		@WithMockCustomUser
		@Throws(Exception::class)
		fun should_ExpectOKResponseToCreateUserResponse_when_GivenCreateUserRequest() {
			Mockito.`when`(changeUserServiceImpl.createUser(any<CreateUserCommand>())).thenReturn(from(user, defaultAccessToken))
			mockMvc
				.perform(
					MockMvcRequestBuilders
						.post(
							"/api/v1/users/register"
						).with(
							SecurityMockMvcRequestPostProcessors.csrf()
						).content(objectMapper.writeValueAsString(createUserRequest))
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON)
				).andExpect(
					MockMvcResultMatchers.status().isCreated
				).andExpect(
					MockMvcResultMatchers.jsonPath("$.code").value(commonStatus)
				).andExpect(
					MockMvcResultMatchers.jsonPath("$.message").value(commonMessage)
				).andExpect(
					MockMvcResultMatchers.jsonPath("$.data.userId").value(user.id)
				).andExpect(
					MockMvcResultMatchers.jsonPath("$.data.email").value(user.email)
				).andExpect(
					MockMvcResultMatchers.jsonPath("$.data.name").value(user.name)
				).andExpect(
					MockMvcResultMatchers.jsonPath("$.data.role").value(user.role.name)
				).andExpect(MockMvcResultMatchers.jsonPath("$.data.accessToken").value(defaultAccessToken))
		}

		@Test
		@DisplayName("Field Valid Exception POST /api/v1/users/register Response")
		@WithMockCustomUser
		@Throws(Exception::class)
		fun should_ExpectErrorResponseToValidException_when_GivenWrongCreateUserRequest() {
			val wrongCreateUserRequest = createUserRequest.copy(email = "wrong_email", password = "1234567")
			mockMvc
				.perform(
					MockMvcRequestBuilders
						.post(
							"/api/v1/users/register"
						).with(
							SecurityMockMvcRequestPostProcessors.csrf()
						).content(objectMapper.writeValueAsString(wrongCreateUserRequest))
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON)
				).andExpect(
					MockMvcResultMatchers.status().isBadRequest
				).andExpect(
					MockMvcResultMatchers.jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value())
				).andExpect(MockMvcResultMatchers.jsonPath("$.message").isString)
				.andExpect(MockMvcResultMatchers.jsonPath("$.errors").isNotEmpty)
		}

		@Test
		@DisplayName("Already Exist Exception POST /api/v1/users/register Response")
		@WithMockCustomUser
		@Throws(Exception::class)
		fun should_ExpectErrorResponseToAlreadyUserExistException_when_GivenCreateUserRequest() {
			val alreadyUserExistException = AlreadyUserExistException(createUserRequest.email)
			Mockito.`when`(changeUserServiceImpl.createUser(any<CreateUserCommand>())).thenThrow(alreadyUserExistException)
			mockMvc
				.perform(
					MockMvcRequestBuilders
						.post(
							"/api/v1/users/register"
						).with(
							SecurityMockMvcRequestPostProcessors.csrf()
						).content(objectMapper.writeValueAsString(createUserRequest))
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON)
				).andExpect(
					MockMvcResultMatchers.status().isConflict
				).andExpect(MockMvcResultMatchers.jsonPath("$.message").value(alreadyUserExistException.message))
				.andExpect(MockMvcResultMatchers.jsonPath("$.errors").isEmpty)
		}
	}

	@Nested
	@DisplayName("PATCH /api/v1/users/{userId} Test")
	inner class UpdateUserTest {
		private val updateUserRequest: UpdateUserRequest = Instancio.create(UpdateUserRequest::class.java).copy(role = UserRole.USER.name)

		@Test
		@DisplayName("PATCH /api/v1/users/{userId} Response")
		@WithMockCustomUser
		@Throws(Exception::class)
		fun should_ExpectOKResponseToUpdateUserResponse_when_GivenUserIdAndUpdateUserRequestAndUserIsAuthenticated() {
			Mockito.`when`(changeUserServiceImpl.updateUser(any<Long>(), any<UpdateUserCommand>())).thenReturn(UpdateUserResponse.from(user))
			mockMvc
				.perform(
					MockMvcRequestBuilders
						.patch(
							"/api/v1/users/{userId}",
							user.id
						).with(
							SecurityMockMvcRequestPostProcessors.csrf()
						).content(objectMapper.writeValueAsString(updateUserRequest))
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON)
				).andExpect(
					MockMvcResultMatchers.status().isOk
				).andExpect(
					MockMvcResultMatchers.jsonPath("$.code").value(commonStatus)
				).andExpect(
					MockMvcResultMatchers.jsonPath("$.message").value(commonMessage)
				).andExpect(
					MockMvcResultMatchers.jsonPath("$.data.userId").value(user.id)
				).andExpect(
					MockMvcResultMatchers.jsonPath("$.data.email").value(user.email)
				).andExpect(MockMvcResultMatchers.jsonPath("$.data.name").value(user.name))
				.andExpect(MockMvcResultMatchers.jsonPath("$.data.role").value(user.role.name))
		}

		@Test
		@DisplayName("Field Valid Exception PATCH /api/v1/users/{userId} Response")
		@WithMockCustomUser
		@Throws(Exception::class)
		fun should_ExpectErrorResponseToValidException_when_GivenUserIdAndWrongUpdateUserRequestAndUserIsAuthenticated() {
			val wrongUpdateUserRequest = updateUserRequest.copy(name = "")
			mockMvc
				.perform(
					MockMvcRequestBuilders
						.patch(
							"/api/v1/users/{userId}",
							user.id
						).with(
							SecurityMockMvcRequestPostProcessors.csrf()
						).content(objectMapper.writeValueAsString(wrongUpdateUserRequest))
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON)
				).andExpect(
					MockMvcResultMatchers.status().isBadRequest
				).andExpect(MockMvcResultMatchers.jsonPath("$.message").isString)
				.andExpect(MockMvcResultMatchers.jsonPath("$.errors").isNotEmpty)
		}

		@Test
		@DisplayName("Unauthorized Exception PATCH /api/v1/users/{userId} Response")
		@Throws(Exception::class)
		fun should_ExpectErrorResponseToUnauthorizedException_when_GivenUserIdAndUpdateUserRequestAndUserIsNotAuthenticated() {
			mockMvc
				.perform(
					MockMvcRequestBuilders
						.patch(
							"/api/v1/users/{userId}",
							user.id
						).with(
							SecurityMockMvcRequestPostProcessors.csrf()
						).content(objectMapper.writeValueAsString(updateUserRequest))
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON)
				).andExpect(MockMvcResultMatchers.status().isUnauthorized)
		}

		@Test
		@DisplayName("Not Found Exception PATCH /api/v1/users/{userId} Response")
		@WithMockCustomUser
		@Throws(Exception::class)
		fun should_ExpectErrorResponseToUserNotFoundException_when_GivenUserIdAndUpdateUserRequestAndUserIsAuthenticated() {
			val userNotFoundException = UserNotFoundException(user.id)
			Mockito.`when`(changeUserServiceImpl.updateUser(any<Long>(), any<UpdateUserCommand>())).thenThrow(userNotFoundException)
			mockMvc
				.perform(
					MockMvcRequestBuilders
						.patch(
							"/api/v1/users/{userId}",
							user.id
						).with(
							SecurityMockMvcRequestPostProcessors.csrf()
						).content(objectMapper.writeValueAsString(updateUserRequest))
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON)
				).andExpect(
					MockMvcResultMatchers.status().isNotFound
				).andExpect(MockMvcResultMatchers.jsonPath("$.message").value(userNotFoundException.message))
				.andExpect(MockMvcResultMatchers.jsonPath("$.errors").isEmpty)
		}
	}

	@Nested
	@DisplayName("PATCH /api/v1/users Test")
	inner class UpdateMeTest {
		private val updateUserRequest: UpdateUserRequest = Instancio.create(UpdateUserRequest::class.java).copy(role = UserRole.USER.name)

		@Test
		@DisplayName("PATCH /api/v1/users Response")
		@WithMockCustomUser
		@Throws(Exception::class)
		fun should_ExpectOKResponseToUpdateMeResponse_when_GivenSecurityUserItemAndUpdateUserRequestAndUserIsAuthenticated() {
			Mockito.`when`(changeUserServiceImpl.updateMe(any<Long>(), any<UpdateUserCommand>())).thenReturn(UpdateMeResponse.from(user, defaultAccessToken))
			mockMvc
				.perform(
					MockMvcRequestBuilders
						.patch(
							"/api/v1/users"
						).with(
							SecurityMockMvcRequestPostProcessors.csrf()
						).content(objectMapper.writeValueAsString(updateUserRequest))
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON)
				).andExpect(
					MockMvcResultMatchers.status().isOk
				).andExpect(
					MockMvcResultMatchers.jsonPath("$.code").value(commonStatus)
				).andExpect(
					MockMvcResultMatchers.jsonPath("$.message").value(commonMessage)
				).andExpect(
					MockMvcResultMatchers.jsonPath("$.data.userId").value(user.id)
				).andExpect(
					MockMvcResultMatchers.jsonPath("$.data.email").value(user.email)
				).andExpect(MockMvcResultMatchers.jsonPath("$.data.name").value(user.name))
				.andExpect(MockMvcResultMatchers.jsonPath("$.data.role").value(user.role.name))
		}

		@Test
		@DisplayName("Field Valid Exception PATCH /api/v1/users Response")
		@WithMockCustomUser
		@Throws(Exception::class)
		fun should_ExpectErrorResponseToValidException_when_GivenSecurityUserItemAndWrongUpdateUserRequestAndUserIsAuthenticated() {
			val wrongUpdateUserRequest = updateUserRequest.copy(name = "")
			mockMvc
				.perform(
					MockMvcRequestBuilders
						.patch(
							"/api/v1/users"
						).with(
							SecurityMockMvcRequestPostProcessors.csrf()
						).content(objectMapper.writeValueAsString(wrongUpdateUserRequest))
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON)
				).andExpect(
					MockMvcResultMatchers.status().isBadRequest
				).andExpect(
					MockMvcResultMatchers.jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value())
				).andExpect(MockMvcResultMatchers.jsonPath("$.message").isString)
				.andExpect(MockMvcResultMatchers.jsonPath("$.errors").isNotEmpty)
		}

		@Test
		@DisplayName("Unauthorized Exception PATCH /api/v1/users Response")
		@Throws(Exception::class)
		fun should_ExpectErrorResponseToUnauthorizedException_when_GivenUpdateUserRequestAndUserIsNotAuthenticated() {
			mockMvc
				.perform(
					MockMvcRequestBuilders
						.patch(
							"/api/v1/users"
						).with(
							SecurityMockMvcRequestPostProcessors.csrf()
						).content(objectMapper.writeValueAsString(updateUserRequest))
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON)
				).andExpect(MockMvcResultMatchers.status().isUnauthorized)
		}

		@Test
		@DisplayName("Not Found Exception PATCH /api/v1/users Response")
		@WithMockCustomUser
		@Throws(Exception::class)
		fun should_ExpectErrorResponseToUserNotFoundException_when_GivenSecurityUserItemAndUpdateUserRequestAndUserIsAuthenticated() {
			val userNotFoundException = UserNotFoundException(user.id)
			Mockito.`when`(changeUserServiceImpl.updateMe(any<Long>(), any<UpdateUserCommand>())).thenThrow(userNotFoundException)
			mockMvc
				.perform(
					MockMvcRequestBuilders
						.patch(
							"/api/v1/users"
						).with(
							SecurityMockMvcRequestPostProcessors.csrf()
						).content(objectMapper.writeValueAsString(updateUserRequest))
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON)
				).andExpect(
					MockMvcResultMatchers.status().isNotFound
				).andExpect(MockMvcResultMatchers.jsonPath("$.message").value(userNotFoundException.message))
				.andExpect(MockMvcResultMatchers.jsonPath("$.errors").isEmpty)
		}
	}

	@Nested
	@DisplayName("DELETE /api/v1/users/{userId} Test")
	inner class DeleteUserTest {
		@Test
		@DisplayName("DELETE /api/v1/users/{userId} Response")
		@WithMockCustomUser
		@Throws(Exception::class)
		fun should_ExpectOKResponse_when_GivenUserIdAndUserIsAuthenticated() {
			mockMvc
				.perform(
					MockMvcRequestBuilders
						.delete(
							"/api/v1/users/{userId}",
							user.id
						).with(SecurityMockMvcRequestPostProcessors.csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON)
				).andExpect(MockMvcResultMatchers.status().isNoContent)
				.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(commonMessage))
		}

		@Test
		@DisplayName("Unauthorized Error DELETE /api/v1/users/{userId} Response")
		@Throws(Exception::class)
		fun should_ExpectErrorResponseToUnauthorizedException_when_GivenUserIdAndUserIsNotAuthenticated() {
			mockMvc
				.perform(
					MockMvcRequestBuilders
						.delete(
							"/api/v1/users/{userId}",
							user.id
						).with(SecurityMockMvcRequestPostProcessors.csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON)
				).andExpect(MockMvcResultMatchers.status().isUnauthorized)
		}
	}
}
