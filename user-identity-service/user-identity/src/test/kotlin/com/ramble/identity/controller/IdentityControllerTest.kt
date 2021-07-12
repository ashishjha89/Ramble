package com.ramble.identity.controller

import com.ramble.identity.common.Result
import com.ramble.identity.common.userAlreadyActivatedError
import com.ramble.identity.common.userSuspendedError
import com.ramble.identity.models.RegisterUserRequest
import com.ramble.identity.models.RegisteredUserResponse
import com.ramble.identity.models.UserInfo
import com.ramble.identity.service.UserInfoService
import com.ramble.identity.service.UserRegistrationService
import org.junit.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito.mock
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import java.security.Principal
import kotlin.test.assertEquals

class IdentityControllerTest {

    private val userInfoService = mock(UserInfoService::class.java)
    private val userRegistrationService = mock(UserRegistrationService::class.java)

    private val identityController = IdentityController(userInfoService, userRegistrationService)

    @Test
    fun `signUp should send registeredUserResponse if user was successfully saved`() {
        val registerUserRequest = mock(RegisterUserRequest::class.java)
        val registeredUserResponse = mock(RegisteredUserResponse::class.java)
        val saveUserResult = Result.Success(data = registeredUserResponse)
        val expectedResponse = ResponseEntity(registeredUserResponse, HttpStatus.OK)

        // Stub
        given(userRegistrationService.saveUser(registerUserRequest)).willReturn(saveUserResult)

        // Call method and assert
        assertEquals(expectedResponse, identityController.signUp(registerUserRequest))
    }

    @Test
    fun `signUp should send error if user save failed`() {
        val registerUserRequest = mock(RegisterUserRequest::class.java)
        val saveUserError = Result.Error<RegisteredUserResponse>(HttpStatus.FORBIDDEN, userAlreadyActivatedError)
        val expectedResponse = ResponseEntity(saveUserError.errorBody, saveUserError.httpStatus)

        // Stub
        given(userRegistrationService.saveUser(registerUserRequest)).willReturn(saveUserError)

        // Call method and assert
        assertEquals(expectedResponse, identityController.signUp(registerUserRequest))
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
        assertEquals(expectedResponse, identityController.confirmRegistration(confirmRegistrationToken))
    }

    @Test
    fun `confirmRegistration send error if user registration failed`() {
        val confirmRegistrationToken = "some_confirm_registration_token"
        val saveUserError = Result.Error<RegisteredUserResponse>(HttpStatus.FORBIDDEN, userAlreadyActivatedError)
        val expectedResponse = ResponseEntity(saveUserError.errorBody, saveUserError.httpStatus)

        // Stub
        given(userRegistrationService.confirmToken(confirmRegistrationToken)).willReturn(saveUserError)

        // Call method and assert
        assertEquals(expectedResponse, identityController.confirmRegistration(confirmRegistrationToken))
    }

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