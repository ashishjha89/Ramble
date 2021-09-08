package com.ramble.accesstoken.handler.helper

import org.springframework.security.core.GrantedAuthority

internal class TokenClaimsMapGenerator {

    companion object {

        const val ROLES = "ROLES"

        const val USER_ID = "USER_ID"

        const val CLIENT_ID = "CLIENT_ID"
    }

    fun getAccessTokenClaimsMap(clientId: String, userId: String, authorities: Collection<GrantedAuthority>) =
        mapOf(
            ROLES to authorities.map { it.authority },
            CLIENT_ID to clientId,
            USER_ID to userId
        )
}