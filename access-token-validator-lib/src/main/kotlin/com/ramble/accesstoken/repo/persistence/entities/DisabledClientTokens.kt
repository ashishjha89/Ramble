package com.ramble.accesstoken.repo.persistence.entities

import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash

@RedisHash("DisabledClientTokens")
data class DisabledClientTokens(
    @Id val id: String = "",
    val disabledAccessTokens: List<String> = listOf()
)