package com.ramble.token.model

import io.jsonwebtoken.Claims
import org.springframework.security.core.GrantedAuthority
import java.util.*

data class UserAuthInfo(
    val userId: String,
    val email: String,
    val accessToken: String,
    val refreshToken: String
)

data class AccessClaims(
    val clientId: String,
    val userId: String,
    val email: String,
    val claims: Claims,
    val authorities: List<GrantedAuthority>
)

data class RegistrationConfirmationToken(val userId: String, val email: String, val token: String)

class RefreshTokenIsInvalidException : Exception()

class AccessTokenIsInvalidException : Exception()

internal data class TokenDuration(val issuedDate: Date, val expiryDate: Date)