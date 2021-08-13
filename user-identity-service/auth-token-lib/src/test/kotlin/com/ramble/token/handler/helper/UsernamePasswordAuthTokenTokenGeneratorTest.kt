package com.ramble.token.handler.helper

import io.jsonwebtoken.Claims
import org.junit.Test
import org.mockito.Mockito.mock
import org.springframework.security.core.GrantedAuthority
import kotlin.test.assertEquals
import kotlin.test.assertNull

class UsernamePasswordAuthTokenTokenGeneratorTest {

    @Test
    fun getUsernamePasswordAuthenticationTokenTest() {
        val usernamePasswordAuthTokenTokenGenerator = UsernamePasswordAuthTokenTokenGenerator()
        val claims = mock(Claims::class.java)
        val authority = mock(GrantedAuthority::class.java)
        val authorities = listOf(authority)

        // Call method
        val token = usernamePasswordAuthTokenTokenGenerator.getUsernamePasswordAuthenticationToken(claims, authorities)

        // Assert
        assertEquals(authorities, token.authorities)
        assertEquals(claims, token.principal)
        assertNull(token.credentials)
    }
}