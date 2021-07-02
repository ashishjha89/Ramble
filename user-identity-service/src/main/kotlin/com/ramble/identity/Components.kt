package com.ramble.identity

import com.ramble.token.handler.AuthTokensHandler
import com.ramble.token.handler.RegistrationConfirmationHandler
import org.springframework.context.annotation.Bean
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component

@Component
class Components {

    @Bean
    fun authTokensHandler() = AuthTokensHandler()

    @Bean
    fun bCryptPasswordEncoder() = BCryptPasswordEncoder(10)

    @Bean
    fun registrationConfirmationHandler() = RegistrationConfirmationHandler()

}