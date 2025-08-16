package com.example.demo.mockito.post.application

import com.example.demo.post.application.impl.ChangePostServiceImpl
import com.example.demo.post.application.impl.PostServiceImpl
import com.example.demo.post.dto.serve.request.CreatePostRequest
import com.example.demo.post.dto.serve.request.UpdatePostRequest
import com.example.demo.post.entity.Post
import com.example.demo.post.exception.PostNotFoundException
import com.example.demo.post.repository.PostRepository
import com.example.demo.user.application.impl.UserServiceImpl
import com.example.demo.user.entity.User
import com.example.demo.user.exception.UserNotFoundException
import org.instancio.Instancio
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@Tag("mockito-unit-test")
@DisplayName("Mockito Unit - Post / Put / Delete / Patch Post Service Test")
@ExtendWith(
	MockitoExtension::class
)
class ChangePostServiceTests {
	@Mock
	private lateinit var userServiceImpl: UserServiceImpl

	@Mock
	private lateinit var postServiceImpl: PostServiceImpl

	@Mock
	private lateinit var postRepository: PostRepository

	@InjectMocks
	private lateinit var changePostServiceImpl: ChangePostServiceImpl

	private val post: Post = Instancio.create(Post::class.java)
	private val user: User = Instancio.create(User::class.java)

	@Nested
	@DisplayName("Delete Post Test")
	inner class DeleteTest {
		@Test
		@DisplayName("Success soft delete post by post id")
		fun should_VerifyCallSoftDeleteMethods_when_GivenPostId() {
			Mockito.`when`(postServiceImpl.validateReturnPost(any<Long>())).thenReturn(post)

			changePostServiceImpl.deletePostById(post.id)

			Mockito.verify(postRepository, Mockito.times(1)).deleteById(post.id)
		}

		@Test
		@DisplayName("Not found post")
		fun should_AssertPostNotFoundException_when_GivenPostId() {
			Mockito
				.`when`(postServiceImpl.validateReturnPost(any<Long>()))
				.thenThrow(PostNotFoundException(post.id))

			Assertions.assertThrows(
				PostNotFoundException::class.java
			) { changePostServiceImpl.deletePostById(post.id) }

			Mockito.verify(postRepository, Mockito.never()).deleteById(any<Long>())
		}

		@Test
		@DisplayName("Success soft delete post by user id")
		fun should_VerifyCallSoftDeleteMethods_when_GivenUserId() {
			Mockito.`when`(userServiceImpl.validateReturnUser(any<Long>())).thenReturn(user)

			changePostServiceImpl.deletePostByUserId(user.id)

			Mockito.verify(postRepository, Mockito.times(1)).deleteByUserId(user.id)
		}

		@Test
		@DisplayName("Not found user")
		fun should_AssertPostNotFoundException_when_GivenUserId() {
			Mockito
				.`when`(userServiceImpl.validateReturnUser(any<Long>()))
				.thenThrow(UserNotFoundException(user.id))

			Assertions.assertThrows(
				UserNotFoundException::class.java
			) { changePostServiceImpl.deletePostByUserId(user.id) }

			Mockito.verify(postRepository, Mockito.never()).deleteByUserId(any<Long>())
		}
	}

	@Nested
	@DisplayName("Update Post Test")
	inner class UpdateTest {
		private val updatePostRequest: UpdatePostRequest =
			Instancio.create(
				UpdatePostRequest::class.java
			)

		@Test
		@DisplayName("Success update post")
		fun should_AssertUpdatePostResponse_when_GivenPostIdAndUpdatePostRequest() {
			Mockito.`when`(postServiceImpl.validateReturnPost(any<Long>())).thenReturn(post)

			val updatePostResponse =
				changePostServiceImpl.updatePost(
					post.id,
					updatePostRequest
				)

			assertNotNull(updatePostResponse)
			assertEquals(post.id, updatePostResponse.postId)
			assertEquals(post.title, updatePostResponse.title)
			assertEquals(post.subTitle, updatePostResponse.subTitle)
			assertEquals(post.content, updatePostResponse.content)
			assertEquals(
				post.userId,
				updatePostResponse.userId
			)
		}

		@Test
		@DisplayName("Not found post")
		fun should_AssertPostNotFoundException_when_GivenPostIdAndUpdatePostRequest() {
			Mockito
				.`when`(postServiceImpl.validateReturnPost(any<Long>()))
				.thenThrow(PostNotFoundException(post.id))

			Assertions.assertThrows(
				PostNotFoundException::class.java
			) { changePostServiceImpl.updatePost(post.id, updatePostRequest) }
		}
	}

	@Nested
	@DisplayName("Create Post Test")
	inner class CreatePostTest {
		private val createPostRequest: CreatePostRequest =
			Instancio.create(
				CreatePostRequest::class.java
			)

		@Test
		@DisplayName("Success create post")
		fun should_AssertCreatePostResponse_when_GivenUserIdAndCreatePostRequest() {
			Mockito.`when`(userServiceImpl.validateReturnUser(any<Long>())).thenReturn(user)

			Mockito.`when`(postRepository.save(any<Post>())).thenReturn(post)

			val createPostResponse =
				changePostServiceImpl.createPost(
					user.id,
					createPostRequest
				)

			assertNotNull(createPostResponse)
			assertEquals(post.id, createPostResponse.postId)
			assertEquals(post.title, createPostResponse.title)
			assertEquals(post.subTitle, createPostResponse.subTitle)
			assertEquals(post.content, createPostResponse.content)
			assertEquals(
				post.userId,
				createPostResponse.userId
			)
		}

		@Test
		@DisplayName("Not found user")
		fun should_AssertUserNotFoundException_when_GivenUserIdAndCreatePostRequest() {
			Mockito
				.`when`(userServiceImpl.validateReturnUser(any<Long>()))
				.thenThrow(UserNotFoundException(user.id))

			Assertions.assertThrows(
				UserNotFoundException::class.java
			) { changePostServiceImpl.createPost(user.id, createPostRequest) }
		}
	}
}
