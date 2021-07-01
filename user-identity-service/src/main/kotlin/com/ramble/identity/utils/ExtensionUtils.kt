package com.ramble.identity.utils

import com.ramble.identity.common.Result
import org.springframework.http.ResponseEntity

fun <T> Result<T>.toResponseEntity(): ResponseEntity<*> {
    return when (this) {
        is Result.Success -> ResponseEntity(data, httpStatus)
        is Result.Error -> ResponseEntity(errorBody, httpStatus)
    }
}