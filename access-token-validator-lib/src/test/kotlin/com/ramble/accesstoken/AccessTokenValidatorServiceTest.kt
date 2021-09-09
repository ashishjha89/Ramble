package com.ramble.accesstoken

import com.ramble.accesstoken.config.AccessTokenValidatorBuilder
import com.ramble.accesstoken.handler.AccessTokenHandler
import com.ramble.accesstoken.model.AccessClaims
import com.ramble.accesstoken.repo.AccessTokenValidatorRepo
import io.jsonwebtoken.Claims
import io.jsonwebtoken.JwtBuilder
import io.jsonwebtoken.JwtParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.test.assertEquals

class AccessTokenValidatorServiceTest {

    private val authTokenRepo = mock(AccessTokenValidatorRepo::class.java)

    private val tokenComponentBuilder = mock(AccessTokenValidatorBuilder::class.java)

    private val accessTokenHandler = mock(AccessTokenHandler::class.java)

    private val jwtBuilder = mock(JwtBuilder::class.java)

    private val jwtParserAccessToken = mock(JwtParser::class.java)

    private val ioScope = mock(CoroutineScope::class.java)

    private val authTokensService by lazy { AccessTokenValidatorService(authTokenRepo, tokenComponentBuilder) }

    @Before
    fun setup() {
        given(tokenComponentBuilder.accessTokenHandler()).willReturn(accessTokenHandler)
        given(tokenComponentBuilder.jwtBuilder()).willReturn(jwtBuilder)
        given(tokenComponentBuilder.jwtParserAccessToken()).willReturn(jwtParserAccessToken)
        given(tokenComponentBuilder.defaultIoScope).willReturn(ioScope)
    }

    @Test
    fun generateAccessTokenTest() = runBlocking {
        val roles = listOf("User")
        val userId = "someUserId"
        val clientId = "someClientId"
        val email = "someEmailId@ramble.com"
        val issuedInstant = Instant.now()
        val expiryDurationAmount = 30L
        val expiryDurationUnit = ChronoUnit.MINUTES
        val accessToken = "some_long_random_access_token"

        // Stub
        given(
            accessTokenHandler
                .generateToken(
                    roles,
                    clientId,
                    userId,
                    email,
                    issuedInstant,
                    expiryDurationAmount,
                    expiryDurationUnit,
                    jwtBuilder
                )
        ).willReturn(accessToken)

        assertEquals(
            accessToken,
            authTokensService.generateAccessToken(
                roles, clientId, userId, email, issuedInstant, expiryDurationAmount, expiryDurationUnit
            )
        )
    }

    @Test
    fun `getClaimsFromAccessToken from token when there is no disabled token for the client`() = runBlocking {
        val token = "some_token"
        val now = Instant.now()
        val clientId = "someClientId"
        val userId = "someUserId"
        val emailId = "someEmail@ramble.com"
        val claims = mock(Claims::class.java)
        val roles = listOf("User", "Admin")

        val expectedAccessClaims = AccessClaims(clientId, userId, emailId, claims, roles)

        // Stub
        given(accessTokenHandler.getAccessClaims(token, jwtParserAccessToken, now)).willReturn(expectedAccessClaims)
        given(authTokenRepo.getDisabledAccessTokensForClient(clientId, ioScope)).willReturn(setOf())

        // Call method and assert
        val accessClaims = authTokensService.getClaimsFromAccessToken(token, now)

        // Verify
        assertEquals(expectedAccessClaims, accessClaims)
    }

    @Test
    fun `getClaimsFromAccessToken from token when there are disabled tokens but disabledTokens do not contain current accessToken`() =
        runBlocking {
            val token = "some_token"
            val disabledToken1 = "disabled_token_1"
            val disabledToken2 = "disabled_token_2"
            val now = Instant.now()
            val clientId = "someClientId"
            val userId = "someUserId"
            val emailId = "someEmail@ramble.com"
            val claims = mock(Claims::class.java)
            val roles = listOf("User", "Admin")

            val allDisabledTokens = setOf(disabledToken1, disabledToken2)
            val expectedAccessClaims = AccessClaims(clientId, userId, emailId, claims, roles)

            // Stub
            given(accessTokenHandler.getAccessClaims(token, jwtParserAccessToken, now)).willReturn(expectedAccessClaims)
            given(authTokenRepo.getDisabledAccessTokensForClient(clientId, ioScope)).willReturn(allDisabledTokens)

            // Call method and assert
            val accessClaims = authTokensService.getClaimsFromAccessToken(token, now)

            // Verify
            assertEquals(expectedAccessClaims, accessClaims)
        }

    @Test
    fun `getClaimsFromAccessToken from token when there are disabled tokens and disabledTokens also contain current accessToken`() =
        runBlocking {
            val token = "some_token"
            val disabledToken1 = "disabled_token_1"
            val now = Instant.now()
            val clientId = "someClientId"
            val userId = "someUserId"
            val emailId = "someEmail@ramble.com"
            val claims = mock(Claims::class.java)
            val roles = listOf("User", "Admin")

            val allDisabledTokens = setOf(disabledToken1, token) // contains current token
            val expectedAccessClaims = AccessClaims(clientId, userId, emailId, claims, roles)

            // Stub
            given(accessTokenHandler.getAccessClaims(token, jwtParserAccessToken, now)).willReturn(expectedAccessClaims)
            given(authTokenRepo.getDisabledAccessTokensForClient(clientId, ioScope)).willReturn(allDisabledTokens)

            // Call method and assert
            assertNull(authTokensService.getClaimsFromAccessToken(token, now))
        }

    @Test
    fun getAccessClaimsTest() = runBlocking {
        val claims = mock(Claims::class.java)
        val accessClaims = mock(AccessClaims::class.java)
        // Stub
        given(accessTokenHandler.getAccessClaims(claims)).willReturn(accessClaims)
        // Call method and assert
        assertEquals(accessClaims, authTokensService.getAccessClaims(claims))
    }

    @Test
    fun `disableAccessToken when there was no previously disabled token`() = runBlocking {
        val clientId = "someClientId"
        val accessToken = "someAccessToken"
        val now = Instant.now()

        // Stub
        given(authTokenRepo.getDisabledAccessTokensForClient(clientId, ioScope)).willReturn(emptySet())

        // Call method
        authTokensService.disableAccessToken(clientId, accessToken, now)

        // Verify
        verify(authTokenRepo).updateDisabledAccessTokensForClient(clientId, setOf(accessToken), ioScope)
    }

    @Test
    fun `disableAccessToken when there were old disabled tokens and only some of them valid`() = runBlocking {
        val clientId = "someClientId"
        val accessToken = "someAccessToken"
        val now = Instant.now()

        val token1 = "disabledToken1"
        val token2 = "validToken1"
        val token3 = "disableToken2"
        val token4 = "validToken2"

        // Stub
        given(authTokenRepo.getDisabledAccessTokensForClient(clientId, ioScope))
            .willReturn(setOf(token1, token2, token3, token4))
        given(accessTokenHandler.isValidToken(token1, jwtParserAccessToken, now)).willReturn(false)
        given(accessTokenHandler.isValidToken(token2, jwtParserAccessToken, now)).willReturn(true)
        given(accessTokenHandler.isValidToken(token3, jwtParserAccessToken, now)).willReturn(false)
        given(accessTokenHandler.isValidToken(token4, jwtParserAccessToken, now)).willReturn(true)

        // Call method
        authTokensService.disableAccessToken(clientId, accessToken, now)

        // Verify
        verify(authTokenRepo).updateDisabledAccessTokensForClient(clientId, setOf(accessToken, token2, token4), ioScope)
    }

}