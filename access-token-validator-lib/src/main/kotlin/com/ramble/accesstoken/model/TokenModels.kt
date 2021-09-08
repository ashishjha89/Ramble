package com.ramble.accesstoken.model

import io.jsonwebtoken.Claims
import org.springframework.security.core.GrantedAuthority
import java.util.*

data class AccessClaims(
    val clientId: String,
    val userId: String,
    val email: String,
    val claims: Claims,
    val authorities: List<GrantedAuthority>
)

class AccessTokenValidatorInternalException: Exception()

internal data class TokenDuration(val issuedDate: Date, val expiryDate: Date)