package com.example.demo.kotest.post.api

import com.example.demo.kotest.common.BaseIntegrationController
import com.example.demo.kotest.common.security.SecurityListenerFactory
import com.example.demo.post.api.PostController
import com.example.demo.post.application.ChangePostService
import com.example.demo.post.application.GetPostService
import com.example.demo.post.dto.serve.request.CreatePostRequest
import com.example.demo.post.dto.serve.request.GetExcludeUsersPostsRequest
import com.example.demo.post.dto.serve.request.UpdatePostRequest
import com.example.demo.post.dto.serve.response.CreatePostResponse
import com.example.demo.post.dto.serve.response.GetPostResponse
import com.example.demo.post.dto.serve.response.UpdatePostResponse
import com.example.demo.post.entity.Post
import com.example.demo.post.exception.PostNotFoundException
import com.ninjasquad.springmockk.MockkBean
import io.kotest.core.annotation.Tags
import io.mockk.every
import io.mockk.justRun
import org.instancio.Instancio
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

@ActiveProfiles("test")
@Tags("kotest-integration-test")
@WebMvcTest(
	PostController::class
)
class PostIntegrationControllerTests : BaseIntegrationController() {
	@MockkBean
	private lateinit var getPostService: GetPostService

	@MockkBean
	private lateinit var changePostService: ChangePostService

	val post: Post = Instancio.create(Post::class.java)
	val defaultPageable = Pageable.ofSize(1)

	init {
		initialize()

		Given("GET /api/v1/posts/{postId}") {

			When("Success GET /api/v1/posts/{postId}") {

				every { getPostService.getPostById(any<Long>()) } returns GetPostResponse.from(post)

				Then("Call GET /api/v1/posts/{postId}") {
					mockMvc
						.perform(
							MockMvcRequestBuilders
								.get("/api/v1/posts/{postId}", post.id)
								.with(SecurityMockMvcRequestPostProcessors.csrf())
								.contentType(MediaType.APPLICATION_JSON)
								.accept(MediaType.APPLICATION_JSON)
						).andExpect(MockMvcResultMatchers.status().isOk)
						.andExpect(MockMvcResultMatchers.jsonPath("$.code").value(commonStatus))
						.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(commonMessage))
						.andExpect(MockMvcResultMatchers.jsonPath("$.data.postId").value(post.id))
						.andExpect(MockMvcResultMatchers.jsonPath("$.data.title").value(post.title))
						.andExpect(MockMvcResultMatchers.jsonPath("$.data.subTitle").value(post.subTitle))
						.andExpect(MockMvcResultMatchers.jsonPath("$.data.content").value(post.content))
						.andExpect(MockMvcResultMatchers.jsonPath("$.data.userId").value(post.userId))
				}
			}

			When("Not Found Exception GET /api/v1/posts/{postId}") {
				val postNotFoundException = PostNotFoundException(post.id)

				every { getPostService.getPostById(any<Long>()) } throws postNotFoundException

				Then("Call GET /api/v1/posts/{postId}") {
					mockMvc
						.perform(
							MockMvcRequestBuilders
								.get("/api/v1/posts/{postId}", post.id)
								.with(SecurityMockMvcRequestPostProcessors.csrf())
								.contentType(MediaType.APPLICATION_JSON)
								.accept(MediaType.APPLICATION_JSON)
						).andExpect(MockMvcResultMatchers.status().isNotFound)
						.andExpect(MockMvcResultMatchers.jsonPath("$.code").value(HttpStatus.NOT_FOUND.value()))
						.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(postNotFoundException.message))
						.andExpect(MockMvcResultMatchers.jsonPath("$.errors").isEmpty)
				}
			}
		}

		Given("GET /api/v1/posts") {

			When("Success GET /api/v1/posts") {

				every { getPostService.getPostList(any<Pageable>()) } returns
					PageImpl(
						listOf(
							GetPostResponse.from(post)
						),
						defaultPageable,
						1
					)

				Then("Call GET /api/v1/posts") {
					mockMvc
						.perform(
							MockMvcRequestBuilders
								.get("/api/v1/posts")
								.with(SecurityMockMvcRequestPostProcessors.csrf())
								.contentType(MediaType.APPLICATION_JSON)
								.accept(MediaType.APPLICATION_JSON)
						).andExpect(MockMvcResultMatchers.status().isOk)
						.andExpect(MockMvcResultMatchers.jsonPath("$.code").value(commonStatus))
						.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(commonMessage))
						.andExpect(MockMvcResultMatchers.jsonPath("$.data.content[0].postId").value(post.id))
						.andExpect(MockMvcResultMatchers.jsonPath("$.data.content[0].title").value(post.title))
						.andExpect(MockMvcResultMatchers.jsonPath("$.data.content[0].subTitle").value(post.subTitle))
						.andExpect(MockMvcResultMatchers.jsonPath("$.data.content[0].content").value(post.content))
						.andExpect(MockMvcResultMatchers.jsonPath("$.data.content[0].userId").value(post.userId))
				}
			}

			When("Empty GET /api/v1/posts") {

				every { getPostService.getPostList(any<Pageable>()) } returns
					PageImpl(
						listOf(),
						defaultPageable,
						0
					)

				Then("Call GET /api/v1/posts") {
					mockMvc
						.perform(
							MockMvcRequestBuilders
								.get("/api/v1/posts")
								.with(SecurityMockMvcRequestPostProcessors.csrf())
								.contentType(MediaType.APPLICATION_JSON)
								.accept(MediaType.APPLICATION_JSON)
						).andExpect(MockMvcResultMatchers.status().isOk)
						.andExpect(MockMvcResultMatchers.jsonPath("$.code").value(commonStatus))
						.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(commonMessage))
						.andExpect(MockMvcResultMatchers.jsonPath("$.data.content").isEmpty)
				}
			}
		}

		Given("GET /api/v1/posts/exclude-users") {
			val getExcludeUsersPostsRequest: GetExcludeUsersPostsRequest =
				Instancio.create(
					GetExcludeUsersPostsRequest::class.java
				)

			When("Success GET /api/v1/posts/exclude-users") {

				every {
					getPostService.getExcludeUsersPostList(
						any<List<Long>>(),
						any<Pageable>()
					)
				} returns
					PageImpl(
						listOf(GetPostResponse.from(post)),
						defaultPageable,
						1
					)

				Then("Call GET /api/v1/posts/exclude-users") {
					mockMvc
						.perform(
							MockMvcRequestBuilders
								.get("/api/v1/posts/exclude-users")
								.with(SecurityMockMvcRequestPostProcessors.csrf())
								.param(
									"userIds",
									objectMapper.writeValueAsString(
										getExcludeUsersPostsRequest.userIds[0]
									)
								).contentType(MediaType.APPLICATION_JSON)
								.accept(MediaType.APPLICATION_JSON)
						).andExpect(MockMvcResultMatchers.status().isOk)
						.andExpect(MockMvcResultMatchers.jsonPath("$.code").value(commonStatus))
						.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(commonMessage))
						.andExpect(MockMvcResultMatchers.jsonPath("$.data.content[0].postId").value(post.id))
						.andExpect(MockMvcResultMatchers.jsonPath("$.data.content[0].title").value(post.title))
						.andExpect(MockMvcResultMatchers.jsonPath("$.data.content[0].subTitle").value(post.subTitle))
						.andExpect(MockMvcResultMatchers.jsonPath("$.data.content[0].content").value(post.content))
						.andExpect(
							MockMvcResultMatchers.jsonPath("$.data.content[0].userId").value(post.userId)
						)
				}
			}

			When("Empty GET /api/v1/posts/exclude-users") {

				every {
					getPostService.getExcludeUsersPostList(
						any<List<Long>>(),
						any<Pageable>()
					)
				} returns
					PageImpl(
						listOf(),
						defaultPageable,
						0
					)

				Then("Call GET /api/v1/posts/exclude-users") {
					mockMvc
						.perform(
							MockMvcRequestBuilders
								.get("/api/v1/posts/exclude-users")
								.with(SecurityMockMvcRequestPostProcessors.csrf())
								.param(
									"userIds",
									objectMapper.writeValueAsString(
										getExcludeUsersPostsRequest.userIds[0]
									)
								).contentType(MediaType.APPLICATION_JSON)
								.accept(MediaType.APPLICATION_JSON)
						).andExpect(MockMvcResultMatchers.status().isOk)
						.andExpect(MockMvcResultMatchers.jsonPath("$.code").value(commonStatus))
						.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(commonMessage))
						.andExpect(MockMvcResultMatchers.jsonPath("$.data.content").isEmpty)
				}
			}
		}

		Given("PUT /api/v1/posts") {
			val createPostRequest: CreatePostRequest =
				Instancio.create(
					CreatePostRequest::class.java
				)

			When("Success PUT /api/v1/posts") {

				every {
					changePostService.createPost(
						any<Long>(),
						any<CreatePostRequest>()
					)
				} returns CreatePostResponse.from(post)

				Then("Call PUT /api/v1/posts") {
					mockMvc
						.perform(
							MockMvcRequestBuilders
								.put("/api/v1/posts")
								.with(SecurityMockMvcRequestPostProcessors.csrf())
								.content(objectMapper.writeValueAsString(createPostRequest))
								.contentType(MediaType.APPLICATION_JSON)
								.accept(MediaType.APPLICATION_JSON)
						).andExpect(MockMvcResultMatchers.status().isCreated)
						.andExpect(MockMvcResultMatchers.jsonPath("$.code").value(commonStatus))
						.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(commonMessage))
						.andExpect(MockMvcResultMatchers.jsonPath("$.data.postId").value(post.id))
						.andExpect(MockMvcResultMatchers.jsonPath("$.data.title").value(post.title))
						.andExpect(MockMvcResultMatchers.jsonPath("$.data.subTitle").value(post.subTitle))
						.andExpect(MockMvcResultMatchers.jsonPath("$.data.content").value(post.content))
						.andExpect(
							MockMvcResultMatchers.jsonPath("$.data.userId").value(post.userId)
						)
				}
			}

			When("Field Valid Exception PUT /api/v1/posts") {
				val wrongCreatePostRequest =
					createPostRequest.copy(
						title = "",
						subTitle = ""
					)

				Then("Call PUT /api/v1/posts") {
					mockMvc
						.perform(
							MockMvcRequestBuilders
								.put("/api/v1/posts")
								.with(SecurityMockMvcRequestPostProcessors.csrf())
								.content(objectMapper.writeValueAsString(wrongCreatePostRequest))
								.contentType(MediaType.APPLICATION_JSON)
								.accept(MediaType.APPLICATION_JSON)
						).andExpect(MockMvcResultMatchers.status().isBadRequest)
						.andExpect(
							MockMvcResultMatchers.jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value())
						)
						// title:field title is blank, subTitle:field subTitle is blank,
						.andExpect(MockMvcResultMatchers.jsonPath("$.message").isString)
						.andExpect(MockMvcResultMatchers.jsonPath("$.errors").isNotEmpty)
				}
			}
		}

		Given("PATCH /api/v1/posts/{postId}") {
			val updatePostRequest: UpdatePostRequest =
				Instancio.create(
					UpdatePostRequest::class.java
				)

			When("Success PATCH /api/v1/posts/{postId}") {

				every {
					changePostService.updatePost(
						any<Long>(),
						any<UpdatePostRequest>()
					)
				} returns UpdatePostResponse.from(post)

				Then("Call PATCH /api/v1/posts/{postId}") {
					mockMvc
						.perform(
							MockMvcRequestBuilders
								.patch("/api/v1/posts/{postId}", post.id)
								.with(SecurityMockMvcRequestPostProcessors.csrf())
								.content(objectMapper.writeValueAsString(updatePostRequest))
								.contentType(MediaType.APPLICATION_JSON)
								.accept(MediaType.APPLICATION_JSON)
						).andExpect(MockMvcResultMatchers.status().isOk)
						.andExpect(MockMvcResultMatchers.jsonPath("$.code").value(commonStatus))
						.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(commonMessage))
						.andExpect(MockMvcResultMatchers.jsonPath("$.data.postId").value(post.id))
						.andExpect(MockMvcResultMatchers.jsonPath("$.data.title").value(post.title))
						.andExpect(MockMvcResultMatchers.jsonPath("$.data.subTitle").value(post.subTitle))
						.andExpect(MockMvcResultMatchers.jsonPath("$.data.content").value(post.content))
						.andExpect(
							MockMvcResultMatchers.jsonPath("$.data.userId").value(post.userId)
						)
				}
			}

			When("Field Valid Exception PATCH /api/v1/posts/{postId}") {
				val wrongUpdatePostRequest =
					updatePostRequest.copy(
						title = "",
						subTitle = ""
					)

				Then("Call PATCH /api/v1/posts/{postId}") {
					mockMvc
						.perform(
							MockMvcRequestBuilders
								.patch("/api/v1/posts/{postId}", post.id)
								.with(SecurityMockMvcRequestPostProcessors.csrf())
								.content(objectMapper.writeValueAsString(wrongUpdatePostRequest))
								.contentType(MediaType.APPLICATION_JSON)
								.accept(MediaType.APPLICATION_JSON)
						).andExpect(MockMvcResultMatchers.status().isBadRequest)
						.andExpect(
							MockMvcResultMatchers.jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value())
						) // subTitle:field subTitle is blank, title:field title is blank,
						.andExpect(MockMvcResultMatchers.jsonPath("$.message").isString)
						.andExpect(MockMvcResultMatchers.jsonPath("$.errors").isNotEmpty)
				}
			}

			When("Not Found Exception PATCH /api/v1/posts/{postId}") {
				val postNotFoundException =
					PostNotFoundException(
						post.id
					)

				every { changePostService.updatePost(any<Long>(), any<UpdatePostRequest>()) } throws postNotFoundException

				Then("Call PATCH /api/v1/posts/{postId}") {
					mockMvc
						.perform(
							MockMvcRequestBuilders
								.patch("/api/v1/posts/{postId}", post.id)
								.with(SecurityMockMvcRequestPostProcessors.csrf())
								.content(objectMapper.writeValueAsString(updatePostRequest))
								.contentType(MediaType.APPLICATION_JSON)
								.accept(MediaType.APPLICATION_JSON)
						).andExpect(MockMvcResultMatchers.status().isNotFound)
						.andExpect(MockMvcResultMatchers.jsonPath("$.code").value(HttpStatus.NOT_FOUND.value()))
						.andExpect(
							MockMvcResultMatchers.jsonPath("$.message").value(postNotFoundException.message)
						).andExpect(MockMvcResultMatchers.jsonPath("$.errors").isEmpty)
				}
			}
		}

		Given("DELETE /api/v1/posts/{postId}") {

			When("Success DELETE /api/v1/posts/{postId}") {

				justRun { changePostService.deletePostById(any<Long>()) }

				Then("Call DELETE /api/v1/posts/{postId}") {
					mockMvc
						.perform(
							MockMvcRequestBuilders
								.delete("/api/v1/posts/{postId}", post.id)
								.with(SecurityMockMvcRequestPostProcessors.csrf())
								.contentType(MediaType.APPLICATION_JSON)
								.accept(MediaType.APPLICATION_JSON)
						).andExpect(MockMvcResultMatchers.status().isNoContent)
						.andExpect(MockMvcResultMatchers.jsonPath("$.code").value(commonStatus))
						.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(commonMessage))
				}
			}
		}

		Given("Spring Security Context is not set.") {

			When("UnAuthorized Exception GET /api/v1/posts/{postId}") {

				Then("Call GET /api/v1/posts/{postId}").config(tags = setOf(SecurityListenerFactory.NonSecurityOption)) {
					mockMvc
						.perform(
							MockMvcRequestBuilders
								.get("/api/v1/posts/{postId}", post.id)
								.with(SecurityMockMvcRequestPostProcessors.csrf())
								.contentType(MediaType.APPLICATION_JSON)
								.accept(MediaType.APPLICATION_JSON)
						).andExpect(MockMvcResultMatchers.status().isUnauthorized)
				}
			}

			When("UnAuthorized Exception GET /api/v1/posts") {

				Then("Call GET /api/v1/posts").config(tags = setOf(SecurityListenerFactory.NonSecurityOption)) {
					mockMvc
						.perform(
							MockMvcRequestBuilders
								.get("/api/v1/posts")
								.with(SecurityMockMvcRequestPostProcessors.csrf())
								.contentType(MediaType.APPLICATION_JSON)
								.accept(MediaType.APPLICATION_JSON)
						).andExpect(MockMvcResultMatchers.status().isUnauthorized)
				}
			}

			When("UnAuthorized Exception GET /api/v1/posts/exclude-users") {

				Then("Call GET /api/v1/posts/exclude-users").config(tags = setOf(SecurityListenerFactory.NonSecurityOption)) {
					mockMvc
						.perform(
							MockMvcRequestBuilders
								.get("/api/v1/posts/exclude-users")
								.with(SecurityMockMvcRequestPostProcessors.csrf())
								.contentType(MediaType.APPLICATION_JSON)
								.accept(MediaType.APPLICATION_JSON)
						).andExpect(MockMvcResultMatchers.status().isUnauthorized)
				}
			}

			When("UnAuthorized Exception PUT /api/v1/posts") {

				Then("Call PUT /api/v1/posts").config(tags = setOf(SecurityListenerFactory.NonSecurityOption)) {
					mockMvc
						.perform(
							MockMvcRequestBuilders
								.put("/api/v1/posts")
								.with(SecurityMockMvcRequestPostProcessors.csrf())
								.contentType(MediaType.APPLICATION_JSON)
								.accept(MediaType.APPLICATION_JSON)
						).andExpect(MockMvcResultMatchers.status().isUnauthorized)
				}
			}

			When("UnAuthorized Exception PATCH /api/v1/posts/{postId}") {

				Then("Call PATCH /api/v1/posts/{postId}").config(tags = setOf(SecurityListenerFactory.NonSecurityOption)) {
					mockMvc
						.perform(
							MockMvcRequestBuilders
								.patch("/api/v1/posts/{postId}", post.id)
								.with(SecurityMockMvcRequestPostProcessors.csrf())
								.contentType(MediaType.APPLICATION_JSON)
								.accept(MediaType.APPLICATION_JSON)
						).andExpect(MockMvcResultMatchers.status().isUnauthorized)
				}
			}

			When("UnAuthorized Exception DELETE /api/v1/posts/{postId}") {

				Then("Call DELETE /api/v1/posts/{postId}").config(tags = setOf(SecurityListenerFactory.NonSecurityOption)) {
					mockMvc
						.perform(
							MockMvcRequestBuilders
								.delete("/api/v1/posts/{postId}", post.id)
								.with(SecurityMockMvcRequestPostProcessors.csrf())
								.contentType(MediaType.APPLICATION_JSON)
								.accept(MediaType.APPLICATION_JSON)
						).andExpect(MockMvcResultMatchers.status().isUnauthorized)
				}
			}
		}
	}
}
