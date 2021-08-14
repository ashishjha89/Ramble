package com.ramble.token.repository

import com.ramble.token.repository.persistence.RegistrationConfirmationTokenSqlRepo
import com.ramble.token.repository.persistence.entities.RegistrationConfirmationToken
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.mockito.BDDMockito.*
import org.mockito.Mockito.mock
import java.util.*
import kotlin.test.assertEquals

class RegistrationConfirmationRepoTest {

    private val userId = "someUserId"
    private val email = "someEmail"
    private val token = "someRegistrationToken"

    private val registrationTokenSqlRepo = mock(RegistrationConfirmationTokenSqlRepo::class.java)

    private val registrationConfirmationRepo = RegistrationConfirmationRepo(registrationTokenSqlRepo)

    @Test
    fun addRegistrationConfirmationTokenTest() = runBlocking {
        // Stub
        given(registrationTokenSqlRepo.save(any())).willReturn(RegistrationConfirmationToken(userId, email, token))

        // Call method and assert
        val result = registrationConfirmationRepo.addRegistrationConfirmationToken(
            RegistrationConfirmationToken(userId, email, token)
        )
        assertEquals(userId, result.userId)
        assertEquals(email, result.email)
        assertEquals(token, result.token)
    }

    @Test
    fun deleteRegistrationConfirmationTokenTest() = runBlocking {
        // Call method
        registrationConfirmationRepo.deleteRegistrationConfirmationToken(userId)

        // Verify
        verify(registrationTokenSqlRepo).deleteById(userId)
    }

    @Test
    fun getRegistrationConfirmationTokenTest() = runBlocking {
        // Stub
        given(registrationTokenSqlRepo.findById(userId)).willReturn(
            Optional.of(RegistrationConfirmationToken(userId, email, token))
        )

        // Call method and assert
        val result = registrationConfirmationRepo.getRegistrationConfirmationToken(userId)!!
        assertEquals(userId, result.userId)
        assertEquals(email, result.email)
        assertEquals(token, result.token)
    }
}