package com.ramble.identity.service

import com.ramble.identity.common.invalidUserId
import com.ramble.identity.models.*
import com.ramble.identity.repo.UserRepo
import com.ramble.identity.utils.TimeAndIdGenerator
import com.ramble.token.AuthTokensService
import com.ramble.token.model.AccessClaims
import com.ramble.token.model.AccessTokenIsInvalidException
import com.ramble.token.model.RefreshTokenIsInvalidException
import com.ramble.token.model.UserAuthInfo
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.BDDMockito.given
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UsernameNotFoundException
import java.security.Principal
import java.time.Instant
import kotlin.test.assertEquals

class UserInfoServiceTest {

    private val userRepo = mock(UserRepo::class.java)
    private val authTokensService = mock(AuthTokensService::class.java)

    private val accessClaims = mock(AccessClaims::class.java)
    private val userInfo = mock(UserInfo::class.java)

    private val applicationUser = mock(ApplicationUser::class.java)
    private val password = "somePassword"
    private val emailId = "someEmailId"
    private val grantedAuthorities = setOf(SimpleGrantedAuthority("user"))
    private val timeAndIdGenerator = mock(TimeAndIdGenerator::class.java)

    private val userInfoService = UserInfoService(userRepo, authTokensService, timeAndIdGenerator)

    @Test
    fun `getUserInfoResult should return user if valid principal`() {
        val principal = mock(Principal::class.java)

        // Stub
        given(authTokensService.getAccessTokenClaims(principal)).willReturn(accessClaims)
        given(accessClaims.email).willReturn(emailId)
        given(userRepo.getUserInfo(emailId)).willReturn(userInfo)

        // Call method and assert
        val result = userInfoService.getUserInfoResult(principal)
        assertEquals(userInfo, result)
    }

    @Test(expected = UserNotFoundException::class)
    fun `getUserInfoResult should throw UserNotFoundException if claims is missing principal`() {
        val principal = mock(Principal::class.java)

        // Stub
        given(authTokensService.getAccessTokenClaims(principal)).willReturn(null)

        // Call method and assert that exception is thrown
        userInfoService.getUserInfoResult(principal)
    }

    @Test
    fun `loadUserByUsername should return SpringUser if repo returns user`() {
        given(applicationUser.email).willReturn(emailId)
        given(applicationUser.password).willReturn(password)
        given(applicationUser.grantedAuthorities).willReturn(grantedAuthorities)

        // Stub
        given(userRepo.getApplicationUser(emailId)).willReturn(applicationUser)

        // Call method and assert
        val userDetailsResult = userInfoService.loadUserByUsername(emailId)
        assertEquals(emailId, userDetailsResult.username)
        assertEquals(password, userDetailsResult.password)
        assertEquals(grantedAuthorities, userDetailsResult.authorities)
    }

    @Test
    fun `loadUserByUsername should throw UsernameNotFoundException(invalidUserId) if repo cannot find user`() {
        given(applicationUser.email).willReturn(emailId)
        given(applicationUser.password).willReturn(password)
        given(applicationUser.grantedAuthorities).willReturn(grantedAuthorities)

        // Stub
        given(userRepo.getApplicationUser(emailId)).willReturn(null)

        // Call method and assert
        val exception: UsernameNotFoundException = assertThrows {
            userInfoService.loadUserByUsername(emailId)
        }
        assertEquals(invalidUserId.errorMessage, exception.message)
    }

    @Test
    fun `refreshToken should return LoginResponse result if auth-token-lib refreshes token successfully`() =
        runBlocking<Unit> {
            val refreshTokenStr = "someRefreshToken"
            val refreshTokenRequest = RefreshTokenRequest(refreshToken = refreshTokenStr)
            val now = Instant.now()

            val userId = "someUserId"
            val emailId = "someEmailId@ramble.com"
            val newAccessToken = "this_is_new_access_token"
            val newRefreshToken = "this_is_new_refresh_token"

            val userAuthInfo = UserAuthInfo(userId, emailId, newAccessToken, newRefreshToken)
            val expectedLoginResponse = LoginResponse(userId, newAccessToken, newRefreshToken)

            // Stub
            given(timeAndIdGenerator.getCurrentTime()).willReturn(now)
            given(authTokensService.refreshAuthToken(refreshTokenStr, now)).willReturn(userAuthInfo)

            // Call method
            val result = userInfoService.refreshToken(refreshTokenRequest)
            assertEquals(expectedLoginResponse, result)
        }

    @Test(expected = RefreshTokenIsInvalidException::class)
    fun `refreshToken should throw RefreshTokenIsInvalidException if auth-token-lib throws RefreshTokenIsInvalidException`() =
        runBlocking<Unit> {
            val refreshTokenStr = "someRefreshToken"
            val refreshTokenRequest = RefreshTokenRequest(refreshToken = refreshTokenStr)
            val now = Instant.now()

            // Stub
            given(timeAndIdGenerator.getCurrentTime()).willReturn(now)
            given(authTokensService.refreshAuthToken(refreshTokenStr, now)).willThrow(RefreshTokenIsInvalidException())

            // Call method
            userInfoService.refreshToken(refreshTokenRequest)
        }

    @Test
    fun `logout should return success if logged-out successfully by auth-token-lib`() {
        val now = Instant.now()
        val accessToken = "someAccessToken"

        // Stub
        given(timeAndIdGenerator.getCurrentTime()).willReturn(now)

        // Call method
        userInfoService.logout(accessToken)

        // Verify
        verify(authTokensService).logout(accessToken, now)
    }

    @Test(expected = AccessTokenIsInvalidException::class)
    fun `logout should throw AccessTokenIsInvalidException if logout failed by auth-token-lib`() {
        val now = Instant.now()
        val accessToken = "someAccessToken"

        // Stub
        given(timeAndIdGenerator.getCurrentTime()).willReturn(now)
        given(authTokensService.logout(accessToken, now)).willThrow(AccessTokenIsInvalidException())

        // Call method
        userInfoService.logout(accessToken)
    }
}