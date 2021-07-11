package com.ramble.token

import com.ramble.token.config.TokenComponentBuilder
import com.ramble.token.handler.RegistrationConfirmationTokenHandler
import com.ramble.token.model.RegistrationConfirmationToken
import com.ramble.token.repository.RegistrationConfirmationRepo
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
class RegistrationConfirmationService(tokenComponentBuilder: TokenComponentBuilder) {

    private val tokenConfirmationRepo: RegistrationConfirmationRepo =
            tokenComponentBuilder.registrationConfirmationRepo()


    private val registrationConfirmationTokenHandler: RegistrationConfirmationTokenHandler =
            tokenComponentBuilder.registrationConfirmationTokenHandler()

    /**
     * Return newly created RegistrationConfirmationToken.
     */
    fun addRegistrationConfirmationToken(userId: String,
                                         email: String,
                                         now: Instant = Instant.now(),
                                         expirationDurationAmount: Long = 15,
                                         expiryDurationUnit: ChronoUnit = ChronoUnit.MINUTES
    ): RegistrationConfirmationToken {
        // Delete old tokens for this userId. This could be in cases when user registered but didn't activate.
        tokenConfirmationRepo.deleteRegistrationConfirmationToken(userId)
        val token = generateRegistrationConfirmationToken(userId, email, now, expirationDurationAmount, expiryDurationUnit)
        // Add new registrationConfirmationToken.
        tokenConfirmationRepo.addRegistrationConfirmationToken(registrationConfirmationToken = token)
        return token
    }

    /**
     * Return RegistrationConfirmationToken if the token is still valid now.
     */
    fun processRegistrationConfirmationToken(
            registrationConfirmationToken: String,
            now: Instant = Instant.now()
    ): RegistrationConfirmationToken? {
        val userId = registrationConfirmationTokenHandler.getUserIdFromToken(registrationConfirmationToken)
                ?: return null
        val isValidToken = registrationConfirmationTokenHandler.isValidToken(registrationConfirmationToken, now)
        if (!isValidToken) {
            // Delete old tokens for this userId. This could be in cases when user registered but didn't activate.
            tokenConfirmationRepo.deleteRegistrationConfirmationToken(userId)
            return null
        }
        // Get RegisteredUser for the passed registrationConfirmationToken.
        return tokenConfirmationRepo.getRegistrationConfirmationToken(userId) ?: return null
    }

    /**
     * Delete all tokens for the user.
     */
    fun deleteRegistrationConfirmationToken(userId: String) {
        tokenConfirmationRepo.deleteRegistrationConfirmationToken(userId)
    }

    private fun generateRegistrationConfirmationToken(
            userId: String,
            email: String,
            now: Instant,
            expirationDurationAmount: Long,
            expiryDurationUnit: ChronoUnit
    ): RegistrationConfirmationToken {
        val token = registrationConfirmationTokenHandler.generateToken(userId, email, now, expirationDurationAmount, expiryDurationUnit)
        return RegistrationConfirmationToken(userId = userId, email = email, token = token)
    }

}
