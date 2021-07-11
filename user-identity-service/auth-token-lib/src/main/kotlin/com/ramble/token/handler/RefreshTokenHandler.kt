package com.ramble.token.handler

import com.ramble.token.handler.helper.UUIDGenerator

internal class RefreshTokenHandler(private val uUIDGenerator: UUIDGenerator) {

    fun generateRefreshToken(): String = uUIDGenerator.getRandomUUID()
}