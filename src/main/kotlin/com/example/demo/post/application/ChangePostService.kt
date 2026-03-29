package com.example.demo.post.application

import com.example.demo.post.dto.command.CreatePostCommand
import com.example.demo.post.dto.command.UpdatePostCommand
import com.example.demo.post.dto.response.CreatePostResponse
import com.example.demo.post.dto.response.UpdatePostResponse

interface ChangePostService {
	fun updatePost(
		postId: Long,
		command: UpdatePostCommand
	): UpdatePostResponse

	fun createPost(
		userId: Long,
		command: CreatePostCommand
	): CreatePostResponse

	fun deletePostById(postId: Long)

	fun deletePostByUserId(userId: Long)

	fun hardDeletePostByUserId(userId: Long)
}
