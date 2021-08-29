package com.ramble.token.repository.persistence

import com.ramble.token.model.InternalTokenStorageException
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.withTimeout

private const val SQL_TIMEOUT = 500L

@Throws(InternalTokenStorageException::class)
internal suspend fun <T> performDeferredTask(
    deferredTask: Deferred<T>,
    timeoutInMilliseconds: Long = SQL_TIMEOUT
): T =
    try {
        withTimeout(timeoutInMilliseconds) {
            deferredTask.await()
        }
    } catch (e: Exception) {
        if (deferredTask.isActive) deferredTask.cancel()
        throw InternalTokenStorageException()
    }