package com.example.demo.kotest.user.repository

import com.example.demo.common.config.JpaAuditConfig
import com.example.demo.common.config.QueryDslConfig
import com.example.demo.user.constant.UserRole
import com.example.demo.user.dto.request.UpdateUserRequest
import com.example.demo.user.entity.User
import com.example.demo.user.repository.UserRepository
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.instancio.Instancio
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@Tags("kotest-unit-test")
@Import(value = [QueryDslConfig::class, JpaAuditConfig::class])
@DataJpaTest
class UserRepositoryTests(
	@Autowired
	private val userRepository: UserRepository
) : DescribeSpec({
		lateinit var userEntity: User
		val defaultUserEmail = "user@example.com"
		val defaultUserPassword = "test_password_123!@"
		val defaultUserName = "username"
		val defaultUserRole = UserRole.USER

		beforeContainer {
			userEntity =
				User(
					name = defaultUserName,
					email = defaultUserEmail,
					password = defaultUserPassword,
					role = defaultUserRole
				)
		}

		describe("Create user") {

			context("Save user") {
				val createUser = userRepository.save(userEntity)

				it("Assert User Entity") {
					createUser.id shouldBe userEntity.id
					createUser.email shouldBe userEntity.email
					createUser.name shouldBe userEntity.name
					createUser.role shouldBe userEntity.role
				}
			}
		}

		describe("Update user") {

			context("Save user") {
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

				it("Assert User Entity") {
					afterUpdateUser.name shouldBe updateUserRequest.name
					afterUpdateUser.role shouldBe userRole
				}
			}
		}

		describe("Delete User") {

			context("Call deleteById") {
				val beforeDeleteUser = userRepository.save(userEntity)

				userRepository.deleteById(beforeDeleteUser.id)

				val afterDeleteUser: User? =
					userRepository
						.findOneById(beforeDeleteUser.id)

				it("Assert Null") {
					afterDeleteUser.shouldBeNull()
				}
			}
		}

		describe("Find User By Id") {

			context("Call findOneById") {
				val beforeFindUser = userRepository.save(userEntity)

				val afterFindUser: User =
					requireNotNull(
						userRepository
							.findOneById(beforeFindUser.id)
					) {
						"User must not be null"
					}

				it("Assert User Entity") {
					afterFindUser.id shouldBe beforeFindUser.id
					afterFindUser.email shouldBe beforeFindUser.email
					afterFindUser.name shouldBe beforeFindUser.name
					afterFindUser.role shouldBe beforeFindUser.role
				}
			}
		}

		describe("Find User By Email") {

			context("Call findOneByEmail") {
				val beforeFindUser = userRepository.save(userEntity)

				val afterFindUser: User =
					requireNotNull(
						userRepository
							.findOneByEmail(beforeFindUser.email)
					) {
						"User must not be null"
					}

				it("Assert User Entity") {
					afterFindUser.id shouldBe beforeFindUser.id
					afterFindUser.email shouldBe beforeFindUser.email
					afterFindUser.name shouldBe beforeFindUser.name
					afterFindUser.role shouldBe beforeFindUser.role
				}
			}
		}
	})
