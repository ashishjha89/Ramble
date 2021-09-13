package com.ramble.messaging.controller

import com.ramble.messaging.common.MESSAGING_API_BASE_PATH
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(MESSAGING_API_BASE_PATH)
class MessagingController {

    @GetMapping("/hello")
    fun helloFromMessaging() = "Hello from Messaging!"
}