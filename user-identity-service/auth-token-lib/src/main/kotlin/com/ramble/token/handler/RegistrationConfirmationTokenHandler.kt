package com.ramble.token.handler

import io.jsonwebtoken.Claims
import io.jsonwebtoken.JwtParser
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*
import javax.crypto.SecretKey

internal class RegistrationConfirmationTokenHandler(
        private val jwtKey: SecretKey,
        private val accessTokenDurationGenerator: AccessTokenDurationGenerator
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
        val accessTokenDuration = accessTokenDurationGenerator.getTokenDuration(issuedInstant, expiryDurationAmount, expiryDurationUnit)
        val claimsMap = mapOf(USER_ID to userId)
        return Jwts.builder()
                .setClaims(claimsMap)
                .setSubject(email)
                .setIssuedAt(accessTokenDuration.issuedDate)
                .setExpiration(accessTokenDuration.expiryDate)
                .signWith(jwtKey, SignatureAlgorithm.HS512)
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
            this ?: Jwts.parserBuilder().setSigningKey(jwtKey).build()

}