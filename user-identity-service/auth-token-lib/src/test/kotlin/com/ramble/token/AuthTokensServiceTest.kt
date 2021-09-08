package com.ramble.token

import com.ramble.accesstoken.AccessTokenValidatorService
import com.ramble.accesstoken.model.AccessClaims
import com.ramble.token.config.TokenComponentBuilder
import com.ramble.token.handler.RefreshTokenHandler
import com.ramble.token.handler.helper.UsernamePasswordAuthTokenTokenGenerator
import com.ramble.token.model.AccessTokenIsInvalidException
import com.ramble.token.model.RefreshTokenIsInvalidException
import com.ramble.token.model.UserAuthInfo
import com.ramble.token.repository.AuthTokenRepo
import com.ramble.token.repository.persistence.entities.ClientAuthInfo
import com.ramble.token.repository.persistence.entities.ClientRefreshToken
import io.jsonwebtoken.Claims
import io.jsonwebtoken.JwtBuilder
import io.jsonwebtoken.JwtParser
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.verify
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

    private val tokenValidatorService = mock(AccessTokenValidatorService::class.java)

    private val tokenComponentBuilder = mock(TokenComponentBuilder::class.java)

    private val refreshTokenHandler = mock(RefreshTokenHandler::class.java)

    private val usernamePasswordAuthTokenTokenGenerator = mock(UsernamePasswordAuthTokenTokenGenerator::class.java)

    private val jwtBuilder = mock(JwtBuilder::class.java)

    private val jwtParserRefreshToken = mock(JwtParser::class.java)

    private val authTokensService by lazy {
        AuthTokensService(authTokenRepo, tokenValidatorService, tokenComponentBuilder)
    }

    @Before
    fun setup() {
        given(tokenComponentBuilder.refreshTokenHandler()).willReturn(refreshTokenHandler)
        given(tokenComponentBuilder.usernamePasswordAuthTokenTokenGenerator())
            .willReturn(usernamePasswordAuthTokenTokenGenerator)
        given(tokenComponentBuilder.jwtBuilder()).willReturn(jwtBuilder)
        given(tokenComponentBuilder.jwtParserRefreshToken()).willReturn(jwtParserRefreshToken)
    }

    @Test
    fun `generateAuthToken when there are no existing tokens`(): Unit = runBlocking {
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
        given(
            tokenValidatorService.generateAccessToken(
                authorities,
                clientId,
                userId,
                email,
                issuedInstant,
                accessTokenExpiryDurationAmount,
                accessTokenExpiryDurationUnit
            )
        ).willReturn(accessToken)
        given(
            refreshTokenHandler.generateRefreshToken(
                clientId,
                userId,
                issuedInstant,
                refreshTokenExpiryDurationAmount,
                refreshTokenExpiryDurationUnit,
                jwtBuilder
            )
        ).willReturn(refreshToken)
        given(authTokenRepo.getExistingTokensForClient(clientId, expectedAuthInfo)).willReturn(null)

        // Call method
        val userAuthInfo = authTokensService.generateUserAuthToken(
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

        // Verify
        assertEquals(expectedAuthInfo, userAuthInfo)
        verify(authTokenRepo).insertUserAuthInfo(clientId, userAuthInfo)
        verify(authTokenRepo).getExistingTokensForClient(clientId, expectedAuthInfo)
    }

    @Test
    fun `generateAuthToken when there were existing tokens`() = runBlocking {
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

        val existingRefreshToken = "existing_refresh_token"
        val existingAccessToken = "existing_access_token"
        val existingClientRefreshToken = ClientRefreshToken(existingRefreshToken, existingAccessToken, clientId, userId)

        val expectedAuthInfo = UserAuthInfo(userId, email, accessToken, refreshToken)

        // Stub
        given(
            tokenValidatorService.generateAccessToken(
                authorities,
                clientId,
                userId,
                email,
                issuedInstant,
                accessTokenExpiryDurationAmount,
                accessTokenExpiryDurationUnit
            )
        ).willReturn(accessToken)
        given(
            refreshTokenHandler.generateRefreshToken(
                clientId,
                userId,
                issuedInstant,
                refreshTokenExpiryDurationAmount,
                refreshTokenExpiryDurationUnit,
                jwtBuilder
            )
        ).willReturn(refreshToken)
        given(authTokenRepo.getExistingTokensForClient(clientId, expectedAuthInfo))
            .willReturn(existingClientRefreshToken)

        // Call method
        val userAuthInfo = authTokensService.generateUserAuthToken(
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

        // Verify
        assertEquals(expectedAuthInfo, userAuthInfo)
        verify(authTokenRepo).insertUserAuthInfo(clientId, userAuthInfo)
        verify(authTokenRepo).getExistingTokensForClient(clientId, expectedAuthInfo)
        verify(tokenValidatorService).disableAccessToken(clientId, existingAccessToken, issuedInstant)
    }

    @Test
    fun `getAccessTokenClaims gets claims from TokenValidatorService`() = runBlocking {
        val token = "some_token"
        val now = Instant.now()
        val expectedAccessClaims = mock(AccessClaims::class.java)

        // Stub
        given(tokenValidatorService.getClaimsFromAccessToken(token, now)).willReturn(expectedAccessClaims)

        // Call method and assert
        val accessClaims = authTokensService.getAccessTokenClaims(token, now)

        // Verify
        assertEquals(expectedAccessClaims, accessClaims)
    }

    @Test
    fun `getAccessTokenClaims from Principal should return accessToken if principal is UsernamePasswordAuthenticationToken`() {
        val principal = mock(UsernamePasswordAuthenticationToken::class.java)
        val accessClaims = mock(AccessClaims::class.java)
        val claims = mock(Claims::class.java)
        val authority = mock(GrantedAuthority::class.java)
        val authorities = listOf(authority)

        // Stub
        given(principal.principal).willReturn(claims)
        given(principal.authorities).willReturn(authorities)
        given(tokenValidatorService.getAccessClaims(claims, authorities)).willReturn(accessClaims)

        // Call method and assert
        assertEquals(accessClaims, authTokensService.getAccessTokenClaims(principal))
    }

    @Test
    fun `getAccessTokenClaims from Principal should return null if principal is not UsernamePasswordAuthenticationToken`() {
        val principal = mock(Principal::class.java)

        // Call method and assert
        assertNull(authTokensService.getAccessTokenClaims(principal))
    }

    @Test
    fun `getAccessTokenClaims from Principal should return null if authorities not present`() {
        val principal = mock(UsernamePasswordAuthenticationToken::class.java)
        val accessClaims = mock(AccessClaims::class.java)

        // Stub
        given(principal.principal).willReturn(accessClaims)
        given(principal.authorities).willReturn(null)

        // Call method and assert
        assertNull(authTokensService.getAccessTokenClaims(principal))
    }

    @Test
    fun springAuthenticationTest() {
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

    @Test
    fun `refreshToken should generate new authTokens and disable oldAccessToken`() =
        runBlocking<Unit> {
            val refreshToken = "someRefreshToken"
            val now = Instant.now()
            val accessTokenExpiryDurationAmount = 30L
            val accessTokenExpiryDurationUnit = ChronoUnit.MINUTES
            val refreshTokenExpiryDurationAmount = 300L
            val refreshTokenExpiryDurationUnit = ChronoUnit.DAYS

            val accessTokenOld = "someOldAccessToken"
            val clientId = "someClientId"
            val userId = "someUserId"
            val emailId = "someEmail@ramble.com"
            val authority = mock(GrantedAuthority::class.java)
            val authorities = listOf(authority)
            val clientAuthInfo = ClientAuthInfo(clientId, userId, accessTokenOld)

            val newAccessToken = "this_is_new_generated_access_token"
            val newRefreshToken = "this_is_new_refresh_token"
            val expectedUserAuthInfo = UserAuthInfo(userId, emailId, newAccessToken, newRefreshToken)

            // Stub
            given(refreshTokenHandler.getClientIdFromToken(refreshToken, jwtParserRefreshToken)).willReturn(clientId)
            given(refreshTokenHandler.getUserIdFromToken(refreshToken, jwtParserRefreshToken)).willReturn(userId)
            given(refreshTokenHandler.isValidToken(refreshToken, jwtParserRefreshToken, now)).willReturn(true)

            given(authTokenRepo.deleteOldAuthTokens(clientId, userId)).willReturn(clientAuthInfo)

            given(tokenValidatorService.getClientIdFromToken(accessTokenOld)).willReturn(clientId)
            given(tokenValidatorService.getUserIdFromToken(accessTokenOld)).willReturn(userId)
            given(tokenValidatorService.getEmailFromToken(accessTokenOld)).willReturn(emailId)
            given(tokenValidatorService.getRolesFromToken(accessTokenOld)).willReturn(authorities)

            given(
                tokenValidatorService.generateAccessToken(
                    authorities,
                    clientId,
                    userId,
                    emailId,
                    now,
                    accessTokenExpiryDurationAmount,
                    accessTokenExpiryDurationUnit
                )
            ).willReturn(newAccessToken)
            given(
                refreshTokenHandler.generateRefreshToken(
                    clientId,
                    userId,
                    now,
                    refreshTokenExpiryDurationAmount,
                    refreshTokenExpiryDurationUnit,
                    jwtBuilder
                )
            ).willReturn(newRefreshToken)

            // Call method
            val userAuthInfo = authTokensService.refreshAuthToken(
                refreshToken,
                now,
                accessTokenExpiryDurationAmount,
                accessTokenExpiryDurationUnit,
                refreshTokenExpiryDurationAmount,
                refreshTokenExpiryDurationUnit
            )

            // Verify
            assertEquals(expectedUserAuthInfo, userAuthInfo)
            verify(tokenValidatorService).disableAccessToken(clientId, accessTokenOld, now)
            verify(authTokenRepo)
                .insertUserAuthInfo(clientId, UserAuthInfo(userId, emailId, newAccessToken, newRefreshToken))
        }

    @Test(expected = RefreshTokenIsInvalidException::class)
    fun `refreshAuthToken should throw RefreshTokenIsInvalidException if token is invalid`() = runBlocking<Unit> {
        val refreshToken = "someRefreshToken"
        val now = Instant.now()
        val accessTokenExpiryDurationAmount = 30L
        val accessTokenExpiryDurationUnit = ChronoUnit.MINUTES
        val refreshTokenExpiryDurationAmount = 300L
        val refreshTokenExpiryDurationUnit = ChronoUnit.DAYS

        val clientId = "someClientId"
        val userId = "someUserId"

        // Stub
        given(refreshTokenHandler.getClientIdFromToken(refreshToken, jwtParserRefreshToken)).willReturn(clientId)
        given(refreshTokenHandler.getUserIdFromToken(refreshToken, jwtParserRefreshToken)).willReturn(userId)
        given(refreshTokenHandler.isValidToken(refreshToken, jwtParserRefreshToken, now)).willReturn(false)

        // Call method
        authTokensService.refreshAuthToken(
            refreshToken,
            now,
            accessTokenExpiryDurationAmount,
            accessTokenExpiryDurationUnit,
            refreshTokenExpiryDurationAmount,
            refreshTokenExpiryDurationUnit
        )

        verify(authTokenRepo).deleteOldAuthTokens(clientId, userId)
    }

    @Test(expected = RefreshTokenIsInvalidException::class)
    fun `refreshAuthToken should throw RefreshTokenIsInvalidException if clientId could not be found from token`() =
        runBlocking<Unit> {
            val refreshToken = "someRefreshToken"
            val now = Instant.now()
            val accessTokenExpiryDurationAmount = 30L
            val accessTokenExpiryDurationUnit = ChronoUnit.MINUTES
            val refreshTokenExpiryDurationAmount = 300L
            val refreshTokenExpiryDurationUnit = ChronoUnit.DAYS

            val userId = "someUserId"

            // Stub
            given(refreshTokenHandler.getClientIdFromToken(refreshToken, jwtParserRefreshToken)).willReturn(null)
            given(refreshTokenHandler.getUserIdFromToken(refreshToken, jwtParserRefreshToken)).willReturn(userId)
            given(refreshTokenHandler.isValidToken(refreshToken, jwtParserRefreshToken, now)).willReturn(true)

            // Call method
            authTokensService.refreshAuthToken(
                refreshToken,
                now,
                accessTokenExpiryDurationAmount,
                accessTokenExpiryDurationUnit,
                refreshTokenExpiryDurationAmount,
                refreshTokenExpiryDurationUnit
            )
        }

    @Test(expected = RefreshTokenIsInvalidException::class)
    fun `refreshAuthToken should throw RefreshTokenIsInvalidException if userId could not be dound from token`() =
        runBlocking<Unit> {
            val refreshToken = "someRefreshToken"
            val now = Instant.now()
            val accessTokenExpiryDurationAmount = 30L
            val accessTokenExpiryDurationUnit = ChronoUnit.MINUTES
            val refreshTokenExpiryDurationAmount = 300L
            val refreshTokenExpiryDurationUnit = ChronoUnit.DAYS

            val clientId = "someClientId"

            // Stub
            given(refreshTokenHandler.getClientIdFromToken(refreshToken, jwtParserRefreshToken)).willReturn(clientId)
            given(refreshTokenHandler.getUserIdFromToken(refreshToken, jwtParserRefreshToken)).willReturn(null)
            given(refreshTokenHandler.isValidToken(refreshToken, jwtParserRefreshToken, now)).willReturn(true)

            // Call method
            authTokensService.refreshAuthToken(
                refreshToken,
                now,
                accessTokenExpiryDurationAmount,
                accessTokenExpiryDurationUnit,
                refreshTokenExpiryDurationAmount,
                refreshTokenExpiryDurationUnit
            )
        }

    @Test
    fun logoutTest() = runBlocking {
        val now = Instant.now()
        val accessToken = "someAccessToken"

        val clientId = "someClientId"
        val userId = "someUserId"

        // Stub
        given(tokenValidatorService.getClientIdFromToken(accessToken)).willReturn(clientId)
        given(tokenValidatorService.getUserIdFromToken(accessToken)).willReturn(userId)

        // Call method
        authTokensService.logout(accessToken, now)

        // Verify
        verify(tokenValidatorService).disableAccessToken(clientId, accessToken, now)
    }

    @Test(expected = AccessTokenIsInvalidException::class)
    fun `logout should throw AccessTokenIsInvalidException if clientId cannot be obtained from token`() = runBlocking {
        val now = Instant.now()
        val accessToken = "someAccessToken"
        val userId = "someUserId"

        // Stub
        given(tokenValidatorService.getClientIdFromToken(accessToken)).willReturn(null)
        given(tokenValidatorService.getUserIdFromToken(accessToken)).willReturn(userId)

        // Call method
        authTokensService.logout(accessToken, now)
    }

}