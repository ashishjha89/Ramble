package com.ramble.identity.service.helper

import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import javax.mail.MessagingException

@Service
class ConfirmRegistrationEmailService(private val mailSender: JavaMailSender) {

    @Async
    fun sendEmail(to: String, body: String) {
        try {
            val mimeMessage = mailSender.createMimeMessage()
            val helper = MimeMessageHelper(mimeMessage, "utf-8")
            helper.setText(body, true)
            helper.setTo(to)
            helper.setSubject("Confirm your email")
            helper.setFrom("ashishjha.mymail@gmail.com")
            mailSender.send(mimeMessage)
        } catch (e: MessagingException) {
            println("ConfirmRegistrationEmailService sendEmail() MessagingException e:$e")
        }
    }
}