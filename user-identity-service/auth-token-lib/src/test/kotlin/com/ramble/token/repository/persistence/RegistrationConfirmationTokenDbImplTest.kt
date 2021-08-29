package com.ramble.token.repository.persistence

import com.ramble.token.repository.persistence.entities.RegistrationConfirmationToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.verify
import org.mockito.Mockito.mock
import java.util.*
import kotlin.test.assertEquals

class RegistrationConfirmationTokenDbImplTest {

    private val emailId = "someEmailId"

    private val registrationConfirmationTokenSql = mock(RegistrationConfirmationTokenSqlRepo::class.java)

    private val scope = CoroutineScope(Job())

    private val registrationConfirmationTokenDbImpl =
        RegistrationConfirmationTokenDbImpl(registrationConfirmationTokenSql)

    @Test
    fun getRegistrationConfirmationTokenTest() = runBlocking {
        val registrationConfirmationToken = RegistrationConfirmationToken(emailId, "someToken")

        // Stub
        given(registrationConfirmationTokenSql.findById(emailId))
            .willReturn(Optional.of(registrationConfirmationToken))

        // Call method
        val result = registrationConfirmationTokenDbImpl.getRegistrationConfirmationToken(emailId, scope)
        assertEquals(emailId, result?.email)
        assertEquals("someToken", result?.token)
    }

    @Test
    fun hasRegistrationConfirmationTokenTest() = runBlocking {
        // Stub
        given(registrationConfirmationTokenSql.existsById(emailId)).willReturn(true)

        // Call method
        assertTrue(registrationConfirmationTokenDbImpl.hasRegistrationConfirmationToken(emailId, scope))
    }

    @Test
    fun saveRegistrationConfirmationTokenTest() = runBlocking {
        val registrationConfirmationToken = RegistrationConfirmationToken(emailId, "someToken")

        // Stub
        given(registrationConfirmationTokenSql.save(registrationConfirmationToken)).willReturn(
            registrationConfirmationToken
        )

        // Call method and assert
        assertEquals(
            registrationConfirmationToken,
            registrationConfirmationTokenDbImpl.saveRegistrationConfirmationToken(registrationConfirmationToken, scope)
        )
    }

    @Test
    fun deleteRegistrationConfirmationTokenTest() = runBlocking {
        // Call method and assert
        registrationConfirmationTokenDbImpl.deleteRegistrationConfirmationToken(emailId, scope)

        // Verify
        verify(registrationConfirmationTokenSql).deleteById(emailId)
    }

}