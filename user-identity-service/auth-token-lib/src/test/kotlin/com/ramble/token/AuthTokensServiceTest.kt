package com.ramble.token

import com.ramble.token.config.TokenComponentBuilder
import com.ramble.token.handler.AccessTokenHandler
import com.ramble.token.handler.RefreshTokenHandler
import com.ramble.token.handler.helper.UsernamePasswordAuthTokenTokenGenerator
import com.ramble.token.model.AccessClaims
import com.ramble.token.model.AccessTokenIsInvalidException
import com.ramble.token.model.UserAuthInfo
import com.ramble.token.repository.AuthTokenRepo
import com.ramble.token.repository.persistence.entities.ClientAuthInfo
import io.jsonwebtoken.Claims
import io.jsonwebtoken.JwtBuilder
import io.jsonwebtoken.JwtParser
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.verify
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
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
    fun generateAuthTokenTest() = runBlocking<Unit> {
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
            accessTokenHandler
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
    }

    @Test
    fun `getClaims from token when there is no disabled token for the client`() = runBlocking {
        val token = "some_token"
        val now = Instant.now()
        val clientId = "someClientId"
        val userId = "someUserId"
        val emailId = "someEmail@ramble.com"
        val claims = mock(Claims::class.java)
        val authority = mock(SimpleGrantedAuthority::class.java)

        val expectedAccessClaims = AccessClaims(clientId, userId, emailId, claims, listOf(authority))
        val expectedClientAuthInfo = ClientAuthInfo(clientId, userId, token)

        // Stub
        given(accessTokenHandler.getTokenClaims(token, jwtParser, now)).willReturn(expectedAccessClaims)
        given(authTokenRepo.getDisabledAccessTokensForClient(expectedClientAuthInfo)).willReturn(setOf())

        // Call method and assert
        val accessClaims = authTokensService.getAccessTokenClaims(token, now)

        // Verify
        assertEquals(expectedAccessClaims, accessClaims)
        verify(authTokenRepo, times(0)).updateDisabledAccessTokensForClient(any(), any())
    }

    @Test
    fun `getClaims from token when the current token is not disabled for the client, and some disabled token is now invalid`() =
        runBlocking {
            val token = "some_token"
            val disabledToken1 = "disabled_token_1"
            val disabledToken2 = "disabled_token_2"
            val now = Instant.now()
            val clientId = "someClientId"
            val userId = "someUserId"
            val emailId = "someEmail@ramble.com"
            val claims = mock(Claims::class.java)
            val authority = mock(SimpleGrantedAuthority::class.java)

            val allDisabledTokens = setOf(disabledToken1, disabledToken2)
            val expectedAccessClaims = AccessClaims(clientId, userId, emailId, claims, listOf(authority))
            val expectedClientAuthInfo = ClientAuthInfo(clientId, userId, token)
            val expectedDisabledTokens = setOf(disabledToken2)

            // Stub
            given(accessTokenHandler.getTokenClaims(token, jwtParser, now)).willReturn(expectedAccessClaims)
            given(authTokenRepo.getDisabledAccessTokensForClient(expectedClientAuthInfo)).willReturn(allDisabledTokens)
            given(accessTokenHandler.isValidAccessToken(disabledToken1, jwtParser, now)).willReturn(false)
            given(accessTokenHandler.isValidAccessToken(disabledToken2, jwtParser, now)).willReturn(true)

            // Call method and assert
            val accessClaims = authTokensService.getAccessTokenClaims(token, now)

            // Verify
            assertEquals(expectedAccessClaims, accessClaims)
            verify(authTokenRepo).updateDisabledAccessTokensForClient(expectedClientAuthInfo, expectedDisabledTokens)
        }

    @Test
    fun `getClaims from token when the current token is not disabled for the client`() = runBlocking {
        val token = "some_token"
        val disabledToken1 = "disabled_token_1"
        val disabledToken2 = "disabled_token_2"
        val now = Instant.now()
        val clientId = "someClientId"
        val userId = "someUserId"
        val emailId = "someEmail@ramble.com"
        val claims = mock(Claims::class.java)
        val authority = mock(SimpleGrantedAuthority::class.java)

        val allDisabledTokens = setOf(disabledToken1, disabledToken2)
        val expectedAccessClaims = AccessClaims(clientId, userId, emailId, claims, listOf(authority))
        val expectedClientAuthInfo = ClientAuthInfo(clientId, userId, token)

        // Stub
        given(accessTokenHandler.getTokenClaims(token, jwtParser, now)).willReturn(expectedAccessClaims)
        given(authTokenRepo.getDisabledAccessTokensForClient(expectedClientAuthInfo)).willReturn(allDisabledTokens)
        given(accessTokenHandler.isValidAccessToken(disabledToken1, jwtParser, now)).willReturn(true)
        given(accessTokenHandler.isValidAccessToken(disabledToken2, jwtParser, now)).willReturn(true)

        // Call method and assert
        val accessClaims = authTokensService.getAccessTokenClaims(token, now)

        // Verify
        assertEquals(expectedAccessClaims, accessClaims)
        verify(authTokenRepo, times(0)).updateDisabledAccessTokensForClient(any(), any())
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

    @Test
    fun `refreshToken should generate new authTokens and disable oldAccessToken, when there was no disabled tokens before and oldAccessToken is still valid`() =
        runBlocking<Unit> {
            val refreshToken = "someRefreshToken"
            val now = Instant.now()
            val accessTokenExpiryDurationAmount = 30L
            val accessTokenExpiryDurationUnit = ChronoUnit.MINUTES

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
            given(authTokenRepo.deleteOldAuthTokens(refreshToken)).willReturn(clientAuthInfo)
            given(authTokenRepo.getDisabledAccessTokensForClient(clientAuthInfo)).willReturn(setOf())

            given(accessTokenHandler.getClientIdFromAccessToken(accessTokenOld, jwtParser)).willReturn(clientId)
            given(accessTokenHandler.getUserIdFromAccessToken(accessTokenOld, jwtParser)).willReturn(userId)
            given(accessTokenHandler.getEmailFromAccessToken(accessTokenOld, jwtParser)).willReturn(emailId)
            given(accessTokenHandler.getRolesFromAccessToken(accessTokenOld, jwtParser)).willReturn(authorities)

            given(accessTokenHandler.isValidAccessToken(accessTokenOld, jwtParser, now)).willReturn(true)

            given(
                accessTokenHandler.generateAccessToken(
                    authorities,
                    clientId,
                    userId,
                    emailId,
                    now,
                    accessTokenExpiryDurationAmount,
                    accessTokenExpiryDurationUnit,
                    jwtBuilder
                )
            ).willReturn(newAccessToken)
            given(refreshTokenHandler.generateRefreshToken()).willReturn(newRefreshToken)

            // Call method
            val userAuthInfo = authTokensService.refreshAuthToken(
                refreshToken, now, accessTokenExpiryDurationAmount, accessTokenExpiryDurationUnit
            )

            // Verify
            assertEquals(expectedUserAuthInfo, userAuthInfo)
            verify(authTokenRepo).updateDisabledAccessTokensForClient(clientAuthInfo, setOf(accessTokenOld))
            verify(authTokenRepo).insertUserAuthInfo(
                clientId,
                UserAuthInfo(userId, emailId, newAccessToken, newRefreshToken)
            )
        }

    @Test
    fun `refreshToken should generate new authTokens and disable oldAccessToken, when there was no disabled tokens before and oldAccessToken is already invalid`() =
        runBlocking<Unit> {
            val refreshToken = "someRefreshToken"
            val now = Instant.now()
            val accessTokenExpiryDurationAmount = 30L
            val accessTokenExpiryDurationUnit = ChronoUnit.MINUTES

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
            given(authTokenRepo.deleteOldAuthTokens(refreshToken)).willReturn(clientAuthInfo)
            given(authTokenRepo.getDisabledAccessTokensForClient(clientAuthInfo)).willReturn(setOf())

            given(accessTokenHandler.getClientIdFromAccessToken(accessTokenOld, jwtParser)).willReturn(clientId)
            given(accessTokenHandler.getUserIdFromAccessToken(accessTokenOld, jwtParser)).willReturn(userId)
            given(accessTokenHandler.getEmailFromAccessToken(accessTokenOld, jwtParser)).willReturn(emailId)
            given(accessTokenHandler.getRolesFromAccessToken(accessTokenOld, jwtParser)).willReturn(authorities)

            given(accessTokenHandler.isValidAccessToken(accessTokenOld, jwtParser, now)).willReturn(false)

            given(
                accessTokenHandler.generateAccessToken(
                    authorities,
                    clientId,
                    userId,
                    emailId,
                    now,
                    accessTokenExpiryDurationAmount,
                    accessTokenExpiryDurationUnit,
                    jwtBuilder
                )
            ).willReturn(newAccessToken)
            given(refreshTokenHandler.generateRefreshToken()).willReturn(newRefreshToken)

            // Call method
            val userAuthInfo = authTokensService.refreshAuthToken(
                refreshToken, now, accessTokenExpiryDurationAmount, accessTokenExpiryDurationUnit
            )

            // Verify
            assertEquals(expectedUserAuthInfo, userAuthInfo)
            verify(authTokenRepo).updateDisabledAccessTokensForClient(
                clientAuthInfo,
                setOf()
            ) // since oldAccessToken is invalid
            verify(authTokenRepo).insertUserAuthInfo(
                clientId,
                UserAuthInfo(userId, emailId, newAccessToken, newRefreshToken)
            )
        }

    @Test
    fun `refreshToken should generate new authTokens and disable oldAccessToken, when there were disabled tokens before and all of them were valid`() =
        runBlocking<Unit> {
            val refreshToken = "someRefreshToken"
            val now = Instant.now()
            val accessTokenExpiryDurationAmount = 30L
            val accessTokenExpiryDurationUnit = ChronoUnit.MINUTES

            val accessTokenOld = "someOldAccessToken"
            val clientId = "someClientId"
            val userId = "someUserId"
            val emailId = "someEmail@ramble.com"
            val authority = mock(GrantedAuthority::class.java)
            val authorities = listOf(authority)
            val clientAuthInfo = ClientAuthInfo(clientId, userId, accessTokenOld)

            val disabledAccessToken1 = "someOldDisabledAccessToken1"
            val disabledAccessToken2 = "someOldDisabledAccessToken2"
            val disabledOldAccessTokens = setOf(disabledAccessToken1, disabledAccessToken2)
            val allExpectedDisabledTokens = disabledOldAccessTokens.plus(accessTokenOld)

            val newAccessToken = "this_is_new_generated_access_token"
            val newRefreshToken = "this_is_new_refresh_token"
            val expectedUserAuthInfo = UserAuthInfo(userId, emailId, newAccessToken, newRefreshToken)

            // Stub
            given(authTokenRepo.deleteOldAuthTokens(refreshToken)).willReturn(clientAuthInfo)
            given(authTokenRepo.getDisabledAccessTokensForClient(clientAuthInfo)).willReturn(disabledOldAccessTokens)

            given(accessTokenHandler.getClientIdFromAccessToken(accessTokenOld, jwtParser)).willReturn(clientId)
            given(accessTokenHandler.getUserIdFromAccessToken(accessTokenOld, jwtParser)).willReturn(userId)
            given(accessTokenHandler.getEmailFromAccessToken(accessTokenOld, jwtParser)).willReturn(emailId)
            given(accessTokenHandler.getRolesFromAccessToken(accessTokenOld, jwtParser)).willReturn(authorities)

            given(accessTokenHandler.isValidAccessToken(accessTokenOld, jwtParser, now)).willReturn(true)
            given(accessTokenHandler.isValidAccessToken(disabledAccessToken1, jwtParser, now)).willReturn(true)
            given(accessTokenHandler.isValidAccessToken(disabledAccessToken2, jwtParser, now)).willReturn(true)

            given(
                accessTokenHandler.generateAccessToken(
                    authorities,
                    clientId,
                    userId,
                    emailId,
                    now,
                    accessTokenExpiryDurationAmount,
                    accessTokenExpiryDurationUnit,
                    jwtBuilder
                )
            ).willReturn(newAccessToken)
            given(refreshTokenHandler.generateRefreshToken()).willReturn(newRefreshToken)

            // Call method
            val userAuthInfo = authTokensService.refreshAuthToken(
                refreshToken, now, accessTokenExpiryDurationAmount, accessTokenExpiryDurationUnit
            )

            // Verify
            assertEquals(expectedUserAuthInfo, userAuthInfo)
            verify(authTokenRepo).updateDisabledAccessTokensForClient(clientAuthInfo, allExpectedDisabledTokens)
            verify(authTokenRepo).insertUserAuthInfo(
                clientId,
                UserAuthInfo(userId, emailId, newAccessToken, newRefreshToken)
            )
        }

    @Test
    fun `refreshToken should generate new authTokens and disable oldAccessToken, when there were disabled tokens before and some of them are invalid`() =
        runBlocking<Unit> {
            val refreshToken = "someRefreshToken"
            val now = Instant.now()
            val accessTokenExpiryDurationAmount = 30L
            val accessTokenExpiryDurationUnit = ChronoUnit.MINUTES

            val accessTokenOld = "someOldAccessToken"
            val clientId = "someClientId"
            val userId = "someUserId"
            val emailId = "someEmail@ramble.com"
            val authority = mock(GrantedAuthority::class.java)
            val authorities = listOf(authority)
            val clientAuthInfo = ClientAuthInfo(clientId, userId, accessTokenOld)

            val disabledAccessToken1 = "someOldDisabledAccessToken1" // it will be stubbed below as invalid
            val disabledAccessToken2 = "someOldDisabledAccessToken2"
            val disabledOldAccessTokens = setOf(disabledAccessToken1, disabledAccessToken2)
            val allExpectedDisabledTokens =
                setOf(disabledAccessToken2, accessTokenOld) // doesn't contain disabledAccessToken1

            val newAccessToken = "this_is_new_generated_access_token"
            val newRefreshToken = "this_is_new_refresh_token"
            val expectedUserAuthInfo = UserAuthInfo(userId, emailId, newAccessToken, newRefreshToken)

            // Stub
            given(authTokenRepo.deleteOldAuthTokens(refreshToken)).willReturn(clientAuthInfo)
            given(authTokenRepo.getDisabledAccessTokensForClient(clientAuthInfo)).willReturn(disabledOldAccessTokens)

            given(accessTokenHandler.getClientIdFromAccessToken(accessTokenOld, jwtParser)).willReturn(clientId)
            given(accessTokenHandler.getUserIdFromAccessToken(accessTokenOld, jwtParser)).willReturn(userId)
            given(accessTokenHandler.getEmailFromAccessToken(accessTokenOld, jwtParser)).willReturn(emailId)
            given(accessTokenHandler.getRolesFromAccessToken(accessTokenOld, jwtParser)).willReturn(authorities)

            given(accessTokenHandler.isValidAccessToken(accessTokenOld, jwtParser, now)).willReturn(true)
            given(accessTokenHandler.isValidAccessToken(disabledAccessToken1, jwtParser, now)).willReturn(false)
            given(accessTokenHandler.isValidAccessToken(disabledAccessToken2, jwtParser, now)).willReturn(true)

            given(
                accessTokenHandler.generateAccessToken(
                    authorities,
                    clientId,
                    userId,
                    emailId,
                    now,
                    accessTokenExpiryDurationAmount,
                    accessTokenExpiryDurationUnit,
                    jwtBuilder
                )
            ).willReturn(newAccessToken)
            given(refreshTokenHandler.generateRefreshToken()).willReturn(newRefreshToken)

            // Call method
            val userAuthInfo = authTokensService.refreshAuthToken(
                refreshToken, now, accessTokenExpiryDurationAmount, accessTokenExpiryDurationUnit
            )

            // Verify
            assertEquals(expectedUserAuthInfo, userAuthInfo)
            verify(authTokenRepo).updateDisabledAccessTokensForClient(clientAuthInfo, allExpectedDisabledTokens)
            verify(authTokenRepo).insertUserAuthInfo(
                clientId,
                UserAuthInfo(userId, emailId, newAccessToken, newRefreshToken)
            )
        }

    @Test
    fun `logout when valid token and there were no disabled tokens for client`() = runBlocking {
        val now = Instant.now()
        val accessToken = "someAccessToken"

        val clientId = "someClientId"
        val userId = "someUserId"

        val clientAuthInfo = ClientAuthInfo(clientId, userId, accessToken)

        // Stub
        given(accessTokenHandler.getClientIdFromAccessToken(accessToken, jwtParser)).willReturn(clientId)
        given(accessTokenHandler.getUserIdFromAccessToken(accessToken, jwtParser)).willReturn(userId)
        given(accessTokenHandler.isValidAccessToken(accessToken, jwtParser, now)).willReturn(true)
        given(authTokenRepo.getDisabledAccessTokensForClient(clientAuthInfo)).willReturn(setOf())

        // Call method
        authTokensService.logout(accessToken, now)

        // Verify
        verify(authTokenRepo).updateDisabledAccessTokensForClient(clientAuthInfo, setOf(accessToken))
    }

    @Test
    fun `logout when valid token and there were some disabled tokens for client`() = runBlocking {
        val now = Instant.now()
        val accessToken = "someAccessToken"

        val clientId = "someClientId"
        val userId = "someUserId"

        val disabledAccessToken1 = "someOldDisabledAccessToken1" // it will be stubbed as invalid
        val disabledAccessToken2 = "someOldDisabledAccessToken2"
        val oldDisabledTokens = setOf(disabledAccessToken1, disabledAccessToken2)

        val clientAuthInfo = ClientAuthInfo(clientId, userId, accessToken)

        // Stub
        given(accessTokenHandler.getClientIdFromAccessToken(accessToken, jwtParser)).willReturn(clientId)
        given(accessTokenHandler.getUserIdFromAccessToken(accessToken, jwtParser)).willReturn(userId)
        given(accessTokenHandler.isValidAccessToken(accessToken, jwtParser, now)).willReturn(true)
        given(accessTokenHandler.isValidAccessToken(disabledAccessToken1, jwtParser, now)).willReturn(false)
        given(accessTokenHandler.isValidAccessToken(disabledAccessToken2, jwtParser, now)).willReturn(true)
        given(authTokenRepo.getDisabledAccessTokensForClient(clientAuthInfo)).willReturn(oldDisabledTokens)

        // Call method
        authTokensService.logout(accessToken, now)

        // Verify
        verify(authTokenRepo).updateDisabledAccessTokensForClient(
            clientAuthInfo,
            setOf(accessToken, disabledAccessToken2)
        )
    }

    @Test(expected = AccessTokenIsInvalidException::class)
    fun `logout when passed token is in disabled tokens list for client`() = runBlocking {
        val now = Instant.now()
        val accessToken = "someAccessToken"

        val clientId = "someClientId"
        val userId = "someUserId"

        val disabledAccessToken = "someOldDisabledAccessToken"
        val oldDisabledTokens = setOf(accessToken, disabledAccessToken) // accessToken is part of disabled list

        val clientAuthInfo = ClientAuthInfo(clientId, userId, accessToken)

        // Stub
        given(accessTokenHandler.getClientIdFromAccessToken(accessToken, jwtParser)).willReturn(clientId)
        given(accessTokenHandler.getUserIdFromAccessToken(accessToken, jwtParser)).willReturn(userId)
        given(accessTokenHandler.isValidAccessToken(accessToken, jwtParser, now)).willReturn(true)
        given(accessTokenHandler.isValidAccessToken(disabledAccessToken, jwtParser, now)).willReturn(true)
        given(authTokenRepo.getDisabledAccessTokensForClient(clientAuthInfo)).willReturn(oldDisabledTokens)

        // Call method
        authTokensService.logout(accessToken, now)
    }

    @Test(expected = AccessTokenIsInvalidException::class)
    fun `logout should throw AccessTokenIsInvalidException if clientId cannot be obtained from token`() = runBlocking {
        val now = Instant.now()
        val accessToken = "someAccessToken"
        val userId = "someUserId"

        // Stub
        given(accessTokenHandler.getClientIdFromAccessToken(accessToken, jwtParser)).willReturn(null)
        given(accessTokenHandler.getUserIdFromAccessToken(accessToken, jwtParser)).willReturn(userId)

        // Call method
        authTokensService.logout(accessToken, now)
    }

    @Test(expected = AccessTokenIsInvalidException::class)
    fun `logout should throw AccessTokenIsInvalidException if userId cannot be obtained from token`() = runBlocking {
        val now = Instant.now()
        val accessToken = "someAccessToken"
        val clientId = "someClientId"

        // Stub
        given(accessTokenHandler.getUserIdFromAccessToken(accessToken, jwtParser)).willReturn(null)
        given(accessTokenHandler.getClientIdFromAccessToken(accessToken, jwtParser)).willReturn(clientId)

        // Call method
        authTokensService.logout(accessToken, now)
    }
}