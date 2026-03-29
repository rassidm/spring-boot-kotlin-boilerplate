package com.example.demo.kotest.user.application

import com.example.demo.user.application.GetUserService
import com.example.demo.user.dto.response.GetUserResponse
import com.example.demo.user.entity.User
import com.example.demo.user.exception.UserNotFoundException
import com.example.demo.user.repository.UserRepository
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.instancio.Instancio
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@Tags("kotest-unit-test")
class GetUserServiceTests :
	BehaviorSpec({
		val userRepository = mockk<UserRepository>()
		val getUserService = mockk<GetUserService>()

		val user: User = Instancio.create(User::class.java)
		val defaultPageable = Pageable.ofSize(1)

		Given("Get User By Id") {

			When("Success Get User By Id") {

				every { userRepository.findOneById(any<Long>()) } returns user

				every {
					getUserService.getUserById(
						any<Long>()
					)
				} returns GetUserResponse.from(user)

				val getUserResponse =
					getUserService.getUserById(
						user.id
					)

				Then("Assert User Entity") {
					getUserResponse shouldNotBeNull {
						userId shouldBe user.id
						email shouldBe user.email
						name shouldBe user.name
						role shouldBe user.role
					}
				}
			}

			When("User Not Found Exception") {

				every { userRepository.findOneById(any<Long>()) } returns null

				every { getUserService.getUserById(any<Long>()) } throws UserNotFoundException(user.id)

				shouldThrowExactly<UserNotFoundException> {
					getUserService.getUserById(user.id)
				}
			}
		}

		Given("Get User By Email") {

			When("Success Get User By Email") {

				every { userRepository.findOneByEmail(any<String>()) } returns user

				every {
					getUserService.getUserByEmail(
						any<String>()
					)
				} returns GetUserResponse.from(user)

				val getUserResponse =
					getUserService.getUserByEmail(
						user.email
					)

				Then("Assert User Entity") {
					getUserResponse shouldNotBeNull {
						userId shouldBe user.id
						email shouldBe user.email
						name shouldBe user.name
						role shouldBe user.role
					}
				}
			}

			When("User is null") {

				every { userRepository.findOneByEmail(any<String>()) } returns null

				every { getUserService.getUserByEmail(any<String>()) } returns null

				val getUserResponse =
					getUserService.getUserByEmail(
						user.email
					)

				Then("Assert Null") {
					getUserResponse.shouldBeNull()
				}
			}
		}

		Given("Get User List") {

			When("Success Get User List") {

				every { userRepository.findAll(any<Pageable>()) } returns PageImpl(listOf(user), defaultPageable, 1)

				every {
					getUserService.getUserList(
						any<Pageable>()
					)
				} returns PageImpl(listOf(GetUserResponse.from(user)), defaultPageable, 1)

				val getUserResponseList =
					getUserService.getUserList(
						defaultPageable
					)

				Then("Assert User List") {
					getUserResponseList.shouldNotBeEmpty()
					getUserResponseList.content[0] shouldNotBeNull {
						userId shouldBe user.id
						email shouldBe user.email
						name shouldBe user.name
						role shouldBe user.role
					}
				}
			}
		}
	})
