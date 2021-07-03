package com.ramble.identity

import com.ramble.token.handler.AuthTokensHandler
import com.ramble.token.handler.RegistrationConfirmationHandler
import org.springframework.context.annotation.Bean
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.JavaMailSenderImpl
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component
import java.util.*


@Component
class Components {

    @Bean
    fun authTokensHandler() = AuthTokensHandler()

    @Bean
    fun bCryptPasswordEncoder() = BCryptPasswordEncoder(10)

    @Bean
    fun registrationConfirmationHandler() = RegistrationConfirmationHandler()

    @Bean
    fun javaMailSender(): JavaMailSender {
        val mailSender = JavaMailSenderImpl()
        mailSender.host = "localhost"
        mailSender.port = 1025
        mailSender.username = "hello"
        mailSender.password = "hello"

        val props: Properties = mailSender.javaMailProperties
        props["mail.transport.protocol"] = "smtp"
        props["mail.smtp.ssl.trust"] = "*"
        props["mail.smtp.auth"] = "true"
        props["mail.smtp.starttls.enable"] = "true"
        props["mail.smtp.starttls.required"] = "true"
        props["mail.smtp.connectiontimeout"] = "5000"
        props["mail.smtp.timeout"] = "5000"
        props["mail.smtp.writetimeout"] = "5000"

        return mailSender
    }

}