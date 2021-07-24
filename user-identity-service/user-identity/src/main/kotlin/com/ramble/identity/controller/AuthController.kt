package com.ramble.identity.controller

import com.ramble.identity.common.AUTH_API_BASE_PATH
import com.ramble.identity.common.REFRESH_TOKEN_PATH
import com.ramble.identity.common.USER_REGISTER_PATH
import com.ramble.identity.common.USER_REGISTRATION_CONFIRM_PATH
import com.ramble.identity.models.RefreshTokenRequest
import com.ramble.identity.models.RegisterUserRequest
import com.ramble.identity.service.UserInfoService
import com.ramble.identity.service.UserRegistrationService
import com.ramble.identity.utils.toResponseEntity
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(AUTH_API_BASE_PATH)
class AuthController(private val userInfoService: UserInfoService,
                     private val userRegistrationService: UserRegistrationService) {

    @PostMapping(USER_REGISTER_PATH)
    fun signUp(@RequestBody user: RegisterUserRequest): ResponseEntity<*> =
            userRegistrationService.saveUser(user).toResponseEntity()

    @GetMapping(USER_REGISTRATION_CONFIRM_PATH)
    fun confirmRegistration(@RequestParam(value = "token") token: String): ResponseEntity<*> =
            userRegistrationService.confirmToken(token).toResponseEntity()

    @PostMapping(REFRESH_TOKEN_PATH)
    fun refreshToken(@RequestBody refreshTokenRequest: RefreshTokenRequest): ResponseEntity<*> =
            userInfoService.refreshToken(refreshTokenRequest).toResponseEntity()
}