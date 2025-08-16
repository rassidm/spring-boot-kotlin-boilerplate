package com.example.demo.post.dto.serve.response

import com.example.demo.post.entity.Post
import io.swagger.v3.oas.annotations.media.Schema

data class GetPostResponse(
	@field:Schema(description = "Post Id", nullable = false)
	val postId: Long,
	@field:Schema(description = "Post Title", nullable = false)
	val title: String,
	@field:Schema(description = "Post Sub Title", nullable = false)
	val subTitle: String,
	@field:Schema(description = "Post Content", nullable = false)
	val content: String,
	@field:Schema(description = "User Id", nullable = false)
	val userId: Long
) {
	companion object {
		fun from(post: Post): GetPostResponse =
			with(post) {
				GetPostResponse(
					postId = id,
					title = title,
					subTitle = subTitle,
					content = content,
					userId = userId
				)
			}
	}
}
