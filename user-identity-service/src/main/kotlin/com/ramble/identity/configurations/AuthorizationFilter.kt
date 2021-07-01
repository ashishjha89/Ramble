package com.ramble.identity.configurations

import com.ramble.identity.common.AUTHORIZATION_HEADER
import com.ramble.token.handler.TokensHandler
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class AuthorizationFilter(
        authManager: AuthenticationManager,
        private val tokensHandler: TokensHandler) : BasicAuthenticationFilter(authManager) {

    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) {
        val token: String? = request.getHeader(AUTHORIZATION_HEADER)
        println("AuthorizationFilter doFilterInternal() token:$token")
        if (token != null) SecurityContextHolder.getContext().authentication = authenticate(token)
        chain.doFilter(request, response)
    }

    private fun authenticate(token: String): UsernamePasswordAuthenticationToken? {
        try {
            val tokenClaims = tokensHandler.getAccessTokenClaims(token) ?: return null
            println("AuthorizationFilter authenticate() accessTokens:$tokenClaims")
            return UsernamePasswordAuthenticationToken(tokenClaims.claims, null, tokenClaims.authorities)
        } catch (e: Exception) {
            return null
        }
    }

}