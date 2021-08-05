package com.ramble.identity.controller

import com.ramble.identity.common.*
import com.ramble.identity.models.*
import com.ramble.identity.service.UserInfoService
import com.ramble.identity.service.UserRegistrationService
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping(AUTH_API_BASE_PATH)
class AuthController(
        private val userInfoService: UserInfoService,
        private val userRegistrationService: UserRegistrationService) {

    @PostMapping(USER_REGISTER_PATH)
    fun signUp(@RequestBody user: RegisterUserRequest): RegisteredUserResponse =
            userRegistrationService.saveUser(user)

    @PostMapping(LOGOUT_PATH)
    fun logout(@RequestHeader(name = AUTHORIZATION_HEADER) accessToken: String) =
            userInfoService.logout(accessToken)

    @PostMapping(REFRESH_TOKEN_PATH)
    fun refreshToken(@RequestBody refreshTokenRequest: RefreshTokenRequest): LoginResponse =
            userInfoService.refreshToken(refreshTokenRequest)

    @GetMapping(USER_REGISTRATION_CONFIRM_PATH)
    fun confirmRegistration(@RequestParam(value = "token") token: String): RegisteredUserResponse =
            userRegistrationService.confirmToken(token)

    // Check AuthenticationFilter
    @PostMapping(LOGIN_PATH)
    fun fakeLogin(
            @RequestHeader(name = CLIENT_ID_HEADER) clientIdHeader: String,
            @RequestBody loginUserRequest: LoginUserRequest
    ): LoginResponse {
        throw IllegalStateException("This method won't be called. Login is overridden by Spring Security filters.")
    }

}