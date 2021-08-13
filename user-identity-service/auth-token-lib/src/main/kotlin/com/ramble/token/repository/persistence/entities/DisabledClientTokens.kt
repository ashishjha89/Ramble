package com.ramble.token.repository.persistence.entities

import org.springframework.data.redis.core.RedisHash
import javax.persistence.Id

@RedisHash("DisabledClientTokens")
data class DisabledClientTokens(
    @Id val id: String = "",
    val disabledAccessTokens: List<String> = listOf()
)