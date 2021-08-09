package com.ramble.token

import com.ramble.token.config.TokenComponentBuilder
import com.ramble.token.handler.AccessTokenHandler
import com.ramble.token.handler.RefreshTokenHandler
import com.ramble.token.handler.helper.UsernamePasswordAuthTokenTokenGenerator
import com.ramble.token.model.*
import com.ramble.token.repository.AuthTokenRepo
import io.jsonwebtoken.Claims
import org.springframework.security.core.GrantedAuthority
import org.springframework.stereotype.Service
import java.security.Principal
import java.time.Instant
import java.time.temporal.ChronoUnit
import org.springframework.security.core.Authentication as SpringAuthentication

@Service
class AuthTokensService(private val authTokenRepo: AuthTokenRepo, tokenComponentBuilder: TokenComponentBuilder) {

    private val jwtParser = tokenComponentBuilder.jwtParserAccessToken()

    private val jwtBuilder = tokenComponentBuilder.jwtBuilder()

    private val accessTokenHandler: AccessTokenHandler = tokenComponentBuilder.accessTokenHandler()

    private val refreshTokenHandler: RefreshTokenHandler = tokenComponentBuilder.refreshTokenHandler()

    private val usernamePasswordAuthTokenTokenGenerator: UsernamePasswordAuthTokenTokenGenerator =
            tokenComponentBuilder.usernamePasswordAuthTokenTokenGenerator()

    suspend fun generateUserAuthToken(
            authorities: Collection<GrantedAuthority>,
            clientId: String,
            userId: String,
            email: String,
            issuedInstant: Instant = Instant.now(),
            accessTokenExpiryDurationAmount: Long = 30,
            accessTokenExpiryDurationUnit: ChronoUnit = ChronoUnit.MINUTES,
            refreshTokenExpiryDurationAmount: Long = 356,
            refreshTokenExpiryDurationUnit: ChronoUnit = ChronoUnit.DAYS
    ): UserAuthInfo {
        val userAuthInfo = UserAuthInfo(
                userId = userId,
                email = email,
                accessToken = accessTokenHandler.generateAccessToken(
                        authorities = authorities,
                        clientId = clientId,
                        userId = userId,
                        email = email,
                        issuedInstant = issuedInstant,
                        expiryDurationAmount = accessTokenExpiryDurationAmount,
                        expiryDurationUnit = accessTokenExpiryDurationUnit,
                        jwtBuilder = jwtBuilder
                ),
                refreshToken = refreshTokenHandler.generateRefreshToken()
        )
        authTokenRepo.insertUserAuthInfo(clientId, userAuthInfo)
        return userAuthInfo
    }

    @Throws(RefreshTokenIsInvalidException::class)
    suspend fun refreshAuthToken(
            refreshToken: String,
            now: Instant,
            accessTokenExpiryDurationAmount: Long = 30,
            accessTokenExpiryDurationUnit: ChronoUnit = ChronoUnit.MINUTES
    ): UserAuthInfo? {
        // 1. Delete old entry of refresh-token for the client.
        val clientAuthInfo = authTokenRepo.deleteOldAuthTokens(refreshToken)
        val accessToken = clientAuthInfo.accessToken

        // 2a) Insert access-token for above refresh-token in disabled list.
        // 2b) Clean stale access-tokens for this client.
        val disabledAccessTokens = authTokenRepo.getDisabledAccessTokensForClient(clientAuthInfo).plus(accessToken)
        val updatedDisabledAccessTokens = disabledAccessTokens.filter {
            accessTokenHandler.isValidAccessToken(token = it, parser = jwtParser, now = now)
        }.toSet()
        authTokenRepo.updateDisabledAccessTokensForClient(clientAuthInfo, updatedDisabledAccessTokens)

        // 3. Now, generate new auth-tokens
        val clientId = accessTokenHandler.getClientIdFromAccessToken(accessToken, jwtParser) ?: return null
        val userId = accessTokenHandler.getUserIdFromAccessToken(accessToken, jwtParser) ?: return null
        val email = accessTokenHandler.getEmailFromAccessToken(accessToken, jwtParser) ?: return null
        val roles = accessTokenHandler.getRolesFromAccessToken(accessToken, jwtParser) ?: return null

        return generateUserAuthToken(
                authorities = roles,
                clientId = clientId,
                userId = userId,
                email = email,
                issuedInstant = now,
                accessTokenExpiryDurationAmount = accessTokenExpiryDurationAmount,
                accessTokenExpiryDurationUnit = accessTokenExpiryDurationUnit
        )
    }

    fun getAccessTokenClaims(accessToken: String?, now: Instant = Instant.now()): AccessClaims? {
        accessToken ?: return null
        // 1. Check if token is of correct format
        val accessClaims = accessTokenHandler.getTokenClaims(accessToken, jwtParser, now) ?: return null

        // 2. Check if token is not in disabled-token-list
        val clientAuthInfo = ClientAuthInfo(
                clientId = accessClaims.clientId,
                userId = accessClaims.userId,
                accessToken = accessToken
        )
        val disabledTokens = authTokenRepo.getDisabledAccessTokensForClient(clientAuthInfo)
        val disabledTokensNonExpired = disabledTokens.filter {
            accessTokenHandler.isValidAccessToken(token = it, parser = jwtParser, now = now)
        }.toSet()
        if (disabledTokens.size != disabledTokensNonExpired.size) {
            // If some disabled-token is not invalid, update the list.
            authTokenRepo.updateDisabledAccessTokensForClient(clientAuthInfo, disabledTokensNonExpired)
        }
        return if (!disabledTokensNonExpired.contains(accessToken)) accessClaims else null
    }

    @Throws(AccessTokenIsInvalidException::class)
    fun logout(accessToken: String, now: Instant) {
        val clientId = accessTokenHandler.getClientIdFromAccessToken(accessToken, jwtParser)
        val userId = accessTokenHandler.getUserIdFromAccessToken(accessToken, jwtParser)
        if (clientId == null || userId == null) throw AccessTokenIsInvalidException()
        val clientAuthInfo = ClientAuthInfo(clientId, userId, accessToken)

        // Get existing disabled tokens for the client.
        val disabledTokens = authTokenRepo.getDisabledAccessTokensForClient(clientAuthInfo)
        if (disabledTokens.contains(accessToken)) throw AccessTokenIsInvalidException()

        val disabledTokensNonExpired = disabledTokens.plus(accessToken).filter {
            accessTokenHandler.isValidAccessToken(token = it, parser = jwtParser, now = now)
        }.toSet()

        // Add accessToken to disabledToken list
        authTokenRepo.updateDisabledAccessTokensForClient(clientAuthInfo, disabledTokensNonExpired)
    }

    fun getAccessTokenClaims(principal: Principal): AccessClaims? =
            accessTokenHandler.getPrincipalClaims(principal)

    fun springAuthentication(claims: Claims, authorities: List<GrantedAuthority>): SpringAuthentication =
            usernamePasswordAuthTokenTokenGenerator.getUsernamePasswordAuthenticationToken(claims, authorities)

}