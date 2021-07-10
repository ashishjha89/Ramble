package com.ramble.token.handler.helper

import com.ramble.token.model.AccessClaims
import io.jsonwebtoken.Claims
import io.jsonwebtoken.JwtParser
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import java.security.Principal
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

internal class AccessTokenHelper(
        private val jwtKeyGenerator: JwtKeyGenerator = JwtKeyGenerator(),
        private val accessTokenDurationHelper: AccessTokenDurationHelper = AccessTokenDurationHelper()
) {

    companion object {

        private const val ROLES = "ROLES"

        private const val USER_ID = "USER_ID"
    }

    fun generateAccessToken(
            authResult: Authentication,
            userId: String,
            email: String,
            issuedInstant: Instant,
            expiryDurationAmount: Long,
            expiryDurationUnit: ChronoUnit
    ): String {
        val key = jwtKeyGenerator.key
        val accessTokenDuration = accessTokenDurationHelper.getTokenDurationHelper(issuedInstant, expiryDurationAmount, expiryDurationUnit)
        val claimsMap = mapOf(
                ROLES to authResult.authorities.map { it.authority },
                USER_ID to userId
        )
        return Jwts.builder()
                .setClaims(claimsMap)
                .setSubject(email)
                .setIssuedAt(accessTokenDuration.issuedDate)
                .setExpiration(accessTokenDuration.expiryDate)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact()
    }

    fun getClaims(token: String?, jwtParser: JwtParser? = null, now: Instant = Instant.now()): AccessClaims? {
        val parser = jwtParser ?: Jwts.parserBuilder().setSigningKey(jwtKeyGenerator.key).build()
        if (token == null || !isValidAccessToken(token, parser, now)) return null
        return AccessClaims(
                userId = getUserIdFromAccessToken(token, parser) ?: return null,
                email = getEmailFromAccessToken(token, parser) ?: return null,
                claims = getClaimsFromAccessToken(token, parser) ?: return null,
                authorities = getRolesFromAccessToken(token, parser) ?: return null
        )
    }

    fun getClaims(principal: Principal): AccessClaims? {
        val claims = getPrincipalClaims(principal) ?: return null
        val authorities = getAuthorities(principal) ?: return null
        val userId = getUserId(claims) ?: return null
        val email = claims.subject ?: return null
        return AccessClaims(
                userId = userId,
                email = email,
                claims = claims,
                authorities = authorities
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

    private fun getPrincipalClaims(principal: Principal): Claims? {
        if (principal is UsernamePasswordAuthenticationToken) {
            val claims = principal.principal
            if (claims is Claims) {
                return claims
            }
        }
        return null
    }

    private fun getAuthorities(principal: Principal): List<GrantedAuthority>? {
        if (principal is UsernamePasswordAuthenticationToken) {
            return principal.authorities?.toList()
        }
        return null
    }

    private fun getUserId(claims: Claims): String? {
        return claims[USER_ID].takeIf { it is String }?.toString()
    }


}