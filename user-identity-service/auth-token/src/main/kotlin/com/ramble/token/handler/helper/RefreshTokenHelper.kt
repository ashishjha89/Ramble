package com.ramble.token.handler.helper

internal class RefreshTokenHelper(
        private val uUIDGenerator: UUIDGenerator = UUIDGenerator()
) {

    fun generateRefreshToken(): String = uUIDGenerator.getRandomUUID()
}