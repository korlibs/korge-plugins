plugins {
	java
	`java-gradle-plugin`
	kotlin("jvm")
	maven
	`maven-publish`
	id("com.gradle.plugin-publish")
}

pluginBundle {
	website = "https://korge.soywiz.com/"
	vcsUrl = "https://github.com/korlibs/korge-plugins"
	tags = listOf("korge", "game", "engine", "game engine", "multiplatform", "kotlin")
}

gradlePlugin {
	plugins {
		create("korge") {
			id = "com.soywiz.korge"
			displayName = "Korge"
			description = "Multiplatform Game Engine for Kotlin"
			implementationClass = "com.soywiz.korge.gradle.KorgeGradlePlugin"
		}
	}
}

tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class).all {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

val kotlinVersion: String by project

dependencies {
	implementation(project(":korge-build"))

	implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
	implementation("com.moowork.gradle:gradle-node-plugin:1.2.0")
	implementation("net.sf.proguard:proguard-gradle:6.2.2")

	implementation(gradleApi())
	implementation(localGroovy())
}
