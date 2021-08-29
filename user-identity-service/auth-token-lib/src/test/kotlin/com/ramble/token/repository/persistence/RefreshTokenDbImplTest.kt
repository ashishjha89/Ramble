package com.ramble.token.repository.persistence

import com.ramble.token.repository.persistence.entities.ClientRefreshToken
import com.ramble.token.repository.persistence.entities.ClientUserId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.verify
import org.mockito.Mockito.mock
import java.util.*
import kotlin.test.assertEquals

class RefreshTokenDbImplTest {

    private val clientId = "someClientId"
    private val userId = "someUserId"
    private val clientUserId = ClientUserId(clientId, userId)

    private val refreshToken = "someRefreshToken"
    private val accessToken = "someAccessToken"

    private val clientRefreshToken = ClientRefreshToken(refreshToken, accessToken, clientId, userId)

    private val refreshTokenSqlRepo = mock(ClientRefreshTokenSqlRepo::class.java)

    private val scope = CoroutineScope(Job())

    private val refreshTokenDbImpl = RefreshTokenDbImpl(refreshTokenSqlRepo)

    @Test
    fun getClientRefreshTokenTest() = runBlocking {
        // Stub
        given(refreshTokenSqlRepo.findById(clientUserId)).willReturn(Optional.of(clientRefreshToken))

        // Call method and assert
        assertEquals(clientRefreshToken, refreshTokenDbImpl.getClientRefreshToken(clientUserId, scope))
    }

    @Test
    fun saveClientRefreshTokenTest() = runBlocking {
        // Stub
        given(refreshTokenSqlRepo.save(clientRefreshToken)).willReturn(clientRefreshToken)

        // Call method and assert
        assertEquals(clientRefreshToken, refreshTokenDbImpl.saveClientRefreshToken(clientRefreshToken, scope))
    }

    @Test
    fun deleteClientRefreshTokenTest() = runBlocking {
        // Call method
        refreshTokenDbImpl.deleteClientRefreshToken(clientUserId, scope)

        // Verify
        verify(refreshTokenSqlRepo).deleteById(clientUserId)
    }
}