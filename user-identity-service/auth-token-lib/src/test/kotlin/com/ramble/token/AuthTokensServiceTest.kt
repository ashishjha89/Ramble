package com.ramble.token

import com.ramble.token.config.TokenComponentBuilder
import com.ramble.token.handler.AccessTokenHandler
import com.ramble.token.handler.RefreshTokenHandler
import com.ramble.token.handler.helper.UsernamePasswordAuthTokenTokenGenerator
import com.ramble.token.model.AccessClaims
import com.ramble.token.model.UserAuthInfo
import com.ramble.token.repository.AuthTokenRepo
import io.jsonwebtoken.Claims
import io.jsonwebtoken.JwtBuilder
import io.jsonwebtoken.JwtParser
import org.junit.Before
import org.junit.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito.mock
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import java.security.Principal
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.test.assertEquals

class AuthTokensServiceTest {

    private val authTokenRepo = mock(AuthTokenRepo::class.java)

    private val tokenComponentBuilder = mock(TokenComponentBuilder::class.java)

    private val accessTokenHandler = mock(AccessTokenHandler::class.java)

    private val refreshTokenHandler = mock(RefreshTokenHandler::class.java)

    private val usernamePasswordAuthTokenTokenGenerator = mock(UsernamePasswordAuthTokenTokenGenerator::class.java)

    private val jwtBuilder = mock(JwtBuilder::class.java)

    private val jwtParser = mock(JwtParser::class.java)

    private val authTokensService by lazy { AuthTokensService(authTokenRepo, tokenComponentBuilder) }

    @Before
    fun setup() {
        given(tokenComponentBuilder.accessTokenHandler()).willReturn(accessTokenHandler)
        given(tokenComponentBuilder.refreshTokenHandler()).willReturn(refreshTokenHandler)
        given(tokenComponentBuilder.usernamePasswordAuthTokenTokenGenerator())
                .willReturn(usernamePasswordAuthTokenTokenGenerator)
        given(tokenComponentBuilder.jwtBuilder()).willReturn(jwtBuilder)
        given(tokenComponentBuilder.jwtParserAccessToken()).willReturn(jwtParser)
    }

    @Test
    fun generateAuthTokenTest() {
        val authority = mock(SimpleGrantedAuthority::class.java)
        val authorities = listOf(authority)
        val userId = "someUserId"
        val clientId = "someClientId"
        val email = "someEmailId@ramble.com"
        val issuedInstant = Instant.now()

        val accessTokenExpiryDurationAmount = 30L
        val accessTokenExpiryDurationUnit = ChronoUnit.MINUTES

        val refreshTokenExpiryDurationAmount = 365L
        val refreshTokenExpiryDurationUnit = ChronoUnit.DAYS

        val accessToken = "some_long_random_access_token"
        val refreshToken = "some_refresh_token"

        val expectedAuthInfo = UserAuthInfo(userId, email, accessToken, refreshToken)

        // Stub
        given(accessTokenHandler
                .generateAccessToken(
                        authorities,
                        clientId,
                        userId,
                        email,
                        issuedInstant,
                        accessTokenExpiryDurationAmount,
                        accessTokenExpiryDurationUnit,
                        jwtBuilder
                )
        ).willReturn(accessToken)
        given(refreshTokenHandler.generateRefreshToken()).willReturn(refreshToken)

        // Call method and assert
        assertEquals(
                expectedAuthInfo,
                authTokensService.generateUserAuthToken(
                        authorities,
                        clientId,
                        userId,
                        email,
                        issuedInstant,
                        accessTokenExpiryDurationAmount,
                        accessTokenExpiryDurationUnit,
                        refreshTokenExpiryDurationAmount,
                        refreshTokenExpiryDurationUnit
                )
        )
    }

    @Test
    fun `getClaims from token`() {
        val token = "some_token"
        val now = Instant.now()
        val accessClaims = AccessClaims("clientId", "userId", "someEmail", mock(Claims::class.java), listOf(mock(SimpleGrantedAuthority::class.java)))

        // Stub
        given(accessTokenHandler.getTokenClaims(token, jwtParser, now)).willReturn(accessClaims)

        // Call method and assert
        assertEquals(accessClaims, authTokensService.getAccessTokenClaims(token, now))
    }

    @Test
    fun `getClaims from Principal`() {
        val principal = mock(Principal::class.java)
        val accessClaims = mock(AccessClaims::class.java)

        // Stub
        given(accessTokenHandler.getPrincipalClaims(principal)).willReturn(accessClaims)

        // Call method and assert
        assertEquals(accessClaims, authTokensService.getAccessTokenClaims(principal))
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
        assertEquals(authentication, authTokensService.springAuthentication(claims, authorities))
    }
}