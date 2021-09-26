package com.ramble.accesstoken.repo.persistence

import com.ramble.accesstoken.model.AccessTokenValidatorInternalException
import com.ramble.accesstoken.repo.persistence.entities.DisabledClientTokens
import com.ramble.accesstoken.util.value
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository

@Repository
class DisabledTokensCacheImpl(private val disabledTokensRedisRepo: DisabledTokensRedisRepo) {

    private val logger = LoggerFactory.getLogger(DisabledTokensCacheImpl::class.java)

    @Throws(AccessTokenValidatorInternalException::class)
    internal suspend fun getDisabledTokens(clientId: String, scope: CoroutineScope): Set<String> {
        val deferredTask = scope.async {
            disabledTokensRedisRepo.findById(clientId).value?.disabledAccessTokens?.toSet() ?: setOf()
        }
        return try {
            performDeferredTask(deferredTask)
        } catch (e: Exception) {
            logger.error("Exception in Db Operation for getDisabledTokens clientId:$clientId")
            if (deferredTask.isActive) deferredTask.cancel()
            throw AccessTokenValidatorInternalException()
        }
    }

    @Throws(AccessTokenValidatorInternalException::class)
    internal suspend fun hasDisabledToken(clientId: String, scope: CoroutineScope): Boolean {
        val deferredTask = scope.async { disabledTokensRedisRepo.existsById(clientId) }
        return try {
            performDeferredTask(deferredTask)
        } catch (e: Exception) {
            logger.error("Exception in Db Operation for hasDisabledToken clientId:$clientId")
            if (deferredTask.isActive) deferredTask.cancel()
            throw AccessTokenValidatorInternalException()
        }
    }

    @Throws(AccessTokenValidatorInternalException::class)
    internal suspend fun saveDisabledToken(
        disabledClientTokens: DisabledClientTokens,
        scope: CoroutineScope
    ): DisabledClientTokens {
        val deferredTask = scope.async { disabledTokensRedisRepo.save(disabledClientTokens) }
        return try {
            performDeferredTask(deferredTask)
        } catch (e: Exception) {
            logger.error("Exception in Db Operation for saveDisabledToken id:${disabledClientTokens.id}")
            if (deferredTask.isActive) deferredTask.cancel()
            throw AccessTokenValidatorInternalException()
        }
    }

    @Throws(AccessTokenValidatorInternalException::class)
    internal suspend fun deleteDisabledToken(clientId: String, scope: CoroutineScope) {
        val deferredTask = scope.async { disabledTokensRedisRepo.deleteById(clientId) }
        return try {
            performDeferredTask(deferredTask)
        } catch (e: Exception) {
            logger.error("Exception in Db Operation for deleteDisabledToken clientId:$clientId")
            if (deferredTask.isActive) deferredTask.cancel()
            throw AccessTokenValidatorInternalException()
        }
    }
}