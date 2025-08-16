package com.example.demo.kotest.post.application

import com.example.demo.post.application.ChangePostService
import com.example.demo.post.application.PostService
import com.example.demo.post.dto.serve.request.CreatePostRequest
import com.example.demo.post.dto.serve.request.UpdatePostRequest
import com.example.demo.post.dto.serve.response.CreatePostResponse
import com.example.demo.post.dto.serve.response.UpdatePostResponse
import com.example.demo.post.entity.Post
import com.example.demo.post.exception.PostNotFoundException
import com.example.demo.post.repository.PostRepository
import com.example.demo.user.application.UserService
import com.example.demo.user.entity.User
import com.example.demo.user.exception.UserNotFoundException
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import org.instancio.Instancio
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@Tags("kotest-unit-test")
class ChangePostServiceTests :
	BehaviorSpec({
		val userService = mockk<UserService>()
		val postService = mockk<PostService>()
		val postRepository = mockk<PostRepository>()
		val changePostService = mockk<ChangePostService>()

		val post: Post = Instancio.create(Post::class.java)
		val user: User = Instancio.create(User::class.java)

		Given("Delete Post By Id") {

			When("Success Soft Delete Post") {

				every { postService.validateReturnPost(any<Long>()) } returns post
				every { postRepository.save(any<Post>()) } returns post

				justRun {
					changePostService.deletePostById(any<Long>())
				}

				changePostService.deletePostById(post.id)

				Then("Verify Soft Delete Call") {
					verify(exactly = 1) {
						changePostService.deletePostById(post.id)
					}
				}
			}

			When("Post Not Found on Delete") {

				every { postService.validateReturnPost(any<Long>()) } throws PostNotFoundException(post.id)

				every {
					changePostService.deletePostById(any<Long>())
				} throws PostNotFoundException(post.id)

				shouldThrowExactly<PostNotFoundException> {
					changePostService.deletePostById(post.id)
				}
			}
		}

		Given("Delete Post By User Id") {

			When("Success Soft Delete Post") {

				every { userService.validateReturnUser(any<Long>()) } returns user
				every { postRepository.save(any<Post>()) } returns post

				justRun {
					changePostService.deletePostByUserId(any<Long>())
				}

				changePostService.deletePostByUserId(user.id)

				Then("Verify Soft Delete Call") {
					verify(exactly = 1) {
						changePostService.deletePostByUserId(user.id)
					}
				}
			}

			When("User Not Found on Delete") {

				every { userService.validateReturnUser(any<Long>()) } throws UserNotFoundException(user.id)

				every {
					changePostService.deletePostByUserId(any<Long>())
				} throws UserNotFoundException(user.id)

				shouldThrowExactly<UserNotFoundException> {
					changePostService.deletePostByUserId(user.id)
				}
			}
		}

		Given("Update Post") {
			val updatePostRequest =
				Instancio.create(
					UpdatePostRequest::class.java
				)

			When("Success Update Post") {

				every { postService.validateReturnPost(any<Long>()) } returns post

				every {
					changePostService.updatePost(
						any<Long>(),
						any<UpdatePostRequest>()
					)
				} returns
					UpdatePostResponse.from(
						post.apply {
							title = updatePostRequest.title
							subTitle = updatePostRequest.subTitle
							content = updatePostRequest.content
						}
					)

				val updatePostResponse =
					changePostService.updatePost(
						post.id,
						updatePostRequest
					)

				Then("Assert Post Entity") {
					updatePostResponse shouldNotBeNull {
						title shouldBe updatePostRequest.title
						subTitle shouldBe updatePostRequest.subTitle
						content shouldBe updatePostRequest.content
					}
				}
			}

			When("Post Not Found Exception") {

				every { postService.validateReturnPost(any<Long>()) } throws PostNotFoundException(post.id)

				every {
					changePostService.updatePost(
						any<Long>(),
						any<UpdatePostRequest>()
					)
				} throws PostNotFoundException(post.id)

				shouldThrowExactly<PostNotFoundException> { changePostService.updatePost(post.id, updatePostRequest) }
			}
		}

		Given("Create Post") {
			val createPostRequest: CreatePostRequest =
				Instancio.create(
					CreatePostRequest::class.java
				)

			When("Success Create Post") {

				every { userService.validateReturnUser(any<Long>()) } returns user

				every { postRepository.save(any<Post>()) } returns post

				every { changePostService.createPost(any<Long>(), any<CreatePostRequest>()) } returns CreatePostResponse.from(post)

				val createPostResponse = changePostService.createPost(user.id, createPostRequest)

				createPostResponse shouldNotBeNull {
					postId shouldBe post.id
					title shouldBe post.title
					subTitle shouldBe post.subTitle
					content shouldBe post.content
					userId shouldBe post.userId
				}
			}

			When("User Not Found Exception") {

				every { userService.validateReturnUser(any<Long>()) } throws UserNotFoundException(user.id)

				shouldThrowExactly<UserNotFoundException> { userService.validateReturnUser(user.id) }
			}
		}
	})
