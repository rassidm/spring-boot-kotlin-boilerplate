package com.example.demo.kotest.user.api

import com.example.demo.kotest.common.BaseIntegrationController
import com.example.demo.kotest.common.security.SecurityListenerFactory
import com.example.demo.user.api.UserController
import com.example.demo.user.application.ChangeUserService
import com.example.demo.user.application.GetUserService
import com.example.demo.user.constant.UserRole
import com.example.demo.user.dto.serve.request.CreateUserRequest
import com.example.demo.user.dto.serve.request.UpdateUserRequest
import com.example.demo.user.dto.serve.response.CreateUserResponse
import com.example.demo.user.dto.serve.response.GetUserResponse
import com.example.demo.user.dto.serve.response.UpdateMeResponse
import com.example.demo.user.dto.serve.response.UpdateUserResponse
import com.example.demo.user.entity.User
import com.example.demo.user.exception.AlreadyUserExistException
import com.example.demo.user.exception.UserNotFoundException
import com.ninjasquad.springmockk.MockkBean
import io.kotest.core.annotation.Tags
import io.mockk.every
import io.mockk.justRun
import org.instancio.Instancio
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

@ActiveProfiles("test")
@Tags("kotest-integration-test")
@WebMvcTest(
	UserController::class
)
class UserIntegrationControllerTests : BaseIntegrationController() {
	@MockkBean
	private lateinit var getUserService: GetUserService

	@MockkBean
	private lateinit var changeUserService: ChangeUserService

	private val user: User = Instancio.create(User::class.java)
	private val defaultUserEmail = "user@example.com"
	private val defaultUserPassword = "test_password_123!@"
	private val defaultAccessToken =
		"""
    eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.
    eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c
    """
	private val defaultPageable = Pageable.ofSize(1)

	init {
		initialize()

		Given("GET /api/v1/users/{userId}") {

			When("Success GET /api/v1/users/{userId}") {

				every { getUserService.getUserById(any<Long>()) } returns GetUserResponse.from(user)

				Then("Call GET /api/v1/users/{userId}") {
					mockMvc
						.perform(
							MockMvcRequestBuilders
								.get("/api/v1/users/{userId}", user.id)
								.with(SecurityMockMvcRequestPostProcessors.csrf())
								.contentType(MediaType.APPLICATION_JSON)
								.accept(MediaType.APPLICATION_JSON)
						).andExpect(MockMvcResultMatchers.status().isOk)
						.andExpect(MockMvcResultMatchers.jsonPath("$.code").value(commonStatus))
						.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(commonMessage))
						.andExpect(MockMvcResultMatchers.jsonPath("$.data.userId").value(user.id))
						.andExpect(MockMvcResultMatchers.jsonPath("$.data.email").value(user.email))
						.andExpect(MockMvcResultMatchers.jsonPath("$.data.name").value(user.name))
						.andExpect(MockMvcResultMatchers.jsonPath("$.data.role").value(user.role.name))
				}
			}

			When("Not Found Exception GET /api/v1/users/{userId}") {
				val userNotFoundException = UserNotFoundException(user.id)

				every { getUserService.getUserById(any<Long>()) } throws userNotFoundException

				Then("Call GET /api/v1/users/{userId}") {
					mockMvc
						.perform(
							MockMvcRequestBuilders
								.get("/api/v1/users/{userId}", user.id)
								.with(SecurityMockMvcRequestPostProcessors.csrf())
								.contentType(MediaType.APPLICATION_JSON)
								.accept(MediaType.APPLICATION_JSON)
						).andExpect(MockMvcResultMatchers.status().isNotFound)
						.andExpect(MockMvcResultMatchers.jsonPath("$.code").value(HttpStatus.NOT_FOUND.value()))
						.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(userNotFoundException.message))
						.andExpect(MockMvcResultMatchers.jsonPath("$.errors").isEmpty)
				}
			}
		}

		Given("GET /api/v1/users") {

			When("Success GET /api/v1/users") {

				every { getUserService.getUserList(any<Pageable>()) } returns
					PageImpl(
						listOf(
							GetUserResponse.from(user)
						),
						defaultPageable,
						1
					)

				Then("Call GET /api/v1/users") {
					mockMvc
						.perform(
							MockMvcRequestBuilders
								.get("/api/v1/users")
								.with(SecurityMockMvcRequestPostProcessors.csrf())
								.contentType(MediaType.APPLICATION_JSON)
								.accept(MediaType.APPLICATION_JSON)
						).andExpect(MockMvcResultMatchers.status().isOk)
						.andExpect(MockMvcResultMatchers.jsonPath("$.code").value(commonStatus))
						.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(commonMessage))
						.andExpect(MockMvcResultMatchers.jsonPath("$.data.content[0].userId").value(user.id))
						.andExpect(MockMvcResultMatchers.jsonPath("$.data.content[0].email").value(user.email))
						.andExpect(MockMvcResultMatchers.jsonPath("$.data.content[0].name").value(user.name))
						.andExpect(MockMvcResultMatchers.jsonPath("$.data.content[0].role").value(user.role.name))
				}
			}

			When("Empty GET /api/v1/users") {

				every { getUserService.getUserList(any<Pageable>()) } returns
					PageImpl(
						listOf(),
						defaultPageable,
						0
					)

				Then("Call GET /api/v1/users") {
					mockMvc
						.perform(
							MockMvcRequestBuilders
								.get("/api/v1/users")
								.with(SecurityMockMvcRequestPostProcessors.csrf())
								.contentType(MediaType.APPLICATION_JSON)
								.accept(MediaType.APPLICATION_JSON)
						).andExpect(MockMvcResultMatchers.status().isOk)
						.andExpect(MockMvcResultMatchers.jsonPath("$.code").value(commonStatus))
						.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(commonMessage))
						.andExpect(MockMvcResultMatchers.jsonPath("$.data.content").isEmpty)
				}
			}
		}

		Given("POST /api/v1/users/register") {
			val mockCreateUserRequest: CreateUserRequest =
				Instancio.create(
					CreateUserRequest::class.java
				)

			val createUserRequest: CreateUserRequest =
				mockCreateUserRequest.copy(
					email = defaultUserEmail,
					password = defaultUserPassword
				)

			When("Success POST /api/v1/users/register") {

				every { changeUserService.createUser(any<CreateUserRequest>()) } returns
					CreateUserResponse.from(
						user,
						defaultAccessToken
					)

				Then("Call POST /api/v1/users/register") {
					mockMvc
						.perform(
							MockMvcRequestBuilders
								.post("/api/v1/users/register")
								.with(SecurityMockMvcRequestPostProcessors.csrf())
								.content(objectMapper.writeValueAsString(createUserRequest))
								.contentType(MediaType.APPLICATION_JSON)
								.accept(MediaType.APPLICATION_JSON)
						).andExpect(MockMvcResultMatchers.status().isCreated)
						.andExpect(MockMvcResultMatchers.jsonPath("$.code").value(commonStatus))
						.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(commonMessage))
						.andExpect(MockMvcResultMatchers.jsonPath("$.data.userId").value(user.id))
						.andExpect(MockMvcResultMatchers.jsonPath("$.data.email").value(user.email))
						.andExpect(MockMvcResultMatchers.jsonPath("$.data.name").value(user.name))
						.andExpect(MockMvcResultMatchers.jsonPath("$.data.role").value(user.role.name))
						.andExpect(MockMvcResultMatchers.jsonPath("$.data.accessToken").value(defaultAccessToken))
				}
			}

			When("Field Valid Exception POST /api/v1/users/register") {
				val wrongCreateUserRequest =
					createUserRequest.copy(
						email = "wrong_email",
						password = "1234567"
					)

				Then("Call POST /api/v1/users/register") {
					mockMvc
						.perform(
							MockMvcRequestBuilders
								.post("/api/v1/users/register")
								.with(SecurityMockMvcRequestPostProcessors.csrf())
								.content(objectMapper.writeValueAsString(wrongCreateUserRequest))
								.contentType(MediaType.APPLICATION_JSON)
								.accept(MediaType.APPLICATION_JSON)
						).andExpect(MockMvcResultMatchers.status().isBadRequest)
						.andExpect(
							MockMvcResultMatchers.jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value())
						)
						// email:field email is not email format, password:field password is min size 8 and max size 20,
						.andExpect(MockMvcResultMatchers.jsonPath("$.message").isString)
						.andExpect(MockMvcResultMatchers.jsonPath("$.errors").isNotEmpty)
				}
			}

			When("Already Exist Exception POST /api/v1/users/register") {
				val alreadyUserExistException = AlreadyUserExistException(user.email)

				every { changeUserService.createUser(any<CreateUserRequest>()) } throws alreadyUserExistException

				Then("Call POST /api/v1/users/register") {
					mockMvc
						.perform(
							MockMvcRequestBuilders
								.post("/api/v1/users/register")
								.with(SecurityMockMvcRequestPostProcessors.csrf())
								.content(objectMapper.writeValueAsString(createUserRequest))
								.contentType(MediaType.APPLICATION_JSON)
								.accept(MediaType.APPLICATION_JSON)
						).andExpect(MockMvcResultMatchers.status().isConflict)
						.andExpect(
							MockMvcResultMatchers.jsonPath("$.code").value(HttpStatus.CONFLICT.value())
						).andExpect(
							MockMvcResultMatchers.jsonPath("$.message").value(alreadyUserExistException.message)
						).andExpect(MockMvcResultMatchers.jsonPath("$.errors").isEmpty)
				}
			}
		}

		Given("PATCH /api/v1/users/{userId}") {
			val updateUserRequest: UpdateUserRequest =
				Instancio
					.create(UpdateUserRequest::class.java)
					.copy(role = UserRole.USER.name)

			justRun {
				webHookProvider.sendAll(
					any<String>(),
					any<List<String>>()
				)
			}

			When("Success PATCH /api/v1/users/{userId}") {

				every {
					changeUserService.updateUser(
						any<Long>(),
						any<UpdateUserRequest>()
					)
				} returns UpdateUserResponse.from(user)

				Then("Call PATCH /api/v1/users/{userId}") {
					mockMvc
						.perform(
							MockMvcRequestBuilders
								.patch("/api/v1/users/{userId}", user.id)
								.with(SecurityMockMvcRequestPostProcessors.csrf())
								.content(objectMapper.writeValueAsString(updateUserRequest))
								.contentType(MediaType.APPLICATION_JSON)
								.accept(MediaType.APPLICATION_JSON)
						).andExpect(MockMvcResultMatchers.status().isOk)
						.andExpect(MockMvcResultMatchers.jsonPath("$.code").value(commonStatus))
						.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(commonMessage))
						.andExpect(MockMvcResultMatchers.jsonPath("$.data.userId").value(user.id))
						.andExpect(MockMvcResultMatchers.jsonPath("$.data.email").value(user.email))
						.andExpect(MockMvcResultMatchers.jsonPath("$.data.name").value(user.name))
						.andExpect(MockMvcResultMatchers.jsonPath("$.data.role").value(user.role.name))
				}
			}

			When("Field Valid Exception PATCH /api/v1/users/{userId}") {
				val wrongUpdateUserRequest =
					updateUserRequest.copy(
						name = ""
					)

				Then("Call PATCH /api/v1/users/{userId}") {
					mockMvc
						.perform(
							MockMvcRequestBuilders
								.patch("/api/v1/users/{userId}", user.id)
								.with(SecurityMockMvcRequestPostProcessors.csrf())
								.content(objectMapper.writeValueAsString(wrongUpdateUserRequest))
								.contentType(MediaType.APPLICATION_JSON)
								.accept(MediaType.APPLICATION_JSON)
						).andExpect(MockMvcResultMatchers.status().isBadRequest)
						.andExpect(
							MockMvcResultMatchers.jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value())
						)
						// name:field name is blank
						.andExpect(MockMvcResultMatchers.jsonPath("$.message").isString)
						.andExpect(MockMvcResultMatchers.jsonPath("$.errors").isNotEmpty)
				}
			}

			When("Not Found Exception PATCH /api/v1/users/{userId}") {
				val userNotFoundException = UserNotFoundException(user.id)

				every { changeUserService.updateUser(any<Long>(), any<UpdateUserRequest>()) } throws userNotFoundException

				Then("Call GET /api/v1/users/{userId}") {
					mockMvc
						.perform(
							MockMvcRequestBuilders
								.patch("/api/v1/users/{userId}", user.id)
								.with(SecurityMockMvcRequestPostProcessors.csrf())
								.content(objectMapper.writeValueAsString(updateUserRequest))
								.contentType(MediaType.APPLICATION_JSON)
								.accept(MediaType.APPLICATION_JSON)
						).andExpect(MockMvcResultMatchers.status().isNotFound)
						.andExpect(
							MockMvcResultMatchers.jsonPath("$.code").value(HttpStatus.NOT_FOUND.value())
						).andExpect(
							MockMvcResultMatchers.jsonPath("$.message").value(userNotFoundException.message)
						).andExpect(MockMvcResultMatchers.jsonPath("$.errors").isEmpty)
				}
			}
		}

		Given("PATCH /api/v1/users") {
			val updateUserRequest: UpdateUserRequest =
				Instancio
					.create(
						UpdateUserRequest::class.java
					).copy(
						role = UserRole.USER.name
					)

			justRun {
				webHookProvider.sendAll(
					any<String>(),
					any<List<String>>()
				)
			}

			When("Success PATCH /api/v1/users") {

				every { changeUserService.updateMe(any<Long>(), any<UpdateUserRequest>()) } returns
					UpdateMeResponse.from(
						user,
						defaultAccessToken
					)

				Then("Call PATCH /api/v1/users") {
					mockMvc
						.perform(
							MockMvcRequestBuilders
								.patch("/api/v1/users")
								.with(SecurityMockMvcRequestPostProcessors.csrf())
								.content(objectMapper.writeValueAsString(updateUserRequest))
								.contentType(MediaType.APPLICATION_JSON)
								.accept(MediaType.APPLICATION_JSON)
						).andExpect(MockMvcResultMatchers.status().isOk)
						.andExpect(MockMvcResultMatchers.jsonPath("$.code").value(commonStatus))
						.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(commonMessage))
						.andExpect(MockMvcResultMatchers.jsonPath("$.data.userId").value(user.id))
						.andExpect(MockMvcResultMatchers.jsonPath("$.data.email").value(user.email))
						.andExpect(MockMvcResultMatchers.jsonPath("$.data.name").value(user.name))
						.andExpect(MockMvcResultMatchers.jsonPath("$.data.role").value(user.role.name))
				}
			}

			When("Field Valid Exception PATCH /api/v1/users") {
				val wrongUpdateUserRequest =
					updateUserRequest.copy(
						name = ""
					)

				Then("Call PATCH /api/v1/users") {
					mockMvc
						.perform(
							MockMvcRequestBuilders
								.patch("/api/v1/users")
								.with(SecurityMockMvcRequestPostProcessors.csrf())
								.content(objectMapper.writeValueAsString(wrongUpdateUserRequest))
								.contentType(MediaType.APPLICATION_JSON)
								.accept(MediaType.APPLICATION_JSON)
						).andExpect(MockMvcResultMatchers.status().isBadRequest)
						.andExpect(MockMvcResultMatchers.jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
						// name:field name is blank
						.andExpect(MockMvcResultMatchers.jsonPath("$.message").isString)
						.andExpect(MockMvcResultMatchers.jsonPath("$.errors").isNotEmpty)
				}
			}

			When("Not Found Exception PATCH /api/v1/users") {
				val userNotFoundException = UserNotFoundException(user.id)

				every { changeUserService.updateMe(any<Long>(), any<UpdateUserRequest>()) } throws userNotFoundException

				Then("Call GET /api/v1/users") {
					mockMvc
						.perform(
							MockMvcRequestBuilders
								.patch("/api/v1/users")
								.with(SecurityMockMvcRequestPostProcessors.csrf())
								.content(objectMapper.writeValueAsString(updateUserRequest))
								.contentType(MediaType.APPLICATION_JSON)
								.accept(MediaType.APPLICATION_JSON)
						).andExpect(MockMvcResultMatchers.status().isNotFound)
						.andExpect(MockMvcResultMatchers.jsonPath("$.code").value(HttpStatus.NOT_FOUND.value()))
						.andExpect(
							MockMvcResultMatchers.jsonPath("$.message").value(userNotFoundException.message)
						).andExpect(MockMvcResultMatchers.jsonPath("$.errors").isEmpty)
				}
			}
		}

		Given("DELETE /api/v1/users/{userId}") {

			When("Success DELETE /api/v1/users/{userId}") {

				justRun { changeUserService.deleteUserById(any<Long>()) }

				Then("Call DELETE /api/v1/users/{userId}") {
					mockMvc
						.perform(
							MockMvcRequestBuilders
								.delete("/api/v1/users/{userId}", user.id)
								.with(SecurityMockMvcRequestPostProcessors.csrf())
								.contentType(MediaType.APPLICATION_JSON)
								.accept(MediaType.APPLICATION_JSON)
						).andExpect(MockMvcResultMatchers.status().isNoContent)
						.andExpect(MockMvcResultMatchers.jsonPath("$.code").value(commonStatus))
						.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(commonMessage))
				}
			}
		}

		Given("Spring Security Context is not set.") {

			When("UnAuthorized Exception GET /api/v1/users/{userId}") {

				Then("Call GET /api/v1/users/{userId}").config(tags = setOf(SecurityListenerFactory.NonSecurityOption)) {
					mockMvc
						.perform(
							MockMvcRequestBuilders
								.get("/api/v1/users/{userId}", user.id)
								.with(SecurityMockMvcRequestPostProcessors.csrf())
								.contentType(MediaType.APPLICATION_JSON)
								.accept(MediaType.APPLICATION_JSON)
						).andExpect(MockMvcResultMatchers.status().isUnauthorized)
				}
			}

			When("UnAuthorized Exception GET /api/v1/users") {

				Then("Call GET /api/v1/users").config(tags = setOf(SecurityListenerFactory.NonSecurityOption)) {
					mockMvc
						.perform(
							MockMvcRequestBuilders
								.get("/api/v1/users")
								.with(SecurityMockMvcRequestPostProcessors.csrf())
								.contentType(MediaType.APPLICATION_JSON)
								.accept(MediaType.APPLICATION_JSON)
						).andExpect(MockMvcResultMatchers.status().isUnauthorized)
				}
			}

			When("UnAuthorized Exception PATCH /api/v1/users/{userId}") {

				Then("Call PATCH /api/v1/users/{userId}").config(tags = setOf(SecurityListenerFactory.NonSecurityOption)) {
					mockMvc
						.perform(
							MockMvcRequestBuilders
								.patch("/api/v1/users/{userId}", user.id)
								.with(SecurityMockMvcRequestPostProcessors.csrf())
								.contentType(MediaType.APPLICATION_JSON)
								.accept(MediaType.APPLICATION_JSON)
						).andExpect(MockMvcResultMatchers.status().isUnauthorized)
				}
			}

			When("UnAuthorized Exception PATCH /api/v1/users") {

				Then("Call PATCH /api/v1/users").config(tags = setOf(SecurityListenerFactory.NonSecurityOption)) {
					mockMvc
						.perform(
							MockMvcRequestBuilders
								.patch("/api/v1/users")
								.with(SecurityMockMvcRequestPostProcessors.csrf())
								.contentType(MediaType.APPLICATION_JSON)
								.accept(MediaType.APPLICATION_JSON)
						).andExpect(MockMvcResultMatchers.status().isUnauthorized)
				}
			}

			When("UnAuthorized Exception DELETE /api/v1/users/{userId}") {

				Then("Call DELETE /api/v1/users/{userId}").config(tags = setOf(SecurityListenerFactory.NonSecurityOption)) {
					mockMvc
						.perform(
							MockMvcRequestBuilders
								.delete("/api/v1/users/{userId}", user.id)
								.with(SecurityMockMvcRequestPostProcessors.csrf())
								.contentType(MediaType.APPLICATION_JSON)
								.accept(MediaType.APPLICATION_JSON)
						).andExpect(MockMvcResultMatchers.status().isUnauthorized)
				}
			}
		}
	}
}
