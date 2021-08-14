package com.ramble.token.repository.persistence.entities

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id

@Entity
class RegistrationConfirmationToken(
    @Id val userId: String = "",
    val email: String = "",
    @Column(length = 1000) val token: String = ""
)