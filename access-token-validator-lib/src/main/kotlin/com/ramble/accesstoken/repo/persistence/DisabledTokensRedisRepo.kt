package com.ramble.accesstoken.repo.persistence

import com.ramble.accesstoken.repo.persistence.entities.DisabledClientTokens
import org.springframework.data.repository.CrudRepository

private typealias ClientId = String
interface DisabledTokensRedisRepo: CrudRepository<DisabledClientTokens, ClientId>