package com.ramble.token.handler

import com.ramble.token.handler.helper.UUIDGenerator
import org.junit.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito.mock
import kotlin.test.assertEquals

class RefreshTokenHandlerTest {

    @Test
    fun generateRefreshTokenTest() {
        val uUIDGenerator = mock(UUIDGenerator::class.java)
        val randomUuid = "some_random_uuid"

        // Stub
        given(uUIDGenerator.getRandomUUID()).willReturn(randomUuid)

        // Call method and assert
        assertEquals(randomUuid, RefreshTokenHandler(uUIDGenerator).generateRefreshToken())
    }
}