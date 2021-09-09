package com.ramble.accesstoken.model

import io.jsonwebtoken.Claims
import java.util.*

data class AccessClaims(
    val clientId: String,
    val userId: String,
    val email: String,
    val claims: Claims,
    val roles: List<String>
)

class AccessTokenValidatorInternalException : Exception()

internal data class TokenDuration(val issuedDate: Date, val expiryDate: Date)