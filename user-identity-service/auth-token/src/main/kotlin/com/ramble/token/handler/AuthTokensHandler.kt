package com.ramble.token.handler

import com.ramble.token.handler.helper.AccessTokenHelper
import com.ramble.token.handler.helper.RefreshTokenHelper
import com.ramble.token.handler.helper.UsernamePasswordAuthTokenTokenHelper
import com.ramble.token.model.AccessClaims
import com.ramble.token.model.AuthInfo
import io.jsonwebtoken.Claims
import io.jsonwebtoken.JwtParser
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import java.security.Principal
import java.time.Instant
import java.time.temporal.ChronoUnit

class AuthTokensHandler {

    private val accessTokenHelper: AccessTokenHelper

    private val refreshTokenHelper: RefreshTokenHelper

    private val usernamePasswordAuthTokenTokenHelper: UsernamePasswordAuthTokenTokenHelper

    @Suppress("unused")
    internal constructor(
            accessTokenHelper: AccessTokenHelper,
            refreshTokenHelper: RefreshTokenHelper,
            usernamePasswordAuthTokenTokenHelper: UsernamePasswordAuthTokenTokenHelper
    ) {
        this.accessTokenHelper = accessTokenHelper
        this.refreshTokenHelper = refreshTokenHelper
        this.usernamePasswordAuthTokenTokenHelper = usernamePasswordAuthTokenTokenHelper
    }

    constructor() {
        this.accessTokenHelper = AccessTokenHelper()
        this.refreshTokenHelper = RefreshTokenHelper()
        this.usernamePasswordAuthTokenTokenHelper = UsernamePasswordAuthTokenTokenHelper()
    }

    fun generateAuthToken(
            authResult: Authentication,
            userId: String,
            email: String,
            issuedInstant: Instant = Instant.now(),
            expiryDurationAmount: Long = 30,
            expiryDurationUnit: ChronoUnit = ChronoUnit.MINUTES
    ): AuthInfo =
            AuthInfo(
                    userId = userId,
                    email = email,
                    accessToken = accessTokenHelper.generateAccessToken(
                            authResult = authResult,
                            userId = userId,
                            email = email,
                            issuedInstant = issuedInstant,
                            expiryDurationAmount = expiryDurationAmount,
                            expiryDurationUnit = expiryDurationUnit
                    ),
                    refreshToken = refreshTokenHelper.generateRefreshToken()
            )

    fun getClaims(token: String?, jwtParser: JwtParser? = null): AccessClaims? =
            accessTokenHelper.getClaims(token, jwtParser)

    fun getClaims(principal: Principal): AccessClaims? =
            accessTokenHelper.getClaims(principal)

    fun getAuthentication(claims: Claims, authorities: List<GrantedAuthority>): Authentication =
            usernamePasswordAuthTokenTokenHelper.getUsernamePasswordAuthenticationToken(claims, authorities)

}