package com.ramble.token.repository.persistence

import com.ramble.token.model.InternalTokenStorageException
import com.ramble.token.repository.persistence.entities.DisabledClientTokens
import com.ramble.token.util.value
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import org.springframework.stereotype.Repository

@Repository
class DisabledTokensCacheImpl(private val disabledTokensRedisRepo: DisabledTokensRedisRepo) {

    @Throws(InternalTokenStorageException::class)
    internal suspend fun getDisabledTokens(clientId: String, scope: CoroutineScope): Set<String> =
        performDeferredTask(
            deferredTask = scope.async {
                disabledTokensRedisRepo.findById(clientId).value?.disabledAccessTokens?.toSet() ?: setOf()
            }
        )

    @Throws(InternalTokenStorageException::class)
    internal suspend fun hasDisabledToken(clientId: String, scope: CoroutineScope): Boolean =
        performDeferredTask(
            deferredTask = scope.async { disabledTokensRedisRepo.existsById(clientId) }
        )

    @Throws(InternalTokenStorageException::class)
    internal suspend fun saveDisabledToken(
        disabledClientTokens: DisabledClientTokens,
        scope: CoroutineScope
    ): DisabledClientTokens =
        performDeferredTask(
            deferredTask = scope.async { disabledTokensRedisRepo.save(disabledClientTokens) }
        )

    @Throws(InternalTokenStorageException::class)
    internal suspend fun deleteDisabledToken(clientId: String, scope: CoroutineScope) =
        performDeferredTask(
            deferredTask = scope.async { disabledTokensRedisRepo.deleteById(clientId) }
        )
}