package com.example.demo.mockito.post.api

import com.example.demo.post.api.PostController
import com.example.demo.post.application.impl.ChangePostServiceImpl
import com.example.demo.post.application.impl.GetPostServiceImpl
import com.example.demo.post.dto.serve.request.CreatePostRequest
import com.example.demo.post.dto.serve.request.GetExcludeUsersPostsRequest
import com.example.demo.post.dto.serve.request.UpdatePostRequest
import com.example.demo.post.dto.serve.response.CreatePostResponse
import com.example.demo.post.dto.serve.response.GetPostResponse
import com.example.demo.post.dto.serve.response.UpdatePostResponse
import com.example.demo.post.entity.Post
import com.example.demo.security.SecurityUserItem
import org.assertj.core.api.Assertions.assertThat
import org.instancio.Instancio
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@Tag("mockito-unit-test")
@DisplayName("Mockito Unit - Post Controller Test")
@ExtendWith(
	MockitoExtension::class
)
class PostControllerTests {
	@InjectMocks
	private lateinit var postController: PostController

	@Mock
	private lateinit var getPostServiceImpl: GetPostServiceImpl

	@Mock
	private lateinit var changePostServiceImpl: ChangePostServiceImpl

	private val defaultPageable = Pageable.ofSize(1)

	private val post: Post = Instancio.create(Post::class.java)

	@Test
	@DisplayName("Get post by id")
	fun should_AssertGetPostResponse_when_GivenPostId() {
		whenever(getPostServiceImpl.getPostById(any<Long>()))
			.thenReturn(GetPostResponse.from(post))

		val response =
			postController.getPostById(
				post.id
			)

		assertNotNull(response)
		assertNotNull(response.body)
		assertEquals(HttpStatus.OK, response.statusCode)

		val body =
			requireNotNull(response.body) {
				"Response body must not be null"
			}

		assertEquals(post.id, body.postId)
		assertEquals(post.title, body.title)
		assertEquals(post.subTitle, body.subTitle)
		assertEquals(post.content, body.content)
		assertEquals(post.userId, body.userId)
	}

	@Test
	@DisplayName("Get post list")
	fun should_AssertPageOfGetPostResponse_when_GivenDefaultPageable() {
		whenever(getPostServiceImpl.getPostList(any<Pageable>()))
			.thenReturn(PageImpl(listOf(GetPostResponse.from(post)), defaultPageable, 1))

		val response =
			postController.getPostList(
				defaultPageable
			)

		assertNotNull(response)
		assertNotNull(response.body)
		assertEquals(HttpStatus.OK, response.statusCode)

		val body =
			requireNotNull(response.body) {
				"Response body must not be null"
			}

		assertThat(body).isNotEmpty()
		assertEquals(post.id, body.content[0].postId)
		assertEquals(post.title, body.content[0].title)
		assertEquals(post.subTitle, body.content[0].subTitle)
		assertEquals(post.content, body.content[0].content)
		assertEquals(post.userId, body.content[0].userId)
	}

	@Test
	@DisplayName("Get exclude users post list")
	fun should_AssertPageOfGetPostResponse_when_GivenDefaultPageableAndGetExcludeUsersPostsRequest() {
		val getExcludeUsersPostsRequest =
			Instancio.create(
				GetExcludeUsersPostsRequest::class.java
			)

		whenever(getPostServiceImpl.getExcludeUsersPostList(any<List<Long>>(), any<Pageable>()))
			.thenReturn(PageImpl(listOf(GetPostResponse.from(post)), defaultPageable, 1))

		val response =
			postController.getExcludeUsersPostList(
				getExcludeUsersPostsRequest.userIds,
				defaultPageable
			)

		assertNotNull(response)
		assertNotNull(response.body)
		assertEquals(HttpStatus.OK, response.statusCode)

		val body =
			requireNotNull(response.body) {
				"Response body must not be null"
			}

		assertThat(body).isNotEmpty()
		assertEquals(post.id, body.content[0].postId)
		assertEquals(post.title, body.content[0].title)
		assertEquals(post.subTitle, body.content[0].subTitle)
		assertEquals(post.content, body.content[0].content)
		assertEquals(post.userId, body.content[0].userId)
	}

	@Test
	@DisplayName("Create post")
	fun should_AssertCreatePostResponse_when_GivenUserIdAndCreatePostRequest() {
		val createPostRequest =
			Instancio.create(
				CreatePostRequest::class.java
			)
		val securityUserItem =
			Instancio.create(
				SecurityUserItem::class.java
			)

		whenever(changePostServiceImpl.createPost(any<Long>(), any<CreatePostRequest>()))
			.thenReturn(CreatePostResponse.from(post))

		val response =
			postController.createPost(
				createPostRequest,
				securityUserItem
			)

		assertNotNull(response)
		assertNotNull(response.body)
		assertEquals(HttpStatus.CREATED, response.statusCode)

		val body =
			requireNotNull(response.body) {
				"Response body must not be null"
			}

		assertEquals(post.id, body.postId)
		assertEquals(post.title, body.title)
		assertEquals(post.subTitle, body.subTitle)
		assertEquals(post.content, body.content)
		assertEquals(post.userId, body.userId)
	}

	@Test
	@DisplayName("Update post")
	fun should_AssertUpdatePostResponse_when_GivenPostIdAndUpdatePostRequest() {
		val updatePostRequest =
			Instancio.create(
				UpdatePostRequest::class.java
			)

		whenever(changePostServiceImpl.updatePost(any<Long>(), any<UpdatePostRequest>()))
			.thenReturn(UpdatePostResponse.from(post))

		val response =
			postController.updatePost(
				updatePostRequest,
				post.id
			)

		assertNotNull(response)
		assertNotNull(response.body)
		assertEquals(HttpStatus.OK, response.statusCode)

		val body =
			requireNotNull(response.body) {
				"Response body must not be null"
			}

		assertEquals(post.id, body.postId)
		assertEquals(post.title, body.title)
		assertEquals(post.subTitle, body.subTitle)
		assertEquals(post.content, body.content)
		assertEquals(post.userId, body.userId)
	}

	@Test
	@DisplayName("Delete post")
	fun should_VerifyCallDeletePostMethod_when_GivenPostId() {
		val response = postController.deletePost(post.id)

		assertNotNull(response)
		assertNull(response.body)
		assertEquals(HttpStatus.NO_CONTENT, response.statusCode)

		verify(changePostServiceImpl, times(1)).deletePostById(any<Long>())
	}
}
