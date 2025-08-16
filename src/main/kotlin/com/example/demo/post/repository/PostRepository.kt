package com.example.demo.post.repository

import com.example.demo.post.entity.Post
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface PostRepository :
	JpaRepository<Post, Long>,
	CustomPostRepository {
	fun findOneById(postId: Long): Post?

	fun findOneByUserId(userId: Long): Post?

	fun deleteByUserId(userId: Long)

	@Modifying
	@Query(
		value = """
            DELETE FROM "post"
            WHERE user_id = :userId
        """,
		nativeQuery = true
	)
	fun hardDeleteByUserId(
		@Param("userId") userId: Long
	): Int
}
