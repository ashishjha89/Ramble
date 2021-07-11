package com.ramble.token.config

import com.ramble.token.handler.AccessTokenHandler
import com.ramble.token.handler.RefreshTokenHandler
import com.ramble.token.handler.RegistrationConfirmationTokenHandler
import com.ramble.token.handler.helper.AccessTokenClaimsMapGenerator
import com.ramble.token.handler.helper.TokenDurationGenerator
import com.ramble.token.handler.helper.UUIDGenerator
import com.ramble.token.handler.helper.UsernamePasswordAuthTokenTokenGenerator
import com.ramble.token.repository.RegistrationConfirmationRepo
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Component

@Component
class TokenComponentBuilder(jwtTokenConfig: JwtTokenConfig) {

    private val jwtTokenProperties = jwtTokenConfig.jwtConfigProperties

    private val jwtKey = Keys.hmacShaKeyFor(jwtTokenProperties.signingKey.toByteArray())

    private val tokenDurationGenerator = TokenDurationGenerator()

    private val accessTokenClaimsMapGenerator = AccessTokenClaimsMapGenerator()

    internal fun accessTokenHandler(): AccessTokenHandler =
            AccessTokenHandler(
                    jwtKey = jwtKey,
                    tokenDurationGenerator = tokenDurationGenerator,
                    accessTokenClaimsMapGenerator = accessTokenClaimsMapGenerator
            )

    internal fun refreshTokenHandler(): RefreshTokenHandler =
            RefreshTokenHandler(uUIDGenerator = UUIDGenerator())

    internal fun usernamePasswordAuthTokenTokenGenerator(): UsernamePasswordAuthTokenTokenGenerator =
            UsernamePasswordAuthTokenTokenGenerator()

    internal fun registrationConfirmationRepo(): RegistrationConfirmationRepo =
            RegistrationConfirmationRepo()

    internal fun registrationConfirmationTokenHandler(): RegistrationConfirmationTokenHandler =
            RegistrationConfirmationTokenHandler(jwtKey = jwtKey, tokenDurationGenerator = tokenDurationGenerator)

    internal fun jwtParser() = Jwts.parserBuilder().setSigningKey(jwtKey).build()

    internal fun jwtBuilder() = Jwts.builder()

}