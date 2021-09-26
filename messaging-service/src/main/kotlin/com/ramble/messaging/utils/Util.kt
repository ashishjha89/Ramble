package com.ramble.messaging.utils

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.ramble.messaging.common.ErrorBody
import com.ramble.messaging.common.ErrorCode
import com.ramble.messaging.model.InternalServerException
import com.ramble.messaging.model.UserNotFoundException
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.withTimeout
import org.slf4j.Logger
import org.springframework.web.client.HttpClientErrorException

@Throws(InternalServerException::class, UserNotFoundException::class)
suspend fun <T> performDeferredTask(
    deferredTask: Deferred<T>,
    timeoutInMilliseconds: Long,
    logger: Logger
): T =
    try {
        withTimeout(timeoutInMilliseconds) {
            deferredTask.await()
        }
    } catch (e: Exception) {
        if (deferredTask.isActive) deferredTask.cancel()
        if (e is HttpClientErrorException) {
            val responseBodyAsString = e.responseBodyAsString
            try {
                val errorBody = Gson().fromJson(responseBodyAsString, ErrorBody::class.java)
                when (errorBody?.errorCode) {
                    ErrorCode.USER_INFO_NOT_FOUND -> {
                        logger.error("HttpClientErrorException UserNotFoundException")
                        throw UserNotFoundException()
                    }
                    null -> {
                        logger.error("HttpClientErrorException null body")
                        throw InternalServerException()
                    }
                    else -> {
                        logger.error("HttpClientErrorException non-recognizable errorBody code:${errorBody.errorCode}")
                        throw InternalServerException()
                    }
                }
            } catch (syntaxException: JsonSyntaxException) {
                logger.error("HttpClientErrorException JsonSyntaxException responseBodyAsString:$responseBodyAsString")
                throw InternalServerException()
            }
        }
        logger.error("Exception thrown error.message:${e.message}")
        throw e
    }