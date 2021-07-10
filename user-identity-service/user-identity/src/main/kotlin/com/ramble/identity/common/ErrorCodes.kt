package com.ramble.identity.common

private object ErrorCode {
    const val SOMETHING_WENT_WRONG = "SOMETHING_WENT_WRONG"
    const val INVALID_USER_ID = "INVALID_USER_ID"
    const val USER_INFO_NOT_FOUND = "USER_INFO_NOT_FOUND"
    const val USER_NOT_ACTIVATED = "USER_NOT_ACTIVATED"
    const val UNAUTHORIZED_ACCESS = "UNAUTHORIZED_ACCESS"
    const val USER_IS_SUSPENDED = "USER_IS_SUSPENDED"
    const val USER_ALREADY_ACTIVATED = "USER_ALREADY_ACTIVATED"
    const val EMAIL_SENDING_FAILED = "EMAIL_SENDING_FAILED"
}

private object ErrorMessage {
    const val somethingWentWrongMsg = "Sorry, something went wrong."
    const val invalidUserIdMsg = "This is not a valid user id."
    const val userInfoNotFoundMsg = "User info not found."
    const val userNotActivatedMsg = "Your account is not activated."
    const val unauthorizedAccessMsg = "You are not authorized to access this resource."
    const val userSuspendedMsg = "You are currently suspended. Contact us for more info."
    const val userAlreadyActivated = "This account is already activated."
    const val emailSendingFailed = "Failed to send email."
}

val unauthorizedAccess: ErrorBody =
        ErrorBody(errorCode = ErrorCode.UNAUTHORIZED_ACCESS, errorMessage = ErrorMessage.unauthorizedAccessMsg)

val invalidUserId: ErrorBody =
        ErrorBody(errorCode = ErrorCode.INVALID_USER_ID, errorMessage = ErrorMessage.invalidUserIdMsg)

val userInfoNotFound: ErrorBody =
        ErrorBody(errorCode = ErrorCode.USER_INFO_NOT_FOUND, errorMessage = ErrorMessage.userInfoNotFoundMsg)

val internalServerError: ErrorBody =
        ErrorBody(errorCode = ErrorCode.SOMETHING_WENT_WRONG, errorMessage = ErrorMessage.somethingWentWrongMsg)

val userSuspendedError: ErrorBody =
        ErrorBody(errorCode = ErrorCode.USER_IS_SUSPENDED, errorMessage = ErrorMessage.userSuspendedMsg)

val userAlreadyActivatedError: ErrorBody =
        ErrorBody(errorCode = ErrorCode.USER_ALREADY_ACTIVATED, errorMessage = ErrorMessage.userAlreadyActivated)

val userNotActivatedError: ErrorBody =
        ErrorBody(errorCode = ErrorCode.USER_NOT_ACTIVATED, errorMessage = ErrorMessage.userNotActivatedMsg)

val emailSendingFailed: ErrorBody =
        ErrorBody(errorCode = ErrorCode.EMAIL_SENDING_FAILED, errorMessage = ErrorMessage.emailSendingFailed)
