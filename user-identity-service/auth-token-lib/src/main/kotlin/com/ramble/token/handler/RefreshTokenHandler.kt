package com.ramble.token.handler

internal class RefreshTokenHandler(
        private val uUIDGenerator: UUIDGenerator = UUIDGenerator()
) {

    fun generateRefreshToken(): String = uUIDGenerator.getRandomUUID()
}