package com.ramble.token.repository.persistence

import com.ramble.token.model.InternalTokenStorageException
import com.ramble.token.repository.persistence.entities.ClientRefreshToken
import com.ramble.token.repository.persistence.entities.ClientUserId
import com.ramble.token.util.value
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository

@Repository
class RefreshTokenDbImpl(private val refreshTokenSqlRepo: ClientRefreshTokenSqlRepo) {

    private val logger = LoggerFactory.getLogger(RefreshTokenDbImpl::class.java)

    @Throws(InternalTokenStorageException::class)
    internal suspend fun getClientRefreshToken(clientUserId: ClientUserId, scope: CoroutineScope): ClientRefreshToken? {
        val deferredTask = scope.async { refreshTokenSqlRepo.findById(clientUserId).value }
        return try {
            performDeferredTask(deferredTask)
        } catch (e: Exception) {
            logger.error("Exception in Db Operation for getClientRefreshToken clientUserId:$clientUserId")
            if (deferredTask.isActive) deferredTask.cancel()
            throw InternalTokenStorageException()
        }
    }

    @Throws(InternalTokenStorageException::class)
    internal suspend fun saveClientRefreshToken(
        clientRefreshToken: ClientRefreshToken,
        scope: CoroutineScope
    ): ClientRefreshToken {
        val deferredTask = scope.async { refreshTokenSqlRepo.save(clientRefreshToken) }
        return try {
            performDeferredTask(deferredTask)
        } catch (e: Exception) {
            val clientUserId = ClientUserId(clientId = clientRefreshToken.clientId, userId = clientRefreshToken.userId)
            logger.error("Exception in Db Operation for saveClientRefreshToken clientUserId:$clientUserId")
            if (deferredTask.isActive) deferredTask.cancel()
            throw InternalTokenStorageException()
        }
    }

    @Throws(InternalTokenStorageException::class)
    internal suspend fun deleteClientRefreshToken(clientUserId: ClientUserId, scope: CoroutineScope) {
        val deferredTask = scope.async { refreshTokenSqlRepo.deleteById(clientUserId) }
        return try {
            performDeferredTask(deferredTask)
        } catch (e: Exception) {
            logger.error("Exception in Db Operation for deleteClientRefreshToken clientUserId:$clientUserId")
            if (deferredTask.isActive) deferredTask.cancel()
            throw InternalTokenStorageException()
        }
    }

}