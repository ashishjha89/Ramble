package com.ramble.token

import com.ramble.accesstoken.AccessTokenValidatorService
import com.ramble.accesstoken.model.AccessClaims
import com.ramble.token.config.TokenComponentBuilder
import com.ramble.token.handler.RefreshTokenHandler
import com.ramble.token.handler.helper.UsernamePasswordAuthTokenTokenGenerator
import com.ramble.token.model.AccessTokenIsInvalidException
import com.ramble.token.model.InternalTokenStorageException
import com.ramble.token.model.RefreshTokenIsInvalidException
import com.ramble.token.model.UserAuthInfo
import com.ramble.token.repository.AuthTokenRepo
import io.jsonwebtoken.Claims
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.stereotype.Service
import java.security.Principal
import java.time.Instant
import java.time.temporal.ChronoUnit
import org.springframework.security.core.Authentication as SpringAuthentication

@Service
class AuthTokensService(
    private val authTokenRepo: AuthTokenRepo,
    private val tokenValidatorService: AccessTokenValidatorService,
    tokenComponentBuilder: TokenComponentBuilder
) {

    private val jwtParserRefreshToken = tokenComponentBuilder.jwtParserRefreshToken()

    private val jwtBuilder = tokenComponentBuilder.jwtBuilder()

    private val refreshTokenHandler: RefreshTokenHandler = tokenComponentBuilder.refreshTokenHandler()

    private val usernamePasswordAuthTokenTokenGenerator: UsernamePasswordAuthTokenTokenGenerator =
        tokenComponentBuilder.usernamePasswordAuthTokenTokenGenerator()

    @Throws(InternalTokenStorageException::class)
    suspend fun generateUserAuthToken(
        roles: List<String>,
        clientId: String,
        userId: String,
        email: String,
        issuedInstant: Instant = Instant.now(),
        accessTokenExpiryDurationAmount: Long = 30,
        accessTokenExpiryDurationUnit: ChronoUnit = ChronoUnit.MINUTES,
        refreshTokenExpiryDurationAmount: Long = 365,
        refreshTokenExpiryDurationUnit: ChronoUnit = ChronoUnit.DAYS
    ): UserAuthInfo {
        val userAuthInfo = UserAuthInfo(
            userId = userId,
            email = email,
            accessToken = tokenValidatorService.generateAccessToken(
                roles = roles,
                clientId = clientId,
                userId = userId,
                email = email,
                issuedInstant = issuedInstant,
                expiryDurationAmount = accessTokenExpiryDurationAmount,
                expiryDurationUnit = accessTokenExpiryDurationUnit
            ),
            refreshToken = refreshTokenHandler.generateRefreshToken(
                clientId = clientId,
                userId = userId,
                issuedInstant = issuedInstant,
                expiryDurationAmount = refreshTokenExpiryDurationAmount,
                expiryDurationUnit = refreshTokenExpiryDurationUnit,
                jwtBuilder = jwtBuilder
            )
        )
        val existingToken = authTokenRepo.getExistingTokensForClient(clientId, userAuthInfo)
        disableAccessToken(clientId, existingToken?.accessToken, issuedInstant)
        authTokenRepo.insertUserAuthInfo(clientId, userAuthInfo)
        return userAuthInfo
    }

    @Throws(RefreshTokenIsInvalidException::class, InternalTokenStorageException::class)
    suspend fun refreshAuthToken(
        refreshToken: String,
        now: Instant,
        accessTokenExpiryDurationAmount: Long = 30,
        accessTokenExpiryDurationUnit: ChronoUnit = ChronoUnit.MINUTES,
        refreshTokenExpiryDurationAmount: Long = 365,
        refreshTokenExpiryDurationUnit: ChronoUnit = ChronoUnit.DAYS
    ): UserAuthInfo? {
        // 1. Get clientId and userId from RefreshToken
        val clientIdFromRefreshToken = refreshTokenHandler.getClientIdFromToken(refreshToken, jwtParserRefreshToken)
            ?: throw RefreshTokenIsInvalidException()
        val userIdFromRefreshToken = refreshTokenHandler.getUserIdFromToken(refreshToken, jwtParserRefreshToken)
            ?: throw RefreshTokenIsInvalidException()

        // 2. Delete old entry of token for the client.
        val clientAuthInfo = authTokenRepo.deleteOldAuthTokens(clientIdFromRefreshToken, userIdFromRefreshToken)

        // 3. Check if RefreshToken has valid format
        if (!refreshTokenHandler.isValidToken(refreshToken, jwtParserRefreshToken, now))
            throw RefreshTokenIsInvalidException()

        // 4) Clean old access-token for this client.
        val accessToken = clientAuthInfo.accessToken
        disableAccessToken(clientAuthInfo.clientId, accessToken, now)

        // 5. Now, generate new auth-tokens
        val accessClaims = tokenValidatorService.getClaimsFromAccessToken(accessToken, now) ?: return null

        return generateUserAuthToken(
            roles = accessClaims.roles,
            clientId = accessClaims.clientId,
            userId = accessClaims.userId,
            email = accessClaims.email,
            issuedInstant = now,
            accessTokenExpiryDurationAmount = accessTokenExpiryDurationAmount,
            accessTokenExpiryDurationUnit = accessTokenExpiryDurationUnit,
            refreshTokenExpiryDurationAmount = refreshTokenExpiryDurationAmount,
            refreshTokenExpiryDurationUnit = refreshTokenExpiryDurationUnit
        )
    }

    @Throws(InternalTokenStorageException::class)
    suspend fun getAccessTokenClaims(accessToken: String?, now: Instant = Instant.now()): AccessClaims? {
        return tokenValidatorService.getClaimsFromAccessToken(
            accessToken = accessToken ?: return null,
            now = now
        )
    }

    fun getAccessTokenClaims(principal: Principal): AccessClaims? {
        return tokenValidatorService.getAccessClaims(
            claims = principalClaims(principal) ?: return null,
            authorities = authorities(principal) ?: return null
        )
    }

    @Throws(InternalTokenStorageException::class, AccessTokenIsInvalidException::class)
    suspend fun logout(accessToken: String, now: Instant) {
        val accessClaims =
            tokenValidatorService.getClaimsFromAccessToken(accessToken, now) ?: throw AccessTokenIsInvalidException()
        disableAccessToken(accessClaims.clientId, accessToken, now)
    }

    fun springAuthentication(claims: Claims, authorities: List<GrantedAuthority>): SpringAuthentication =
        usernamePasswordAuthTokenTokenGenerator.getUsernamePasswordAuthenticationToken(claims, authorities)

    @Throws(InternalTokenStorageException::class)
    suspend fun disableAccessToken(clientId: String, accessToken: String?, now: Instant) {
        accessToken ?: return
        try {
            tokenValidatorService.disableAccessToken(clientId, accessToken, now)
        } catch (e: Exception) {
            throw InternalTokenStorageException()
        }
    }

    private fun principalClaims(principal: Principal): Claims? {
        if (principal is UsernamePasswordAuthenticationToken) {
            val claims = principal.principal
            if (claims is Claims) {
                return claims
            }
        }
        return null
    }

    private fun authorities(principal: Principal): List<GrantedAuthority>? {
        if (principal is UsernamePasswordAuthenticationToken) {
            return principal.authorities?.toList()
        }
        return null
    }

}