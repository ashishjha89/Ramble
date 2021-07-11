package com.ramble.token.config

import com.ramble.token.handler.*
import com.ramble.token.repository.RegistrationConfirmationRepo
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Component

@Component
class TokenComponentBuilder(jwtTokenConfig: JwtTokenConfig) {

    private val jwtTokenProperties = jwtTokenConfig.jwtConfigProperties

    private val jwtKey = Keys.hmacShaKeyFor(jwtTokenProperties.signingKey.toByteArray())

    private val accessTokenDurationGenerator = AccessTokenDurationGenerator()

    internal fun accessTokenHandler(): AccessTokenHandler =
            AccessTokenHandler(jwtKey = jwtKey, accessTokenDurationGenerator = accessTokenDurationGenerator)

    internal fun refreshTokenHandler(): RefreshTokenHandler =
            RefreshTokenHandler(uUIDGenerator = UUIDGenerator())

    internal fun usernamePasswordAuthTokenTokenGenerator(): UsernamePasswordAuthTokenTokenGenerator =
            UsernamePasswordAuthTokenTokenGenerator()

    internal fun registrationConfirmationRepo(): RegistrationConfirmationRepo =
            RegistrationConfirmationRepo()

    internal fun registrationConfirmationTokenHandler(): RegistrationConfirmationTokenHandler =
            RegistrationConfirmationTokenHandler(jwtKey = jwtKey, accessTokenDurationGenerator = accessTokenDurationGenerator)
}