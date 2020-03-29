package com.soywiz.korge.build.util

import com.soywiz.korio.async.*
import com.soywiz.korio.file.*
import com.soywiz.korio.stream.*
import com.soywiz.krypto.*

suspend fun AsyncInputStream.hash(factory: HasherFactory): ByteArray {
	val hash = factory.create()
	val temp = ByteArray(64 * 1024)
	while (true) {
		val read = read(temp)
		if (read <= 0) break
		hash.update(temp, 0, read)
	}
	return hash.digest().bytes
}

suspend fun VfsFile.hash(factory: HasherFactory): ByteArray = openInputStream().use { this.hash(factory) }
