package com.ramble.identity.security

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.Gson
import com.ramble.identity.common.*
import com.ramble.identity.models.*
import com.ramble.identity.service.UserInfoService
import com.ramble.token.AuthTokensService
import kotlinx.coroutines.runBlocking
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class AuthenticationFilter(
    private val manager: AuthenticationManager,
    private val authTokensService: AuthTokensService,
    private val userInfoService: UserInfoService,
    loginPath: String
) : UsernamePasswordAuthenticationFilter() {

    init {
        setFilterProcessesUrl(loginPath)
    }

    override fun attemptAuthentication(request: HttpServletRequest, response: HttpServletResponse): Authentication {
        val loginUserRequest = ObjectMapper().readValue(request.inputStream, LoginUserRequest::class.java)
        val authentication = UsernamePasswordAuthenticationToken(loginUserRequest.email, loginUserRequest.password)
        return manager.authenticate(authentication)
    }

    override fun successfulAuthentication(
        request: HttpServletRequest?,
        response: HttpServletResponse,
        chain: FilterChain?,
        authResult: Authentication
    ) {
        response.apply {
            contentType = "application/json"
            characterEncoding = "UTF-8"
        }
        runBlocking {
            try {
                val deviceId = request?.getHeader(CLIENT_ID_HEADER) ?: throw ClientIdHeaderAbsentException()
                val email = authResult.name
                val userId = userInfoService.getUserInfo(email).id
                val authToken = authTokensService.generateUserAuthToken(authResult.authorities, deviceId, userId, email)
                val loginResponse = LoginResponse(userId, authToken.accessToken, authToken.refreshToken)
                response.apply {
                    status = HttpServletResponse.SC_OK
                    writer.apply {
                        print(Gson().toJson(loginResponse))
                        flush()
                    }
                }
            } catch (e: Exception) {
                val (errorResult, statusCode) = when (e) {
                    is UserNotFoundException -> Pair(userInfoNotFound, HttpServletResponse.SC_BAD_REQUEST)
                    is UserSuspendedException -> Pair(userSuspendedError, HttpServletResponse.SC_FORBIDDEN)
                    is UserNotActivatedException -> Pair(userNotActivatedError, HttpServletResponse.SC_FORBIDDEN)
                    is ClientIdHeaderAbsentException -> Pair(clientIdHeaderMissing, HttpServletResponse.SC_BAD_REQUEST)
                    else -> Pair(internalServerError, HttpServletResponse.SC_INTERNAL_SERVER_ERROR)
                }
                response.apply {
                    status = statusCode
                    writer.apply {
                        print(Gson().toJson(errorResult))
                        flush()
                    }
                }
            }
        }
    }
}