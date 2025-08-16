package com.example.demo.post.repository.impl

import com.example.demo.post.dto.serve.response.GetPostResponse
import com.example.demo.post.entity.QPost.post
import com.example.demo.post.repository.CustomPostRepository
import com.querydsl.core.types.Projections
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.transaction.annotation.Transactional

open class CustomPostRepositoryImpl(
	private val jpaQueryFactory: JPAQueryFactory
) : CustomPostRepository {
	@Transactional(readOnly = true)
	override fun getExcludeUsersPosts(
		userIds: List<Long>,
		pageable: Pageable
	): Page<GetPostResponse> {
		val whereCondition =
			post.userId
				.notIn(userIds)

		val content =
			jpaQueryFactory
				.select(
					Projections.constructor(
						GetPostResponse::class.java,
						post.id,
						post.title,
						post.subTitle,
						post.content,
						post.userId
					)
				).from(post)
				.where(whereCondition)
				.offset(pageable.offset)
				.limit(pageable.pageSize.toLong())
				.fetch()

		val totalCount =
			jpaQueryFactory
				.select(post.count())
				.from(post)
				.where(whereCondition)
				.fetchOne() ?: 0L

		return PageImpl(content, pageable, totalCount)
	}
}
