package com.ramble.identity.controller

import com.ramble.identity.models.LoginResponse
import com.ramble.identity.models.RefreshTokenRequest
import com.ramble.identity.models.RegisterUserRequest
import com.ramble.identity.models.RegisteredUserResponse
import com.ramble.identity.service.UserInfoService
import com.ramble.identity.service.UserRegistrationService
import com.ramble.token.model.AccessTokenIsInvalidException
import kotlinx.coroutines.runBlocking
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
    fun `logout should send success if successfully logged out`() = runBlocking {
        val accessToken = "some-access-token"
        val authTokenHeader = "Bearer $accessToken"

        // Call method
        authController.logout(authTokenHeader)

        verify(userInfoService).logout(accessToken)
    }

    @Test(expected = AccessTokenIsInvalidException::class)
    fun `logout should throw AccessTokenIsInvalidException if userInfoService throws AccessTokenIsInvalidException`() =
        runBlocking {
            val accessToken = "some-access-token"
            val authTokenHeader = "Bearer $accessToken"

            // Stub
            given(userInfoService.logout(accessToken)).willThrow(AccessTokenIsInvalidException())

            // Call method
            authController.logout(authTokenHeader)
        }

    @Test(expected = AccessTokenIsInvalidException::class)
    fun `logout should throw AccessTokenIsInvalidException if header does not start with Bearer`() =
        runBlocking {
            authController.logout("some-access-token")
        }

    @Test
    fun `refreshToken should send new LoginResponse if successful`() = runBlocking<Unit> {
        val refreshTokenRequest = RefreshTokenRequest(refreshToken = "someRefreshToken")
        val loginResponse = mock(LoginResponse::class.java)

        // Stub
        given(userInfoService.refreshToken(refreshTokenRequest)).willReturn(loginResponse)

        // Call method and assert
        assertEquals(loginResponse, authController.refreshToken(refreshTokenRequest))
    }
}