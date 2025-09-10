package com.example.demo.kotest.user.batch

import com.example.demo.common.config.TestBatchConfig
import com.example.demo.example.UserDeleteConsumer
import com.example.demo.post.application.ChangePostService
import com.example.demo.user.application.ChangeUserService
import com.example.demo.user.batch.config.UserDeleteConfig
import com.example.demo.user.batch.mapper.UserDeleteItem
import com.example.demo.user.constant.UserRole
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.batch.core.BatchStatus
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.test.JobLauncherTestUtils
import org.springframework.batch.test.JobRepositoryTestUtils
import org.springframework.batch.test.context.SpringBatchTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime

@ActiveProfiles("test")
@Tags("kotest-integration-test")
@SpringBootTest(classes = [UserDeleteConfig::class, TestBatchConfig::class])
@SpringBatchTest
class UserDeleteConfigIntegrationTests(
	@Autowired
	private val jdbcTemplate: JdbcTemplate,
	@Autowired
	private val jobLauncherTestUtils: JobLauncherTestUtils,
	@Autowired
	private val jobRepositoryTestUtils: JobRepositoryTestUtils
) : FunSpec({

		val defaultUserEmail = "user@example.com"
		val defaultUserName = "username"
		val defaultUserRole = UserRole.USER

		fun cleanupDatabase() {
			jdbcTemplate.update("DELETE FROM \"user\"")
			jobRepositoryTestUtils.removeJobExecutions()
		}

		fun insertUser(
			email: String,
			createdDt: LocalDateTime,
			updatedDt: LocalDateTime,
			deletedDt: LocalDateTime?
		) {
			jdbcTemplate.update(
				"""
			INSERT INTO "user" (created_dt, updated_dt, deleted_dt, email, name, password, role)
			VALUES (?, ?, ?, ?, ?, ?, ?)
			""",
				createdDt,
				updatedDt,
				deletedDt,
				email,
				defaultUserName,
				"$2a$10\$T44NRNpbxkQ9qHbCtqQZ7O3gYfipzC0cHvOIJ/aV4PTlvJjtDl7x2",
				defaultUserRole.name
			)
		}

		test("UserDelete Batch Job Integration Test") {
			cleanupDatabase()

			val now = LocalDateTime.now().withNano(0)

			insertUser(
				email = "deleted-1year-1@example.com",
				createdDt = now.minusYears(2),
				updatedDt = now.minusYears(1),
				deletedDt = now.minusYears(1).minusDays(1)
			)

			insertUser(
				email = "deleted-1year-2@example.com",
				createdDt = now.minusYears(2),
				updatedDt = now.minusYears(1),
				deletedDt = now.minusYears(1).minusDays(5)
			)

			insertUser(
				email = "deleted-exactly-1year@example.com",
				createdDt = now.minusYears(2),
				updatedDt = now.minusYears(1),
				deletedDt = now.minusYears(1)
			)

			insertUser(
				email = "deleted-6months@example.com",
				createdDt = now.minusYears(1),
				updatedDt = now.minusMonths(6),
				deletedDt = now.minusMonths(6)
			)

			insertUser(
				email = "active-user@example.com",
				createdDt = now.minusYears(2),
				updatedDt = now,
				deletedDt = null
			)

			val jobParameters =
				JobParametersBuilder()
					.addLocalDateTime("now", now)
					.toJobParameters()

			val jobExecution = jobLauncherTestUtils.launchJob(jobParameters)

			jobExecution.status shouldBe BatchStatus.COMPLETED

			val recentlyDeletedUsers =
				jdbcTemplate.queryForObject(
					"SELECT COUNT(*) FROM \"user\" WHERE deleted_dt = ?",
					Int::class.java,
					now.minusMonths(6)
				)
			recentlyDeletedUsers shouldBe 1

			val activeUsers =
				jdbcTemplate.queryForObject(
					"SELECT COUNT(*) FROM \"user\" WHERE deleted_dt IS NULL",
					Int::class.java
				)
			activeUsers shouldBe 1

			val oldDeletedUsers =
				jdbcTemplate.queryForObject(
					"SELECT COUNT(*) FROM \"user\" WHERE deleted_dt IS NOT NULL AND deleted_dt <= ?",
					Int::class.java,
					now.minusYears(1)
				)
			oldDeletedUsers shouldBe 3

			val changeUserService = mockk<ChangeUserService>(relaxed = true)
			val changePostService = mockk<ChangePostService>(relaxed = true)
			val userDeleteConsumer = UserDeleteConsumer(changePostService, changeUserService)

			val testItem =
				UserDeleteItem(
					id = 100L,
					email = defaultUserEmail,
					name = defaultUserName,
					role = defaultUserRole.name,
					deletedDt = now.minusYears(1)
				)

			userDeleteConsumer.consume(testItem)

			verify(exactly = 1) {
				changePostService.hardDeletePostByUserId(100L)
				changeUserService.hardDeleteUserById(100L)
			}

			val errorService = mockk<ChangePostService>()
			every { errorService.hardDeletePostByUserId(any()) } throws
				org.springframework.dao.DataIntegrityViolationException("DB Error")

			val errorConsumer = UserDeleteConsumer(errorService, changeUserService)

			try {
				errorConsumer.consume(testItem)
			} catch (e: Exception) {
				e.message shouldBe "DB Error"
			}

			verify(exactly = 1) {
				errorService.hardDeletePostByUserId(100L)
			}
		}
	})
