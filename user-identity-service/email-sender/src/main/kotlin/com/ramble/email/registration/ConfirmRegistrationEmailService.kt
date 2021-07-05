package com.ramble.email.registration

import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import javax.mail.MessagingException

@Service
internal class ConfirmRegistrationEmailService(
        private val mailSender: JavaMailSender,
        private val confirmRegistrationEmailBuilder: ConfirmRegistrationEmailBuilder
) {

    @Async
    fun sendEmail(signUpUrl: String, to: String, fullName: String, token: String) {
        try {
            val body = confirmRegistrationEmailBuilder.buildEmail(
                    name = fullName,
                    link = confirmRegistrationEmailBuilder.getEmailLink(token, signUpUrl)
            )
            val mimeMessage = mailSender.createMimeMessage()
            val helper = MimeMessageHelper(mimeMessage, "utf-8")
            helper.setText(body, true)
            helper.setTo(to)
            helper.setSubject("Confirm your email")
            helper.setFrom("ashish.kr.jha89l@gmail.com") // TODO: Read these values from a defined Property
            mailSender.send(mimeMessage)
        } catch (e: MessagingException) {
            println("ConfirmRegistrationEmailService sendEmail() MessagingException e:$e")
        }
    }
}