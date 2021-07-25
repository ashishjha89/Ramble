package com.ramble.identity.controller

import com.ramble.identity.common.Result
import com.ramble.identity.common.refreshTokenInvalid
import com.ramble.identity.common.userAlreadyActivatedError
import com.ramble.identity.models.LoginResponse
import com.ramble.identity.models.RefreshTokenRequest
import com.ramble.identity.models.RegisterUserRequest
import com.ramble.identity.models.RegisteredUserResponse
import com.ramble.identity.service.UserInfoService
import com.ramble.identity.service.UserRegistrationService
import org.junit.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito.mock
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import kotlin.test.assertEquals

class AuthControllerTest {

    private val userInfoService = mock(UserInfoService::class.java)
    private val userRegistrationService = mock(UserRegistrationService::class.java)

    private val authController = AuthController(userInfoService, userRegistrationService)

    @Test
    fun `signUp should send registeredUserResponse if user was successfully saved`() {
        val registerUserRequest = mock(RegisterUserRequest::class.java)
        val registeredUserResponse = mock(RegisteredUserResponse::class.java)
        val saveUserResult = Result.Success(data = registeredUserResponse)
        val expectedResponse = ResponseEntity(registeredUserResponse, HttpStatus.OK)

        // Stub
        given(userRegistrationService.saveUser(registerUserRequest)).willReturn(saveUserResult)

        // Call method and assert
        assertEquals(expectedResponse, authController.signUp(registerUserRequest))
    }

    @Test
    fun `signUp should send error if user save failed`() {
        val registerUserRequest = mock(RegisterUserRequest::class.java)
        val saveUserError = Result.Error<RegisteredUserResponse>(HttpStatus.FORBIDDEN, userAlreadyActivatedError)
        val expectedResponse = ResponseEntity(saveUserError.errorBody, saveUserError.httpStatus)

        // Stub
        given(userRegistrationService.saveUser(registerUserRequest)).willReturn(saveUserError)

        // Call method and assert
        assertEquals(expectedResponse, authController.signUp(registerUserRequest))
    }

    @Test
    fun `confirmRegistration should send registrationToken if user was successfully registered`() {
        val confirmRegistrationToken = "some_confirm_registration_token"
        val registeredUserResponse = mock(RegisteredUserResponse::class.java)
        val saveUserResult = Result.Success(data = registeredUserResponse)
        val expectedResponse = ResponseEntity(registeredUserResponse, HttpStatus.OK)

        // Stub
        given(userRegistrationService.confirmToken(confirmRegistrationToken)).willReturn(saveUserResult)

        // Call method and assert
        assertEquals(expectedResponse, authController.confirmRegistration(confirmRegistrationToken))
    }

    @Test
    fun `confirmRegistration send error if user registration failed`() {
        val confirmRegistrationToken = "some_confirm_registration_token"
        val saveUserError = Result.Error<RegisteredUserResponse>(HttpStatus.FORBIDDEN, userAlreadyActivatedError)
        val expectedResponse = ResponseEntity(saveUserError.errorBody, saveUserError.httpStatus)

        // Stub
        given(userRegistrationService.confirmToken(confirmRegistrationToken)).willReturn(saveUserError)

        // Call method and assert
        assertEquals(expectedResponse, authController.confirmRegistration(confirmRegistrationToken))
    }

    @Test
    fun `refreshToken should send new LoginResponse if successful`() {
        val refreshTokenRequest = RefreshTokenRequest(refreshToken = "someRefreshToken")
        val loginResponse = mock(LoginResponse::class.java)
        val refreshTokenResult = Result.Success(data = loginResponse)
        val expectedResponse = ResponseEntity(loginResponse, HttpStatus.OK)

        // Stub
        given(userInfoService.refreshToken(refreshTokenRequest)).willReturn(refreshTokenResult)

        // Call method and assert
        assertEquals(expectedResponse, authController.refreshToken(refreshTokenRequest))
    }

    @Test
    fun `refreshToken send error if failed`() {
        val refreshTokenRequest = RefreshTokenRequest(refreshToken = "someRefreshToken")
        val refreshTokenError = Result.Error<LoginResponse>(HttpStatus.FORBIDDEN, refreshTokenInvalid)
        val expectedResponse = ResponseEntity(refreshTokenError.errorBody, refreshTokenError.httpStatus)

        // Stub
        given(userInfoService.refreshToken(refreshTokenRequest)).willReturn(refreshTokenError)

        // Call method and assert
        assertEquals(expectedResponse, authController.refreshToken(refreshTokenRequest))
    }
}