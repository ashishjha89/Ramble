package com.ramble.token.repository.persistence

import com.ramble.token.model.InternalTokenStorageException
import com.ramble.token.repository.persistence.entities.RegistrationConfirmationToken
import com.ramble.token.util.value
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository

@Repository
class RegistrationConfirmationTokenDbImpl(private val registrationConfirmationTokenSql: RegistrationConfirmationTokenSqlRepo) {

    private val logger = LoggerFactory.getLogger(RegistrationConfirmationTokenDbImpl::class.java)

    @Throws(InternalTokenStorageException::class)
    internal suspend fun getRegistrationConfirmationToken(
        email: String,
        scope: CoroutineScope
    ): RegistrationConfirmationToken? {
        val deferredTask = scope.async { registrationConfirmationTokenSql.findById(email).value }
        return try {
            performDeferredTask(deferredTask)
        } catch (e: Exception) {
            logger.error("Exception in Db Operation for getRegistrationConfirmationToken email:$email")
            if (deferredTask.isActive) deferredTask.cancel()
            throw InternalTokenStorageException()
        }
    }

    @Throws(InternalTokenStorageException::class)
    internal suspend fun hasRegistrationConfirmationToken(email: String, scope: CoroutineScope): Boolean {
        val deferredTask = scope.async { registrationConfirmationTokenSql.existsById(email) }
        return try {
            performDeferredTask(deferredTask)
        } catch (e: Exception) {
            logger.error("Exception in Db Operation for hasRegistrationConfirmationToken email:$email")
            if (deferredTask.isActive) deferredTask.cancel()
            throw InternalTokenStorageException()
        }
    }

    @Throws(InternalTokenStorageException::class)
    internal suspend fun saveRegistrationConfirmationToken(
        registrationConfirmationToken: RegistrationConfirmationToken,
        scope: CoroutineScope
    ): RegistrationConfirmationToken {
        val deferredTask = scope.async { registrationConfirmationTokenSql.save(registrationConfirmationToken) }
        return try {
            performDeferredTask(deferredTask)
        } catch (e: Exception) {
            logger.error("Exception in Db Operation for saveRegistrationConfirmationToken email:${registrationConfirmationToken.email}")
            if (deferredTask.isActive) deferredTask.cancel()
            throw InternalTokenStorageException()
        }
    }

    @Throws(InternalTokenStorageException::class)
    internal suspend fun deleteRegistrationConfirmationToken(
        email: String,
        scope: CoroutineScope
    ) {
        val deferredTask = scope.async { registrationConfirmationTokenSql.deleteById(email) }
        return try {
            performDeferredTask(deferredTask)
        } catch (e: Exception) {
            logger.error("Exception in Db Operation for deleteRegistrationConfirmationToken email:$email")
            if (deferredTask.isActive) deferredTask.cancel()
            throw InternalTokenStorageException()
        }
    }
}