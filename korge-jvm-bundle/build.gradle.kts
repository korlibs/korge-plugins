import groovy.xml.*
import java.util.*
import java.io.*
import java.lang.*
import groovy.util.*

plugins {
	kotlin("jvm")
	java
	`maven-publish`
}

repositories {
	mavenLocal()
	jcenter()
}

tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class).all {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

val gradleProperties = Properties()
gradleProperties.load(StringReader(File(rootDir, "gradle.properties").readText()))
//println(gradleProperties)

fun version(name: String): String? {
	return gradleProperties["${name}Version"]?.toString()
}

configurations {
	create("rtArtifacts") {
		//this.transitive = true
		resolutionStrategy {
			val strat = this
			strat.eachDependency {
				//details.target.
				//println(details.target.name)
			}
		}
	}
}

//println(configurations.names)

//configurations.compileClasspath.extendsFrom(configurations.rtArtifacts)
configurations.runtime.extendsFrom(configurations["rtArtifacts"])
//configurations.runtimeOnly.extendsFrom(configurations.rtArtifacts)
//configurations.runtimeClasspath.extendsFrom(configurations.rtArtifacts)
//configurations.runtime.extendsFrom(configurations.rtArtifacts)
//configurations.runtime.dependencies.add(configurations)

// https://github.com/square/okio/issues/647
// https://docs.gradle.org/current/userguide/component_metadata_rules.html
configurations {
	named("rtArtifacts") {
		attributes.attribute(org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType.attribute, org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType.jvm)
	}
}

dependencies {
	add("rtArtifacts", "net.java.dev.jna:jna:${version("jna")}")
	add("rtArtifacts", "net.java.dev.jna:jna-platform:${version("jna")}")

	add("rtArtifacts", "com.soywiz.korlibs.klock:klock-jvm:${version("klock")}")
	add("rtArtifacts", "com.soywiz.korlibs.krypto:krypto-jvm:${version("krypto")}")
	add("rtArtifacts", "com.soywiz.korlibs.korinject:korinject-jvm:${version("korinject")}")
	add("rtArtifacts", "com.soywiz.korlibs.klogger:klogger-jvm:${version("klogger")}")
	add("rtArtifacts", "com.soywiz.korlibs.kds:kds-jvm:${version("kds")}")
	add("rtArtifacts", "com.soywiz.korlibs.kmem:kmem-jvm:${version("kmem")}")
	add("rtArtifacts", "com.soywiz.korlibs.korio:korio-jvm:${version("korio")}")
	add("rtArtifacts", "com.soywiz.korlibs.korma:korma-jvm:${version("korma")}")
	add("rtArtifacts", "com.soywiz.korlibs.korau:korau-jvm:${version("korau")}")
	add("rtArtifacts", "com.soywiz.korlibs.korim:korim-jvm:${version("korim")}")
	add("rtArtifacts", "com.soywiz.korlibs.korgw:korgw-jvm:${version("korgw")}")
	add("rtArtifacts", "com.soywiz.korlibs.korge:korge-jvm:${version("korge")}")
	add("rtArtifacts", "com.soywiz.korlibs.korge:korge-swf-jvm:${version("korge")}")
	add("rtArtifacts", "org.jetbrains.kotlin:kotlin-stdlib:${version("kotlin")}")
	add("rtArtifacts", "org.jetbrains.kotlinx:kotlinx-coroutines-core:${version("coroutines")}")
	//add("rtArtifacts", "org.jetbrains.kotlin:kotlin-runtime:${version("kotlin")}")
}

//println(configurations["rtArtifacts"].getAll().toList())

//configurations["rtArtifacts"].resolvedConfiguration.resolvedArtifacts.each {
//	//println(configurations["rtArtifacts"].getFiles())
//	println(it)
//}

val processrtRtArtifacts by tasks.creating {

	inputs.files(configurations["rtArtifacts"])
	outputs.dir("src/main/resources")
	doLast {
		//println(configurations.rtArtifacts.resolve())

		configurations["rtArtifacts"].resolvedConfiguration.resolvedArtifacts.forEach { dep ->
			//println dep.name
		}

		val files = mutableSetOf("")

		for (jarFile in configurations["rtArtifacts"].resolve()) {
			//if (jarFile.name.contains("kotlin-stdlib")) continue
			//if (jarFile.name.contains("annotations-13")) continue

			val baseJarTree = zipTree(jarFile)

			println("Including: $jarFile")
			copy {
				from(baseJarTree.matching {
					exclude("META-INF/services/*")
				})
				into("src/main/resources")
				//into("libs")
			}
			val tree = baseJarTree.matching {
				include("META-INF/services/*")
			}

			tree.visit {
				val details = this
				if (!details.isDirectory) {
					val relative = details.relativePath.toString()
					val contents = details.file.readText()
					val targetFile = File(project.projectDir, "src/main/resources/${relative}")
					val firstSet = !files.contains(relative)
					if (firstSet) {
						files.add(relative)
					}
					if (firstSet){
						targetFile.writeText(contents)
					} else {
						targetFile.writeText(targetFile.readText() + "\n" + contents)
					}
					println("Merging service: $relative")
				}
			}
			/*
			println(tree.root)
			tree.each { file ->

				def relative = file.relativePath(jarFile)
				println("HELLO: ${jarFile} -- ${file}")
			}
			 */
		}
	}
}

//kotlin.sourceSets {
//	jvmMain {
//		resources.srcDir("libs")
//	}
//}

tasks.getByName("processResources").dependsOn(processrtRtArtifacts)


/*
jar {
	manifest {
		attributes "Main-Class": "com.baeldung.fatjar.Application"
	}

	from {
		//configurations.compile.collect { it.isDirectory() ? it : zipTree(it) }
	}
}
*/

val Node.localName get() = (this.name() as? QName?)?.localPart ?: this.name().toString()
fun Node.children(name: String): List<Node> = children().filterIsInstance<Node>().filter { it.localName == name }
fun Iterable<Node>.children(name: String): List<Node> = this.map { it.children(name) }.flatten()
fun Node.removeFromParent() = this.parent().remove(this)

publishing {
	publications {
		maybeCreate<MavenPublication>("maven").apply {
			pom.withXml {
				for (dep in asNode().children("dependencies").children("dependency")) {
					dep.removeFromParent()
				}
			}
		}
	}
}

