package com.ramble.token.repository

import com.ramble.token.repository.persistence.RegistrationConfirmationTokenSqlRepo
import com.ramble.token.repository.persistence.entities.RegistrationConfirmationToken
import com.ramble.token.value
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Repository

@Repository
class RegistrationConfirmationRepo(private val registrationTokenSqlRepo: RegistrationConfirmationTokenSqlRepo) {

    internal suspend fun addRegistrationConfirmationToken(registrationConfirmationToken: RegistrationConfirmationToken): RegistrationConfirmationToken =
        coroutineScope {
            withContext(Dispatchers.IO) {
                registrationTokenSqlRepo.save(registrationConfirmationToken)
            }
        }

    /**
     * Return true if token deleted successfully.
     */
    internal suspend fun deleteRegistrationConfirmationToken(userId: UserId) {
        coroutineScope {
            withContext(Dispatchers.IO) {
                registrationTokenSqlRepo.deleteById(userId)
            }
        }
    }

    /**
     * Return RegistrationConfirmationToken for the userId.
     */
    internal suspend fun getRegistrationConfirmationToken(userId: UserId): RegistrationConfirmationToken? =
        coroutineScope {
            withContext(Dispatchers.IO) {
                registrationTokenSqlRepo.findById(userId).value
            }
        }

}