package com.ramble.identity.security

import com.ramble.identity.models.BadRequestException
import com.ramble.identity.models.InternalServerException
import com.ramble.identity.models.LoginUserRequest
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactor.mono
import org.slf4j.LoggerFactory
import org.springframework.core.ResolvableType
import org.springframework.http.MediaType
import org.springframework.http.codec.json.AbstractJackson2Decoder
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Component
class AuthenticationConverter(private val jacksonDecoder: AbstractJackson2Decoder) : ServerAuthenticationConverter {

    private val logger = LoggerFactory.getLogger(AuthenticationConverter::class.java)

    @Throws(BadRequestException::class)
    override fun convert(exchange: ServerWebExchange?): Mono<Authentication> = mono {
        val loginRequest = getUsernameAndPassword(exchange) ?: let {
            logger.warn("LoginRequest body parsing failed")
            throw BadRequestException()
        }
        return@mono UsernamePasswordAuthenticationToken(loginRequest.email, loginRequest.password)
    }

    private suspend fun getUsernameAndPassword(exchange: ServerWebExchange?): LoginUserRequest? {
        val dataBuffer = exchange?.request?.body ?: let {
            logger.warn("LoginRequest body is null")
            throw InternalServerException()
        }
        val type = ResolvableType.forClass(LoginUserRequest::class.java)
        return jacksonDecoder
            .decodeToMono(dataBuffer, type, MediaType.APPLICATION_JSON, mapOf())
            .onErrorResume { Mono.empty<LoginUserRequest>() }
            .cast(LoginUserRequest::class.java)
            .awaitFirstOrNull()
    }
}