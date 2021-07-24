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

    private val jwtKeyAccessToken = Keys.hmacShaKeyFor(jwtTokenProperties.signingKeyAccessToken.toByteArray())
    private val jwtKeyRegistrationToken = Keys.hmacShaKeyFor(jwtTokenProperties.signingKeyRegistrationToken.toByteArray())

    private val tokenDurationGenerator = TokenDurationGenerator()

    private val uuidGenerator = UUIDGenerator()

    private val accessTokenClaimsMapGenerator = AccessTokenClaimsMapGenerator()

    internal fun accessTokenHandler(): AccessTokenHandler =
            AccessTokenHandler(
                    jwtKey = jwtKeyAccessToken,
                    tokenDurationGenerator = tokenDurationGenerator,
                    accessTokenClaimsMapGenerator = accessTokenClaimsMapGenerator
            )

    internal fun refreshTokenHandler(): RefreshTokenHandler = RefreshTokenHandler(uuidGenerator)

    internal fun registrationConfirmationTokenHandler(): RegistrationConfirmationTokenHandler =
            RegistrationConfirmationTokenHandler(jwtKey = jwtKeyRegistrationToken, tokenDurationGenerator = tokenDurationGenerator)

    internal fun usernamePasswordAuthTokenTokenGenerator(): UsernamePasswordAuthTokenTokenGenerator =
            UsernamePasswordAuthTokenTokenGenerator()

    internal fun registrationConfirmationRepo(): RegistrationConfirmationRepo =
            RegistrationConfirmationRepo()

    internal fun jwtParserAccessToken() = Jwts.parserBuilder().setSigningKey(jwtKeyAccessToken).build()

    internal fun jwtParserRegistrationToken() = Jwts.parserBuilder().setSigningKey(jwtKeyRegistrationToken).build()

    internal fun jwtBuilder() = Jwts.builder()

}