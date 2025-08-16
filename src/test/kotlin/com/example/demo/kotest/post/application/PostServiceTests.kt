package com.example.demo.kotest.post.application

import com.example.demo.post.application.PostService
import com.example.demo.post.entity.Post
import com.example.demo.post.exception.PostNotFoundException
import com.example.demo.post.repository.PostRepository
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.instancio.Instancio
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@Tags("kotest-unit-test")
class PostServiceTests :
	BehaviorSpec({
		val postService = mockk<PostService>()
		val postRepository = mockk<PostRepository>()

		val post: Post = Instancio.create(Post::class.java)

		Given("Validate and return Post entity") {

			When("Success validate and get post entity") {

				every { postRepository.findOneById(any<Long>()) } returns post

				every { postService.validateReturnPost(any<Long>()) } returns post

				val validatePost = postService.validateReturnPost(post.id)

				then("Validate & Get user entity") {
					validatePost shouldNotBeNull {
						id shouldBe post.id
						title shouldBe post.title
						subTitle shouldBe post.subTitle
						content shouldBe post.content
						userId shouldBe post.userId
					}
				}
			}

			When("Post Not Found Exception") {

				every { postRepository.findOneById(any<Long>()) } returns null

				every { postService.validateReturnPost(any<Long>()) } throws PostNotFoundException(post.id)

				shouldThrowExactly<PostNotFoundException> {
					postService.validateReturnPost(post.id)
				}
			}
		}
	})
