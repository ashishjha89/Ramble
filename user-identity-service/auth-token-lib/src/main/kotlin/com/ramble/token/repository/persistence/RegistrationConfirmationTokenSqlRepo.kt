package com.ramble.token.repository.persistence

import com.ramble.token.repository.persistence.entities.RegistrationConfirmationToken
import org.springframework.data.repository.CrudRepository

interface RegistrationConfirmationTokenSqlRepo : CrudRepository<RegistrationConfirmationToken, String>