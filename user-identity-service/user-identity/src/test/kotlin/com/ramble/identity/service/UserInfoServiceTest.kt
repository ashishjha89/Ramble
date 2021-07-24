package com.ramble.identity.service

import com.ramble.identity.common.*
import com.ramble.identity.models.*
import com.ramble.identity.repo.UserRepo
import com.ramble.identity.utils.TimeAndIdGenerator
import com.ramble.token.AuthTokensService
import com.ramble.token.model.AccessClaims
import org.junit.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.BDDMockito.given
import org.mockito.Mockito.mock
import org.springframework.http.HttpStatus
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UsernameNotFoundException
import java.security.Principal
import kotlin.test.assertEquals
import kotlin.test.assertTrue

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
        assertTrue(result is Result.Success)
        assertEquals(userInfo, result.data)
    }

    @Test
    fun `getUserInfoResult should return badRequest if claims is missing principal`() {
        val principal = mock(Principal::class.java)

        // Stub
        given(authTokensService.getAccessTokenClaims(principal)).willReturn(null)

        // Call method and assert
        val result = userInfoService.getUserInfoResult(principal)
        assertTrue(result is Result.Error)
        assertEquals(HttpStatus.BAD_REQUEST, result.httpStatus)
        assertEquals(userInfoNotFound, result.errorBody)
    }

    @Test
    fun `getUserInfoResult should return badRequest if claims did not contain email`() {
        val principal = mock(Principal::class.java)

        // Stub
        given(authTokensService.getAccessTokenClaims(principal)).willReturn(accessClaims)
        given(accessClaims.email).willReturn(null)

        // Call method and assert
        val result = userInfoService.getUserInfoResult(principal)
        assertTrue(result is Result.Error)
        assertEquals(HttpStatus.BAD_REQUEST, result.httpStatus)
        assertEquals(userInfoNotFound, result.errorBody)
    }

    @Test
    fun `getUserInfoResult should return badRequestError if userRepo throw UserNotFoundException`() {
        val principal = mock(Principal::class.java)

        // Stub
        given(authTokensService.getAccessTokenClaims(principal)).willReturn(accessClaims)
        given(accessClaims.email).willReturn(emailId)
        given(userRepo.getUserInfo(emailId)).willThrow(UserNotFoundException())

        // Call method and assert
        val result = userInfoService.getUserInfoResult(principal)
        assertTrue(result is Result.Error)
        assertEquals(HttpStatus.BAD_REQUEST, result.httpStatus)
        assertEquals(userInfoNotFound, result.errorBody)
    }

    @Test
    fun `getUserInfoResult should return userSuspendedError if userRepo throw UserSuspendedException`() {
        val principal = mock(Principal::class.java)

        // Stub
        given(authTokensService.getAccessTokenClaims(principal)).willReturn(accessClaims)
        given(accessClaims.email).willReturn(emailId)
        given(userRepo.getUserInfo(emailId)).willThrow(UserSuspendedException())

        // Call method and assert
        val result = userInfoService.getUserInfoResult(principal)
        assertTrue(result is Result.Error)
        assertEquals(HttpStatus.FORBIDDEN, result.httpStatus)
        assertEquals(userSuspendedError, result.errorBody)
    }

    @Test
    fun `getUserInfoResult should return userNotActivatedError if userRepo throw UserNotActivatedException`() {
        val principal = mock(Principal::class.java)

        // Stub
        given(authTokensService.getAccessTokenClaims(principal)).willReturn(accessClaims)
        given(accessClaims.email).willReturn(emailId)
        given(userRepo.getUserInfo(emailId)).willThrow(UserNotActivatedException())

        // Call method and assert
        val result = userInfoService.getUserInfoResult(principal)
        assertTrue(result is Result.Error)
        assertEquals(HttpStatus.FORBIDDEN, result.httpStatus)
        assertEquals(userNotActivatedError, result.errorBody)
    }

    @Test
    fun `getUserInfoResult should return internalServerError if userRepo throw unknown exception`() {
        val principal = mock(Principal::class.java)

        // Stub
        given(authTokensService.getAccessTokenClaims(principal)).willReturn(accessClaims)
        given(accessClaims.email).willReturn(emailId)
        given(userRepo.getUserInfo(emailId)).willThrow(IllegalStateException("random exception"))

        // Call method and assert
        val result = userInfoService.getUserInfoResult(principal)
        assertTrue(result is Result.Error)
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.httpStatus)
        assertEquals(internalServerError, result.errorBody)
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
    fun `loadUserByUsername should throw UsernameNotFoundException(internalServerError) if repo cannot find user`() {
        // Stub
        given(userRepo.getApplicationUser(emailId)).willThrow(IllegalStateException("some exception"))

        // Call method and assert
        val exception: UsernameNotFoundException = assertThrows {
            userInfoService.loadUserByUsername(emailId)
        }
        assertEquals(internalServerError.errorMessage, exception.message)
    }
}