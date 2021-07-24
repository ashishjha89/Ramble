package com.ramble.token.handler.helper

import org.mockito.BDDMockito.given
import org.mockito.Mockito.mock
import org.springframework.security.core.GrantedAuthority
import kotlin.test.Test
import kotlin.test.assertEquals

class AccessTokenClaimsMapGeneratorTest {

    @Test
    fun getAccessTokenClaimsMapTest() {
        val authority = mock(GrantedAuthority::class.java)
        val authorities = listOf(authority)
        val userId = "someUserId"
        val clientId = "someClientId"
        val authorityStr = "Some_Authority"

        // Stub
        given(authority.authority).willReturn(authorityStr)

        // Call method
        val claimsMapResult = AccessTokenClaimsMapGenerator().getAccessTokenClaimsMap(clientId, userId, authorities)

        // Assert
        assertEquals(clientId, claimsMapResult["CLIENT_ID"])
        assertEquals(userId, claimsMapResult["USER_ID"])
        assertEquals(listOf(authorityStr), claimsMapResult["ROLES"])
    }
}