package com.ramble.identity.security

import com.ramble.identity.common.AUTHORIZATION_HEADER
import com.ramble.token.AuthTokensService
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import org.springframework.security.core.Authentication as SpringAuthentication

class AuthorizationFilter(
        authManager: AuthenticationManager,
        private val authTokensService: AuthTokensService
) : BasicAuthenticationFilter(authManager) {

    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) {
        val accessToken = request.getHeader(AUTHORIZATION_HEADER)
        if (accessToken != null) SecurityContextHolder.getContext().authentication = authenticate(accessToken)
        chain.doFilter(request, response)
    }

    // Return SpringAuthentication instance which is used by SpringSecurity to validate authorization.
    private fun authenticate(accessToken: String): SpringAuthentication? {
        try {
            val tokenClaims = authTokensService.getAccessTokenClaims(accessToken) ?: return null
            return authTokensService.springAuthentication(tokenClaims.claims, tokenClaims.authorities)
        } catch (e: Exception) {
            return null
        }
    }

}