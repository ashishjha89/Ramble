package com.ramble.token.repository

import com.ramble.token.model.ClientAuthInfo
import com.ramble.token.model.RefreshTokenIsInvalidException
import com.ramble.token.model.UserAuthInfo
import org.springframework.stereotype.Repository

private typealias RefreshToken = String

private typealias AccessToken = String

internal typealias UserId = String

@Repository
class AuthTokenRepo {

    private val refreshTokenMap = mutableMapOf<RefreshToken, ClientAuthInfo>()

    private val disabledClientAccessTokenMap = mutableMapOf<ClientAuthInfo, Set<AccessToken>>()

    internal fun insertUserAuthInfo(clientId: String, userAuthInfo: UserAuthInfo) {
        refreshTokenMap[userAuthInfo.refreshToken] = userAuthInfo.toClientAuthInfo(clientId)
    }

    @Throws(RefreshTokenIsInvalidException::class)
    internal fun deleteOldAuthTokens(refreshToken: String): ClientAuthInfo =
            refreshTokenMap.remove(refreshToken) ?: throw RefreshTokenIsInvalidException()

    internal fun getDisabledAccessTokensForClient(clientAuthInfo: ClientAuthInfo): Set<AccessToken> =
            disabledClientAccessTokenMap[clientAuthInfo] ?: setOf()

    internal fun updateDisabledAccessTokensForClient(clientAuthInfo: ClientAuthInfo, accessTokens: Set<AccessToken>?) {
        if (accessTokens == null || accessTokens.isEmpty()) disabledClientAccessTokenMap.remove(clientAuthInfo)
        else disabledClientAccessTokenMap[clientAuthInfo] = accessTokens
    }

    private fun UserAuthInfo.toClientAuthInfo(clientId: String) =
            ClientAuthInfo(clientId = clientId, userId = this.userId, accessToken = this.accessToken)

}