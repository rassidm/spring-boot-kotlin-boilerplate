package com.example.demo.user.repository

import com.example.demo.user.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface UserRepository :
	JpaRepository<User, Long>,
	CustomUserRepository {
	fun findOneById(userId: Long): User?

	fun findOneByEmail(email: String): User?

	fun existsByEmail(email: String): Boolean

	@Modifying
	@Query(
		value = """
            DELETE FROM "user"
            WHERE id = :userId
        """,
		nativeQuery = true
	)
	fun hardDeleteById(
		@Param("userId") userId: Long
	): Int
}
