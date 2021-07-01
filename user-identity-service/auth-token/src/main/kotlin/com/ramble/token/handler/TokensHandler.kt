package com.ramble.token.handler

import com.ramble.token.model.AccessTokenClaims
import com.ramble.token.model.AuthInfo
import io.jsonwebtoken.JwtParser
import org.springframework.security.core.Authentication
import java.time.Instant

class TokensHandler {

    private val accessTokenHandler: AccessTokenHandler

    private val refreshTokenHandler: RefreshTokenHandler

    @Suppress("unused")
    internal constructor(accessTokenHandler: AccessTokenHandler, refreshTokenHandler: RefreshTokenHandler) {
        this.accessTokenHandler = accessTokenHandler
        this.refreshTokenHandler = refreshTokenHandler
    }

    constructor() {
        this.accessTokenHandler = AccessTokenHandler()
        this.refreshTokenHandler = RefreshTokenHandler()
    }

    fun generateAuthToken(authResult: Authentication, userId: String, email: String, issuedInstant: Instant = Instant.now()): AuthInfo =
            AuthInfo(
                    userId = userId,
                    email = email,
                    accessToken = accessTokenHandler.generateAccessToken(authResult, userId, email, issuedInstant),
                    refreshToken = refreshTokenHandler.generateRefreshToken()
            )

    fun getAccessTokenClaims(token: String?, jwtParser: JwtParser? = null): AccessTokenClaims? =
            accessTokenHandler.getAccessTokenClaims(token, jwtParser)

}