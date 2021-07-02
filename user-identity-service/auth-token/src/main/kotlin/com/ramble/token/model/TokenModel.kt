package com.ramble.token.model

import io.jsonwebtoken.Claims
import org.springframework.security.core.GrantedAuthority
import java.time.Instant
import java.util.*

data class AuthInfo(val userId: String, val email: String, val accessToken: String, val refreshToken: String)

data class AccessClaims(
        val userId: String,
        val email: String,
        val claims: Claims,
        val authorities: List<GrantedAuthority>
)

internal data class TokenDuration(val issuedDate: Date, val expiryDate: Date)