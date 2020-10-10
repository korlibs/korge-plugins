package com.soywiz.korge.build

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.*
import kotlin.coroutines.*

fun <T> runBlocking2(context: CoroutineContext = EmptyCoroutineContext, block: suspend CoroutineScope.() -> T): T {
	val mutex = java.util.concurrent.Semaphore(1)
	var exception: Throwable? = null
	var value: T? = null

	mutex.acquire()
	block.startCoroutine(CoroutineScope(context), object : Continuation<T> {
		override val context: CoroutineContext = context
		override fun resumeWith(result: Result<T>) {
			if (result.isFailure) {
				exception = result.exceptionOrNull()
			} else {
				value = result.getOrNull()
			}
			mutex.release()
		}
	})

	mutex.acquire()
	if (exception != null) {
		throw exception!!
	}
	return value!!
}
