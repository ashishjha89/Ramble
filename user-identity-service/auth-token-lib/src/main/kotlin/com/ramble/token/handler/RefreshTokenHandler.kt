package com.ramble.token.handler

import com.ramble.token.handler.helper.TokenClaimsMapGenerator
import com.ramble.token.handler.helper.TokenDurationGenerator
import io.jsonwebtoken.Claims
import io.jsonwebtoken.JwtBuilder
import io.jsonwebtoken.JwtParser
import io.jsonwebtoken.SignatureAlgorithm
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*
import javax.crypto.SecretKey

internal class RefreshTokenHandler(
    private val jwtKey: SecretKey,
    private val tokenDurationGenerator: TokenDurationGenerator,
    private val tokenClaimsMapGenerator: TokenClaimsMapGenerator
) {

    fun generateToken(
        clientId: String,
        userId: String,
        issuedInstant: Instant,
        expiryDurationAmount: Long,
        expiryDurationUnit: ChronoUnit,
        jwtBuilder: JwtBuilder
    ): String {
        val tokenDuration =
            tokenDurationGenerator.getTokenDuration(issuedInstant, expiryDurationAmount, expiryDurationUnit)
        val claimsMap = tokenClaimsMapGenerator.getRefreshTokenClaimsMap(clientId, userId)
        return jwtBuilder
            .setClaims(claimsMap)
            .setSubject(userId)
            .setIssuedAt(tokenDuration.issuedDate)
            .setExpiration(tokenDuration.expiryDate)
            .signWith(jwtKey, SignatureAlgorithm.HS512)
            .compact()
    }

    fun isValidToken(token: String, parser: JwtParser, now: Instant): Boolean =
        !isTokenExpired(token, parser, now)
                && !getUserIdFromToken(token, parser).isNullOrBlank()
                && !getClientIdFromToken(token, parser).isNullOrBlank()

    fun getUserIdFromToken(token: String, parser: JwtParser): String? =
        getClaimsFromRefreshToken(token, parser)?.get(TokenClaimsMapGenerator.USER_ID) as? String

    fun getClientIdFromToken(token: String, parser: JwtParser): String? =
        getClaimsFromRefreshToken(token, parser)?.get(TokenClaimsMapGenerator.CLIENT_ID) as? String

    private fun getExpirationDateFromToken(token: String, parser: JwtParser): Date? =
        getClaimsFromRefreshToken(token, parser)?.expiration

    private fun isTokenExpired(token: String, parser: JwtParser, now: Instant) =
        getExpirationDateFromToken(token, parser)?.before(Date.from(now)) ?: false

    private fun getClaimsFromRefreshToken(token: String, parser: JwtParser): Claims? =
        parser.parseClaimsJws(token)?.body
}