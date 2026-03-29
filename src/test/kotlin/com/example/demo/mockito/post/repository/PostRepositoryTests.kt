package com.example.demo.mockito.post.repository

import com.example.demo.common.config.JpaAuditConfig
import com.example.demo.common.config.QueryDslConfig
import com.example.demo.post.dto.request.UpdatePostRequest
import com.example.demo.post.entity.Post
import com.example.demo.post.repository.PostRepository
import org.instancio.Instancio
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@Tag("mockito-unit-test")
@DisplayName("Mockito Unit - Post Repository Test")
@Import(value = [QueryDslConfig::class, JpaAuditConfig::class])
@DataJpaTest
class PostRepositoryTests(
	@Autowired private val postRepository: PostRepository
) {
	private val defaultPostTitle = "unit test"
	private val defaultPostSubTitle = "Post Repository Test"
	private val defaultPostContent = "default value for post repository testing"

	private lateinit var postEntity: Post

	@BeforeEach
	@Throws(Exception::class)
	fun setUp() {
		postEntity =
			Post(
				defaultPostTitle,
				defaultPostSubTitle,
				defaultPostContent,
				1L
			)
	}

	@Test
	@DisplayName("Create post")
	fun should_AssertCreatedPostEntity_when_GivenPostEntity() {
		val createPost = postRepository.save(postEntity)

		assertEquals(createPost.id, postEntity.id)
		assertEquals(createPost.title, postEntity.title)
		assertEquals(createPost.subTitle, postEntity.subTitle)
		assertEquals(createPost.content, postEntity.content)
		assertEquals(createPost.userId, postEntity.userId)
	}

	@Test
	@DisplayName("Update post")
	fun should_AssertUpdatedPostEntity_when_GivenPostIdAndUpdatePostRequest() {
		val updatePostRequest =
			Instancio.create(
				UpdatePostRequest::class.java
			)

		val beforeUpdatePost = postRepository.save(postEntity)

		postRepository.save(
			beforeUpdatePost.update(
				updatePostRequest.title,
				updatePostRequest.subTitle,
				updatePostRequest.content
			)
		)

		val afterUpdatePost: Post =
			requireNotNull(
				postRepository
					.findOneById(beforeUpdatePost.id)
			) {
				"Post must not be null"
			}

		assertEquals(afterUpdatePost.title, updatePostRequest.title)
		assertEquals(
			afterUpdatePost.subTitle,
			updatePostRequest.subTitle
		)
		assertEquals(afterUpdatePost.content, updatePostRequest.content)
	}

	@Test
	@DisplayName("Delete post by postId")
	fun should_AssertDeletedPostEntity_when_GivenPostId() {
		val beforeDeletePost = postRepository.save(postEntity)

		postRepository.deleteById(beforeDeletePost.id)

		val afterDeletePost: Post? =
			postRepository
				.findOneById(beforeDeletePost.id)

		assertNull(afterDeletePost)
	}

	@Test
	@DisplayName("Delete post by userId")
	fun should_AssertDeletedPostEntity_when_GivenUserId() {
		val beforeDeletePost = postRepository.save(postEntity)

		postRepository.deleteByUserId(beforeDeletePost.userId)

		val afterDeletePost: Post? =
			postRepository
				.findOneById(beforeDeletePost.id)

		assertNull(afterDeletePost)
	}

	@Test
	@DisplayName("Find post by id")
	fun should_AssertFindPostEntity_when_GivenPostId() {
		val beforeFindPost = postRepository.save(postEntity)

		val afterFindPost: Post =
			requireNotNull(
				postRepository
					.findOneById(beforeFindPost.id)
			) {
				"Post must not be null"
			}

		assertEquals(beforeFindPost.id, afterFindPost.id)
		assertEquals(beforeFindPost.title, afterFindPost.title)
		assertEquals(beforeFindPost.subTitle, afterFindPost.subTitle)
		assertEquals(beforeFindPost.content, afterFindPost.content)
		assertEquals(
			beforeFindPost.userId,
			afterFindPost.userId
		)
	}

	@Test
	@DisplayName("Find post by userId")
	fun should_AssertFindPostEntity_when_GivenUserId() {
		val beforeFindPost = postRepository.save(postEntity)

		val afterFindPost: Post =
			requireNotNull(
				postRepository
					.findOneByUserId(beforeFindPost.userId)
			) {
				"Post must not be null"
			}

		assertEquals(beforeFindPost.id, afterFindPost.id)
		assertEquals(beforeFindPost.title, afterFindPost.title)
		assertEquals(beforeFindPost.subTitle, afterFindPost.subTitle)
		assertEquals(beforeFindPost.content, afterFindPost.content)
		assertEquals(
			beforeFindPost.userId,
			afterFindPost.userId
		)
	}

	@Test
	@DisplayName("Get exclude users posts with specific user IDs")
	fun should_ReturnPostsExcludingSpecificUsers_when_GivenExcludeUserIds() {
		val userId1 = 1L
		val userId2 = 2L
		val userId3 = 3L
		val userId4 = 4L

		postRepository.save(
			Post("Title 1", "SubTitle 1", "Content 1", userId1)
		)
		postRepository.save(
			Post("Title 2", "SubTitle 2", "Content 2", userId2)
		)
		postRepository.save(
			Post("Title 3", "SubTitle 3", "Content 3", userId3)
		)
		postRepository.save(
			Post("Title 4", "SubTitle 4", "Content 4", userId4)
		)

		val excludeUserIds = listOf(userId1, userId2)
		val pageable = PageRequest.of(0, 10)
		val result = postRepository.getExcludeUsersPosts(excludeUserIds, pageable)

		assertEquals(2, result.totalElements)
		assertEquals(2, result.content.size)

		val userIds = result.content.map { it.userId }.toSet()
		assertEquals(setOf(userId3, userId4), userIds)
	}

	@Test
	@DisplayName("Get exclude users posts with paging")
	fun should_ReturnPagedResults_when_GivenPageable() {
		val userId1 = 10L
		val userId2 = 20L
		val userId3 = 30L

		postRepository.save(Post("Title 1", "SubTitle 1", "Content 1", userId1))
		postRepository.save(Post("Title 2", "SubTitle 2", "Content 2", userId2))
		postRepository.save(Post("Title 3", "SubTitle 3", "Content 3", userId3))

		val excludeUserIds = listOf(userId1)
		val pageable = PageRequest.of(0, 1)
		val result = postRepository.getExcludeUsersPosts(excludeUserIds, pageable)

		assertEquals(2, result.totalElements)
		assertEquals(1, result.content.size)
		assertEquals(2, result.totalPages)
	}

	@Test
	@DisplayName("Get exclude users posts with all users excluded")
	fun should_ReturnEmptyResult_when_AllUsersExcluded() {
		val userId1 = 100L
		val userId2 = 200L

		postRepository.save(Post("Title 1", "SubTitle 1", "Content 1", userId1))
		postRepository.save(Post("Title 2", "SubTitle 2", "Content 2", userId2))

		val excludeAllUserIds = listOf(userId1, userId2)
		val pageable = PageRequest.of(0, 10)
		val result = postRepository.getExcludeUsersPosts(excludeAllUserIds, pageable)

		assertEquals(0, result.totalElements)
		assertEquals(0, result.content.size)
		assertTrue(result.isEmpty)
	}

	@Test
	@DisplayName("Get exclude users posts with empty exclude list")
	fun should_ReturnAllPosts_when_ExcludeListIsEmpty() {
		val userId1 = 1000L
		val userId2 = 2000L

		postRepository.save(Post("Title 1", "SubTitle 1", "Content 1", userId1))
		postRepository.save(Post("Title 2", "SubTitle 2", "Content 2", userId2))

		val emptyExcludeList = emptyList<Long>()
		val pageable = PageRequest.of(0, 10)
		val result = postRepository.getExcludeUsersPosts(emptyExcludeList, pageable)

		assertEquals(2, result.totalElements)
		assertEquals(2, result.content.size)
	}
}
