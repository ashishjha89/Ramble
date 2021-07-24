package com.ramble.identity.controller

import com.ramble.identity.common.Result
import com.ramble.identity.common.userSuspendedError
import com.ramble.identity.models.UserInfo
import com.ramble.identity.service.UserInfoService
import org.junit.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito.mock
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import java.security.Principal
import kotlin.test.assertEquals

class IdentityControllerTest {

    private val userInfoService = mock(UserInfoService::class.java)

    private val identityController = IdentityController(userInfoService)

    @Test
    fun `getMyInfo should send user if user was successfully fetched`() {
        val principal = mock(Principal::class.java)
        val userInfo = mock(UserInfo::class.java)
        val userInfoResult = Result.Success(data = userInfo)
        val expectedResponse = ResponseEntity(userInfo, HttpStatus.OK)

        // Stub
        given(userInfoService.getUserInfoResult(principal)).willReturn(userInfoResult)

        // Call method and assert
        assertEquals(expectedResponse, identityController.getMyInfo(principal))
    }

    @Test
    fun `getMyInfo should send error if user fetching failed`() {
        val principal = mock(Principal::class.java)
        val userInfoResultError = Result.Error<UserInfo>(HttpStatus.FORBIDDEN, userSuspendedError)
        val expectedResponse = ResponseEntity(userInfoResultError.errorBody, userInfoResultError.httpStatus)

        // Stub
        given(userInfoService.getUserInfoResult(principal)).willReturn(userInfoResultError)

        // Call method and assert
        assertEquals(expectedResponse, identityController.getMyInfo(principal))
    }
}