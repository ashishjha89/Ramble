package com.ramble.identity.common

import com.ramble.identity.common.ErrorCode.SOMETHING_WENT_WRONG
import com.ramble.identity.common.ErrorMessage.somethingWentWrong
import org.springframework.http.HttpStatus

sealed class Result<out T>(val httpStatus: HttpStatus) {

    class Success<out T>(httpStatus: HttpStatus = HttpStatus.OK, val data: T) : Result<T>(httpStatus)

    class Error<out T>(httpStatus: HttpStatus, val errorBody: ErrorBody) : Result<T>(httpStatus)
}

data class ErrorBody(val errorCode: String = SOMETHING_WENT_WRONG, val errorMessage: String = somethingWentWrong)