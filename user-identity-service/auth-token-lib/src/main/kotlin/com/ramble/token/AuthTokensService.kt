package com.ramble.token

import com.ramble.token.config.TokenComponentBuilder
import com.ramble.token.handler.AccessTokenHandler
import com.ramble.token.handler.RefreshTokenHandler
import com.ramble.token.handler.helper.UsernamePasswordAuthTokenTokenGenerator
import com.ramble.token.model.AccessClaims
import com.ramble.token.model.AuthInfo
import io.jsonwebtoken.Claims
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.stereotype.Service
import java.security.Principal
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
class AuthTokensService(tokenComponentBuilder: TokenComponentBuilder) {

    private val jwtParser = tokenComponentBuilder.jwtParser()

    private val jwtBuilder = tokenComponentBuilder.jwtBuilder()

    private val accessTokenHandler: AccessTokenHandler =
            tokenComponentBuilder.accessTokenHandler()

    private val refreshTokenHandler: RefreshTokenHandler =
            tokenComponentBuilder.refreshTokenHandler()

    private val usernamePasswordAuthTokenTokenGenerator: UsernamePasswordAuthTokenTokenGenerator =
            tokenComponentBuilder.usernamePasswordAuthTokenTokenGenerator()

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
                            expiryDurationUnit = expiryDurationUnit,
                            jwtBuilder = jwtBuilder
                    ),
                    refreshToken = refreshTokenHandler.generateRefreshToken()
            )

    fun getClaims(token: String?, now: Instant = Instant.now()): AccessClaims? =
            accessTokenHandler.getTokenClaims(token, jwtParser, now)

    fun getClaims(principal: Principal): AccessClaims? =
            accessTokenHandler.getPrincipalClaims(principal)

    fun getAuthentication(claims: Claims, authorities: List<GrantedAuthority>): Authentication =
            usernamePasswordAuthTokenTokenGenerator.getUsernamePasswordAuthenticationToken(claims, authorities)

}