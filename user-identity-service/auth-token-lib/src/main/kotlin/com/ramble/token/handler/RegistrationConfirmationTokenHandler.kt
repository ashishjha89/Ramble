package com.ramble.token.handler

import com.ramble.token.handler.helper.TokenDurationGenerator
import io.jsonwebtoken.Claims
import io.jsonwebtoken.JwtBuilder
import io.jsonwebtoken.JwtParser
import io.jsonwebtoken.SignatureAlgorithm
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*
import javax.crypto.SecretKey

internal class RegistrationConfirmationTokenHandler(
        private val jwtKey: SecretKey,
        private val tokenDurationGenerator: TokenDurationGenerator
) {

    companion object {
        private const val USER_ID = "USER_ID"
    }

    fun generateToken(
            userId: String,
            email: String,
            issuedInstant: Instant,
            expiryDurationAmount: Long,
            expiryDurationUnit: ChronoUnit,
            jwtBuilder: JwtBuilder): String {
        val tokenDuration = tokenDurationGenerator.getTokenDuration(issuedInstant, expiryDurationAmount, expiryDurationUnit)
        val claimsMap = mapOf(USER_ID to userId)
        return jwtBuilder
                .setClaims(claimsMap)
                .setSubject(email)
                .setIssuedAt(tokenDuration.issuedDate)
                .setExpiration(tokenDuration.expiryDate)
                .signWith(jwtKey, SignatureAlgorithm.HS512)
                .compact()
    }

    fun isValidToken(token: String, now: Instant, parser: JwtParser): Boolean =
            !isTokenExpired(token, parser, now) && !getUserIdFromToken(token, parser).isNullOrBlank()

    fun getUserIdFromToken(token: String, parser: JwtParser): String? =
            getClaimsFromAccessToken(token, parser)?.get(USER_ID) as? String

    private fun getClaimsFromAccessToken(token: String, parser: JwtParser): Claims? =
            parser.parseClaimsJws(token)?.body

    private fun getExpirationDateFromToken(token: String, parser: JwtParser): Date? =
            getClaimsFromAccessToken(token, parser)?.expiration

    private fun isTokenExpired(token: String, parser: JwtParser, now: Instant) =
            getExpirationDateFromToken(token, parser)?.before(Date.from(now)) ?: false

}