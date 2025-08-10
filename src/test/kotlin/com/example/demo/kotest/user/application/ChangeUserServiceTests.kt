package com.example.demo.kotest.user.application

import com.example.demo.security.component.provider.TokenProvider
import com.example.demo.user.application.ChangeUserService
import com.example.demo.user.application.UserService
import com.example.demo.user.constant.UserRole
import com.example.demo.user.dto.serve.request.CreateUserRequest
import com.example.demo.user.dto.serve.request.UpdateUserRequest
import com.example.demo.user.dto.serve.response.CreateUserResponse
import com.example.demo.user.dto.serve.response.UpdateUserResponse
import com.example.demo.user.entity.User
import com.example.demo.user.event.UserEvent
import com.example.demo.user.exception.AlreadyUserExistException
import com.example.demo.user.exception.UserNotFoundException
import com.example.demo.user.repository.UserRepository
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import org.instancio.Instancio
import org.springframework.context.ApplicationEventPublisher
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@Tags("kotest-unit-test")
class ChangeUserServiceTests :
	BehaviorSpec({
		val userRepository = mockk<UserRepository>()
		val eventPublisher = mockk<ApplicationEventPublisher>(relaxed = true)
		val bCryptPasswordEncoder = mockk<BCryptPasswordEncoder>()
		val tokenProvider = mockk<TokenProvider>()
		val userService = mockk<UserService>()
		val changeUserService = mockk<ChangeUserService>()

		val user: User = Instancio.create(User::class.java)
		val defaultUserEncodePassword =
			"$2a$10\$T44NRNpbxkQ9qHbCtqQZ7O3gYfipzC0cHvOIJ/aV4PTlvJjtDl7x2\n" + //
				""
		val defaultAccessToken =
			"""
      eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.
      eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c
      """

		Given("Delete User") {

			When("Success Delete User") {

				justRun {
					userRepository.deleteById(any<Long>())
					tokenProvider.deleteRefreshToken(any<Long>())
					changeUserService.deleteUser(any<Long>())
				}

				userRepository.deleteById(user.id)
				tokenProvider.deleteRefreshToken(user.id)
				changeUserService.deleteUser(user.id)

				Then("Verify Call Method") {
					verify(exactly = 1) {
						userRepository.deleteById(user.id)
						tokenProvider.deleteRefreshToken(user.id)
						changeUserService.deleteUser(user.id)
					}
				}
			}
		}

		Given("Update User") {
			val updateUserRequest: UpdateUserRequest =
				Instancio
					.create(UpdateUserRequest::class.java)
					.copy(role = UserRole.USER.name)
			val userRole = UserRole.valueOf(updateUserRequest.role)

			When("Success Update User") {

				every { userService.validateReturnUser(any<Long>()) } returns user

				every {
					changeUserService.updateUser(
						any<Long>(),
						any<UpdateUserRequest>()
					)
				} returns
					UpdateUserResponse.from(
						user.apply {
							name = updateUserRequest.name
							role = userRole
						}
					)

				val updateUserResponse =
					changeUserService.updateUser(
						user.id,
						updateUserRequest
					)

				Then("Assert User Entity") {
					updateUserResponse shouldNotBeNull {
						name shouldBe updateUserRequest.name
						role shouldBe userRole
					}
				}
			}

			When("User Not Found Exception") {

				every { userService.validateReturnUser(any<Long>()) } throws UserNotFoundException(user.id)

				every {
					changeUserService.updateUser(
						any<Long>(),
						any<UpdateUserRequest>()
					)
				} throws UserNotFoundException(user.id)

				shouldThrowExactly<UserNotFoundException> { changeUserService.updateUser(user.id, updateUserRequest) }
			}
		}

		Given("Create User") {
			val createUserRequest: CreateUserRequest =
				Instancio.create(
					CreateUserRequest::class.java
				)

			When("Success Create User") {

				every { bCryptPasswordEncoder.encode(any<String>()) } returns defaultUserEncodePassword

				every { userRepository.save(any<User>()) } returns user

				justRun { eventPublisher.publishEvent(any<UserEvent.WelcomeSignUpEvent>()) }

				every { tokenProvider.createFullTokens(any<User>()) } returns defaultAccessToken

				every {
					changeUserService.createUser(
						any<CreateUserRequest>()
					)
				} returns CreateUserResponse.from(user, defaultAccessToken)

				val createUserResponse =
					changeUserService.createUser(
						createUserRequest
					)

				Then("Assert User Entity & Verify Call Publish Event") {
					createUserResponse shouldNotBeNull {
						email shouldBe user.email
						name shouldBe user.name
						role shouldBe user.role
					}
				}
			}

			When("Already User Exist Exception") {

				every { userRepository.existsByEmail(any<String>()) } returns true

				every { changeUserService.createUser(any<CreateUserRequest>()) } throws AlreadyUserExistException(user.id)

				shouldThrowExactly<AlreadyUserExistException> {
					changeUserService.createUser(createUserRequest)
				}
			}
		}
	})
