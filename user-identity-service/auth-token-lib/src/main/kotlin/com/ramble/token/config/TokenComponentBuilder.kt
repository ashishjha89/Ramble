package com.ramble.token.config

import com.ramble.token.handler.RefreshTokenHandler
import com.ramble.token.handler.RegistrationConfirmationTokenHandler
import com.ramble.token.handler.helper.RefreshTokenClaimsMapGenerator
import com.ramble.token.handler.helper.TokenDurationGenerator
import com.ramble.token.handler.helper.UsernamePasswordAuthTokenTokenGenerator
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Component

@Component
class TokenComponentBuilder(jwtTokenConfig: JwtTokenConfig) {

    private val jwtTokenProperties = jwtTokenConfig.jwtConfigProperties

    private val jwtKeyRefreshToken = Keys.hmacShaKeyFor(jwtTokenProperties.signingKeyRefreshToken.toByteArray())

    private val jwtKeyRegistrationToken =
        Keys.hmacShaKeyFor(jwtTokenProperties.signingKeyRegistrationToken.toByteArray())

    private val tokenDurationGenerator = TokenDurationGenerator()

    internal fun refreshTokenHandler(): RefreshTokenHandler =
        RefreshTokenHandler(
            jwtKey = jwtKeyRefreshToken,
            tokenDurationGenerator = tokenDurationGenerator,
            tokenClaimsMapGenerator = RefreshTokenClaimsMapGenerator()
        )

    internal fun registrationConfirmationTokenHandler(): RegistrationConfirmationTokenHandler =
        RegistrationConfirmationTokenHandler(
            jwtKey = jwtKeyRegistrationToken,
            tokenDurationGenerator = tokenDurationGenerator
        )

    internal fun usernamePasswordAuthTokenTokenGenerator(): UsernamePasswordAuthTokenTokenGenerator =
        UsernamePasswordAuthTokenTokenGenerator()

    internal fun jwtParserRefreshToken() = Jwts.parserBuilder().setSigningKey(jwtKeyRefreshToken).build()

    internal fun jwtParserRegistrationToken() = Jwts.parserBuilder().setSigningKey(jwtKeyRegistrationToken).build()

    internal fun jwtBuilder() = Jwts.builder()

}