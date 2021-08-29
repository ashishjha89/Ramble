package com.ramble.token.repository.persistence

import com.ramble.token.model.InternalTokenStorageException
import com.ramble.token.repository.persistence.entities.ClientRefreshToken
import com.ramble.token.repository.persistence.entities.ClientUserId
import com.ramble.token.util.value
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import org.springframework.stereotype.Repository

@Repository
class RefreshTokenDbImpl(private val refreshTokenSqlRepo: ClientRefreshTokenSqlRepo) {

    @Throws(InternalTokenStorageException::class)
    internal suspend fun getClientRefreshToken(clientUserId: ClientUserId, scope: CoroutineScope): ClientRefreshToken? =
        performDeferredTask(
            deferredTask = scope.async { refreshTokenSqlRepo.findById(clientUserId).value }
        )

    @Throws(InternalTokenStorageException::class)
    internal suspend fun saveClientRefreshToken(
        clientRefreshToken: ClientRefreshToken,
        scope: CoroutineScope
    ): ClientRefreshToken =
        performDeferredTask(
            deferredTask = scope.async { refreshTokenSqlRepo.save(clientRefreshToken) }
        )

    @Throws(InternalTokenStorageException::class)
    internal suspend fun deleteClientRefreshToken(clientUserId: ClientUserId, scope: CoroutineScope) {
        performDeferredTask(
            deferredTask = scope.async { refreshTokenSqlRepo.deleteById(clientUserId) }
        )
    }

}