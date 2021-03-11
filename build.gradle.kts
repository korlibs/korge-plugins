import java.util.*
import java.io.*
import java.nio.file.Files
import kotlin.io.path.*

plugins {
    idea
	//id("com.moowork.node") version "1.3.1"
	id("com.gradle.plugin-publish") version "0.12.0" apply false
    id("org.jetbrains.kotlin.jvm")
    //signing
    //`maven-publish`
}

allprojects {
    val forcedVersion = System.getenv("FORCED_KORGE_PLUGINS_VERSION")
    project.version = forcedVersion?.removePrefix("refs/tags/v")?.removePrefix("v") ?: project.version

    //println(project.version)
	repositories {
		mavenLocal()
        mavenCentral()
		google()
	}
}

fun version(name: String): String? {
	return properties["${name}Version"]?.toString()
}

val gitVersion = try {
    Runtime.getRuntime().exec("git describe --abbrev=8 --tags --dirty".split(" ").toTypedArray(), arrayOf(), rootDir).inputStream.reader()
        .readText().lines().first().trim()
} catch (e: Throwable) {
    e.printStackTrace()
    "unknown"
}

//new File("korge-build/src/main/kotlin/com/soywiz/korge/build/BuildVersions.kt").write("""
File(rootDir, "korge-gradle-plugin/src/main/kotlin/com/soywiz/korge/gradle/BuildVersions.kt").writeText("""
package com.soywiz.korge.gradle

object BuildVersions {
    const val GIT = "$gitVersion"
    const val KRYPTO = "${version("krypto")}"
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

    val ALL_PROPERTIES = listOf(::GIT, ::KRYPTO, ::KLOCK, ::KDS, ::KMEM, ::KORMA, ::KORIO, ::KORIM, ::KORAU, ::KORGW, ::KORGE, ::KOTLIN, ::JNA, ::COROUTINES, ::ANDROID_BUILD)
    val ALL = ALL_PROPERTIES.associate { it.name to it.get() }
}
""")

//val publishUser = (rootProject.findProperty("BINTRAY_USER") ?: project.findProperty("bintrayUser") ?: System.getenv("BINTRAY_USER"))?.toString()
//val publishPassword = (rootProject.findProperty("BINTRAY_KEY") ?: project.findProperty("bintrayKey") ?: System.getenv("BINTRAY_KEY"))?.toString()
val sonatypePublishUser = (System.getenv("SONATYPE_USERNAME") ?: rootProject.findProperty("SONATYPE_USERNAME")?.toString() ?: project.findProperty("sonatypeUsername")?.toString())
val sonatypePublishPassword = (System.getenv("SONATYPE_PASSWORD") ?: rootProject.findProperty("SONATYPE_PASSWORD")?.toString() ?: project.findProperty("sonatypePassword")?.toString())

subprojects {
	repositories {
		mavenLocal()
		mavenCentral()
		maven { url = uri("https://plugins.gradle.org/m2/") }
	}

	apply(plugin = "maven")
	apply(plugin = "maven-publish")
    apply(plugin = "signing")
    apply(plugin = "kotlin")


    val signingSecretKeyRingFile = System.getenv("ORG_GRADLE_PROJECT_signingSecretKeyRingFile") ?: project.findProperty("signing.secretKeyRingFile")?.toString()

// gpg --armor --export-secret-keys foobar@example.com | awk 'NR == 1 { print "signing.signingKey=" } 1' ORS='\\n'
    val signingKey = System.getenv("ORG_GRADLE_PROJECT_signingKey") ?: project.findProperty("signing.signingKey")?.toString()
    val signingPassword = System.getenv("ORG_GRADLE_PROJECT_signingPassword") ?: project.findProperty("signing.password")?.toString()

    if (signingSecretKeyRingFile != null || signingKey != null) {
        project.extensions.getByType(SigningExtension::class.java).apply {
            isRequired = !project.version.toString().endsWith("-SNAPSHOT")
            if (signingKey != null) {
                useInMemoryPgpKeys(signingKey, signingPassword)
            }
            sign(project.extensions.getByType(PublishingExtension::class.java).publications)
        }
    }

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
		if (sonatypePublishUser != null && sonatypePublishPassword != null) {
			repositories {
				maven {
					credentials {
						username = sonatypePublishUser
						password = sonatypePublishPassword
					}
                    if (version.toString().contains("-SNAPSHOT")) {
                        url = uri("https://oss.sonatype.org/content/repositories/snapshots/")
                    } else {
                        url = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
                    }
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

tasks.create("externalReleaseMavenCentral", GradleBuild::class.java) {
    val task = this
    task.tasks = listOf("releaseMavenCentral")
    var tempDir: File? = null
    task.doFirst {
        tempDir = Files.createTempDirectory("prefix-").toFile()
        println("Created dir $tempDir...")
        task.dir = tempDir!!
        File(tempDir, "settings.gradle").writeText("")
        File(tempDir, "build.gradle").writeText("""
			\tbuildscript {
			\t\trepositories {
			\t\t\tmavenLocal()
			\t\t\tmavenCentral()
			\t\t\tgoogle()
			\t\t\tmaven { url = uri("https://plugins.gradle.org/m2/") }
			\t\t}
			\t\tdependencies {
			\t\t\tclasspath("com.soywiz.korlibs:easy-kotlin-mpp-gradle-plugin:${project.version}")
			\t\t}
			\t}
			
			project.group = '${project.group}'
			\tapply plugin: "com.soywiz.korlibs.easy-kotlin-mpp-gradle-plugin"
		""")
    }
    task.doLast {
        tempDir?.deleteRecursively()
    }
}


fun ByteArray.encodeBase64() = Base64.getEncoder().encodeToString(this)

/*
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
 */

idea {
    module {
        excludeDirs = excludeDirs + listOf("@old", ".idea", "gradle", "korge-flash-plugin").map { file(it) }.toSet()
    }
}
