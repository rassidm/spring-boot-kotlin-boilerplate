package com.example.demo.mockito.post.application

import com.example.demo.post.application.impl.PostServiceImpl
import com.example.demo.post.entity.Post
import com.example.demo.post.exception.PostNotFoundException
import com.example.demo.post.repository.PostRepository
import org.instancio.Instancio
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@Tag("mockito-unit-test")
@DisplayName("Mockito Unit - Post Service Test")
@ExtendWith(MockitoExtension::class)
class PostServiceTests {
	@Mock
	private lateinit var postRepository: PostRepository

	@InjectMocks
	private lateinit var postServiceImpl: PostServiceImpl

	private val post: Post = Instancio.create(Post::class.java)

	@Nested
	@DisplayName("Validate And Return Post Entity Test")
	inner class ValidateReturnPostTest {
		@Test
		@DisplayName("Success validate and get post entity")
		fun `should return post entity when post exists`() {
			val postId = post.id
			whenever(postRepository.findOneById(postId)) doReturn post

			val validatePost = postServiceImpl.validateReturnPost(postId)

			assertNotNull(validatePost)
			assertEquals(post.id, validatePost.id)
			assertEquals(post.title, validatePost.title)
			assertEquals(post.subTitle, validatePost.subTitle)
			assertEquals(post.content, validatePost.content)
			assertEquals(post.userId, validatePost.userId)

			verify(postRepository).findOneById(postId)
			verifyNoMoreInteractions(postRepository)
		}

		@Test
		@DisplayName("validate and post entity is not found exception")
		fun `should throw PostNotFoundException when post not found`() {
			val postId = post.id
			whenever(postRepository.findOneById(postId)) doReturn null

			assertThrows<PostNotFoundException> {
				postServiceImpl.validateReturnPost(postId)
			}

			verify(postRepository).findOneById(postId)
			verifyNoMoreInteractions(postRepository)
		}
	}
}
