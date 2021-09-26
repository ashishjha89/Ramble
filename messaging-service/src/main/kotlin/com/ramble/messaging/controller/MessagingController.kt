package com.ramble.messaging.controller

import com.ramble.messaging.common.AUTHORIZATION_HEADER
import com.ramble.messaging.common.BEARER
import com.ramble.messaging.common.ErrorBody
import com.ramble.messaging.common.ErrorCode.INVALID_USER_EMAIL
import com.ramble.messaging.common.ErrorCode.SOMETHING_WENT_WRONG
import com.ramble.messaging.common.ErrorCode.UNAUTHORIZED_ACCESS
import com.ramble.messaging.common.ErrorCode.USER_INFO_NOT_FOUND
import com.ramble.messaging.common.MESSAGING_API_BASE_PATH
import com.ramble.messaging.model.AccessTokenIsInvalidException
import com.ramble.messaging.model.UnauthorizedException
import com.ramble.messaging.model.UserProfile
import com.ramble.messaging.service.UserService
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(MESSAGING_API_BASE_PATH)
class MessagingController(private val userService: UserService) {

    private val logger = LoggerFactory.getLogger(MessagingController::class.java)

    @ApiResponses(
        value = [
            ApiResponse(responseCode = OK, content = []),
            ApiResponse(
                responseCode = BAD_REQUEST,
                description = "errorCode: $INVALID_USER_EMAIL",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorBody::class))]
            ),
            ApiResponse(
                responseCode = NOT_FOUND,
                description = "errorCode: $USER_INFO_NOT_FOUND",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorBody::class))]
            ),
            ApiResponse(
                responseCode = FORBIDDEN,
                description = "errorCode: $UNAUTHORIZED_ACCESS",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorBody::class))]
            ),
            ApiResponse(
                responseCode = INTERNAL_SERVER_ERROR,
                description = "errorCode: $SOMETHING_WENT_WRONG",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorBody::class))]
            )]
    )
    @GetMapping("/user-from-chat")
    suspend fun getUserFromMessaging(
        @RequestHeader(name = AUTHORIZATION_HEADER) authorizationHeader: String?
    ): UserProfile {
        logger.info("MessagingController getUserFromMessaging")
        if (authorizationHeader.isNullOrBlank()) throw UnauthorizedException()
        val accessToken = getTokenFromBearerHeader(authorizationHeader) ?: throw AccessTokenIsInvalidException()
        return userService.getUserProfile(accessToken)
    }

    private fun getTokenFromBearerHeader(bearerStr: String): String? {
        if (!bearerStr.startsWith(BEARER)) return null
        return bearerStr.substring(BEARER.length)
    }
}