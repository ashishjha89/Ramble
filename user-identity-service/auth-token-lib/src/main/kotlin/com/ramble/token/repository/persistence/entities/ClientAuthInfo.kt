package com.ramble.token.repository.persistence.entities

import org.springframework.data.redis.core.RedisHash
import javax.persistence.Entity
import javax.persistence.Id

@RedisHash("ClientAuthInfo")
data class ClientAuthInfo(
    @Id val clientId: String = "",
    val userId: String = "",
    val accessToken: String = ""
)