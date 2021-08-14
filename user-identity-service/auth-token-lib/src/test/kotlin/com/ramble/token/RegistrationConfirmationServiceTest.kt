package com.ramble.token

import com.ramble.token.config.TokenComponentBuilder
import com.ramble.token.handler.RegistrationConfirmationTokenHandler
import com.ramble.token.repository.RegistrationConfirmationRepo
import com.ramble.token.repository.persistence.entities.RegistrationConfirmationToken
import io.jsonwebtoken.JwtBuilder
import io.jsonwebtoken.JwtParser
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito.*
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RegistrationConfirmationServiceTest {

    private val tokenComponentBuilder = mock(TokenComponentBuilder::class.java)

    private val registrationConfirmationRepo = mock(RegistrationConfirmationRepo::class.java)

    private val registrationTokenHandler = mock(RegistrationConfirmationTokenHandler::class.java)

    private val jwtBuilder = mock(JwtBuilder::class.java)

    private val jwtParser = mock(JwtParser::class.java)

    private val registrationConfirmationService by lazy {
        RegistrationConfirmationService(tokenComponentBuilder, registrationConfirmationRepo)
    }

    @Before
    fun setup() {
        given(tokenComponentBuilder.registrationConfirmationTokenHandler()).willReturn(registrationTokenHandler)
        given(tokenComponentBuilder.jwtBuilder()).willReturn(jwtBuilder)
        given(tokenComponentBuilder.jwtParserRegistrationToken()).willReturn(jwtParser)
    }

    @Test
    fun addRegistrationConfirmationTokenTest() = runBlocking {
        val userId = "someUserIdd"
        val email = "someEmailId@ramble.com"
        val now = Instant.now()
        val expiryDurationAmount = 30L
        val expiryDurationUnit = ChronoUnit.MINUTES
        val tokenStr = "some_registration_confirmation_token"

        // Stub
        given(
            registrationTokenHandler
                .generateToken(userId, email, now, expiryDurationAmount, expiryDurationUnit, jwtBuilder)
        ).willReturn(tokenStr)

        // Call method
        val tokenResult = registrationConfirmationService
            .addRegistrationConfirmationToken(userId, email, now, expiryDurationAmount, expiryDurationUnit)

        // Verify
        verify(registrationConfirmationRepo).deleteRegistrationConfirmationToken(userId) // delete old token for user
        verify(registrationConfirmationRepo).addRegistrationConfirmationToken(any()) // add new token
        assertEquals(userId, tokenResult.userId)
        assertEquals(email, tokenResult.email)
        assertEquals(tokenStr, tokenResult.token)
    }

    @Test
    fun `processRegistrationConfirmationToken should return null if passed token is invalid for user`() = runBlocking {
        val confirmRegistrationTokenStr = "some_registration_confirmation_token"
        val now = Instant.now()

        // Stub
        given(registrationTokenHandler.getUserIdFromToken(confirmRegistrationTokenStr, jwtParser))
            .willReturn(null)

        // Call method and assert
        assertNull(
            registrationConfirmationService
                .processRegistrationConfirmationToken(confirmRegistrationTokenStr, now)
        )
    }

    @Test
    fun `processRegistrationConfirmationToken should return null if invalid token and delete that token `() = runBlocking {
        val confirmRegistrationTokenStr = "some_registration_confirmation_token"
        val now = Instant.now()
        val userId = "validUserId"

        // Stub
        given(registrationTokenHandler.getUserIdFromToken(confirmRegistrationTokenStr, jwtParser))
            .willReturn(userId)
        given(registrationTokenHandler.isValidToken(confirmRegistrationTokenStr, now, jwtParser))
            .willReturn(false)

        // Call method
        val tokenResult = registrationConfirmationService
            .processRegistrationConfirmationToken(confirmRegistrationTokenStr, now)

        // Verify
        assertNull(tokenResult)
        verify(registrationConfirmationRepo).deleteRegistrationConfirmationToken(userId)
    }

    @Test
    fun `processRegistrationConfirmationToken should return token if valid token for user`() = runBlocking {
        val confirmRegistrationTokenStr = "some_registration_confirmation_token"
        val now = Instant.now()
        val userId = "validUserId"
        val expectedToken = mock(RegistrationConfirmationToken::class.java)

        // Stub
        given(registrationTokenHandler.getUserIdFromToken(confirmRegistrationTokenStr, jwtParser))
            .willReturn(userId)
        given(registrationTokenHandler.isValidToken(confirmRegistrationTokenStr, now, jwtParser))
            .willReturn(true)
        given(registrationConfirmationRepo.getRegistrationConfirmationToken(userId))
            .willReturn(expectedToken)

        // Call method
        val tokenResult = registrationConfirmationService
            .processRegistrationConfirmationToken(confirmRegistrationTokenStr, now)

        // Verify
        assertEquals(expectedToken, tokenResult)
        verify(registrationConfirmationRepo, times(0)).deleteRegistrationConfirmationToken(userId)
    }
}