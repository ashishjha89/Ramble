package com.ramble.accesstoken.config

import com.ramble.accesstoken.handler.AccessTokenHandler
import com.ramble.accesstoken.handler.helper.TokenClaimsMapGenerator
import com.ramble.accesstoken.handler.helper.TokenDurationGenerator
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import org.springframework.stereotype.Component

@Component
class AccessTokenValidatorBuilder(accessTokenValidatorConfig: AccessTokenValidatorConfig) {

    private val jwtTokenProperties = accessTokenValidatorConfig.accessTokenConfigProperties

    private val jwtKeyAccessToken = Keys.hmacShaKeyFor(jwtTokenProperties.signingKeyAccessToken.toByteArray())

    private val tokenDurationGenerator = TokenDurationGenerator()

    private val tokenClaimsMapGenerator = TokenClaimsMapGenerator()

    val defaultIoScope: CoroutineScope
        get() = CoroutineScope(Job() + Dispatchers.IO)

    internal fun accessTokenHandler(): AccessTokenHandler =
        AccessTokenHandler(
            jwtKey = jwtKeyAccessToken,
            tokenDurationGenerator = tokenDurationGenerator,
            tokenClaimsMapGenerator = tokenClaimsMapGenerator
        )

    internal fun jwtParserAccessToken() = Jwts.parserBuilder().setSigningKey(jwtKeyAccessToken).build()

    internal fun jwtBuilder() = Jwts.builder()

}