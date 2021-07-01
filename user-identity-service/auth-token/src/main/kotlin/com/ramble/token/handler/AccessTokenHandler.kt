package com.ramble.token.handler

import com.ramble.token.model.AccessTokenClaims
import io.jsonwebtoken.Claims
import io.jsonwebtoken.JwtParser
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

internal class AccessTokenHandler {

    companion object {

        private const val KEY = "OMQBBbeM64asPB0mvUYI4b+G08t4e6npxRXqzq6ZwmC1Ao4ibnBPT1oGH09rQuwWPHFy0hWCsfjm0MsiA2eJeA=="  // Key to sign the token. Minimum length should be 512 bytes

        private const val ROLES = "ROLES"

        private const val USER_ID = "USER_ID"
    }

    fun generateAccessToken(authResult: Authentication, userId: String, email: String, issuedInstant: Instant = Instant.now()): String {
        val expiryInstant = issuedInstant.plus(30, ChronoUnit.SECONDS)
        val issuedDate = Date.from(issuedInstant)
        val expiryDate = Date.from(expiryInstant)
        val key = Keys.hmacShaKeyFor(KEY.toByteArray())
        val claimsMap = mapOf(
                ROLES to authResult.authorities.map { it.authority },
                USER_ID to userId
        )
        return Jwts.builder()
                .setClaims(claimsMap)
                .setSubject(email)
                .setIssuedAt(issuedDate)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact()
    }

    fun getAccessTokenClaims(token: String?, jwtParser: JwtParser? = null, now: Instant = Instant.now()): AccessTokenClaims? {
        val parser = jwtParser ?: Jwts.parserBuilder().setSigningKey(Keys.hmacShaKeyFor(KEY.toByteArray())).build()
        if (token == null || !isValidAccessToken(token, parser, now)) return null
        return AccessTokenClaims(
                userId = getUserIdFromAccessToken(token, parser) ?: return null,
                email = getEmailFromAccessToken(token, parser) ?: return null,
                claims = getClaimsFromAccessToken(token, parser) ?: return null,
                authorities = getRolesFromAccessToken(token, parser) ?: return null
        )
    }

    private fun isValidAccessToken(token: String, parser: JwtParser, now: Instant): Boolean =
            !isTokenExpired(token, parser, now) && !getEmailFromAccessToken(token, parser).isNullOrBlank()

    private fun getClaimsFromAccessToken(token: String, parser: JwtParser): Claims? =
            parser.parseClaimsJws(token)?.body

    @Suppress("UNCHECKED_CAST")
    private fun getRolesFromAccessToken(token: String, parser: JwtParser): List<GrantedAuthority>? =
            (getClaimsFromAccessToken(token, parser)?.get(ROLES) as List<String>).map { SimpleGrantedAuthority(it) }

    private fun getUserIdFromAccessToken(token: String, parser: JwtParser): String? =
            getClaimsFromAccessToken(token, parser)?.get(USER_ID) as String

    private fun getEmailFromAccessToken(token: String, parser: JwtParser): String? =
            getClaimsFromAccessToken(token, parser)?.subject

    private fun getExpirationDateFromToken(token: String, parser: JwtParser): Date? =
            getClaimsFromAccessToken(token, parser)?.expiration

    private fun isTokenExpired(token: String, parser: JwtParser, now: Instant) =
            getExpirationDateFromToken(token, parser)?.before(Date.from(now)) ?: false

}