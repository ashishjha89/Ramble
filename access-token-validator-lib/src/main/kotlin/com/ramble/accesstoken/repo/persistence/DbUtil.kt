package com.ramble.accesstoken.repo.persistence

import com.ramble.accesstoken.model.AccessTokenValidatorInternalException
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.withTimeout

private const val TIMEOUT = 500L

@Throws(AccessTokenValidatorInternalException::class)
internal suspend fun <T> performDeferredTask(
    deferredTask: Deferred<T>,
    timeoutInMilliseconds: Long = TIMEOUT
): T =
    try {
        withTimeout(timeoutInMilliseconds) {
            deferredTask.await()
        }
    } catch (e: Exception) {
        if (deferredTask.isActive) deferredTask.cancel()
        throw AccessTokenValidatorInternalException()
    }