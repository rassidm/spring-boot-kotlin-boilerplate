package com.example.demo.kotest.user.application

import com.example.demo.user.application.UserService
import com.example.demo.user.entity.User
import com.example.demo.user.exception.UserNotFoundException
import com.example.demo.user.exception.UserUnAuthorizedException
import com.example.demo.user.repository.UserRepository
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.instancio.Instancio
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@Tags("kotest-unit-test")
class UserServiceTests :
	BehaviorSpec({
		val userService = mockk<UserService>()
		val userRepository = mockk<UserRepository>()
		val bCryptPasswordEncoder = mockk<BCryptPasswordEncoder>()

		val user = Instancio.create(User::class.java)

		Given("Validate and return User entity") {

			When("Success validate and get user entity") {

				every { userRepository.findOneById(any<Long>()) } returns user

				every { userService.validateReturnUser(any<Long>()) } returns user

				val validateUser = userService.validateReturnUser(user.id)

				then("Validate & Get user entity") {
					validateUser shouldNotBeNull {
						id shouldBe user.id
						email shouldBe user.email
						name shouldBe user.name
						role shouldBe user.role
					}
				}
			}

			When("User Not Found Exception") {

				every { userRepository.findOneById(any<Long>()) } returns null

				every { userService.validateReturnUser(any<Long>()) } throws UserNotFoundException(user.id)

				shouldThrowExactly<UserNotFoundException> {
					userService.validateReturnUser(user.id)
				}
			}
		}

		Given("Validate and authenticated Return User Entity") {
			val testEmail = "test@example.com"
			val testPassword = "password123"

			When("Success validate and authenticated get user entity") {

				every { userRepository.findOneByEmail(any<String>()) } returns user

				every {
					user.validatePassword(
						any<String>(),
						bCryptPasswordEncoder
					)
				} returns true

				every {
					userService.validateAuthReturnUser(
						any<String>(),
						any<String>()
					)
				} returns user

				val validateAuthUser =
					userService.validateAuthReturnUser(
						testEmail,
						testPassword
					)

				Then("Success Auth & Get user entity") {
					validateAuthUser shouldNotBeNull {
						id shouldBe user.id
						email shouldBe user.email
						name shouldBe user.name
						role shouldBe user.role
					}
				}
			}

			When("User Not Found Exception") {

				every { userRepository.findOneByEmail(any<String>()) } returns null

				every { userService.validateAuthReturnUser(any<String>(), any<String>()) } throws UserNotFoundException(user.email)

				shouldThrowExactly<UserNotFoundException> {
					userService.validateAuthReturnUser(testEmail, testPassword)
				}
			}

			When("User Unauthorized exception") {

				every { userRepository.findOneByEmail(any<String>()) } returns user

				every {
					user.validatePassword(
						any<String>(),
						bCryptPasswordEncoder
					)
				} returns false

				every {
					userService.validateAuthReturnUser(any<String>(), any<String>())
				} throws UserUnAuthorizedException(user.email)

				shouldThrowExactly<UserUnAuthorizedException> {
					userService.validateAuthReturnUser(testEmail, testPassword)
				}
			}
		}
	})
