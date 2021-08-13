package com.ramble.token.repository.persistence

import com.ramble.token.repository.persistence.entities.DisabledClientTokens
import org.springframework.data.repository.CrudRepository

interface DisabledTokensRedisRepo: CrudRepository<DisabledClientTokens, String>