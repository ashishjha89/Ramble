package com.ramble.token.repository.persistence

import com.ramble.token.repository.persistence.entities.DisabledClientTokens
import org.springframework.data.repository.CrudRepository

private typealias ClientId = String
interface DisabledTokensRedisRepo: CrudRepository<DisabledClientTokens, ClientId>