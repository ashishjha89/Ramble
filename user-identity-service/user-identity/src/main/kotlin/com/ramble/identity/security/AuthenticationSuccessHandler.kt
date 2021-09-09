package com.ramble.identity.security

import com.ramble.identity.common.AUTHORIZATION_HEADER
import com.ramble.identity.common.CLIENT_ID_HEADER
import com.ramble.identity.common.REFRESH_TOKEN_HEADER
import com.ramble.identity.models.*
import com.ramble.identity.service.UserInfoService
import com.ramble.token.AuthTokensService
import kotlinx.coroutines.reactor.mono
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.web.server.WebFilterExchange
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class AuthenticationSuccessHandler(
    private val authTokensService: AuthTokensService,
    private val userInfoService: UserInfoService
) : ServerAuthenticationSuccessHandler {

    @Throws(UnauthorizedException::class, InternalServerException::class)
    override fun onAuthenticationSuccess(
        webFilterExchange: WebFilterExchange?,
        authentication: Authentication?
    ): Mono<Void?> = mono {
        val request = webFilterExchange?.exchange?.request ?: throw InternalServerException()
        val response = webFilterExchange.exchange?.response ?: throw InternalServerException()

        val deviceId = request.headers.getFirst(CLIENT_ID_HEADER) ?: throw ClientIdHeaderAbsentException()
        val email = authentication?.name ?: throw InternalServerException()

        try {
            val userId = userInfoService.getMyUserInfo(email).id
            val roles = authentication.authorities.map { (it as GrantedAuthority).authority }
            val authToken = authTokensService.generateUserAuthToken(roles, deviceId, userId, email)
            val loginResponse = LoginResponse(userId, authToken.accessToken, authToken.refreshToken)
            response.also {
                it.statusCode = HttpStatus.OK
                it.headers.set("Content-Type", "application/json;charset=UTF-8")
                it.headers.set(AUTHORIZATION_HEADER, loginResponse.accessToken)
                it.headers.set(REFRESH_TOKEN_HEADER, loginResponse.refreshToken)
            }
        } catch (e: Exception) {
            val httpStatus = when (e) {
                is UserNotFoundException -> HttpStatus.BAD_REQUEST
                is UserSuspendedException -> HttpStatus.FORBIDDEN
                is UserNotActivatedException -> HttpStatus.FORBIDDEN
                is ClientIdHeaderAbsentException -> HttpStatus.BAD_REQUEST
                else -> HttpStatus.INTERNAL_SERVER_ERROR
            }
            response.also {
                it.statusCode = httpStatus
                it.headers.set("Content-Type", "application/json;charset=UTF-8")
            }
        }
        null
    }

}