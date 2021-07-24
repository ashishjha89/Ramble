package com.ramble.identity.controller

import com.ramble.identity.common.*
import com.ramble.identity.models.RefreshTokenRequest
import com.ramble.identity.models.RegisterUserRequest
import com.ramble.identity.service.UserInfoService
import com.ramble.identity.service.UserRegistrationService
import com.ramble.identity.utils.toResponseEntity
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.security.Principal

@RestController
@RequestMapping(USER_INFO_API_BASE_PATH)
class IdentityController(private val userInfoService: UserInfoService) {

    @GetMapping(USER_INFO_ME_PATH)
    fun getMyInfo(principal: Principal): ResponseEntity<*> =
            userInfoService.getUserInfoResult(principal).toResponseEntity()

}
