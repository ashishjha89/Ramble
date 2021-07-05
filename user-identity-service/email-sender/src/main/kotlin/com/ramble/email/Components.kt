package com.ramble.email

import com.ramble.email.registration.ConfirmRegistrationEmailBuilder
import com.ramble.email.registration.ConfirmRegistrationEmailService
import org.springframework.context.annotation.Bean
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.JavaMailSenderImpl
import java.util.*

internal object Components{

    private val mailSender: JavaMailSender =
            JavaMailSenderImpl().apply {
                host = "localhost"
                port = 1025
                username = "ashish.kr.jha89@gmail.com"
                password = "this_is_dummy_pwd"

                val props: Properties = javaMailProperties
                props["mail.transport.protocol"] = "smtp"
                props["mail.smtp.ssl.trust"] = "*"
                props["mail.smtp.auth"] = "true"
                props["mail.smtp.starttls.enable"] = "true"
                props["mail.smtp.starttls.required"] = "true"
                props["mail.smtp.connectiontimeout"] = "5000"
                props["mail.smtp.timeout"] = "3000"
                props["mail.smtp.writetimeout"] = "5000"
            }

    @Bean
    fun confirmRegistrationEmailService() =
            ConfirmRegistrationEmailService(
                    mailSender = mailSender,
                    confirmRegistrationEmailBuilder = ConfirmRegistrationEmailBuilder()
            )

}