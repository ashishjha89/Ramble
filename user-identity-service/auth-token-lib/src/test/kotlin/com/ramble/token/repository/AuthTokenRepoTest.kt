package com.ramble.token.repository

import com.ramble.token.any
import com.ramble.token.model.InternalTokenStorageException
import com.ramble.token.model.RefreshTokenIsInvalidException
import com.ramble.token.model.UserAuthInfo
import com.ramble.token.repository.persistence.RefreshTokenDbImpl
import com.ramble.token.repository.persistence.entities.ClientAuthInfo
import com.ramble.token.repository.persistence.entities.ClientRefreshToken
import com.ramble.token.repository.persistence.entities.ClientUserId
import com.ramble.token.util.AuthTokenCoroutineScopeBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito.mock
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AuthTokenRepoTest {

    private val clientId = "someClientId"
    private val email = "someEmailId"
    private val userId = "someUserId"
    private val accessToken = "someAccessToken"
    private val refreshToken = "someRefreshToken"
    private val userAuthInfo = UserAuthInfo(userId, email, accessToken, refreshToken)

    private val refreshTokenDb = mock(RefreshTokenDbImpl::class.java)
    private val coroutineScopeBuilder = mock(AuthTokenCoroutineScopeBuilder::class.java)
    private val ioCoroutineScope = mock(CoroutineScope::class.java)

    private val authTokenRepo = AuthTokenRepo(refreshTokenDb, coroutineScopeBuilder)

    @Before
    fun setup() {
        given(coroutineScopeBuilder.defaultIoScope).willReturn(ioCoroutineScope)
    }

    @Test
    fun insertUserAuthInfoTest() = runBlocking {
        val expectedClientRefreshToken = ClientRefreshToken(refreshToken, accessToken, clientId)

        // Stub
        given(refreshTokenDb.saveClientRefreshToken(any(), any())).willReturn(expectedClientRefreshToken)

        // Call method and assert
        assertEquals(expectedClientRefreshToken, authTokenRepo.insertUserAuthInfo(clientId, userAuthInfo))
    }

    @Test(expected = InternalTokenStorageException::class)
    fun `insertUserAuthInfoTest throws InternalTokenStorageException if db operation fails`() = runBlocking {
        val expectedClientRefreshToken = ClientRefreshToken(refreshToken, accessToken, clientId)

        // Stub
        given(refreshTokenDb.saveClientRefreshToken(any(), any())).willThrow(InternalTokenStorageException())

        // Call method and assert
        assertEquals(expectedClientRefreshToken, authTokenRepo.insertUserAuthInfo(clientId, userAuthInfo))
    }

    @Test
    fun `deleteOldAuthTokens when token is present in Sql`() = runBlocking {
        val clientUserId = ClientUserId(clientId, userId)
        val clientRefreshToken = ClientRefreshToken(refreshToken, accessToken, clientId, userId)
        val clientAuthInfo = ClientAuthInfo(clientId, userId, accessToken)

        // Stub
        given(refreshTokenDb.getClientRefreshToken(clientUserId, ioCoroutineScope))
            .willReturn(clientRefreshToken)

        // Call method and assert
        assertEquals(clientAuthInfo, authTokenRepo.deleteOldAuthTokens(clientId, userId))
    }

    @Test(expected = RefreshTokenIsInvalidException::class)
    fun `deleteOldAuthTokens should throw RefreshTokenIsInvalidException when token is absent in Sql`() =
        runBlocking<Unit> {
            val clientUserId = ClientUserId(clientId, userId)

            // Stub
            given(refreshTokenDb.getClientRefreshToken(clientUserId, ioCoroutineScope)).willReturn(null)

            // Call method and assert
            authTokenRepo.deleteOldAuthTokens(clientId, userId)
        }

    @Test(expected = InternalTokenStorageException::class)
    fun `deleteOldAuthTokens should throw InternalTokenStorageException when sql operation fails`() =
        runBlocking<Unit> {
            val clientUserId = ClientUserId(clientId, userId)

            // Stub
            given(refreshTokenDb.getClientRefreshToken(clientUserId, ioCoroutineScope))
                .willThrow(InternalTokenStorageException())

            // Call method and assert
            authTokenRepo.deleteOldAuthTokens(clientId, userId)
        }

    @Test
    fun `getExistingTokensForClient when token is present in Sql`() = runBlocking {
        val expectedClientRefreshToken = ClientRefreshToken(refreshToken, accessToken, clientId, userId)

        // Stub
        given(refreshTokenDb.getClientRefreshToken(ClientUserId(clientId, userAuthInfo.userId), ioCoroutineScope))
            .willReturn(expectedClientRefreshToken)

        // Call method and assert
        val clientRefreshToken = authTokenRepo.getExistingTokensForClient(clientId, userAuthInfo)!!
        assertEquals(expectedClientRefreshToken.refreshToken, clientRefreshToken.refreshToken)
        assertEquals(expectedClientRefreshToken.accessToken, clientRefreshToken.accessToken)
        assertEquals(expectedClientRefreshToken.clientId, clientRefreshToken.clientId)
        assertEquals(expectedClientRefreshToken.userId, clientRefreshToken.userId)
    }

    @Test
    fun `getExistingTokensForClient when token is absent in Sql`() = runBlocking {
        // Stub
        given(refreshTokenDb.getClientRefreshToken(ClientUserId(clientId, userAuthInfo.userId), ioCoroutineScope))
            .willReturn(null)

        // Call method and assert
        assertNull(authTokenRepo.getExistingTokensForClient(clientId, userAuthInfo))
    }

    @Test(expected = InternalTokenStorageException::class)
    fun `getExistingTokensForClient should throw InternalTokenStorageException when Sql fails`() = runBlocking {
        // Stub
        given(refreshTokenDb.getClientRefreshToken(ClientUserId(clientId, userAuthInfo.userId), ioCoroutineScope))
            .willThrow(InternalTokenStorageException())

        // Call method and assert
        assertNull(authTokenRepo.getExistingTokensForClient(clientId, userAuthInfo))
    }
}