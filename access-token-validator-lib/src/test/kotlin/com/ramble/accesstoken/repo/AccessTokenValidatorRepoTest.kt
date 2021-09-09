package com.ramble.accesstoken.repo

import com.ramble.accesstoken.model.AccessTokenValidatorInternalException
import com.ramble.accesstoken.repo.persistence.DisabledTokensCacheImpl
import com.ramble.accesstoken.repo.persistence.entities.DisabledClientTokens
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito.*
import kotlin.test.assertEquals

class AccessTokenValidatorRepoTest {

    private val clientId = "someClientId"
    private val accessToken = "someAccessToken"

    private val disabledTokensCache = mock(DisabledTokensCacheImpl::class.java)
    private val ioCoroutineScope = mock(CoroutineScope::class.java)

    private val authTokenRepo = AccessTokenValidatorRepo(disabledTokensCache)

    @Test
    fun `getDisabledAccessTokensForClient when disabled tokens are present in Redis`() = runBlocking {
        // Stub
        given(disabledTokensCache.getDisabledTokens(clientId, ioCoroutineScope))
            .willReturn(setOf("disabled_1", "disabled_2"))

        // Call method and assert
        assertEquals(
            setOf("disabled_1", "disabled_2"),
            authTokenRepo.getDisabledAccessTokensForClient(clientId, ioCoroutineScope)
        )
    }

    @Test
    fun `getDisabledAccessTokensForClient when disabled tokens are absent in Redis`() = runBlocking {
        // Stub
        given(disabledTokensCache.getDisabledTokens(clientId, ioCoroutineScope)).willReturn(emptySet())

        // Call method and assert
        assertEquals(emptySet(), authTokenRepo.getDisabledAccessTokensForClient(clientId, ioCoroutineScope))
    }

    @Test(expected = AccessTokenValidatorInternalException::class)
    fun `getDisabledAccessTokensForClient should throw InternalTokenStorageException when Redis operation fails`() =
        runBlocking {

            // Stub
            given(disabledTokensCache.getDisabledTokens(clientId, ioCoroutineScope))
                .willThrow(AccessTokenValidatorInternalException())

            // Call method and assert
            assertEquals(emptySet(), authTokenRepo.getDisabledAccessTokensForClient(clientId, ioCoroutineScope))
        }

    @Test
    fun `updateDisabledAccessTokensForClient when null tokens passed and clientId existed in Redis`() = runBlocking {
        // Stub
        given(disabledTokensCache.hasDisabledToken(clientId, ioCoroutineScope)).willReturn(true)

        // Call method
        authTokenRepo.updateDisabledAccessTokensForClient(clientId, null, ioCoroutineScope)

        // Verify
        verify(disabledTokensCache).deleteDisabledToken(clientId, ioCoroutineScope)
    }

    @Test
    fun `updateDisabledAccessTokensForClient when null tokens passed and clientId did not exist in Redis`() =
        runBlocking {
            // Stub
            given(disabledTokensCache.hasDisabledToken(clientId, ioCoroutineScope)).willReturn(false)

            // Call method
            authTokenRepo.updateDisabledAccessTokensForClient(clientId, null, ioCoroutineScope)

            // Verify
            verify(disabledTokensCache, times(0)).deleteDisabledToken(clientId, ioCoroutineScope)
        }

    @Test
    fun `updateDisabledAccessTokensForClient when empty tokens passed and clientId existed in Redis`() = runBlocking {
        // Stub
        given(disabledTokensCache.hasDisabledToken(clientId, ioCoroutineScope)).willReturn(true)

        // Call method
        authTokenRepo.updateDisabledAccessTokensForClient(clientId, emptySet(), ioCoroutineScope)

        // Verify
        verify(disabledTokensCache).deleteDisabledToken(clientId, ioCoroutineScope)
    }

    @Test
    fun `updateDisabledAccessTokensForClient when empty tokens passed and clientId did not exist in Redis`() =
        runBlocking {
            // Stub
            given(disabledTokensCache.hasDisabledToken(clientId, ioCoroutineScope)).willReturn(false)

            // Call method
            authTokenRepo.updateDisabledAccessTokensForClient(clientId, emptySet(), ioCoroutineScope)

            // Verify
            verify(disabledTokensCache, times(0)).deleteDisabledToken(clientId, ioCoroutineScope)
        }

    @Test(expected = AccessTokenValidatorInternalException::class)
    fun `updateDisabledAccessTokens should throw Exception when tokens not passed and Redis fails in hasDisabledToken`() =
        runBlocking<Unit> {
            val disabledClientTokens = DisabledClientTokens(clientId, emptyList())
            // Stub
            given(disabledTokensCache.hasDisabledToken(clientId, ioCoroutineScope))
                .willThrow(AccessTokenValidatorInternalException())

            // Call method
            authTokenRepo.updateDisabledAccessTokensForClient(clientId, emptySet(), ioCoroutineScope)

            // Verify
            verify(disabledTokensCache).saveDisabledToken(disabledClientTokens, ioCoroutineScope)
        }

    @Test(expected = AccessTokenValidatorInternalException::class)
    fun `updateDisabledAccessTokens should throw Exception when tokens not passed and Redis fails deleting old existing tokens`() =
        runBlocking<Unit> {
            val disabledClientTokens = DisabledClientTokens(clientId, emptyList())
            // Stub
            given(disabledTokensCache.hasDisabledToken(clientId, ioCoroutineScope))
                .willReturn(true)
            given(disabledTokensCache.deleteDisabledToken(clientId, ioCoroutineScope))
                .willThrow(AccessTokenValidatorInternalException())

            // Call method
            authTokenRepo.updateDisabledAccessTokensForClient(clientId, emptySet(), ioCoroutineScope)

            // Verify
            verify(disabledTokensCache).saveDisabledToken(disabledClientTokens, ioCoroutineScope)
        }

    @Test
    fun `updateDisabledAccessTokens when some tokens passed`() = runBlocking<Unit> {
        val disabledClientTokens = DisabledClientTokens(clientId, listOf(accessToken))
        // Stub
        given(disabledTokensCache.saveDisabledToken(disabledClientTokens, ioCoroutineScope)).willReturn(
            disabledClientTokens
        )

        // Call method
        authTokenRepo.updateDisabledAccessTokensForClient(clientId, setOf(accessToken), ioCoroutineScope)

        // Verify
        verify(disabledTokensCache).saveDisabledToken(disabledClientTokens, ioCoroutineScope)
    }

    @Test(expected = AccessTokenValidatorInternalException::class)
    fun `updateDisabledAccessTokens should throw Exception when tokens passed and Redis fails in saving it`() =
        runBlocking<Unit> {
            val disabledClientTokens = DisabledClientTokens(clientId, listOf(accessToken))
            // Stub
            given(disabledTokensCache.saveDisabledToken(disabledClientTokens, ioCoroutineScope))
                .willThrow(AccessTokenValidatorInternalException())

            // Call method
            authTokenRepo.updateDisabledAccessTokensForClient(clientId, setOf(accessToken), ioCoroutineScope)

            // Verify
            verify(disabledTokensCache).saveDisabledToken(disabledClientTokens, ioCoroutineScope)
        }

}