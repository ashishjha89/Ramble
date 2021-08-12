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

    private val jwtParser = tokenComponentBuilder.jwtParserRegistrationToken()

    private val jwtBuilder = tokenComponentBuilder.jwtBuilder()

    private val registrationConfirmationRepo: RegistrationConfirmationRepo =
        tokenComponentBuilder.registrationConfirmationRepo()

    private val registrationConfirmationTokenHandler: RegistrationConfirmationTokenHandler =
        tokenComponentBuilder.registrationConfirmationTokenHandler()

    /**
     * Return newly created RegistrationConfirmationToken.
     */
    fun addRegistrationConfirmationToken(
        userId: String,
        email: String,
        now: Instant,
        expirationDurationAmount: Long,
        expiryDurationUnit: ChronoUnit
    ): RegistrationConfirmationToken {
        // Delete old tokens for this userId. This could be in cases when user registered but didn't activate.
        registrationConfirmationRepo.deleteRegistrationConfirmationToken(userId)
        val token =
            generateRegistrationConfirmationToken(userId, email, now, expirationDurationAmount, expiryDurationUnit)
        // Add new registrationConfirmationToken.
        registrationConfirmationRepo.addRegistrationConfirmationToken(registrationConfirmationToken = token)
        return token
    }

    /**
     * Return RegistrationConfirmationToken if the token is still valid now.
     */
    fun processRegistrationConfirmationToken(
        registrationConfirmationToken: String,
        now: Instant
    ): RegistrationConfirmationToken? {
        val userId = registrationConfirmationTokenHandler.getUserIdFromToken(registrationConfirmationToken, jwtParser)
            ?: return null
        val isValidToken =
            registrationConfirmationTokenHandler.isValidToken(registrationConfirmationToken, now, jwtParser)
        if (!isValidToken) {
            // Delete old tokens for this userId. This could be in cases when user registered but didn't activate.
            registrationConfirmationRepo.deleteRegistrationConfirmationToken(userId)
            return null
        }
        // Get RegisteredUser for the passed registrationConfirmationToken.
        return registrationConfirmationRepo.getRegistrationConfirmationToken(userId)
    }

    private fun generateRegistrationConfirmationToken(
        userId: String,
        email: String,
        now: Instant,
        expirationDurationAmount: Long,
        expiryDurationUnit: ChronoUnit
    ): RegistrationConfirmationToken {
        val token = registrationConfirmationTokenHandler
            .generateToken(userId, email, now, expirationDurationAmount, expiryDurationUnit, jwtBuilder)
        return RegistrationConfirmationToken(userId = userId, email = email, token = token)
    }

}
