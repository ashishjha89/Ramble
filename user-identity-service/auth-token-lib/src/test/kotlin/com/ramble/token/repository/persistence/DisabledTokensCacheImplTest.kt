package com.ramble.token.repository.persistence

import com.ramble.token.repository.persistence.entities.DisabledClientTokens
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

class DisabledTokensCacheImplTest {

    private val clientId = "someClientId"

    private val disabledTokensRedisRepo = mock(DisabledTokensRedisRepo::class.java)

    private val scope = CoroutineScope(Job())

    private val disabledTokensCacheImpl = DisabledTokensCacheImpl(disabledTokensRedisRepo)

    @Test
    fun `getDisabledTokens when disabled tokens are present in sql`() = runBlocking {
        val disabledAccessTokens = listOf("token1", "token2")
        val disabledClientTokens = DisabledClientTokens(clientId, disabledAccessTokens)

        // Stub
        given(disabledTokensRedisRepo.findById(clientId)).willReturn(Optional.of(disabledClientTokens))

        // Call method and assert
        assertEquals(setOf("token1", "token2"), disabledTokensCacheImpl.getDisabledTokens(clientId, scope))
    }

    @Test
    fun `getDisabledTokens when disabled tokens is empty list in sql`() = runBlocking {
        val disabledAccessTokens = emptyList<String>()
        val disabledClientTokens = DisabledClientTokens(clientId, disabledAccessTokens)

        // Stub
        given(disabledTokensRedisRepo.findById(clientId)).willReturn(Optional.of(disabledClientTokens))

        // Call method and assert
        assertEquals(emptySet(), disabledTokensCacheImpl.getDisabledTokens(clientId, scope))
    }

    @Test
    fun `getDisabledTokens when disabled tokens are missing in sql`() = runBlocking {
        // Stub
        given(disabledTokensRedisRepo.findById(clientId)).willReturn(Optional.empty())

        // Call method and assert
        assertEquals(emptySet(), disabledTokensCacheImpl.getDisabledTokens(clientId, scope))
    }

    @Test
    fun hasDisabledTokenTest() = runBlocking {
        // Stub
        given(disabledTokensRedisRepo.existsById(clientId)).willReturn(true)

        // Call method and assert
        assertTrue(disabledTokensCacheImpl.hasDisabledToken(clientId, scope))
    }

    @Test
    fun saveDisabledTokenTest() = runBlocking {
        val disabledAccessTokens = listOf("token1", "token2")
        val disabledClientTokens = DisabledClientTokens(clientId, disabledAccessTokens)

        // Stub
        given(disabledTokensRedisRepo.save(disabledClientTokens)).willReturn(disabledClientTokens)

        // Call method and assert
        assertEquals(disabledClientTokens, disabledTokensCacheImpl.saveDisabledToken(disabledClientTokens, scope))
    }

    @Test
    fun deleteDisabledTokenTest() = runBlocking {
        // Call method
        disabledTokensCacheImpl.deleteDisabledToken(clientId, scope)

        // Verify
        verify(disabledTokensRedisRepo).deleteById(clientId)
    }
}