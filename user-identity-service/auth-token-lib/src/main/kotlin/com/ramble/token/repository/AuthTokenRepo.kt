package com.ramble.token.repository

import com.ramble.token.model.RefreshTokenIsInvalidException
import com.ramble.token.model.UserAuthInfo
import com.ramble.token.repository.persistence.ClientRefreshTokenSqlRepo
import com.ramble.token.repository.persistence.DisabledTokensRedisRepo
import com.ramble.token.repository.persistence.entities.ClientAuthInfo
import com.ramble.token.repository.persistence.entities.ClientRefreshToken
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
                val clientRefreshToken = refreshTokenSqlRepo.findById(refreshToken).value
                    ?: throw RefreshTokenIsInvalidException()
                refreshTokenSqlRepo.deleteById(clientRefreshToken.refreshToken)
                clientRefreshToken.toClientAuthInfo()
            }
        }

    internal suspend fun getDisabledAccessTokensForClient(clientAuthInfo: ClientAuthInfo): Set<AccessToken> =
        coroutineScope {
            withContext(Dispatchers.IO) {
                disabledTokensRedisRepo.findById(clientAuthInfo.clientId).value?.disabledAccessTokens?.toSet()
                    ?: setOf()
            }
        }

    internal suspend fun updateDisabledAccessTokensForClient(
        clientAuthInfo: ClientAuthInfo,
        accessTokens: Set<AccessToken>?
    ) {
        if (accessTokens.isNullOrEmpty()) disabledTokensRedisRepo.deleteById(clientAuthInfo.clientId)
        else disabledTokensRedisRepo.save(
            DisabledClientTokens(id = clientAuthInfo.clientId, disabledAccessTokens = accessTokens.toList())
        )
    }

    private fun ClientRefreshToken.toClientAuthInfo() =
        ClientAuthInfo(clientId = clientId, userId = userId, accessToken = accessToken)

}