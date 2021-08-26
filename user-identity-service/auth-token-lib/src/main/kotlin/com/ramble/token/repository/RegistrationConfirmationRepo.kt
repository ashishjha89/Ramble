package com.ramble.token.repository

import com.ramble.token.model.InternalTokenStorageException
import com.ramble.token.repository.persistence.RegistrationConfirmationTokenDbImpl
import com.ramble.token.repository.persistence.entities.RegistrationConfirmationToken
import com.ramble.token.util.CoroutineScopeBuilder
import org.springframework.stereotype.Repository

@Repository
class RegistrationConfirmationRepo(
    private val registrationTokenSqlRepo: RegistrationConfirmationTokenDbImpl,
    private val coroutineScopeBuilder: CoroutineScopeBuilder
) {

    @Throws(InternalTokenStorageException::class)
    internal suspend fun addRegistrationConfirmationToken(registrationConfirmationToken: RegistrationConfirmationToken): RegistrationConfirmationToken =
        registrationTokenSqlRepo.saveRegistrationConfirmationToken(
            registrationConfirmationToken,
            coroutineScopeBuilder.defaultIoScope
        )

    @Throws(InternalTokenStorageException::class)
    internal suspend fun deleteRegistrationConfirmationToken(email: String) {
        val scope = coroutineScopeBuilder.defaultIoScope
        if (registrationTokenSqlRepo.hasRegistrationConfirmationToken(email, scope))
            registrationTokenSqlRepo.deleteRegistrationConfirmationToken(email, scope)
    }

    @Throws(InternalTokenStorageException::class)
    internal suspend fun getRegistrationConfirmationToken(email: String): RegistrationConfirmationToken? =
        registrationTokenSqlRepo.getRegistrationConfirmationToken(email, coroutineScopeBuilder.defaultIoScope)

}