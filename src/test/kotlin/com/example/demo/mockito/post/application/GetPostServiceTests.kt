package com.example.demo.mockito.post.application

import com.example.demo.post.application.impl.GetPostServiceImpl
import com.example.demo.post.dto.request.GetExcludeUsersPostsRequest
import com.example.demo.post.dto.response.GetPostResponse
import com.example.demo.post.entity.Post
import com.example.demo.post.exception.PostNotFoundException
import com.example.demo.post.repository.PostRepository
import org.assertj.core.api.Assertions.assertThat
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
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@Tag("mockito-unit-test")
@DisplayName("Mockito Unit - Get Post Service Test")
@ExtendWith(
	MockitoExtension::class
)
class GetPostServiceTests {
	@Mock
	private lateinit var postRepository: PostRepository

	@InjectMocks
	private lateinit var getPostServiceImpl: GetPostServiceImpl

	private val defaultPageable = Pageable.ofSize(1)

	private val post: Post = Instancio.create(Post::class.java)

	@Nested
	@DisplayName("Get Post By Id Test")
	inner class GetPostByIdTest {
		@Test
		@DisplayName("Success get post by id")
		fun should_AssertGetPostResponse_when_GivenPostId() {
			whenever(postRepository.findOneById(any<Long>())).thenReturn(post)

			val getPostResponse =
				getPostServiceImpl.getPostById(
					post.id
				)

			assertNotNull(getPostResponse)
			assertEquals(post.id, getPostResponse.postId)
			assertEquals(post.title, getPostResponse.title)
			assertEquals(post.subTitle, getPostResponse.subTitle)
			assertEquals(post.content, getPostResponse.content)
			assertEquals(
				post.userId,
				getPostResponse.userId
			)
		}

		@Test
		@DisplayName("Not found post")
		fun should_AssertPostNotFoundException_when_GivenPostId() {
			whenever(postRepository.findOneById(any<Long>()))
				.thenThrow(PostNotFoundException(post.id))

			Assertions.assertThrows(
				PostNotFoundException::class.java
			) { getPostServiceImpl.getPostById(post.id) }
		}
	}

	@Nested
	@DisplayName("Get Post List Test")
	inner class GetPostListTest {
		@Test
		@DisplayName("Success get post list")
		fun should_AssertPageOfGetPostResponse_when_GivenDefaultPageable() {
			whenever(
				postRepository.findAll(any<Pageable>())
			).thenReturn(PageImpl(listOf(post), defaultPageable, 1))

			val getPostResponseList =
				getPostServiceImpl.getPostList(
					defaultPageable
				)

			assertThat(getPostResponseList).isNotEmpty()
			assertEquals(post.id, getPostResponseList.content[0].postId)
			assertEquals(post.title, getPostResponseList.content[0].title)
			assertEquals(
				post.subTitle,
				getPostResponseList.content[0].subTitle
			)
			assertEquals(post.content, getPostResponseList.content[0].content)
			assertEquals(
				post.userId,
				getPostResponseList.content[0].userId
			)
		}

		@Test
		@DisplayName("Get post list is empty")
		fun should_AssertPageOfGetPostResponseIsEmpty_when_GivenDefaultPageable() {
			whenever(
				postRepository.findAll(
					any<Pageable>()
				)
			).thenReturn(PageImpl(listOf(), defaultPageable, 0))

			val getPostResponseList =
				getPostServiceImpl.getPostList(
					defaultPageable
				)

			assertThat(getPostResponseList).isEmpty()
			assertEquals(getPostResponseList.totalElements, 0)
		}
	}

	@Nested
	@DisplayName("Get Exclude Users Post List Test")
	inner class GetExcludeUsersPostsTest {
		private val getExcludeUsersPostsRequest: GetExcludeUsersPostsRequest =
			Instancio.create(
				GetExcludeUsersPostsRequest::class.java
			)

		@Test
		@DisplayName("Success get exclude users post list")
		fun should_AssertPageOfGetPostResponse_when_GivenDefaultPageableAndGetExcludeUsersPostsRequest() {
			whenever(
				postRepository.getExcludeUsersPosts(
					any<List<Long>>(),
					any<Pageable>()
				)
			).thenReturn(PageImpl(listOf(GetPostResponse.from(post)), defaultPageable, 1))

			val getPostResponseList =
				getPostServiceImpl.getExcludeUsersPostList(
					getExcludeUsersPostsRequest.userIds,
					defaultPageable
				)

			assertThat(getPostResponseList).isNotEmpty()
			assertEquals(post.id, getPostResponseList.content[0].postId)
			assertEquals(post.title, getPostResponseList.content[0].title)
			assertEquals(
				post.subTitle,
				getPostResponseList.content[0].subTitle
			)
			assertEquals(post.content, getPostResponseList.content[0].content)
			assertEquals(
				post.userId,
				getPostResponseList.content[0].userId
			)
		}

		@Test
		@DisplayName("Get get exclude users post list is empty")
		fun should_AssertPageOfGetPostResponseIsEmpty_when_GivenDefaultPageableAndGetExcludeUsersPostsRequest() {
			whenever(
				postRepository.getExcludeUsersPosts(
					any<List<Long>>(),
					any<Pageable>()
				)
			).thenReturn(PageImpl(listOf(), defaultPageable, 0))

			val getPostResponseList =
				getPostServiceImpl.getExcludeUsersPostList(
					getExcludeUsersPostsRequest.userIds,
					defaultPageable
				)

			assertThat(getPostResponseList.content).isEmpty()
			assertEquals(getPostResponseList.totalElements, 0)
		}
	}
}
