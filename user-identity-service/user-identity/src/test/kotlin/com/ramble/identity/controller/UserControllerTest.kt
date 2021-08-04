package com.ramble.identity.controller

import com.ramble.identity.models.UserInfo
import com.ramble.identity.service.UserInfoService
import org.junit.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito.mock
import java.security.Principal
import kotlin.test.assertEquals

class UserControllerTest {

    private val userInfoService = mock(UserInfoService::class.java)

    private val userController = UserController(userInfoService)

    @Test
    fun `getMyInfo should send user if user was successfully fetched`() {
        val principal = mock(Principal::class.java)
        val userInfo = mock(UserInfo::class.java)

        // Stub
        given(userInfoService.getUserInfoResult(principal)).willReturn(userInfo)

        // Call method and assert
        assertEquals(userInfo, userController.getMyInfo(principal))
    }
}