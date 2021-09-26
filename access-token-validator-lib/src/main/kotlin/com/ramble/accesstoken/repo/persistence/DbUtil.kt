package com.ramble.accesstoken.repo.persistence

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.withTimeout

private const val TIMEOUT = 500L

internal suspend fun <T> performDeferredTask(
    deferredTask: Deferred<T>,
    timeoutInMilliseconds: Long = TIMEOUT
): T =
    withTimeout(timeoutInMilliseconds) {
        deferredTask.await()
    }