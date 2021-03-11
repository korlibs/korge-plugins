
package com.soywiz.korge.gradle

object BuildVersions {
    const val GIT = "v2.0.8.1-1-g80a91843-dirty"
    const val KRYPTO = "2.0.7"
	const val KLOCK = "2.0.7"
	const val KDS = "2.0.9"
	const val KMEM = "2.0.10"
	const val KORMA = "2.0.9"
	const val KORIO = "2.0.10"
	const val KORIM = "2.0.9"
	const val KORAU = "2.0.11"
	const val KORGW = "2.0.9"
	const val KORGE = "2.0.9"
	const val KOTLIN = "1.4.31"
    const val JNA = "5.7.0"
	const val COROUTINES = "1.4.3"
	const val ANDROID_BUILD = "4.2.0-beta06"

    val ALL_PROPERTIES = listOf(::GIT, ::KRYPTO, ::KLOCK, ::KDS, ::KMEM, ::KORMA, ::KORIO, ::KORIM, ::KORAU, ::KORGW, ::KORGE, ::KOTLIN, ::JNA, ::COROUTINES, ::ANDROID_BUILD)
    val ALL = ALL_PROPERTIES.associate { it.name to it.get() }
}
