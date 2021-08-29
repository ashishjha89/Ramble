package com.ramble.token

import com.ramble.token.config.TokenComponentBuilder
import com.ramble.token.handler.RegistrationConfirmationTokenHandler
import com.ramble.token.model.InternalTokenStorageException
import com.ramble.token.repository.RegistrationConfirmationRepo
import com.ramble.token.repository.persistence.entities.RegistrationConfirmationToken
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
class RegistrationConfirmationService(
    tokenComponentBuilder: TokenComponentBuilder,
    private val registrationConfirmationRepo: RegistrationConfirmationRepo
) {

    private val jwtParser = tokenComponentBuilder.jwtParserRegistrationToken()

    private val jwtBuilder = tokenComponentBuilder.jwtBuilder()

    private val registrationConfirmationTokenHandler: RegistrationConfirmationTokenHandler =
        tokenComponentBuilder.registrationConfirmationTokenHandler()

    /**
     * Return newly created RegistrationConfirmationToken.
     */
    @Throws(InternalTokenStorageException::class)
    suspend fun addRegistrationConfirmationToken(
        email: String,
        now: Instant,
        expirationDurationAmount: Long,
        expiryDurationUnit: ChronoUnit
    ): RegistrationConfirmationToken {
        // Delete old tokens for this email. This could be in cases when user registered but didn't activate.
        registrationConfirmationRepo.deleteRegistrationConfirmationToken(email)
        val token = generateRegistrationConfirmationToken(email, now, expirationDurationAmount, expiryDurationUnit)
        // Add new registrationConfirmationToken.
        registrationConfirmationRepo.addRegistrationConfirmationToken(registrationConfirmationToken = token)
        return token
    }

    /**
     * Return RegistrationConfirmationToken if the token is still valid now.
     */
    @Throws(InternalTokenStorageException::class)
    suspend fun processRegistrationConfirmationToken(
        registrationConfirmationToken: String,
        now: Instant
    ): RegistrationConfirmationToken? {
        val email = registrationConfirmationTokenHandler.getEmailFromToken(registrationConfirmationToken, jwtParser)
            ?: return null
        val isValidToken =
            registrationConfirmationTokenHandler.isValidToken(registrationConfirmationToken, now, jwtParser)
        if (!isValidToken) {
            // Delete old tokens for this userId. This could be in cases when user registered but didn't activate.
            registrationConfirmationRepo.deleteRegistrationConfirmationToken(email)
            return null
        }
        // Get RegisteredUser for the passed registrationConfirmationToken.
        return registrationConfirmationRepo.getRegistrationConfirmationToken(email)
    }

    private fun generateRegistrationConfirmationToken(
        email: String,
        now: Instant,
        expirationDurationAmount: Long,
        expiryDurationUnit: ChronoUnit
    ): RegistrationConfirmationToken {
        val token = registrationConfirmationTokenHandler
            .generateToken(email, now, expirationDurationAmount, expiryDurationUnit, jwtBuilder)
        return RegistrationConfirmationToken(email = email, token = token)
    }

}
