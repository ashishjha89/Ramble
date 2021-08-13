package com.ramble.token.repository

import com.ramble.token.any
import com.ramble.token.model.RefreshTokenIsInvalidException
import com.ramble.token.model.UserAuthInfo
import com.ramble.token.repository.persistence.ClientRefreshTokenSqlRepo
import com.ramble.token.repository.persistence.DisabledTokensRedisRepo
import com.ramble.token.repository.persistence.entities.ClientAuthInfo
import com.ramble.token.repository.persistence.entities.ClientRefreshToken
import com.ramble.token.repository.persistence.entities.ClientUserId
import com.ramble.token.repository.persistence.entities.DisabledClientTokens
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.verify
import org.mockito.Mockito.mock
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AuthTokenRepoTest {

    private val clientId = "someClientId"
    private val email = "someEmailId"
    private val userId = "someUserId"
    private val accessToken = "someAccessToken"
    private val refreshToken = "someRefreshToken"
    private val userAuthInfo = UserAuthInfo(userId, email, accessToken, refreshToken)

    private val refreshTokenSqlRepo = mock(ClientRefreshTokenSqlRepo::class.java)
    private val disabledTokensRedisRepo = mock(DisabledTokensRedisRepo::class.java)

    private val authTokenRepo = AuthTokenRepo(refreshTokenSqlRepo, disabledTokensRedisRepo)

    @Test
    fun insertUserAuthInfoTest() = runBlocking {
        val expectedClientRefreshToken = ClientRefreshToken(refreshToken, accessToken, clientId)

        // Stub
        given(refreshTokenSqlRepo.save(any())).willReturn(expectedClientRefreshToken)

        // Call method and assert
        val clientRefreshToken = authTokenRepo.insertUserAuthInfo(clientId, userAuthInfo)
        assertEquals(expectedClientRefreshToken.refreshToken, clientRefreshToken.refreshToken)
        assertEquals(expectedClientRefreshToken.accessToken, clientRefreshToken.accessToken)
        assertEquals(expectedClientRefreshToken.clientId, clientRefreshToken.clientId)
        assertEquals(expectedClientRefreshToken.userId, clientRefreshToken.userId)
    }

    @Test
    fun `deleteOldAuthTokens when token is present in Sql`() = runBlocking {
        val clientUserId = ClientUserId(clientId, userId)
        val clientRefreshToken = ClientRefreshToken(refreshToken, accessToken, clientId, userId)
        val clientAuthInfo = ClientAuthInfo(clientId, userId, accessToken)

        // Stub
        given(refreshTokenSqlRepo.findById(clientUserId)).willReturn(Optional.of(clientRefreshToken))

        // Call method and assert
        assertEquals(clientAuthInfo, authTokenRepo.deleteOldAuthTokens(clientId, userId))
    }

    @Test(expected = RefreshTokenIsInvalidException::class)
    fun `deleteOldAuthTokens when token is absent in Sql`() = runBlocking<Unit> {
        val clientUserId = ClientUserId(clientId, userId)

        // Stub
        given(refreshTokenSqlRepo.findById(clientUserId)).willReturn(Optional.empty())

        // Call method and assert
        authTokenRepo.deleteOldAuthTokens(clientId, userId)
    }

    @Test
    fun `getExistingTokensForClient when token is present in Sql`() = runBlocking {
        val expectedClientRefreshToken = ClientRefreshToken(refreshToken, accessToken, clientId, userId)

        // Stub
        given(refreshTokenSqlRepo.findById(ClientUserId(clientId, userAuthInfo.userId)))
            .willReturn(Optional.of(expectedClientRefreshToken))

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
        given(refreshTokenSqlRepo.findById(ClientUserId(clientId, userAuthInfo.userId))).willReturn(Optional.empty())

        // Call method and assert
        assertNull(authTokenRepo.getExistingTokensForClient(clientId, userAuthInfo))
    }

    @Test
    fun `getDisabledAccessTokensForClient when disabled tokens are present in Redis`() = runBlocking {
        val clientAuthInfo = ClientAuthInfo(clientId, userId, accessToken)
        val disabledClientTokens = DisabledClientTokens(
            id = clientId, disabledAccessTokens = listOf("disabled_1", "disabled_2")
        )
        // Stub
        given(disabledTokensRedisRepo.findById(clientId)).willReturn(Optional.of(disabledClientTokens))

        // Call method and assert
        assertEquals(setOf("disabled_1", "disabled_2"), authTokenRepo.getDisabledAccessTokensForClient(clientAuthInfo))
    }

    @Test
    fun `getDisabledAccessTokensForClient when disabled tokens are absent in Redis`() = runBlocking {
        val clientAuthInfo = ClientAuthInfo(clientId, userId, accessToken)

        // Stub
        given(disabledTokensRedisRepo.findById(clientId)).willReturn(Optional.empty())

        // Call method and assert
        assertEquals(emptySet(), authTokenRepo.getDisabledAccessTokensForClient(clientAuthInfo))
    }

    @Test
    fun `updateDisabledAccessTokensForClient when null tokens passed`() = runBlocking {
        // Call method
        authTokenRepo.updateDisabledAccessTokensForClient(clientId, null)

        // Verify
        verify(disabledTokensRedisRepo).deleteById(clientId)
    }

    @Test
    fun `updateDisabledAccessTokensForClient when empty tokens passed`() = runBlocking {
        // Call method
        authTokenRepo.updateDisabledAccessTokensForClient(clientId, emptySet())

        // Verify
        verify(disabledTokensRedisRepo).deleteById(clientId)
    }

    @Test
    fun `updateDisabledAccessTokensForClient when some tokens passed`() = runBlocking<Unit> {
        // Call method
        authTokenRepo.updateDisabledAccessTokensForClient(clientId, setOf(accessToken))

        // Verify
        verify(disabledTokensRedisRepo).save(DisabledClientTokens(clientId, listOf(accessToken)))
    }
}