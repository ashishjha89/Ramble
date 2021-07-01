package com.ramble.token.model

import io.jsonwebtoken.Claims
import org.springframework.security.core.GrantedAuthority

data class AuthInfo(val userId: String, val email: String, val accessToken: String, val refreshToken: String)

data class AccessTokenClaims(
        val userId: String,
        val email: String,
        val claims: Claims,
        val authorities: List<GrantedAuthority>
)