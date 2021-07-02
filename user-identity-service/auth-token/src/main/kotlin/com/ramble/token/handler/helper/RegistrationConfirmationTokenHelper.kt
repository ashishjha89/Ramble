package com.ramble.token.handler.helper

import io.jsonwebtoken.Claims
import io.jsonwebtoken.JwtParser
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

internal class RegistrationConfirmationTokenHelper(
        private val jwtKeyGenerator: JwtKeyGenerator = JwtKeyGenerator(),
        private val accessTokenDurationHelper: AccessTokenDurationHelper = AccessTokenDurationHelper()
) {

    companion object {
        private const val USER_ID = "USER_ID"
    }

    fun generateToken(
            userId: String,
            email: String,
            issuedInstant: Instant,
            expiryDurationAmount: Long,
            expiryDurationUnit: ChronoUnit): String {
        val key = jwtKeyGenerator.key
        val accessTokenDuration = accessTokenDurationHelper.getTokenDurationHelper(issuedInstant, expiryDurationAmount, expiryDurationUnit)
        val claimsMap = mapOf(USER_ID to userId)
        return Jwts.builder()
                .setClaims(claimsMap)
                .setSubject(email)
                .setIssuedAt(accessTokenDuration.issuedDate)
                .setExpiration(accessTokenDuration.expiryDate)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact()
    }

    fun isValidToken(token: String, now: Instant, parser: JwtParser? = null): Boolean =
            !isTokenExpired(token, parser.validParser(), now) && !getUserIdFromToken(token, parser).isNullOrBlank()

    fun getUserIdFromToken(token: String, parser: JwtParser? = null): String? =
            getClaimsFromAccessToken(token, parser.validParser())?.get(USER_ID) as? String

    private fun getClaimsFromAccessToken(token: String, parser: JwtParser): Claims? =
            parser.parseClaimsJws(token)?.body

    private fun getExpirationDateFromToken(token: String, parser: JwtParser): Date? =
            getClaimsFromAccessToken(token, parser)?.expiration

    private fun isTokenExpired(token: String, parser: JwtParser, now: Instant) =
            getExpirationDateFromToken(token, parser)?.before(Date.from(now)) ?: false

    private fun JwtParser?.validParser() =
            this ?: Jwts.parserBuilder().setSigningKey(jwtKeyGenerator.key).build()

}