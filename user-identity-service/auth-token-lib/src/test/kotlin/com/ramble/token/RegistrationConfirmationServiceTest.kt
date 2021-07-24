package com.ramble.token

import com.ramble.token.config.TokenComponentBuilder
import com.ramble.token.handler.RegistrationConfirmationTokenHandler
import com.ramble.token.model.RegistrationConfirmationToken
import com.ramble.token.repository.RegistrationConfirmationRepo
import io.jsonwebtoken.JwtBuilder
import io.jsonwebtoken.JwtParser
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

    private val registrationConfirmationService by lazy { RegistrationConfirmationService(tokenComponentBuilder) }

    @Before
    fun setup() {
        given(tokenComponentBuilder.registrationConfirmationRepo()).willReturn(registrationConfirmationRepo)
        given(tokenComponentBuilder.registrationConfirmationTokenHandler()).willReturn(registrationTokenHandler)
        given(tokenComponentBuilder.jwtBuilder()).willReturn(jwtBuilder)
        given(tokenComponentBuilder.jwtParserRegistrationToken()).willReturn(jwtParser)
    }

    @Test
    fun addRegistrationConfirmationTokenTest() {
        val userId = "someUserIdd"
        val email = "someEmailId@ramble.com"
        val now = Instant.now()
        val expiryDurationAmount = 30L
        val expiryDurationUnit = ChronoUnit.MINUTES
        val tokenStr = "some_registration_confirmation_token"
        val expectedToken = RegistrationConfirmationToken(userId, email, tokenStr)

        // Stub
        given(registrationTokenHandler
                .generateToken(userId, email, now, expiryDurationAmount, expiryDurationUnit, jwtBuilder)
        ).willReturn(tokenStr)

        // Call method
        val tokenResult = registrationConfirmationService
                .addRegistrationConfirmationToken(userId, email, now, expiryDurationAmount, expiryDurationUnit)

        // Verify
        verify(registrationConfirmationRepo).deleteRegistrationConfirmationToken(userId) // delete old token for user
        verify(registrationConfirmationRepo).addRegistrationConfirmationToken(expectedToken) // add new token
        assertEquals(expectedToken, tokenResult)
    }

    @Test
    fun `processRegistrationConfirmationToken should return null if passed token is invalid for user`() {
        val confirmRegistrationTokenStr = "some_registration_confirmation_token"
        val now = Instant.now()

        // Stub
        given(registrationTokenHandler.getUserIdFromToken(confirmRegistrationTokenStr, jwtParser))
                .willReturn(null)

        // Call method and assert
        assertNull(
                registrationConfirmationService
                        .processRegistrationConfirmationToken(confirmRegistrationTokenStr, now))
    }

    @Test
    fun `processRegistrationConfirmationToken should return null if invalid token and delete that token `() {
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
    fun `processRegistrationConfirmationToken should return token if valid token for user`() {
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