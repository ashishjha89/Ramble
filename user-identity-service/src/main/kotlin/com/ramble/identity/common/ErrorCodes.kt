package com.ramble.identity.common

object ErrorCode {
    const val SOMETHING_WENT_WRONG = "SOMETHING_WENT_WRONG"
    const val INVALID_USER_ID = "INVALID_USER_ID"
    const val USER_ALREADY_REGISTERED = "USER_ALREADY_REGISTERED"
    const val MISSING_AUTHORIZATION_HEADER = "MISSING_AUTHORIZATION_HEADER"
    const val USER_INFO_NOT_FOUND = "USER_INFO_NOT_FOUND"
    const val UNAUTHORIZED_ACCESS = "UNAUTHORIZED_ACCESS"
}

object ErrorMessage {
    const val somethingWentWrong = "Sorry, something went wrong."
    const val invalidUserId = "This is not a valid user id."
    const val userAlreadyRegistered = "This email is not available."
    const val missingAuthorizationHeader = "Authorization header is missing."
    const val userInfoNotFound = "User info not found."
    const val unauthorizedAccess = "You're not authorized to access this resource."
}