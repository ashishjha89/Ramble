package com.ramble.token.repository

import com.ramble.token.model.RegistrationConfirmationToken
import org.springframework.stereotype.Repository

@Repository
internal class RegistrationConfirmationRepo {

    private val tokenMap = mutableMapOf<UserId, RegistrationConfirmationToken>()

    /**
     * Return true if token added successfully.
     */
    fun addRegistrationConfirmationToken(registrationConfirmationToken: RegistrationConfirmationToken): Boolean =
        tokenMap.put(registrationConfirmationToken.userId, registrationConfirmationToken) == null

    /**
     * Return true if token deleted successfully.
     */
    fun deleteRegistrationConfirmationToken(userId: UserId): Boolean =
        tokenMap.remove(userId) != null

    /**
     * Return RegistrationConfirmationToken for the userId.
     */
    fun getRegistrationConfirmationToken(userId: UserId): RegistrationConfirmationToken? =
        tokenMap[userId]

}