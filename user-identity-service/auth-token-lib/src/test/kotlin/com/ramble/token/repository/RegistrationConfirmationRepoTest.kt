package com.ramble.token.repository

import com.ramble.token.model.InternalTokenStorageException
import com.ramble.token.repository.persistence.RegistrationConfirmationTokenDbImpl
import com.ramble.token.repository.persistence.entities.RegistrationConfirmationToken
import com.ramble.token.util.AuthTokenCoroutineScopeBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.mockito.BDDMockito.*
import org.mockito.Mockito.mock
import kotlin.test.assertEquals

class RegistrationConfirmationRepoTest {

    private val email = "someEmail"
    private val token = "someRegistrationToken"

    private val registrationTokenSqlRepo = mock(RegistrationConfirmationTokenDbImpl::class.java)
    private val coroutineScopeBuilder = mock(AuthTokenCoroutineScopeBuilder::class.java)
    private val ioCoroutineScope = mock(CoroutineScope::class.java)

    private val registrationConfirmationRepo =
        RegistrationConfirmationRepo(registrationTokenSqlRepo, coroutineScopeBuilder)

    @Before
    fun setup() {
        given(coroutineScopeBuilder.defaultIoScope).willReturn(ioCoroutineScope)
    }

    @Test
    fun addRegistrationConfirmationTokenTest() = runBlocking {
        val registrationConfirmationToken = RegistrationConfirmationToken(email, token)
        // Stub
        given(
            registrationTokenSqlRepo.saveRegistrationConfirmationToken(
                registrationConfirmationToken,
                ioCoroutineScope
            )
        )
            .willReturn(RegistrationConfirmationToken(email, token))

        // Call method and assert
        val result = registrationConfirmationRepo.addRegistrationConfirmationToken(registrationConfirmationToken)
        assertEquals(email, result.email)
        assertEquals(token, result.token)
    }

    @Test(expected = InternalTokenStorageException::class)
    fun `addRegistrationConfirmationToken should throw Exception if sql fails in saving`() =
        runBlocking<Unit> {
            val registrationConfirmationToken = RegistrationConfirmationToken(email, token)
            // Stub
            given(
                registrationTokenSqlRepo.saveRegistrationConfirmationToken(
                    registrationConfirmationToken, ioCoroutineScope
                )
            ).willThrow(InternalTokenStorageException())

            // Call method and assert
            registrationConfirmationRepo.addRegistrationConfirmationToken(registrationConfirmationToken)
        }

    @Test
    fun `deleteRegistrationConfirmationToken when email existed before`() = runBlocking {
        // Stub
        given(registrationTokenSqlRepo.hasRegistrationConfirmationToken(email, ioCoroutineScope)).willReturn(true)

        // Call method
        registrationConfirmationRepo.deleteRegistrationConfirmationToken(email)

        // Verify
        verify(registrationTokenSqlRepo).deleteRegistrationConfirmationToken(email, ioCoroutineScope)
    }

    @Test
    fun `deleteRegistrationConfirmationToken when email not existed before`() = runBlocking {
        // Stub
        given(registrationTokenSqlRepo.hasRegistrationConfirmationToken(email, ioCoroutineScope)).willReturn(false)

        // Call method
        registrationConfirmationRepo.deleteRegistrationConfirmationToken(email)

        // Verify
        verify(registrationTokenSqlRepo, times(0)).deleteRegistrationConfirmationToken(email, ioCoroutineScope)
    }

    @Test(expected = InternalTokenStorageException::class)
    fun `deleteRegistrationConfirmationToken should throw Exception when sql fails checking if token exists`() =
        runBlocking {
            // Stub
            given(registrationTokenSqlRepo.hasRegistrationConfirmationToken(email, ioCoroutineScope))
                .willThrow(InternalTokenStorageException())

            // Call method
            registrationConfirmationRepo.deleteRegistrationConfirmationToken(email)
        }

    @Test(expected = InternalTokenStorageException::class)
    fun `deleteRegistrationConfirmationToken should throw Exception when sql fails while deleting token`() =
        runBlocking<Unit> {
            // Stub
            given(registrationTokenSqlRepo.hasRegistrationConfirmationToken(email, ioCoroutineScope))
                .willReturn(true)
            given(registrationTokenSqlRepo.deleteRegistrationConfirmationToken(email, ioCoroutineScope))
                .willThrow(InternalTokenStorageException())

            // Call method
            registrationConfirmationRepo.deleteRegistrationConfirmationToken(email)
        }

    @Test
    fun getRegistrationConfirmationTokenTest() = runBlocking {
        // Stub
        given(registrationTokenSqlRepo.getRegistrationConfirmationToken(email, ioCoroutineScope))
            .willReturn(RegistrationConfirmationToken(email, token))

        // Call method and assert
        val result = registrationConfirmationRepo.getRegistrationConfirmationToken(email)!!
        assertEquals(email, result.email)
        assertEquals(token, result.token)
    }

    @Test(expected = InternalTokenStorageException::class)
    fun `getRegistrationConfirmationToken should throw Exception when sql fails in finding token`() =
        runBlocking<Unit> {
            // Stub
            given(registrationTokenSqlRepo.getRegistrationConfirmationToken(email, ioCoroutineScope))
                .willThrow(InternalTokenStorageException())

            // Call method and assert
            registrationConfirmationRepo.getRegistrationConfirmationToken(email)!!
        }
}