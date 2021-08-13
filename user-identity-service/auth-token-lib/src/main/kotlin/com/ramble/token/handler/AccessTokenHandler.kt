package com.ramble.token.handler

import com.ramble.token.handler.helper.TokenClaimsMapGenerator
import com.ramble.token.handler.helper.TokenClaimsMapGenerator.Companion.CLIENT_ID
import com.ramble.token.handler.helper.TokenClaimsMapGenerator.Companion.ROLES
import com.ramble.token.handler.helper.TokenClaimsMapGenerator.Companion.USER_ID
import com.ramble.token.handler.helper.TokenDurationGenerator
import com.ramble.token.model.AccessClaims
import io.jsonwebtoken.Claims
import io.jsonwebtoken.JwtBuilder
import io.jsonwebtoken.JwtParser
import io.jsonwebtoken.SignatureAlgorithm
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import java.security.Principal
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*
import javax.crypto.SecretKey

internal class AccessTokenHandler(
    private val jwtKey: SecretKey,
    private val tokenDurationGenerator: TokenDurationGenerator,
    private val tokenClaimsMapGenerator: TokenClaimsMapGenerator
) {

    fun generateToken(
        authorities: Collection<GrantedAuthority>,
        clientId: String,
        userId: String,
        email: String,
        issuedInstant: Instant,
        expiryDurationAmount: Long,
        expiryDurationUnit: ChronoUnit,
        jwtBuilder: JwtBuilder
    ): String {
        val tokenDuration =
            tokenDurationGenerator.getTokenDuration(issuedInstant, expiryDurationAmount, expiryDurationUnit)
        val claimsMap = tokenClaimsMapGenerator.getAccessTokenClaimsMap(clientId, userId, authorities)
        return jwtBuilder
            .setClaims(claimsMap)
            .setSubject(email)
            .setIssuedAt(tokenDuration.issuedDate)
            .setExpiration(tokenDuration.expiryDate)
            .signWith(jwtKey, SignatureAlgorithm.HS512)
            .compact()
    }

    fun getTokenClaims(token: String?, parser: JwtParser, now: Instant): AccessClaims? {
        if (token == null || !isValidToken(token, parser, now)) return null
        return AccessClaims(
            clientId = getClientIdFromToken(token, parser) ?: return null,
            userId = getUserIdFromToken(token, parser) ?: return null,
            email = getEmailFromToken(token, parser) ?: return null,
            claims = getClaimsFromToken(token, parser) ?: return null,
            authorities = getRolesFromToken(token, parser) ?: return null
        )
    }

    fun getPrincipalClaims(principal: Principal): AccessClaims? {
        val claims = principalClaims(principal) ?: return null
        val authorities = getAuthorities(principal) ?: return null
        val clientId = getClientId(claims) ?: return null
        val userId = getUserId(claims) ?: return null
        val email = claims.subject ?: return null
        return AccessClaims(
            clientId = clientId,
            userId = userId,
            email = email,
            claims = claims,
            authorities = authorities
        )
    }

    fun isValidToken(token: String, parser: JwtParser, now: Instant): Boolean =
        !isTokenExpired(token, parser, now)
                && !getEmailFromToken(token, parser).isNullOrBlank()
                && !getClientIdFromToken(token, parser).isNullOrBlank()

    private fun getClaimsFromToken(token: String, parser: JwtParser): Claims? =
        parser.parseClaimsJws(token)?.body

    @Suppress("UNCHECKED_CAST")
    fun getRolesFromToken(token: String, parser: JwtParser): List<GrantedAuthority>? =
        (getClaimsFromToken(token, parser)?.get(ROLES) as? List<String>)?.map { SimpleGrantedAuthority(it) }

    fun getUserIdFromToken(token: String, parser: JwtParser): String? =
        getClaimsFromToken(token, parser)?.get(USER_ID) as? String

    fun getClientIdFromToken(token: String, parser: JwtParser): String? =
        getClaimsFromToken(token, parser)?.get(CLIENT_ID) as? String

    fun getEmailFromToken(token: String, parser: JwtParser): String? =
        getClaimsFromToken(token, parser)?.subject

    private fun getExpirationDateFromToken(token: String, parser: JwtParser): Date? =
        getClaimsFromToken(token, parser)?.expiration

    private fun isTokenExpired(token: String, parser: JwtParser, now: Instant) =
        getExpirationDateFromToken(token, parser)?.before(Date.from(now)) ?: false

    private fun principalClaims(principal: Principal): Claims? {
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

    private fun getClientId(claims: Claims): String? {
        return claims[CLIENT_ID].takeIf { it is String }?.toString()
    }

    private fun getUserId(claims: Claims): String? {
        return claims[USER_ID].takeIf { it is String }?.toString()
    }

}