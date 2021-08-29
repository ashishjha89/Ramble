package com.ramble.identity.controller

import com.ramble.identity.common.ErrorBody
import com.ramble.identity.common.ErrorCode.SOMETHING_WENT_WRONG
import com.ramble.identity.common.ErrorCode.USER_INFO_NOT_FOUND
import com.ramble.identity.common.ErrorCode.USER_IS_SUSPENDED
import com.ramble.identity.common.ErrorCode.USER_NOT_ACTIVATED
import com.ramble.identity.common.USER_INFO_API_BASE_PATH
import com.ramble.identity.common.USER_INFO_ME_PATH
import com.ramble.identity.common.USER_PROFILE_PATH
import com.ramble.identity.models.UserInfo
import com.ramble.identity.models.UserProfile
import com.ramble.identity.repo.Email
import com.ramble.identity.service.UserInfoService
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.security.Principal

@RestController
@RequestMapping(USER_INFO_API_BASE_PATH)
class UserController(private val userInfoService: UserInfoService) {

    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = OK,
                content = [Content(mediaType = "application/json", schema = Schema(implementation = UserInfo::class))
                ]
            ),
            ApiResponse(
                responseCode = BAD_REQUEST,
                description = "errorCode: $USER_INFO_NOT_FOUND",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorBody::class))]
            ),
            ApiResponse(
                responseCode = INTERNAL_SERVER_ERROR,
                description = "errorCode: $SOMETHING_WENT_WRONG",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorBody::class))]
            )]
    )
    @GetMapping(USER_INFO_ME_PATH)
    suspend fun getMyInfo(principal: Principal): UserInfo =
        userInfoService.getMyUserInfo(principal)

    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = OK,
                content = [Content(mediaType = "application/json", schema = Schema(implementation = UserProfile::class))
                ]
            ),
            ApiResponse(
                responseCode = BAD_REQUEST,
                description = "errorCode: $USER_INFO_NOT_FOUND",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorBody::class))]
            ),
            ApiResponse(
                responseCode = BAD_REQUEST,
                description = "errorCode: $USER_IS_SUSPENDED",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorBody::class))]
            ),
            ApiResponse(
                responseCode = BAD_REQUEST,
                description = "errorCode: $USER_NOT_ACTIVATED",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorBody::class))]
            ),
            ApiResponse(
                responseCode = INTERNAL_SERVER_ERROR,
                description = "errorCode: $SOMETHING_WENT_WRONG",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorBody::class))]
            )]
    )
    @GetMapping("$USER_PROFILE_PATH/{email}")
    suspend fun getUserProfile(@PathVariable email: Email): UserProfile =
        userInfoService.getUserProfile(email)

}
