
package com.soywiz.korge.gradle

object BuildVersions {
    const val GIT = "v2.0.6.0-1-g48141b47-dirty"
    const val KRYPTO = "2.0.6"
	const val KLOCK = "2.0.6"
	const val KDS = "2.0.6"
	const val KMEM = "2.0.6"
	const val KORMA = "2.0.6"
	const val KORIO = "2.0.7"
	const val KORIM = "2.0.6"
	const val KORAU = "2.0.6"
	const val KORGW = "2.0.6"
	const val KORGE = "2.0.6"
	const val KOTLIN = "1.4.30"
    const val JNA = "5.6.0"
	const val COROUTINES = "1.4.2"
	const val ANDROID_BUILD = "4.0.1"

    val ALL_PROPERTIES = listOf(::GIT, ::KRYPTO, ::KLOCK, ::KDS, ::KMEM, ::KORMA, ::KORIO, ::KORIM, ::KORAU, ::KORGW, ::KORGE, ::KOTLIN, ::JNA, ::COROUTINES, ::ANDROID_BUILD)
    val ALL = ALL_PROPERTIES.associate { it.name to it.get() }
}
