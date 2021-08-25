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

    fun generateToken(
        email: String,
        issuedInstant: Instant,
        expiryDurationAmount: Long,
        expiryDurationUnit: ChronoUnit,
        jwtBuilder: JwtBuilder
    ): String {
        val tokenDuration =
            tokenDurationGenerator.getTokenDuration(issuedInstant, expiryDurationAmount, expiryDurationUnit)
        return jwtBuilder
            .setSubject(email)
            .setIssuedAt(tokenDuration.issuedDate)
            .setExpiration(tokenDuration.expiryDate)
            .signWith(jwtKey, SignatureAlgorithm.HS512)
            .compact()
    }

    fun isValidToken(token: String, now: Instant, parser: JwtParser): Boolean =
        !isTokenExpired(token, parser, now) && !getEmailFromToken(token, parser).isNullOrBlank()

    fun getEmailFromToken(token: String, parser: JwtParser): String? =
        getClaimsFromToken(token, parser)?.subject

    private fun getExpirationDateFromToken(token: String, parser: JwtParser): Date? =
        getClaimsFromToken(token, parser)?.expiration

    private fun isTokenExpired(token: String, parser: JwtParser, now: Instant) =
        getExpirationDateFromToken(token, parser)?.before(Date.from(now)) ?: false

    private fun getClaimsFromToken(token: String, parser: JwtParser): Claims? {
        return try {
            parser.parseClaimsJws(token)?.body
        } catch (e: Exception) {
            null
        }
    }

}