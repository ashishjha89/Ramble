package com.ramble.identity

import com.ramble.token.handler.TokensHandler
import org.springframework.context.annotation.Bean
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component

@Component
class Components {

    @Bean
    fun tokensHandler() = TokensHandler()

    @Bean
    fun bCryptPasswordEncoder() = BCryptPasswordEncoder(10)

}