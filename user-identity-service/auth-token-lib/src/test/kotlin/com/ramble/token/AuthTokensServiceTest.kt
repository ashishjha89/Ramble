package com.ramble.token

import com.ramble.token.config.TokenComponentBuilder
import com.ramble.token.handler.AccessTokenHandler
import com.ramble.token.handler.RefreshTokenHandler
import com.ramble.token.handler.helper.UsernamePasswordAuthTokenTokenGenerator
import com.ramble.token.model.AccessClaims
import com.ramble.token.model.AuthInfo
import io.jsonwebtoken.Claims
import io.jsonwebtoken.JwtBuilder
import io.jsonwebtoken.JwtParser
import org.junit.Before
import org.junit.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito.mock
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import java.security.Principal
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.test.assertEquals

class AuthTokensServiceTest {

    private val tokenComponentBuilder = mock(TokenComponentBuilder::class.java)

    private val accessTokenHandler = mock(AccessTokenHandler::class.java)

    private val refreshTokenHandler = mock(RefreshTokenHandler::class.java)

    private val usernamePasswordAuthTokenTokenGenerator = mock(UsernamePasswordAuthTokenTokenGenerator::class.java)

    private val jwtBuilder = mock(JwtBuilder::class.java)

    private val jwtParser = mock(JwtParser::class.java)

    private val authTokensService by lazy { AuthTokensService(tokenComponentBuilder) }

    @Before
    fun setup() {
        given(tokenComponentBuilder.accessTokenHandler()).willReturn(accessTokenHandler)
        given(tokenComponentBuilder.refreshTokenHandler()).willReturn(refreshTokenHandler)
        given(tokenComponentBuilder.usernamePasswordAuthTokenTokenGenerator())
                .willReturn(usernamePasswordAuthTokenTokenGenerator)
        given(tokenComponentBuilder.jwtBuilder()).willReturn(jwtBuilder)
        given(tokenComponentBuilder.jwtParser()).willReturn(jwtParser)
    }

    @Test
    fun generateAuthTokenTest() {
        val authResult = mock(Authentication::class.java)
        val userId = "someUserIdd"
        val email = "someEmailId@ramble.com"
        val issuedInstant = Instant.now()
        val expiryDurationAmount = 30L
        val expiryDurationUnit = ChronoUnit.MINUTES

        val accessToken = "some_long_random_access_token"
        val refreshToken = "some_refresh_token"

        val expectedAuthInfo = AuthInfo(userId, email, accessToken, refreshToken)

        // Stub
        given(accessTokenHandler
                .generateAccessToken(authResult, userId, email, issuedInstant, expiryDurationAmount, expiryDurationUnit, jwtBuilder))
                .willReturn(accessToken)
        given(refreshTokenHandler.generateRefreshToken()).willReturn(refreshToken)

        // Call method and assert
        assertEquals(
                expectedAuthInfo,
                authTokensService.generateAuthToken(
                        authResult, userId, email, issuedInstant, expiryDurationAmount, expiryDurationUnit)
        )
    }

    @Test
    fun `getClaims from token`() {
        val token = "some_token"
        val now = Instant.now()
        val accessClaims = mock(AccessClaims::class.java)

        // Stub
        given(accessTokenHandler.getTokenClaims(token, jwtParser, now)).willReturn(accessClaims)

        // Call method and assert
        assertEquals(accessClaims, authTokensService.getClaims(token, now))
    }

    @Test
    fun `getClaims from Principal`() {
        val principal = mock(Principal::class.java)
        val accessClaims = mock(AccessClaims::class.java)

        // Stub
        given(accessTokenHandler.getPrincipalClaims(principal)).willReturn(accessClaims)

        // Call method and assert
        assertEquals(accessClaims, authTokensService.getClaims(principal))
    }

    @Test
    fun getAuthenticationTest() {
        val claims = mock(Claims::class.java)
        val authority = mock(GrantedAuthority::class.java)
        val authorities = listOf(authority)
        val authentication = mock(UsernamePasswordAuthenticationToken::class.java)

        // Stub
        given(usernamePasswordAuthTokenTokenGenerator.getUsernamePasswordAuthenticationToken(claims, authorities))
                .willReturn(authentication)

        // Call method and assert
        assertEquals(authentication, authTokensService.getAuthentication(claims, authorities))
    }
}