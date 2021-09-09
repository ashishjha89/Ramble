package com.ramble.accesstoken

import com.ramble.accesstoken.config.AccessTokenValidatorBuilder
import com.ramble.accesstoken.model.AccessClaims
import com.ramble.accesstoken.model.AccessTokenValidatorInternalException
import com.ramble.accesstoken.repo.AccessTokenValidatorRepo
import io.jsonwebtoken.Claims
import org.springframework.security.core.GrantedAuthority
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.temporal.ChronoUnit

private typealias AccessToken = String

@Service
class AccessTokenValidatorService(
    private val accessTokenValidatorRepo: AccessTokenValidatorRepo,
    private val tokenComponentBuilder: AccessTokenValidatorBuilder
) {

    private val jwtBuilder = tokenComponentBuilder.jwtBuilder()

    private val jwtParserAccessToken = tokenComponentBuilder.jwtParserAccessToken()

    private val accessTokenHandler = tokenComponentBuilder.accessTokenHandler()

    fun generateAccessToken(
        roles: List<String>,
        clientId: String,
        userId: String,
        email: String,
        issuedInstant: Instant,
        expiryDurationAmount: Long,
        expiryDurationUnit: ChronoUnit
    ): String {
        val authorities = accessTokenHandler.getAuthoritiesForRoles(roles)
        return accessTokenHandler.generateToken(
            authorities, clientId, userId, email, issuedInstant, expiryDurationAmount, expiryDurationUnit, jwtBuilder
        )
    }

    @Throws(AccessTokenValidatorInternalException::class)
    suspend fun getClaimsFromAccessToken(accessToken: String, now: Instant): AccessClaims? {
        // 1. Check if token is of correct format
        val accessClaims = accessTokenHandler.getAccessClaims(accessToken, jwtParserAccessToken, now) ?: return null

        // 2. Check if token is not in disabled-token-list
        val disabledTokens = accessTokenValidatorRepo.getDisabledAccessTokensForClient(
            clientId = accessClaims.clientId,
            scope = tokenComponentBuilder.defaultIoScope
        )
        return if (!disabledTokens.contains(accessToken)) accessClaims else null
    }

    fun getAccessClaims(claims: Claims, authorities: List<GrantedAuthority>): AccessClaims? =
        accessTokenHandler.getAccessClaims(claims, authorities)

    @Throws(AccessTokenValidatorInternalException::class)
    suspend fun disableAccessToken(clientId: String, accessToken: AccessToken, now: Instant) {
        val scope = tokenComponentBuilder.defaultIoScope
        val existingDisabledTokens = accessTokenValidatorRepo.getDisabledAccessTokensForClient(clientId, scope)
        val validDisabledAccessTokens = existingDisabledTokens.filter {
            accessTokenHandler.isValidToken(token = it, parser = jwtParserAccessToken, now = now)
        }.toSet()
        accessTokenValidatorRepo.updateDisabledAccessTokensForClient(
            clientId, validDisabledAccessTokens.plus(accessToken), scope
        )
    }

}