package com.example.demo.post.dto.command

data class CreatePostCommand(
	val title: String,
	val subTitle: String,
	val content: String
)
