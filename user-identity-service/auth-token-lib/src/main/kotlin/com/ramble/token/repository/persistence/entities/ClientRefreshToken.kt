package com.ramble.token.repository.persistence.entities

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id

@Entity
data class ClientRefreshToken(
        @Id @Column(length = 100) val refreshToken: String = "",
        @Column(length = 1000) val accessToken: String = "",
        val userId: String = "",
        val clientId: String = ""
)