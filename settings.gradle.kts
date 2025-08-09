rootProject.name = "demo"

buildCache {
	local {
		directory = File(rootDir, ".gradle/build-cache")
	}
}

pluginManagement {
	val kotlinVersion: String by settings
	val springBootVersion: String by settings
	val springDependencyManagementVersion: String by settings
	val ktLintGradleVersion: String by settings
	val detektVersion: String by settings

	resolutionStrategy {
		eachPlugin {
			when (requested.id.id) {
				"org.springframework.boot" -> useVersion(springBootVersion)
				"org.jetbrains.kotlin.jvm" -> useVersion(kotlinVersion)
				"org.jetbrains.kotlin.kapt" -> useVersion(kotlinVersion)
				"org.jetbrains.kotlin.plugin.spring" -> useVersion(kotlinVersion)
				"org.jetbrains.kotlin.plugin.jpa" -> useVersion(kotlinVersion)
				"io.spring.dependency-management" -> useVersion(springDependencyManagementVersion)
				"org.jlleitschuh.gradle.ktlint" -> useVersion(ktLintGradleVersion)
				"io.gitlab.arturbosch.detekt" -> useVersion(detektVersion)
			}
		}
	}
}
