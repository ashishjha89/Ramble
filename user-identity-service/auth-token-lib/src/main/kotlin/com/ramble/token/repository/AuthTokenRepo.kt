package com.ramble.token.repository

import com.ramble.token.model.InternalTokenStorageException
import com.ramble.token.model.RefreshTokenIsInvalidException
import com.ramble.token.model.UserAuthInfo
import com.ramble.token.repository.persistence.DisabledTokensCacheImpl
import com.ramble.token.repository.persistence.RefreshTokenDbImpl
import com.ramble.token.repository.persistence.entities.ClientAuthInfo
import com.ramble.token.repository.persistence.entities.ClientRefreshToken
import com.ramble.token.repository.persistence.entities.ClientUserId
import com.ramble.token.repository.persistence.entities.DisabledClientTokens
import com.ramble.token.util.AuthTokenCoroutineScopeBuilder
import org.springframework.stereotype.Repository

private typealias AccessToken = String

@Repository
class AuthTokenRepo(
    private val refreshTokenDb: RefreshTokenDbImpl,
    private val disabledTokensCache: DisabledTokensCacheImpl,
    private val coroutineScopeBuilder: AuthTokenCoroutineScopeBuilder
) {

    @Throws(InternalTokenStorageException::class)
    internal suspend fun insertUserAuthInfo(clientId: String, userAuthInfo: UserAuthInfo): ClientRefreshToken =
        refreshTokenDb.saveClientRefreshToken(
            clientRefreshToken = ClientRefreshToken(
                clientId = clientId,
                userId = userAuthInfo.userId,
                refreshToken = userAuthInfo.refreshToken,
                accessToken = userAuthInfo.accessToken
            ),
            scope = coroutineScopeBuilder.defaultIoScope
        )

    @Throws(RefreshTokenIsInvalidException::class, InternalTokenStorageException::class)
    internal suspend fun deleteOldAuthTokens(clientId: String, userId: String): ClientAuthInfo {
        val scope = coroutineScopeBuilder.defaultIoScope
        val clientUserId = ClientUserId(clientId, userId)
        val clientRefreshToken = refreshTokenDb.getClientRefreshToken(clientUserId, scope)
            ?: throw RefreshTokenIsInvalidException()
        refreshTokenDb.deleteClientRefreshToken(clientUserId, scope)
        return clientRefreshToken.toClientAuthInfo()
    }

    @Throws(InternalTokenStorageException::class)
    internal suspend fun getExistingTokensForClient(clientId: String, userAuthInfo: UserAuthInfo): ClientRefreshToken? =
        refreshTokenDb.getClientRefreshToken(
            clientUserId = ClientUserId(clientId, userAuthInfo.userId),
            scope = coroutineScopeBuilder.defaultIoScope
        )

    @Throws(InternalTokenStorageException::class)
    internal suspend fun getDisabledAccessTokensForClient(clientAuthInfo: ClientAuthInfo): Set<AccessToken> =
        disabledTokensCache.getDisabledTokens(clientAuthInfo.clientId, coroutineScopeBuilder.defaultIoScope)

    @Throws(InternalTokenStorageException::class)
    internal suspend fun updateDisabledAccessTokensForClient(clientId: String, accessTokens: Set<AccessToken>?) {
        val scope = coroutineScopeBuilder.defaultIoScope
        if (accessTokens.isNullOrEmpty()) {
            if (disabledTokensCache.hasDisabledToken(clientId, scope)) {
                disabledTokensCache.deleteDisabledToken(clientId, scope)
            } else Unit
        } else
            disabledTokensCache.saveDisabledToken(
                DisabledClientTokens(id = clientId, disabledAccessTokens = accessTokens.toList()),
                scope
            )
    }

    private fun ClientRefreshToken.toClientAuthInfo() =
        ClientAuthInfo(clientId = clientId, userId = userId, accessToken = accessToken)

}