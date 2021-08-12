package com.ramble.token.repository

import com.ramble.token.model.RefreshTokenIsInvalidException
import com.ramble.token.model.UserAuthInfo
import com.ramble.token.repository.persistence.RefreshTokenCrudRepository
import com.ramble.token.repository.persistence.entities.ClientAuthInfo
import com.ramble.token.repository.persistence.entities.ClientRefreshToken
import com.ramble.token.value
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Repository

private typealias AccessToken = String

internal typealias UserId = String

@Repository
class AuthTokenRepo(private val refreshTokenCrudRepo: RefreshTokenCrudRepository) {

    private val disabledClientAccessTokenMap = mutableMapOf<ClientAuthInfo, Set<AccessToken>>()

    internal suspend fun insertUserAuthInfo(clientId: String, userAuthInfo: UserAuthInfo): ClientRefreshToken =
        coroutineScope {
            withContext(Dispatchers.IO) {
                refreshTokenCrudRepo.save(
                    ClientRefreshToken(
                        refreshToken = userAuthInfo.refreshToken,
                        accessToken = userAuthInfo.accessToken,
                        userId = userAuthInfo.userId,
                        clientId = clientId
                    )
                )
            }
        }

    @Throws(RefreshTokenIsInvalidException::class)
    internal suspend fun deleteOldAuthTokens(refreshToken: String): ClientAuthInfo =
        coroutineScope {
            withContext(Dispatchers.IO) {
                val clientRefreshToken = refreshTokenCrudRepo.findById(refreshToken).value
                    ?: throw RefreshTokenIsInvalidException()
                refreshTokenCrudRepo.deleteById(clientRefreshToken.refreshToken)
                clientRefreshToken.toClientAuthInfo()
            }
        }

    internal fun getDisabledAccessTokensForClient(clientAuthInfo: ClientAuthInfo): Set<AccessToken> =
        disabledClientAccessTokenMap[clientAuthInfo] ?: setOf()

    internal fun updateDisabledAccessTokensForClient(clientAuthInfo: ClientAuthInfo, accessTokens: Set<AccessToken>?) {
        if (accessTokens == null || accessTokens.isEmpty()) disabledClientAccessTokenMap.remove(clientAuthInfo)
        else disabledClientAccessTokenMap[clientAuthInfo] = accessTokens
    }

    private fun ClientRefreshToken.toClientAuthInfo() =
        ClientAuthInfo(clientId = clientId, userId = userId, accessToken = accessToken)

}