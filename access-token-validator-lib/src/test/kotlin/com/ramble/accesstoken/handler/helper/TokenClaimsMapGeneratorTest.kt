package com.ramble.accesstoken.handler.helper

import org.junit.Assert.assertEquals
import org.junit.Test

class TokenClaimsMapGeneratorTest {

    @Test
    fun getAccessTokenClaimsMapTest() {
        val roles = listOf("user", "admin")
        val userId = "someUserId"
        val clientId = "someClientId"

        // Call method
        val claimsMapResult = TokenClaimsMapGenerator().getAccessTokenClaimsMap(clientId, userId, roles)

        // Assert
        assertEquals(clientId, claimsMapResult["CLIENT_ID"])
        assertEquals(userId, claimsMapResult["USER_ID"])
        assertEquals(roles, claimsMapResult["ROLES"])
    }

}