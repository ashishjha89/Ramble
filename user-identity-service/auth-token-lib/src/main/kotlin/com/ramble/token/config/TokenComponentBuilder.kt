package com.ramble.token.config

import com.ramble.token.handler.AccessTokenHandler
import com.ramble.token.handler.RefreshTokenHandler
import com.ramble.token.handler.RegistrationConfirmationTokenHandler
import com.ramble.token.handler.helper.TokenClaimsMapGenerator
import com.ramble.token.handler.helper.TokenDurationGenerator
import com.ramble.token.handler.helper.UsernamePasswordAuthTokenTokenGenerator
import com.ramble.token.repository.RegistrationConfirmationRepo
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Component

@Component
class TokenComponentBuilder(jwtTokenConfig: JwtTokenConfig) {

    private val jwtTokenProperties = jwtTokenConfig.jwtConfigProperties

    private val jwtKeyAccessToken = Keys.hmacShaKeyFor(jwtTokenProperties.signingKeyAccessToken.toByteArray())

    private val jwtKeyRefreshToken = Keys.hmacShaKeyFor(jwtTokenProperties.signingKeyRefreshToken.toByteArray())

    private val jwtKeyRegistrationToken =
        Keys.hmacShaKeyFor(jwtTokenProperties.signingKeyRegistrationToken.toByteArray())

    private val tokenDurationGenerator = TokenDurationGenerator()

    private val tokenClaimsMapGenerator = TokenClaimsMapGenerator()

    internal fun accessTokenHandler(): AccessTokenHandler =
        AccessTokenHandler(
            jwtKey = jwtKeyAccessToken,
            tokenDurationGenerator = tokenDurationGenerator,
            tokenClaimsMapGenerator = tokenClaimsMapGenerator
        )

    internal fun refreshTokenHandler(): RefreshTokenHandler =
        RefreshTokenHandler(
            jwtKey = jwtKeyRefreshToken,
            tokenDurationGenerator = tokenDurationGenerator,
            tokenClaimsMapGenerator = tokenClaimsMapGenerator
        )

    internal fun registrationConfirmationTokenHandler(): RegistrationConfirmationTokenHandler =
        RegistrationConfirmationTokenHandler(
            jwtKey = jwtKeyRegistrationToken,
            tokenDurationGenerator = tokenDurationGenerator
        )

    internal fun usernamePasswordAuthTokenTokenGenerator(): UsernamePasswordAuthTokenTokenGenerator =
        UsernamePasswordAuthTokenTokenGenerator()

    internal fun registrationConfirmationRepo(): RegistrationConfirmationRepo = RegistrationConfirmationRepo()

    internal fun jwtParserAccessToken() = Jwts.parserBuilder().setSigningKey(jwtKeyAccessToken).build()

    internal fun jwtParserRefreshToken() = Jwts.parserBuilder().setSigningKey(jwtKeyRefreshToken).build()

    internal fun jwtParserRegistrationToken() = Jwts.parserBuilder().setSigningKey(jwtKeyRegistrationToken).build()

    internal fun jwtBuilder() = Jwts.builder()

}