package com.ramble.identity.repo.persistence

import com.ramble.identity.repo.persistence.entity.ApplicationUserEntity
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param

private typealias Id = String

interface UserSqlRepo : CrudRepository<ApplicationUserEntity, Id> {

    @Query("SELECT u FROM application_user_entity u WHERE u.email = :email")
    fun getUserByEmail(@Param("email") email: String): List<ApplicationUserEntity>?
}