package com.example.demo.example

import com.example.demo.infrastructure.kafka.provider.KafkaTopicMetaProvider
import com.example.demo.post.application.ChangePostService
import com.example.demo.user.application.ChangeUserService
import com.example.demo.user.batch.mapper.UserDeleteItem
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.dao.DataAccessException
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

private val logger = KotlinLogging.logger {}

@Component
class UserDeleteConsumer(
	private val changePostService: ChangePostService,
	private val changeUserService: ChangeUserService
) {
	@KafkaListener(
		topics = [KafkaTopicMetaProvider.USER_DELETE_TOPIC],
		groupId = KafkaTopicMetaProvider.USER_DELETE_GROUP,
		containerFactory = KafkaTopicMetaProvider.USER_DELETE_CONTAINER_FACTORY
	)
	@Retryable(
		value = [DataAccessException::class],
		maxAttempts = 3,
		backoff = Backoff(delay = 2000)
	)
	@Transactional
	fun consume(payload: UserDeleteItem) {
		runCatching {
			logger.info { "Hard Deleted User By = ${payload.name} ${payload.email} ${payload.role} ${payload.deletedDt}" }

			changePostService.hardDeletePostByUserId(payload.id)
			changeUserService.hardDeleteUserById(payload.id)
		}.onFailure { exception ->
			logger.error { "Failed to user delete: ${exception.message}" }

			when (exception) {
				is DataAccessException -> throw exception
				else -> logger.warn { "Unexpected error during user deletion: ${exception.javaClass.simpleName}" }
			}
		}
	}
}
