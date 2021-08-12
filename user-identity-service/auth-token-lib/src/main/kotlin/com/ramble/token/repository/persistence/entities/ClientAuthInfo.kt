package com.ramble.token.repository.persistence.entities

import org.springframework.data.redis.core.RedisHash

@RedisHash("ClientAuthInfo")
internal class ClientAuthInfo(val clientId: String, val userId: String, val accessToken: String)