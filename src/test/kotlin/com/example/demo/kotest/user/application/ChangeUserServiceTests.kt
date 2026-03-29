package com.example.demo.kotest.user.application

import com.example.demo.post.application.ChangePostService
import com.example.demo.security.component.provider.TokenProvider
import com.example.demo.user.application.ChangeUserService
import com.example.demo.user.application.UserService
import com.example.demo.user.constant.UserRole
import com.example.demo.user.dto.command.CreateUserCommand
import com.example.demo.user.dto.command.UpdateUserCommand
import com.example.demo.user.dto.response.CreateUserResponse
import com.example.demo.user.dto.response.UpdateUserResponse
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
		val changePostService = mockk<ChangePostService>()

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
					changePostService.deletePostByUserId(any<Long>())
					changeUserService.deleteUserById(any<Long>())
				}

				userRepository.deleteById(user.id)
				tokenProvider.deleteRefreshToken(user.id)
				changePostService.deletePostByUserId(user.id)
				changeUserService.deleteUserById(user.id)

				Then("Verify Call Method") {
					verify(exactly = 1) {
						userRepository.deleteById(user.id)
						tokenProvider.deleteRefreshToken(user.id)
						changePostService.deletePostByUserId(user.id)
						changeUserService.deleteUserById(user.id)
					}
				}
			}
		}

		Given("Update User") {
			val updateUserCommand: UpdateUserCommand =
				Instancio
					.create(UpdateUserCommand::class.java)
					.copy(role = UserRole.USER.name)
			val userRole = UserRole.valueOf(updateUserCommand.role)

			When("Success Update User") {

				every { userService.validateReturnUser(any<Long>()) } returns user

				every {
					changeUserService.updateUser(
						any<Long>(),
						any<UpdateUserCommand>()
					)
				} returns
					UpdateUserResponse.from(
						user.apply {
							name = updateUserCommand.name
							role = userRole
						}
					)

				val updateUserResponse =
					changeUserService.updateUser(
						user.id,
						updateUserCommand
					)

				Then("Assert User Entity") {
					updateUserResponse shouldNotBeNull {
						name shouldBe updateUserCommand.name
						role shouldBe userRole
					}
				}
			}

			When("User Not Found Exception") {

				every { userService.validateReturnUser(any<Long>()) } throws UserNotFoundException(user.id)

				every {
					changeUserService.updateUser(
						any<Long>(),
						any<UpdateUserCommand>()
					)
				} throws UserNotFoundException(user.id)

				shouldThrowExactly<UserNotFoundException> { changeUserService.updateUser(user.id, updateUserCommand) }
			}
		}

		Given("Create User") {
			val createUserCommand: CreateUserCommand =
				Instancio.create(
					CreateUserCommand::class.java
				)

			When("Success Create User") {

				every { bCryptPasswordEncoder.encode(any<String>()) } returns defaultUserEncodePassword

				every { userRepository.save(any<User>()) } returns user

				justRun { eventPublisher.publishEvent(any<UserEvent.WelcomeSignUpEvent>()) }

				every { tokenProvider.createFullTokens(any<User>()) } returns defaultAccessToken

				every {
					changeUserService.createUser(
						any<CreateUserCommand>()
					)
				} returns CreateUserResponse.from(user, defaultAccessToken)

				val createUserResponse =
					changeUserService.createUser(
						createUserCommand
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

				every { changeUserService.createUser(any<CreateUserCommand>()) } throws AlreadyUserExistException(user.id)

				shouldThrowExactly<AlreadyUserExistException> {
					changeUserService.createUser(createUserCommand)
				}
			}
		}
	})
