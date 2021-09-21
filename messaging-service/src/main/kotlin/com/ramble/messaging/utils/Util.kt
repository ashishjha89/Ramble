package com.ramble.messaging.utils

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.ramble.messaging.common.ErrorBody
import com.ramble.messaging.common.ErrorCode
import com.ramble.messaging.model.InternalServerException
import com.ramble.messaging.model.UserNotFoundException
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.withTimeout
import org.springframework.web.client.HttpClientErrorException

@Throws(InternalServerException::class)
suspend fun <T> performDeferredTask(deferredTask: Deferred<T>, timeoutInMilliseconds: Long): T =
    try {
        withTimeout(timeoutInMilliseconds) {
            deferredTask.await()
        }
    } catch (e: Exception) {
        if (deferredTask.isActive) deferredTask.cancel()
        if (e is HttpClientErrorException) {
            val responseBodyAsString = e.responseBodyAsString
            val gson = Gson()
            try {
                val errorBody = gson.fromJson(responseBodyAsString, ErrorBody::class.java)
                when (errorBody.errorCode) {
                    ErrorCode.USER_INFO_NOT_FOUND -> throw UserNotFoundException()
                    else -> throw InternalServerException()
                }
            } catch (syntaxException: JsonSyntaxException) {
                throw InternalServerException()
            }
        }
        throw e
    }