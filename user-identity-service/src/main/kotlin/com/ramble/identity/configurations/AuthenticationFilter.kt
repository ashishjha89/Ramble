package com.ramble.identity.configurations

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.Gson
import com.ramble.identity.common.ErrorBody
import com.ramble.identity.common.ErrorCode
import com.ramble.identity.common.ErrorMessage
import com.ramble.identity.models.LoginResponse
import com.ramble.identity.models.LoginUserRequest
import com.ramble.identity.models.UserNotFoundException
import com.ramble.identity.service.UserService
import com.ramble.token.handler.TokensHandler
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class AuthenticationFilter(
        private val manager: AuthenticationManager,
        private val tokensHandler: TokensHandler,
        private val userService: UserService,
        loginPath: String
) : UsernamePasswordAuthenticationFilter() {

    init {
        setFilterProcessesUrl(loginPath)
    }

    override fun attemptAuthentication(request: HttpServletRequest, response: HttpServletResponse): Authentication {
        val user = ObjectMapper().readValue(request.inputStream, LoginUserRequest::class.java)
        println("AuthenticationFilter attemptAuthentication() user:$user")
        val authentication = UsernamePasswordAuthenticationToken(user.email, user.password)
        return manager.authenticate(authentication)
    }

    override fun successfulAuthentication(request: HttpServletRequest?, response: HttpServletResponse, chain: FilterChain?, authResult: Authentication) {
        val email = authResult.name
        val userId = userService.getUserInfo(email).id
        val authToken = tokensHandler.generateAuthToken(authResult, userId, email)
        val loginResponse = LoginResponse(userId, email, authToken.accessToken, authToken.refreshToken)
        response.apply {
            contentType = "application/json"
            characterEncoding = "UTF-8"
            status = HttpServletResponse.SC_OK
            writer.apply {
                print(Gson().toJson(loginResponse))
                flush()
            }
        }
        println("AuthenticationFilter successfulAuthentication() authResult.authorities:${authResult.authorities} loginResponse:$loginResponse ")
    }

}