package com.ramble.token

import com.ramble.token.config.TokenComponentBuilder
import com.ramble.token.handler.AccessTokenHandler
import com.ramble.token.handler.RefreshTokenHandler
import com.ramble.token.handler.helper.UsernamePasswordAuthTokenTokenGenerator
import com.ramble.token.model.AccessClaims
import com.ramble.token.model.AccessTokenIsInvalidException
import com.ramble.token.model.RefreshTokenIsInvalidException
import com.ramble.token.model.UserAuthInfo
import com.ramble.token.repository.AuthTokenRepo
import com.ramble.token.repository.persistence.entities.ClientAuthInfo
import io.jsonwebtoken.Claims
import org.springframework.security.core.GrantedAuthority
import org.springframework.stereotype.Service
import java.security.Principal
import java.time.Instant
import java.time.temporal.ChronoUnit
import org.springframework.security.core.Authentication as SpringAuthentication

@Service
class AuthTokensService(private val authTokenRepo: AuthTokenRepo, tokenComponentBuilder: TokenComponentBuilder) {

    private val jwtParserAccessToken = tokenComponentBuilder.jwtParserAccessToken()

    private val jwtParserRefreshToken = tokenComponentBuilder.jwtParserRefreshToken()

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
            accessToken = accessTokenHandler.generateToken(
                authorities = authorities,
                clientId = clientId,
                userId = userId,
                email = email,
                issuedInstant = issuedInstant,
                expiryDurationAmount = accessTokenExpiryDurationAmount,
                expiryDurationUnit = accessTokenExpiryDurationUnit,
                jwtBuilder = jwtBuilder
            ),
            refreshToken = refreshTokenHandler.generateToken(
                clientId = clientId,
                userId = userId,
                issuedInstant = issuedInstant,
                expiryDurationAmount = refreshTokenExpiryDurationAmount,
                expiryDurationUnit = refreshTokenExpiryDurationUnit,
                jwtBuilder = jwtBuilder
            )
        )
        disableOldAccessTokens(clientId, userAuthInfo, issuedInstant)
        authTokenRepo.insertUserAuthInfo(clientId, userAuthInfo)
        return userAuthInfo
    }

    @Throws(RefreshTokenIsInvalidException::class)
    suspend fun refreshAuthToken(
        refreshToken: String,
        now: Instant,
        accessTokenExpiryDurationAmount: Long = 30,
        accessTokenExpiryDurationUnit: ChronoUnit = ChronoUnit.MINUTES,
        refreshTokenExpiryDurationAmount: Long = 356,
        refreshTokenExpiryDurationUnit: ChronoUnit = ChronoUnit.DAYS
    ): UserAuthInfo? {
        // 1. Get clientId and userId from RefreshToken
        val clientIdFromRefreshToken = refreshTokenHandler.getClientIdFromToken(refreshToken, jwtParserRefreshToken)
            ?: throw RefreshTokenIsInvalidException()
        val userIdFromRefreshToken = refreshTokenHandler.getUserIdFromToken(refreshToken, jwtParserRefreshToken)
            ?: throw RefreshTokenIsInvalidException()

        // 2. Delete old entry of token for the client.
        val clientAuthInfo = authTokenRepo.deleteOldAuthTokens(clientIdFromRefreshToken, userIdFromRefreshToken)
        val accessToken = clientAuthInfo.accessToken

        // 3. Check if RefreshToken has valid format
        if (!refreshTokenHandler.isValidToken(refreshToken, jwtParserRefreshToken, now))
            throw RefreshTokenIsInvalidException()

        // 4a) Insert access-token for above refresh-token in disabled list.
        // 4b) Clean stale access-tokens for this client.
        val disabledAccessTokens = authTokenRepo.getDisabledAccessTokensForClient(clientAuthInfo).plus(accessToken)
        val validDisabledAccessTokens = disabledAccessTokens.filter {
            accessTokenHandler.isValidToken(token = it, parser = jwtParserAccessToken, now = now)
        }.toSet()
        authTokenRepo.updateDisabledAccessTokensForClient(clientAuthInfo, validDisabledAccessTokens)

        // 5. Now, generate new auth-tokens
        val clientId = accessTokenHandler.getClientIdFromToken(accessToken, jwtParserAccessToken) ?: return null
        val userId = accessTokenHandler.getUserIdFromToken(accessToken, jwtParserAccessToken) ?: return null
        val email = accessTokenHandler.getEmailFromToken(accessToken, jwtParserAccessToken) ?: return null
        val roles = accessTokenHandler.getRolesFromToken(accessToken, jwtParserAccessToken) ?: return null

        return generateUserAuthToken(
            authorities = roles,
            clientId = clientId,
            userId = userId,
            email = email,
            issuedInstant = now,
            accessTokenExpiryDurationAmount = accessTokenExpiryDurationAmount,
            accessTokenExpiryDurationUnit = accessTokenExpiryDurationUnit,
            refreshTokenExpiryDurationAmount = refreshTokenExpiryDurationAmount,
            refreshTokenExpiryDurationUnit = refreshTokenExpiryDurationUnit
        )
    }

    suspend fun getAccessTokenClaims(accessToken: String?, now: Instant = Instant.now()): AccessClaims? {
        accessToken ?: return null
        // 1. Check if token is of correct format
        val accessClaims = accessTokenHandler.getTokenClaims(accessToken, jwtParserAccessToken, now) ?: return null

        // 2. Check if token is not in disabled-token-list
        val disabledTokens = authTokenRepo.getDisabledAccessTokensForClient(
            ClientAuthInfo(
                clientId = accessClaims.clientId,
                userId = accessClaims.userId,
                accessToken = accessToken
            )
        )
        return if (!disabledTokens.contains(accessToken)) accessClaims else null
    }

    @Throws(AccessTokenIsInvalidException::class)
    suspend fun logout(accessToken: String, now: Instant) {
        val clientId = accessTokenHandler.getClientIdFromToken(accessToken, jwtParserAccessToken)
        val userId = accessTokenHandler.getUserIdFromToken(accessToken, jwtParserAccessToken)
        if (clientId == null || userId == null) throw AccessTokenIsInvalidException()
        val clientAuthInfo = ClientAuthInfo(clientId, userId, accessToken)

        // Get existing disabled tokens for the client.
        val disabledTokens = authTokenRepo.getDisabledAccessTokensForClient(clientAuthInfo)

        // Throw error if current accessToken is disabled!
        if (disabledTokens.contains(accessToken)) throw AccessTokenIsInvalidException()

        // Add current accessToken in disabled accessTokens for client with .
        updateDisabledAccessTokensForClient(
            disabledTokens = disabledTokens.plus(accessToken), clientAuthInfo = clientAuthInfo, now = now
        )
    }

    fun getAccessTokenClaims(principal: Principal): AccessClaims? =
        accessTokenHandler.getPrincipalClaims(principal)

    fun springAuthentication(claims: Claims, authorities: List<GrantedAuthority>): SpringAuthentication =
        usernamePasswordAuthTokenTokenGenerator.getUsernamePasswordAuthenticationToken(claims, authorities)

    private suspend fun disableOldAccessTokens(clientId: String, userAuthInfo: UserAuthInfo, now: Instant) {
        val existingToken = authTokenRepo.getExistingTokensForClient(clientId, userAuthInfo)
        println("##### disableOldAccessTokens() clientId:$clientId userAuthInfo:$userAuthInfo existingToken:$existingToken")
        if (existingToken == null) return
        val clientAuthInfo = ClientAuthInfo(clientId, userAuthInfo.userId, userAuthInfo.accessToken)
        val disabledTokens = authTokenRepo.getDisabledAccessTokensForClient(clientAuthInfo)
        println("##### disableOldAccessTokens() disabledTokens:$disabledTokens")
        updateDisabledAccessTokensForClient(
            disabledTokens = disabledTokens.plus(existingToken.accessToken), clientAuthInfo = clientAuthInfo, now = now
        )
    }

    private suspend fun updateDisabledAccessTokensForClient(
        disabledTokens: Set<String>,
        clientAuthInfo: ClientAuthInfo,
        now: Instant
    ) {
        val validDisabledAccessTokens = disabledTokens.filter {
            accessTokenHandler.isValidToken(token = it, parser = jwtParserAccessToken, now = now)
        }.toSet()
        authTokenRepo.updateDisabledAccessTokensForClient(clientAuthInfo, validDisabledAccessTokens)
    }

}