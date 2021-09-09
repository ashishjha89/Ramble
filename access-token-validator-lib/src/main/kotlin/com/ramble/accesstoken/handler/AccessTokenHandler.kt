package com.ramble.accesstoken.handler

import com.ramble.accesstoken.handler.helper.TokenClaimsMapGenerator
import com.ramble.accesstoken.handler.helper.TokenClaimsMapGenerator.Companion.CLIENT_ID
import com.ramble.accesstoken.handler.helper.TokenClaimsMapGenerator.Companion.ROLES
import com.ramble.accesstoken.handler.helper.TokenClaimsMapGenerator.Companion.USER_ID
import com.ramble.accesstoken.handler.helper.TokenDurationGenerator
import com.ramble.accesstoken.model.AccessClaims
import io.jsonwebtoken.*
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
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

    fun getAccessClaims(token: String, parser: JwtParser, now: Instant): AccessClaims? {
        if (!isValidToken(token, parser, now)) return null
        return AccessClaims(
            clientId = getClientIdFromToken(token, parser) ?: return null,
            userId = getUserIdFromToken(token, parser) ?: return null,
            email = getEmailFromToken(token, parser) ?: return null,
            claims = getClaimsFromToken(token, parser) ?: return null,
            roles = getRolesFromToken(token, parser) ?: return null
        )
    }

    fun getAccessClaims(claims: Claims, authorities: List<GrantedAuthority>): AccessClaims? {
        val clientId = getClientId(claims) ?: return null
        val userId = getUserId(claims) ?: return null
        val email = claims.subject ?: return null
        return AccessClaims(
            clientId = clientId,
            userId = userId,
            email = email,
            claims = claims,
            roles = authorities.map { (it as SimpleGrantedAuthority).authority }
        )
    }

    fun isValidToken(token: String, parser: JwtParser, now: Instant): Boolean =
        !isTokenExpired(token, parser, now)
                && !getEmailFromToken(token, parser).isNullOrBlank()
                && !getClientIdFromToken(token, parser).isNullOrBlank()

    fun getAuthoritiesForRoles(roles: List<String>): List<GrantedAuthority> =
        roles.map { SimpleGrantedAuthority(it) }

    fun getUserIdFromToken(token: String, parser: JwtParser): String? =
        getClaimsFromToken(token, parser)?.get(USER_ID) as? String

    fun getClientIdFromToken(token: String, parser: JwtParser): String? =
        getClaimsFromToken(token, parser)?.get(CLIENT_ID) as? String

    fun getEmailFromToken(token: String, parser: JwtParser): String? =
        getClaimsFromToken(token, parser)?.subject

    private fun getExpirationDateFromToken(token: String, parser: JwtParser): Date? =
        getClaimsFromToken(token, parser)?.expiration

    private fun getClaimsFromToken(token: String, parser: JwtParser): Claims? =
        try {
            parser.parseClaimsJws(token)?.body
        } catch (e: JwtException) {
            null
        }

    private fun isTokenExpired(token: String, parser: JwtParser, now: Instant) =
        getExpirationDateFromToken(token, parser)?.before(Date.from(now)) ?: false

    private fun getClientId(claims: Claims): String? =
        claims[CLIENT_ID].takeIf { it is String }?.toString()

    private fun getUserId(claims: Claims): String? =
        claims[USER_ID].takeIf { it is String }?.toString()

    @Suppress("UNCHECKED_CAST")
    private fun getRolesFromToken(token: String, parser: JwtParser): List<String>? =
        (getClaimsFromToken(token, parser)?.get(ROLES) as? List<String>)

}