package com.ramble.token.handler

import java.util.*

internal class RefreshTokenHandler {

    fun generateRefreshToken(): String = UUID.randomUUID().toString()
}