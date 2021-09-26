package com.ramble.token.repository.persistence

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.withTimeout

private const val SQL_TIMEOUT = 500L

internal suspend fun <T> performDeferredTask(
    deferredTask: Deferred<T>,
    timeoutInMilliseconds: Long = SQL_TIMEOUT
): T =
    withTimeout(timeoutInMilliseconds) {
        deferredTask.await()
    }