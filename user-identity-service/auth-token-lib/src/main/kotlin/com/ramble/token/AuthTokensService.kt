package com.ramble.token

import com.ramble.token.handler.AccessTokenHandler
import com.ramble.token.handler.RefreshTokenHandler
import com.ramble.token.handler.UsernamePasswordAuthTokenTokenGenerator
import com.ramble.token.model.AccessClaims
import com.ramble.token.model.AuthInfo
import io.jsonwebtoken.Claims
import io.jsonwebtoken.JwtParser
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.stereotype.Service
import java.security.Principal
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
class AuthTokensService {

    private val accessTokenHandler: AccessTokenHandler

    private val refreshTokenHandler: RefreshTokenHandler

    private val usernamePasswordAuthTokenTokenGenerator: UsernamePasswordAuthTokenTokenGenerator

    @Suppress("unused")
    internal constructor(
            accessTokenHandler: AccessTokenHandler,
            refreshTokenHandler: RefreshTokenHandler,
            usernamePasswordAuthTokenTokenGenerator: UsernamePasswordAuthTokenTokenGenerator
    ) {
        this.accessTokenHandler = accessTokenHandler
        this.refreshTokenHandler = refreshTokenHandler
        this.usernamePasswordAuthTokenTokenGenerator = usernamePasswordAuthTokenTokenGenerator
    }

    constructor() {
        this.accessTokenHandler = AccessTokenHandler()
        this.refreshTokenHandler = RefreshTokenHandler()
        this.usernamePasswordAuthTokenTokenGenerator = UsernamePasswordAuthTokenTokenGenerator()
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
                    accessToken = accessTokenHandler.generateAccessToken(
                            authResult = authResult,
                            userId = userId,
                            email = email,
                            issuedInstant = issuedInstant,
                            expiryDurationAmount = expiryDurationAmount,
                            expiryDurationUnit = expiryDurationUnit
                    ),
                    refreshToken = refreshTokenHandler.generateRefreshToken()
            )

    fun getClaims(token: String?, jwtParser: JwtParser? = null): AccessClaims? =
            accessTokenHandler.getClaims(token, jwtParser)

    fun getClaims(principal: Principal): AccessClaims? =
            accessTokenHandler.getClaims(principal)

    fun getAuthentication(claims: Claims, authorities: List<GrantedAuthority>): Authentication =
            usernamePasswordAuthTokenTokenGenerator.getUsernamePasswordAuthenticationToken(claims, authorities)

}