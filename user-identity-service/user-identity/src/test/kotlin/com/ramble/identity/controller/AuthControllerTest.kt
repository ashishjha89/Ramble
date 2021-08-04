package com.ramble.identity.controller

import com.ramble.identity.models.LoginResponse
import com.ramble.identity.models.RefreshTokenRequest
import com.ramble.identity.models.RegisterUserRequest
import com.ramble.identity.models.RegisteredUserResponse
import com.ramble.identity.service.UserInfoService
import com.ramble.identity.service.UserRegistrationService
import com.ramble.token.model.AccessTokenIsInvalidException
import org.junit.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import kotlin.test.assertEquals

class AuthControllerTest {

    private val userInfoService = mock(UserInfoService::class.java)

    private val userRegistrationService = mock(UserRegistrationService::class.java)

    private val authController = AuthController(userInfoService, userRegistrationService)

    @Test
    fun `signUp should send registeredUserResponse if user was successfully saved`() {
        val registerUserRequest = mock(RegisterUserRequest::class.java)
        val registeredUserResponse = mock(RegisteredUserResponse::class.java)

        // Stub
        given(userRegistrationService.saveUser(registerUserRequest)).willReturn(registeredUserResponse)

        // Call method and assert
        assertEquals(registeredUserResponse, authController.signUp(registerUserRequest))
    }

    @Test
    fun `confirmRegistration should send registrationToken if user was successfully registered`() {
        val confirmRegistrationToken = "some_confirm_registration_token"
        val registeredUserResponse = mock(RegisteredUserResponse::class.java)

        // Stub
        given(userRegistrationService.confirmToken(confirmRegistrationToken)).willReturn(registeredUserResponse)

        // Call method and assert
        assertEquals(registeredUserResponse, authController.confirmRegistration(confirmRegistrationToken))
    }

    @Test
    fun `logout should send success if successfully logged out`() {
        val accessToken = "some-access-token"

        // Call method
        authController.logout(accessToken)

        verify(userInfoService).logout(accessToken)
    }

    @Test(expected = AccessTokenIsInvalidException::class)
    fun `logout should throw AccessTokenIsInvalidException if userInfoService throws AccessTokenIsInvalidException`() {
        val accessToken = "some-access-token"

        // Stub
        given(userInfoService.logout(accessToken)).willThrow(AccessTokenIsInvalidException())

        // Call method
        authController.logout(accessToken)
    }

    @Test
    fun `refreshToken should send new LoginResponse if successful`() {
        val refreshTokenRequest = RefreshTokenRequest(refreshToken = "someRefreshToken")
        val loginResponse = mock(LoginResponse::class.java)

        // Stub
        given(userInfoService.refreshToken(refreshTokenRequest)).willReturn(loginResponse)

        // Call method and assert
        assertEquals(loginResponse, authController.refreshToken(refreshTokenRequest))
    }
}