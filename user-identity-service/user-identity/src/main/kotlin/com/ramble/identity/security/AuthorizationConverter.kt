package com.ramble.identity.security

import com.ramble.identity.models.UnauthorizedException
import com.ramble.identity.utils.getTokenFromBearerHeader
import com.ramble.token.AuthTokensService
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactor.mono
import org.springframework.http.HttpHeaders
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

class AuthorizationConverter(private val authTokensService: AuthTokensService) : WebFilter {

    @Throws(UnauthorizedException::class)
    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> = mono {
        val authHeader = exchange.request.headers.getFirst(HttpHeaders.AUTHORIZATION)
            ?: return@mono chain.filter(exchange).awaitFirstOrNull()
        val accessToken = getTokenFromBearerHeader(authHeader) ?: return@mono chain.filter(exchange).awaitFirstOrNull()
        return@mono try {
            val tokenClaims = authTokensService.getAccessTokenClaims(accessToken) ?: throw UnauthorizedException()
            val springAuthUser = authTokensService.springAuthentication(tokenClaims.claims, tokenClaims.authorities)
            chain.filter(exchange).contextWrite(ReactiveSecurityContextHolder.withAuthentication(springAuthUser))
                .awaitFirstOrNull()
        } catch (e: Exception) {
            chain.filter(exchange).contextWrite(ReactiveSecurityContextHolder.clearContext()).awaitFirstOrNull()
        }
    }

}