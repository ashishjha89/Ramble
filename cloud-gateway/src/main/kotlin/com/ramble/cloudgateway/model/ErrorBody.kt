package com.ramble.cloudgateway.model

data class ErrorBody(val errorCode: String, val errorMessage: String)

object ErrorCode {
    const val SOMETHING_WENT_WRONG = "SOMETHING_WENT_WRONG"
}

private object ErrorMessage {
    const val takingTooLongMsg = "The server is taking longer than expected. Please try after some time!"
}

val takingTooLong: ErrorBody =
    ErrorBody(errorCode = ErrorCode.SOMETHING_WENT_WRONG, errorMessage = ErrorMessage.takingTooLongMsg)