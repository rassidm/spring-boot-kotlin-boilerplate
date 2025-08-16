package com.example.demo.post.application.impl

import com.example.demo.post.application.ChangePostService
import com.example.demo.post.application.PostService
import com.example.demo.post.dto.serve.request.CreatePostRequest
import com.example.demo.post.dto.serve.request.UpdatePostRequest
import com.example.demo.post.dto.serve.response.CreatePostResponse
import com.example.demo.post.dto.serve.response.UpdatePostResponse
import com.example.demo.post.entity.Post
import com.example.demo.post.repository.PostRepository
import com.example.demo.user.application.UserService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class ChangePostServiceImpl(
	private val postRepository: PostRepository,
	private val postService: PostService,
	private val userService: UserService
) : ChangePostService {
	override fun createPost(
		userId: Long,
		createPostRequest: CreatePostRequest
	): CreatePostResponse {
		val user = userService.validateReturnUser(userId)

		val post =
			postRepository.save(
				Post(
					title = createPostRequest.title,
					subTitle = createPostRequest.subTitle,
					content = createPostRequest.content,
					userId = user.id
				)
			)

		return post.let(CreatePostResponse::from)
	}

	override fun updatePost(
		postId: Long,
		updatePostRequest: UpdatePostRequest
	): UpdatePostResponse {
		val post: Post =
			postService
				.validateReturnPost(postId)
				.update(
					title = updatePostRequest.title,
					subTitle = updatePostRequest.subTitle,
					content = updatePostRequest.content
				)

		return post.let(UpdatePostResponse::from)
	}

	override fun deletePostById(postId: Long) {
		val post = postService.validateReturnPost(postId)

		postRepository.deleteById(post.id)
	}

	override fun deletePostByUserId(userId: Long) {
		val user = userService.validateReturnUser(userId)

		postRepository.deleteByUserId(user.id)
	}

	override fun hardDeletePostByUserId(userId: Long) {
		postRepository.hardDeleteByUserId(userId)
	}
}
