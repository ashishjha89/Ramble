package com.ramble.token.handler.helper

internal class RefreshTokenClaimsMapGenerator {

    companion object {

        const val USER_ID = "USER_ID"

        const val CLIENT_ID = "CLIENT_ID"
    }

    fun getRefreshTokenClaimsMap(clientId: String, userId: String) =
        mapOf(CLIENT_ID to clientId, USER_ID to userId)
}