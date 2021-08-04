package com.ramble.identity.controller

import com.ramble.identity.common.USER_INFO_API_BASE_PATH
import com.ramble.identity.common.USER_INFO_ME_PATH
import com.ramble.identity.models.UserInfo
import com.ramble.identity.service.UserInfoService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.security.Principal

@RestController
@RequestMapping(USER_INFO_API_BASE_PATH)
class UserController(private val userInfoService: UserInfoService) {

    @GetMapping(USER_INFO_ME_PATH)
    fun getMyInfo(principal: Principal): UserInfo =
            userInfoService.getUserInfoResult(principal)

}
