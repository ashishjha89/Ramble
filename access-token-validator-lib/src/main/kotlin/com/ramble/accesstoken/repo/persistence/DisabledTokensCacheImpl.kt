package com.ramble.accesstoken.repo.persistence

import com.ramble.accesstoken.model.AccessTokenValidatorInternalException
import com.ramble.accesstoken.repo.persistence.entities.DisabledClientTokens
import com.ramble.accesstoken.util.value
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import org.springframework.stereotype.Repository

@Repository
class DisabledTokensCacheImpl(private val disabledTokensRedisRepo: DisabledTokensRedisRepo) {

    @Throws(AccessTokenValidatorInternalException::class)
    internal suspend fun getDisabledTokens(clientId: String, scope: CoroutineScope): Set<String> =
        performDeferredTask(
            deferredTask = scope.async {
                disabledTokensRedisRepo.findById(clientId).value?.disabledAccessTokens?.toSet() ?: setOf()
            }
        )

    @Throws(AccessTokenValidatorInternalException::class)
    internal suspend fun hasDisabledToken(clientId: String, scope: CoroutineScope): Boolean =
        performDeferredTask(
            deferredTask = scope.async { disabledTokensRedisRepo.existsById(clientId) }
        )

    @Throws(AccessTokenValidatorInternalException::class)
    internal suspend fun saveDisabledToken(
        disabledClientTokens: DisabledClientTokens,
        scope: CoroutineScope
    ): DisabledClientTokens =
        performDeferredTask(
            deferredTask = scope.async { disabledTokensRedisRepo.save(disabledClientTokens) }
        )

    @Throws(AccessTokenValidatorInternalException::class)
    internal suspend fun deleteDisabledToken(clientId: String, scope: CoroutineScope) =
        performDeferredTask(
            deferredTask = scope.async { disabledTokensRedisRepo.deleteById(clientId) }
        )
}