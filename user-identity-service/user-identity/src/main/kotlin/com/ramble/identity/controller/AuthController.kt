package com.ramble.identity.controller

import com.ramble.identity.common.*
import com.ramble.identity.common.ErrorCode.CLIENT_ID_HEADER_MISSING
import com.ramble.identity.common.ErrorCode.EMAIL_IS_INVALID
import com.ramble.identity.common.ErrorCode.EMAIL_SENDING_FAILED
import com.ramble.identity.common.ErrorCode.REFRESH_TOKEN_IS_INVALID
import com.ramble.identity.common.ErrorCode.UNAUTHORIZED_ACCESS
import com.ramble.identity.common.ErrorCode.USER_ALREADY_ACTIVATED
import com.ramble.identity.common.ErrorCode.USER_INFO_NOT_FOUND
import com.ramble.identity.common.ErrorCode.USER_IS_SUSPENDED
import com.ramble.identity.common.ErrorCode.USER_NOT_ACTIVATED
import com.ramble.identity.models.*
import com.ramble.identity.service.UserInfoService
import com.ramble.identity.service.UserRegistrationService
import com.ramble.identity.utils.getTokenFromBearerHeader
import com.ramble.token.model.AccessTokenIsInvalidException
import io.swagger.v3.oas.annotations.headers.Header
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(AUTH_API_BASE_PATH)
class AuthController(
    private val userInfoService: UserInfoService,
    private val userRegistrationService: UserRegistrationService
) {

    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = OK, content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = RegisteredUserResponse::class)
                    )
                ]
            ),
            ApiResponse(
                responseCode = BAD_REQUEST,
                description = "errorCode: $EMAIL_IS_INVALID",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorBody::class))]
            ),
            ApiResponse(
                responseCode = FORBIDDEN,
                description = "errorCodes: [$USER_ALREADY_ACTIVATED, $USER_IS_SUSPENDED, $USER_NOT_ACTIVATED]",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorBody::class))]
            ),
            ApiResponse(
                responseCode = INTERNAL_SERVER_ERROR,
                description = "errorCode: $EMAIL_SENDING_FAILED",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorBody::class))]
            )]
    )
    @PostMapping(USER_REGISTER_PATH)
    suspend fun signUp(@RequestBody user: RegisterUserRequest): RegisteredUserResponse =
        userRegistrationService.saveUser(user)

    @ApiResponses(
        value = [
            ApiResponse(responseCode = OK, content = []),
            ApiResponse(
                responseCode = FORBIDDEN,
                description = "errorCode: $UNAUTHORIZED_ACCESS",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorBody::class))]
            )]
    )
    @Throws(AccessTokenIsInvalidException::class)
    @PostMapping(LOGOUT_PATH)
    suspend fun logout(@RequestHeader(name = AUTHORIZATION_HEADER) authorizationHeader: String) =
        userInfoService.logout(
            accessToken = getTokenFromBearerHeader(authorizationHeader) ?: throw AccessTokenIsInvalidException()
        )

    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = OK,
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = LoginResponse::class)
                )
                ]
            ),
            ApiResponse(
                responseCode = FORBIDDEN,
                description = "errorCode: $REFRESH_TOKEN_IS_INVALID",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorBody::class))]
            )]
    )
    @PostMapping(REFRESH_TOKEN_PATH)
    suspend fun refreshToken(@RequestBody refreshTokenRequest: RefreshTokenRequest): LoginResponse =
        userInfoService.refreshToken(refreshTokenRequest)

    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = OK,
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = RegisteredUserResponse::class)
                )
                ]
            ),
            ApiResponse(
                responseCode = BAD_REQUEST,
                description = "errorCode: $USER_INFO_NOT_FOUND",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorBody::class))]
            ),
            ApiResponse(
                responseCode = FORBIDDEN,
                description = "errorCodes: [$UNAUTHORIZED_ACCESS, $USER_ALREADY_ACTIVATED, $USER_IS_SUSPENDED]",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorBody::class))]
            )]
    )
    @GetMapping(USER_REGISTRATION_CONFIRM_PATH)
    suspend fun confirmRegistration(@RequestParam(value = "token") token: String): RegisteredUserResponse =
        userRegistrationService.confirmToken(token)

    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = OK,
                headers = [
                    Header(name = AUTHORIZATION_HEADER, description = "Access Token in JWT format"),
                    Header(name = REFRESH_TOKEN_HEADER, description = "Refresh Token in UUID format")
                ],
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = LoginResponse::class)
                )]
            ),
            ApiResponse(
                responseCode = BAD_REQUEST,
                description = "errorCodes: [$CLIENT_ID_HEADER_MISSING, $USER_INFO_NOT_FOUND]",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorBody::class))]
            ),
            ApiResponse(
                responseCode = FORBIDDEN,
                description = "errorCodes: [$USER_IS_SUSPENDED, $USER_NOT_ACTIVATED]",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorBody::class))]
            )]
    )
    @PostMapping(LOGIN_PATH)
    fun fakeLogin(
        @RequestHeader(name = CLIENT_ID_HEADER) clientIdHeader: String,
        @RequestBody loginUserRequest: LoginUserRequest
    ) {
        throw IllegalStateException("This method won't be called. Login is overridden by Spring Security filters.")
    }

}