package com.example.demo.user.entity

import com.example.demo.common.entity.BaseSoftDeleteEntity
import com.example.demo.user.constant.UserRole
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Index
import jakarta.persistence.Table
import org.hibernate.annotations.SQLDelete
import org.hibernate.annotations.SQLRestriction
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

@Entity
@Table(
	name = "\"user\"",
	indexes = [
		Index(name = "idx_user_deleted_dt", columnList = "deleted_dt"),
		Index(name = "idx_user_role_deleted", columnList = "deleted_dt, role")
	]
)
@SQLDelete(sql = "UPDATE \"user\" SET deleted_dt = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_dt IS NULL")
data class User(
	@Column(nullable = false)
	var name: String,
	@Column(
		unique = true,
		nullable = false,
		updatable = false
	)
	var email: String,
	@Column(nullable = false)
	var password: String,
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	var role: UserRole = UserRole.USER
) : BaseSoftDeleteEntity() {
	fun update(
		name: String,
		role: UserRole
	): User {
		this.name = name
		this.role = role
		return this
	}

	fun encodePassword(bCryptPasswordEncoder: BCryptPasswordEncoder): User {
		this.password = bCryptPasswordEncoder.encode(this.password)
		return this
	}

	fun validatePassword(
		password: String,
		bCryptPasswordEncoder: BCryptPasswordEncoder
	): Boolean = bCryptPasswordEncoder.matches(password, this.password)
}
