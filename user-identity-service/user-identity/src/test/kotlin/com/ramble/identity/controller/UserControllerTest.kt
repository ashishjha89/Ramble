package com.ramble.identity.controller

import com.ramble.identity.models.UserInfo
import com.ramble.identity.models.UserProfile
import com.ramble.identity.service.UserInfoService
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito.mock
import java.security.Principal
import kotlin.test.assertEquals

class UserControllerTest {

    private val userInfoService = mock(UserInfoService::class.java)

    private val userController = UserController(userInfoService)

    @Test
    fun `getMyInfo should send user if user was successfully fetched`() = runBlocking {
        val principal = mock(Principal::class.java)
        val userInfo = mock(UserInfo::class.java)

        // Stub
        given(userInfoService.getMyUserInfo(principal)).willReturn(userInfo)

        // Call method and assert
        assertEquals(userInfo, userController.getMyInfo(principal))
    }

    @Test
    fun `getUserProfile should send profile if user was successfully fetched`() = runBlocking {
        val emailId = "someEmailId"
        val userProfile = mock(UserProfile::class.java)

        // Stub
        given(userInfoService.getUserProfile(emailId)).willReturn(userProfile)

        // Call method and assert
        assertEquals(userProfile, userController.getUserProfile(emailId))
    }
}