package com.ramble.identity.repo.persistence

import com.ramble.identity.repo.persistence.entity.ApplicationUserEntity
import org.springframework.data.repository.CrudRepository

private typealias Email = String

interface UserSqlRepo : CrudRepository<ApplicationUserEntity, Email>