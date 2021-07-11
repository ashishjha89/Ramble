package com.ramble.identity

import org.springframework.context.annotation.Bean
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component

@Component
class Components {

    @Bean
    fun bCryptPasswordEncoder() = BCryptPasswordEncoder(10)

}