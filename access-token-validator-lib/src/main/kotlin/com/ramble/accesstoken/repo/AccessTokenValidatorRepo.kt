package com.ramble.accesstoken.repo

import com.ramble.accesstoken.model.AccessTokenValidatorInternalException
import com.ramble.accesstoken.repo.persistence.DisabledTokensCacheImpl
import com.ramble.accesstoken.repo.persistence.entities.DisabledClientTokens
import kotlinx.coroutines.CoroutineScope
import org.springframework.stereotype.Repository

private typealias AccessToken = String

@Repository
class AccessTokenValidatorRepo(private val disabledTokensCache: DisabledTokensCacheImpl) {

    @Throws(AccessTokenValidatorInternalException::class)
    internal suspend fun getDisabledAccessTokensForClient(clientId: String, scope: CoroutineScope): Set<AccessToken> =
        disabledTokensCache.getDisabledTokens(clientId, scope)

    @Throws(AccessTokenValidatorInternalException::class)
    internal suspend fun updateDisabledAccessTokensForClient(
        clientId: String,
        accessTokens: Set<AccessToken>?,
        scope: CoroutineScope
    ) {
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
}