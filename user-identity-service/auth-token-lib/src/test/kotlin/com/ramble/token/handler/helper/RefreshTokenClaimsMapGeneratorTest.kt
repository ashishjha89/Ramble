package com.ramble.token.handler.helper

import kotlin.test.Test
import kotlin.test.assertEquals

class RefreshTokenClaimsMapGeneratorTest {

    @Test
    fun getRefreshTokenClaimsMapTest() {
        val userId = "someUserId"
        val clientId = "someClientId"

        // Call method
        val claimsMapResult = RefreshTokenClaimsMapGenerator().getRefreshTokenClaimsMap(clientId, userId)

        // Assert
        assertEquals(clientId, claimsMapResult["CLIENT_ID"])
        assertEquals(userId, claimsMapResult["USER_ID"])
    }
}