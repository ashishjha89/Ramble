package com.ramble.token.repository

import com.ramble.token.model.RefreshTokenIsInvalidException
import com.ramble.token.model.UserAuthInfo
import com.ramble.token.repository.persistence.ClientRefreshTokenSqlRepo
import com.ramble.token.repository.persistence.DisabledTokensRedisRepo
import com.ramble.token.repository.persistence.entities.ClientAuthInfo
import com.ramble.token.repository.persistence.entities.ClientRefreshToken
import com.ramble.token.repository.persistence.entities.ClientUserId
import com.ramble.token.repository.persistence.entities.DisabledClientTokens
import com.ramble.token.value
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Repository

private typealias AccessToken = String

internal typealias UserId = String

@Repository
class AuthTokenRepo(
    private val refreshTokenSqlRepo: ClientRefreshTokenSqlRepo,
    private val disabledTokensRedisRepo: DisabledTokensRedisRepo
) {

    internal suspend fun insertUserAuthInfo(clientId: String, userAuthInfo: UserAuthInfo): ClientRefreshToken =
        coroutineScope {
            withContext(Dispatchers.IO) {
                refreshTokenSqlRepo.save(
                    ClientRefreshToken(
                        clientId = clientId,
                        userId = userAuthInfo.userId,
                        refreshToken = userAuthInfo.refreshToken,
                        accessToken = userAuthInfo.accessToken
                    )
                )
            }
        }

    @Throws(RefreshTokenIsInvalidException::class)
    internal suspend fun deleteOldAuthTokens(clientId: String, userId: String): ClientAuthInfo =
        coroutineScope {
            withContext(Dispatchers.IO) {
                val clientUserId = ClientUserId(clientId, userId)
                val clientRefreshToken = refreshTokenSqlRepo.findById(clientUserId).value
                    ?: throw RefreshTokenIsInvalidException()
                refreshTokenSqlRepo.deleteById(clientUserId)
                clientRefreshToken.toClientAuthInfo()
            }
        }

    internal suspend fun getExistingTokensForClient(clientId: String, userAuthInfo: UserAuthInfo): ClientRefreshToken? =
        refreshTokenSqlRepo.findById(ClientUserId(clientId, userAuthInfo.userId)).value

    internal suspend fun getDisabledAccessTokensForClient(clientAuthInfo: ClientAuthInfo): Set<AccessToken> =
        coroutineScope {
            withContext(Dispatchers.IO) {
                disabledTokensRedisRepo.findById(clientAuthInfo.clientId).value?.disabledAccessTokens?.toSet()
                    ?: setOf()
            }
        }

    internal suspend fun updateDisabledAccessTokensForClient(clientId: String, accessTokens: Set<AccessToken>?) {
        coroutineScope {
            withContext(Dispatchers.IO) {
                if (accessTokens.isNullOrEmpty()) {
                    if (disabledTokensRedisRepo.existsById(clientId)) {
                        disabledTokensRedisRepo.deleteById(clientId)
                    } else Unit
                } else
                    disabledTokensRedisRepo.save(
                        DisabledClientTokens(id = clientId, disabledAccessTokens = accessTokens.toList())
                    )
            }
        }
    }

    private fun ClientRefreshToken.toClientAuthInfo() =
        ClientAuthInfo(clientId = clientId, userId = userId, accessToken = accessToken)

}