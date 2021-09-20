package com.ramble.messaging.common

object ErrorCode {
    const val SOMETHING_WENT_WRONG = "SOMETHING_WENT_WRONG"
    const val INVALID_USER_EMAIL = "INVALID_USER_EMAIL"
    const val USER_INFO_NOT_FOUND = "USER_INFO_NOT_FOUND"
    const val UNAUTHORIZED_ACCESS = "UNAUTHORIZED_ACCESS"
}

private object ErrorMessage {
    const val somethingWentWrongMsg = "Sorry, something went wrong."
    const val invalidUserEmailMsg = "This is not a valid user email."
    const val userInfoNotFoundMsg = "User info not found."
    const val unauthorizedAccessMsg = "You are not authorized to access this resource."
}

val unauthorizedAccess: ErrorBody =
    ErrorBody(errorCode = ErrorCode.UNAUTHORIZED_ACCESS, errorMessage = ErrorMessage.unauthorizedAccessMsg)

val invalidUserEmail: ErrorBody =
    ErrorBody(errorCode = ErrorCode.INVALID_USER_EMAIL, errorMessage = ErrorMessage.invalidUserEmailMsg)

val userInfoNotFound: ErrorBody =
    ErrorBody(errorCode = ErrorCode.USER_INFO_NOT_FOUND, errorMessage = ErrorMessage.userInfoNotFoundMsg)

val internalServerError: ErrorBody =
    ErrorBody(errorCode = ErrorCode.SOMETHING_WENT_WRONG, errorMessage = ErrorMessage.somethingWentWrongMsg)