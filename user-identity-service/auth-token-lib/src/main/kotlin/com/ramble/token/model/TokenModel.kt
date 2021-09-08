package com.ramble.token.model

import java.util.*

data class UserAuthInfo(
    val userId: String,
    val email: String,
    val accessToken: String,
    val refreshToken: String
)

class RefreshTokenIsInvalidException : Exception()

class AccessTokenIsInvalidException : Exception()

class InternalTokenStorageException : Exception()

internal data class TokenDuration(val issuedDate: Date, val expiryDate: Date)