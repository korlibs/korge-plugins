import java.net.*
import java.util.*
import java.io.*

buildscript {
	val kotlinVersion: String by project
	val isKotlinDev = kotlinVersion.contains("-release")
    val isKotlinEap = kotlinVersion.contains("-eap") || kotlinVersion.contains("-M")
	repositories {
		mavenLocal()
		maven { url = uri("https://plugins.gradle.org/m2/") }
		if (isKotlinDev || isKotlinEap) {
            maven { url = uri("https://dl.bintray.com/kotlin/kotlin-eap") }
			maven { url = uri("https://dl.bintray.com/kotlin/kotlin-dev") }
		}
	}
	dependencies {
		classpath("com.gradle.publish:plugin-publish-plugin:0.10.1")
		classpath("org.jetbrains.kotlin.jvm:org.jetbrains.kotlin.jvm.gradle.plugin:$kotlinVersion")
    }
}

plugins {
	id("com.moowork.node") version "1.3.1"
	id("com.gradle.plugin-publish") version "0.10.1" apply false
}


val kotlinVersion: String by project
val isKotlinDev = kotlinVersion.contains("-release")
val isKotlinEap = kotlinVersion.contains("-eap") || kotlinVersion.contains("-M")

allprojects {
    val forcedVersion = System.getenv("FORCED_KORGE_PLUGINS_VERSION")
    project.version = forcedVersion?.removePrefix("refs/tags/v")?.removePrefix("v") ?: project.version

    //println(project.version)
	repositories {
		mavenLocal {
			content {
				excludeGroup("Kotlin/Native")
			}
		}
		maven {
			url = uri("https://dl.bintray.com/korlibs/korlibs")
			content {
				excludeGroup("Kotlin/Native")
			}
		}
		jcenter {
			content {
				excludeGroup("Kotlin/Native")
			}
		}
		google {
			content {
				excludeGroup("Kotlin/Native")
			}
		}
		if (isKotlinDev || isKotlinEap) {
            maven { url = uri("https://dl.bintray.com/kotlin/kotlin-eap") }
			maven { url = uri("https://dl.bintray.com/kotlin/kotlin-dev") }
		}
	}
}

fun version(name: String): String? {
	return properties["${name}Version"]?.toString()
}

//new File("korge-build/src/main/kotlin/com/soywiz/korge/build/BuildVersions.kt").write("""
File(rootDir, "korge-gradle-plugin/src/main/kotlin/com/soywiz/korge/gradle/BuildVersions.kt").writeText("""
package com.soywiz.korge.gradle

object BuildVersions {
	const val KLOCK = "${version("klock")}"
	const val KDS = "${version("kds")}"
	const val KMEM = "${version("kmem")}"
	const val KORMA = "${version("korma")}"
	const val KORIO = "${version("korio")}"
	const val KORIM = "${version("korim")}"
	const val KORAU = "${version("korau")}"
	const val KORGW = "${version("korgw")}"
	const val KORGE = "${version("korge")}"
	const val KOTLIN = "${version("kotlin")}"
    const val JNA = "${version("jna")}"
	const val COROUTINES = "${version("coroutines")}"
	const val ANDROID_BUILD = "${version("androidBuildGradle")}"
}
""")

val publishUser = (rootProject.findProperty("BINTRAY_USER") ?: project.findProperty("bintrayUser") ?: System.getenv("BINTRAY_USER"))?.toString()
val publishPassword = (rootProject.findProperty("BINTRAY_KEY") ?: project.findProperty("bintrayKey") ?: System.getenv("BINTRAY_KEY"))?.toString()

subprojects {
	repositories {
		mavenLocal()
		jcenter()
		maven { url = uri("https://plugins.gradle.org/m2/") }
		maven { url = uri("https://dl.bintray.com/korlibs/korlibs") }
	}

	apply(plugin = "maven")
	apply(plugin = "maven-publish")
    apply(plugin = "kotlin")

	//println("project: ${project.name}")

	val sourceSets: SourceSetContainer by project
	val publishing: PublishingExtension by project

	val sourcesJar by tasks.creating(Jar::class) {
		archiveClassifier.set("sources")
		from(sourceSets["main"].allSource)
	}

	val javadocJar by tasks.creating(Jar::class) {
		archiveClassifier.set("javadoc")
	}

	publishing.apply {
		if (publishUser != null && publishPassword != null) {
			repositories {
				maven {
					credentials {
						username = publishUser
						password = publishPassword
					}
					url = uri("https://api.bintray.com/maven/${project.property("project.bintray.org")}/${project.property("project.bintray.repository")}/${project.property("project.bintray.package")}/")
				}
			}
		}
		publications {
			maybeCreate<MavenPublication>("maven").apply {
				groupId = project.group.toString()
				artifactId = project.name
				version = project.version.toString()
				from(components["java"])
				artifact(sourcesJar)
				artifact(javadocJar)

				pom {
					name.set(project.name.toString())
					description.set(project.property("project.description").toString())
					url.set(project.property("project.scm.url").toString())
					licenses {
						license {
							name.set(project.property("project.license.name").toString())
							url.set(project.property("project.license.url").toString())
						}
					}
					scm {
						url.set(project.property("project.scm.url").toString())
					}
				}
			}
		}
	}
}

fun ByteArray.encodeBase64() = Base64.getEncoder().encodeToString(this)

val publish by tasks.creating {
	subprojects {
		dependsOn(":${project.name}:publish")
	}
	doLast {
		val subject = project.property("project.bintray.org")
		val repo = project.property("project.bintray.repository")
		val _package = project.property("project.bintray.package")
		val version = project.version

		((URL("https://bintray.com/api/v1/content/$subject/$repo/$_package/$version/publish")).openConnection() as HttpURLConnection).apply {
			requestMethod = "POST"
			doOutput = true

			setRequestProperty("Authorization", "Basic " + "$publishUser:$publishPassword".toByteArray().encodeBase64().toString())
			PrintWriter(outputStream).use { printWriter ->
				printWriter.write("""{"discard": false, "publish_wait_for_secs": -1}""")
			}
			println(inputStream.readBytes().toString(Charsets.UTF_8))
		}
	}
}
