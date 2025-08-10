package com.example.demo.user.event

import com.example.demo.user.entity.User

sealed class UserEvent {
	data class WelcomeSignUpEvent(
		val email: String,
		val name: String
	) : UserEvent() {
		companion object {
			fun from(user: User): WelcomeSignUpEvent =
				with(user) {
					WelcomeSignUpEvent(
						email = email,
						name = name
					)
				}
		}
	}
}
