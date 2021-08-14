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

    private val email = "someEmail"
    private val token = "someRegistrationToken"

    private val registrationTokenSqlRepo = mock(RegistrationConfirmationTokenSqlRepo::class.java)

    private val registrationConfirmationRepo = RegistrationConfirmationRepo(registrationTokenSqlRepo)

    @Test
    fun addRegistrationConfirmationTokenTest() = runBlocking {
        // Stub
        given(registrationTokenSqlRepo.save(any())).willReturn(RegistrationConfirmationToken(email, token))

        // Call method and assert
        val result = registrationConfirmationRepo.addRegistrationConfirmationToken(
            RegistrationConfirmationToken(email, token)
        )
        assertEquals(email, result.email)
        assertEquals(token, result.token)
    }

    @Test
    fun `deleteRegistrationConfirmationToken when email existed before`() = runBlocking {
        // Stub
        given(registrationTokenSqlRepo.existsById(email)).willReturn(true)

        // Call method
        registrationConfirmationRepo.deleteRegistrationConfirmationToken(email)

        // Verify
        verify(registrationTokenSqlRepo).deleteById(email)
    }

    @Test
    fun `deleteRegistrationConfirmationToken when email not existed before`() = runBlocking {
        // Stub
        given(registrationTokenSqlRepo.existsById(email)).willReturn(false)

        // Call method
        registrationConfirmationRepo.deleteRegistrationConfirmationToken(email)

        // Verify
        verify(registrationTokenSqlRepo, times(0)).deleteById(email)
    }

    @Test
    fun getRegistrationConfirmationTokenTest() = runBlocking {
        // Stub
        given(registrationTokenSqlRepo.findById(email)).willReturn(
            Optional.of(RegistrationConfirmationToken(email, token))
        )

        // Call method and assert
        val result = registrationConfirmationRepo.getRegistrationConfirmationToken(email)!!
        assertEquals(email, result.email)
        assertEquals(token, result.token)
    }
}