package com.example.demo.post.repository

import com.example.demo.post.dto.response.GetPostResponse
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface CustomPostRepository {
	fun getExcludeUsersPosts(
		userIds: List<Long>,
		pageable: Pageable
	): Page<GetPostResponse>
}
