package com.ramble.messaging.utils

import com.ramble.messaging.model.InternalServerException
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.withTimeout

@Throws(InternalServerException::class)
suspend fun <T> performDeferredTask(deferredTask: Deferred<T>, timeoutInMilliseconds: Long): T =
    try {
        withTimeout(timeoutInMilliseconds) {
            deferredTask.await()
        }
    } catch (e: Exception) {
        if (deferredTask.isActive) deferredTask.cancel()
        throw e
    }