package com.ramble.token.repository.persistence

import com.ramble.token.repository.persistence.entities.RegistrationConfirmationToken
import org.springframework.data.repository.CrudRepository

private typealias Email = String
interface RegistrationConfirmationTokenSqlRepo : CrudRepository<RegistrationConfirmationToken, Email>