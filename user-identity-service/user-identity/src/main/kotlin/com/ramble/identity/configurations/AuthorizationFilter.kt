package com.ramble.identity.configurations

import com.ramble.identity.common.AUTHORIZATION_HEADER
import com.ramble.token.handler.AuthTokensHandler
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class AuthorizationFilter(
        authManager: AuthenticationManager,
        private val authTokensHandler: AuthTokensHandler
) : BasicAuthenticationFilter(authManager) {

    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) {
        val token = request.getHeader(AUTHORIZATION_HEADER)
        if (token != null) SecurityContextHolder.getContext().authentication = authenticate(token)
        chain.doFilter(request, response)
    }

    private fun authenticate(token: String): Authentication? {
        try {
            val tokenClaims = authTokensHandler.getClaims(token) ?: return null
            return authTokensHandler.getAuthentication(tokenClaims.claims, tokenClaims.authorities)
        } catch (e: Exception) {
            return null
        }
    }

}