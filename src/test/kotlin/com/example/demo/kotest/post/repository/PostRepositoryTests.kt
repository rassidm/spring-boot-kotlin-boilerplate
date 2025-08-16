package com.example.demo.kotest.post.repository

import com.example.demo.common.config.JpaAuditConfig
import com.example.demo.common.config.QueryDslConfig
import com.example.demo.post.dto.serve.request.UpdatePostRequest
import com.example.demo.post.entity.Post
import com.example.demo.post.repository.PostRepository
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.instancio.Instancio
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@Tags("kotest-unit-test")
@Import(value = [QueryDslConfig::class, JpaAuditConfig::class])
@DataJpaTest
class PostRepositoryTests(
	@Autowired
	private val postRepository: PostRepository
) : DescribeSpec({
		lateinit var postEntity: Post
		val defaultPostTitle = "unit test"
		val defaultPostSubTitle = "Post Repository Test"
		val defaultPostContent = "default value for post repository testing"

		beforeContainer {
			postEntity =
				Post(
					defaultPostTitle,
					defaultPostSubTitle,
					defaultPostContent,
					1L
				)
		}

		describe("Create post") {

			context("Save post") {
				val createPost = postRepository.save(postEntity)

				it("Assert Post Entity") {
					createPost.id shouldBe postEntity.id
					createPost.title shouldBe postEntity.title
					createPost.subTitle shouldBe postEntity.subTitle
					createPost.content shouldBe postEntity.content
					createPost.userId shouldBe postEntity.userId
				}
			}
		}

		describe("Update post") {

			context("Save post") {
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

				it("Assert Post Entity") {
					afterUpdatePost.title shouldBe updatePostRequest.title
					afterUpdatePost.subTitle shouldBe updatePostRequest.subTitle
					afterUpdatePost.content shouldBe updatePostRequest.content
				}
			}
		}

		describe("Delete Post") {

			context("Call deleteById") {
				val beforeDeletePost = postRepository.save(postEntity)

				postRepository.deleteById(beforeDeletePost.id)

				val afterDeletePost: Post? =
					postRepository
						.findOneById(beforeDeletePost.id)

				it("Assert Null") {
					afterDeletePost.shouldBeNull()
				}
			}

			context("Call deleteByUserId") {
				val beforeDeletePost = postRepository.save(postEntity)

				postRepository.deleteByUserId(beforeDeletePost.userId)

				val afterDeletePost: Post? =
					postRepository
						.findOneById(beforeDeletePost.id)

				it("Assert Null") {
					afterDeletePost.shouldBeNull()
				}
			}
		}

		describe("Find Post By Id") {

			context("Call findOneById") {
				val beforeFindPost = postRepository.save(postEntity)

				val afterFindPost: Post =
					requireNotNull(
						postRepository
							.findOneById(beforeFindPost.id)
					) {
						"Post must not be null"
					}

				it("Assert Post Entity") {
					afterFindPost.id shouldBe beforeFindPost.id
					afterFindPost.title shouldBe beforeFindPost.title
					afterFindPost.subTitle shouldBe beforeFindPost.subTitle
					afterFindPost.content shouldBe beforeFindPost.content
					afterFindPost.userId shouldBe beforeFindPost.userId
				}
			}
		}

		describe("Find Post By userId") {

			context("Call findOneByUserId") {
				val beforeFindPost = postRepository.save(postEntity)

				val afterFindPost: Post =
					requireNotNull(
						postRepository
							.findOneByUserId(beforeFindPost.userId)
					) {
						"Post must not be null"
					}

				it("Assert Post Entity") {
					afterFindPost.id shouldBe beforeFindPost.id
					afterFindPost.title shouldBe beforeFindPost.title
					afterFindPost.subTitle shouldBe beforeFindPost.subTitle
					afterFindPost.content shouldBe beforeFindPost.content
					afterFindPost.userId shouldBe beforeFindPost.userId
				}
			}
		}
	})
