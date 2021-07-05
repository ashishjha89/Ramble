package com.ramble.identity.common

import org.springframework.http.HttpStatus

sealed class Result<out T>(val httpStatus: HttpStatus) {

    class Success<out T>(httpStatus: HttpStatus = HttpStatus.OK, val data: T) : Result<T>(httpStatus)

    class Error<out T>(httpStatus: HttpStatus, val errorBody: ErrorBody) : Result<T>(httpStatus)
}

data class ErrorBody(val errorCode: String, val errorMessage: String)