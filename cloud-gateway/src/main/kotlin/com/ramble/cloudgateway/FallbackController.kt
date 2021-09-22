package com.ramble.cloudgateway

import com.ramble.cloudgateway.model.ErrorBody
import com.ramble.cloudgateway.model.takingTooLong
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class FallbackController {

    @GetMapping("/serviceFallback")
    fun serviceFallback(): ErrorBody = takingTooLong
}