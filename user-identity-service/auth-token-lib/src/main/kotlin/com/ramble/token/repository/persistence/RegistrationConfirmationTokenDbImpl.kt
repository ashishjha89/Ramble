package com.ramble.token.repository.persistence

import com.ramble.token.model.InternalTokenStorageException
import com.ramble.token.repository.persistence.entities.RegistrationConfirmationToken
import com.ramble.token.util.value
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import org.springframework.stereotype.Repository

@Repository
class RegistrationConfirmationTokenDbImpl(private val registrationConfirmationTokenSql: RegistrationConfirmationTokenSqlRepo) {

    @Throws(InternalTokenStorageException::class)
    internal suspend fun getRegistrationConfirmationToken(
        email: String,
        scope: CoroutineScope
    ): RegistrationConfirmationToken? =
        performDeferredTask(
            deferredTask = scope.async { registrationConfirmationTokenSql.findById(email).value }
        )

    @Throws(InternalTokenStorageException::class)
    internal suspend fun hasRegistrationConfirmationToken(email: String, scope: CoroutineScope): Boolean =
        performDeferredTask(
            deferredTask = scope.async { registrationConfirmationTokenSql.existsById(email) }
        )

    @Throws(InternalTokenStorageException::class)
    internal suspend fun saveRegistrationConfirmationToken(
        registrationConfirmationToken: RegistrationConfirmationToken,
        scope: CoroutineScope
    ): RegistrationConfirmationToken =
        performDeferredTask(
            deferredTask = scope.async { registrationConfirmationTokenSql.save(registrationConfirmationToken) }
        )

    @Throws(InternalTokenStorageException::class)
    internal suspend fun deleteRegistrationConfirmationToken(
        email: String,
        scope: CoroutineScope
    ) =
        performDeferredTask(
            deferredTask = scope.async { registrationConfirmationTokenSql.deleteById(email) }
        )
}