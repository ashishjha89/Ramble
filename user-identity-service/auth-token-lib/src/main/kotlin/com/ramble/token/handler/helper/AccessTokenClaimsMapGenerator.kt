package com.ramble.token.handler.helper

import org.springframework.security.core.GrantedAuthority

internal class AccessTokenClaimsMapGenerator {

    companion object {

        private const val ROLES = "ROLES"

        private const val USER_ID = "USER_ID"
    }

    fun getAccessTokenClaimsMap(userId: String, authorities: Collection<GrantedAuthority>) =
            mapOf(
                    ROLES to authorities.map { it.authority },
                    USER_ID to userId
            )
}