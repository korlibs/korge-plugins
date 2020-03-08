import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import java.io.StringReader
import java.util.Properties

apply(plugin = "kotlin")
apply(plugin = "maven")
apply(plugin = "maven-publish")

tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class).all {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

val gradleProperties = Properties()
gradleProperties.load(StringReader(File(projectDir, "../gradle.properties").readText()))

fun version(name: String): String? {
	if (name == "korge") return gradleProperties["version"]?.toString()
	return gradleProperties["${name}Version"]?.toString()
}

fun setKDep(it: ExternalModuleDependency) {
	//it.targetConfiguration = "default"
	//it.targetConfiguration = "runtime"
	it.attributes { attribute(KotlinPlatformType.attribute, KotlinPlatformType.jvm) }
}

dependencies {
	compile("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	compile("org.jetbrains.kotlin:kotlin-test")
	compile("org.jetbrains.kotlin:kotlin-test-junit")

	compile(project(":korge-jvm-bundle"))
}
