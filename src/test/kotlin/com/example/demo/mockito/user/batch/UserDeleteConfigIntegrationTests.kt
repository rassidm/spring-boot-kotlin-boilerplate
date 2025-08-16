package com.example.demo.mockito.user.batch

import com.example.demo.common.config.TestBatchConfig
import com.example.demo.example.UserDeleteConsumer
import com.example.demo.post.application.ChangePostService
import com.example.demo.user.application.ChangeUserService
import com.example.demo.user.batch.config.UserDeleteConfig
import com.example.demo.user.batch.mapper.UserDeleteItem
import com.example.demo.user.constant.UserRole
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.mockito.Mockito.anyLong
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.batch.core.BatchStatus
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.test.JobLauncherTestUtils
import org.springframework.batch.test.JobRepositoryTestUtils
import org.springframework.batch.test.context.SpringBatchTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime

@ActiveProfiles("test")
@Tag("mockito-integration-test")
@DisplayName("Mockito Integration - User Delete Config Batch Test")
@SpringBootTest(classes = [UserDeleteConfig::class, TestBatchConfig::class])
@SpringBatchTest
class UserDeleteConfigIntegrationTests {
	@Autowired
	private lateinit var jdbcTemplate: JdbcTemplate

	@Autowired
	private lateinit var jobLauncherTestUtils: JobLauncherTestUtils

	@Autowired
	private lateinit var jobRepositoryTestUtils: JobRepositoryTestUtils

	private val defaultUserEmail = "awakelife93@gmail.com"
	private val defaultUserName = "Hyunwoo Park"
	private val defaultUserRole = UserRole.USER

	private fun cleanupDatabase() {
		jdbcTemplate.update("DELETE FROM \"user\"")
		jobRepositoryTestUtils.removeJobExecutions()
	}

	private fun insertUser(
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

	@Test
	@DisplayName("UserDelete Batch Job Integration Test")
	fun testUserDeleteBatchJobIntegration() {
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

		assertEquals(BatchStatus.COMPLETED, jobExecution.status)

		val recentlyDeletedUsers =
			jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM \"user\" WHERE deleted_dt = ?",
				Int::class.java,
				now.minusMonths(6)
			)
		assertEquals(1, recentlyDeletedUsers)

		val activeUsers =
			jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM \"user\" WHERE deleted_dt IS NULL",
				Int::class.java
			)
		assertEquals(1, activeUsers)

		val oldDeletedUsers =
			jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM \"user\" WHERE deleted_dt IS NOT NULL AND deleted_dt <= ?",
				Int::class.java,
				now.minusYears(1)
			)
		assertEquals(3, oldDeletedUsers)

		val changeUserService = mock(ChangeUserService::class.java)
		val changePostService = mock(ChangePostService::class.java)
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

		verify(changePostService, times(1)).hardDeletePostByUserId(100L)
		verify(changeUserService, times(1)).hardDeleteUserById(100L)

		val errorService = mock(ChangePostService::class.java)
		`when`(errorService.hardDeletePostByUserId(anyLong()))
			.thenThrow(DataIntegrityViolationException("DB Error"))

		val errorConsumer = UserDeleteConsumer(errorService, changeUserService)

		val exception =
			assertThrows(DataIntegrityViolationException::class.java) {
				errorConsumer.consume(testItem)
			}
		assertEquals("DB Error", exception.message)

		verify(errorService, times(1)).hardDeletePostByUserId(100L)
	}
}
