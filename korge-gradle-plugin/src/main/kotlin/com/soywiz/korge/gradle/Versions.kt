package com.soywiz.korge.gradle

import org.gradle.api.Project

val Project.klockVersion get() = findProperty("klockVersion") ?: BuildVersions.KLOCK
val Project.kmemVersion get() = findProperty("kmemVersion") ?: BuildVersions.KMEM
val Project.kdsVersion get() = findProperty("kdsVersion") ?: BuildVersions.KDS
val Project.korioVersion get() = findProperty("korioVersion") ?: BuildVersions.KORIO
val Project.kormaVersion get() = findProperty("kormaVersion") ?: BuildVersions.KORMA
val Project.korauVersion get() = findProperty("korauVersion") ?: BuildVersions.KORAU
val Project.korimVersion get() = findProperty("korimVersion") ?: BuildVersions.KORIM
//val Project.koruiVersion get() = findProperty("koruiVersion") ?: BuildVersions.KORUI
//val Project.korevVersion get() = findProperty("korevVersion") ?: BuildVersions.KOREV
val Project.korgwVersion get() = findProperty("korgwVersion") ?: BuildVersions.KORGW
val Project.korgeVersion get() = findProperty("korgeVersion") ?: BuildVersions.KORGE
val Project.kotlinVersion get() = findProperty("kotlinVersion") ?: BuildVersions.KOTLIN
val Project.coroutinesVersion get() = findProperty("coroutinesVersion") ?: BuildVersions.COROUTINES

