package com.ramble.accesstoken.handler.helper

internal class TokenClaimsMapGenerator {

    companion object {
        const val ROLES = "ROLES"
        const val USER_ID = "USER_ID"
        const val CLIENT_ID = "CLIENT_ID"
    }

    fun getAccessTokenClaimsMap(clientId: String, userId: String, roles: List<String>) =
        mapOf(
            ROLES to roles,
            CLIENT_ID to clientId,
            USER_ID to userId
        )
}