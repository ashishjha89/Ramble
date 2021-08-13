package com.ramble.token.repository.persistence

import com.ramble.token.repository.persistence.entities.ClientRefreshToken
import org.springframework.data.repository.CrudRepository

interface ClientRefreshTokenSqlRepo : CrudRepository<ClientRefreshToken, String>