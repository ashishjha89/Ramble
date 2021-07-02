package com.ramble.token.handler.helper

import io.jsonwebtoken.security.Keys
import javax.crypto.SecretKey

class JwtKeyGenerator {

    companion object {
        private const val KEY = "OMQBBbeM64asPB0mvUYI4b+G08t4e6npxRXqzq6ZwmC1Ao4ibnBPT1oGH09rQuwWPHFy0hWCsfjm0MsiA2eJeA=="  // Key to sign the token. Minimum length should be 512 bytes
    }

    val key: SecretKey = Keys.hmacShaKeyFor(KEY.toByteArray())
}