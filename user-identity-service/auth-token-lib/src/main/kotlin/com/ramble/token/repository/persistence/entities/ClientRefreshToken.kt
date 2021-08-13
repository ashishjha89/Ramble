package com.ramble.token.repository.persistence.entities

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.IdClass

@Entity
@IdClass(ClientUserId::class)
class ClientRefreshToken(
    @Column(length = 1000) val refreshToken: String = "",
    @Column(length = 1000) val accessToken: String = "",
    @Id val userId: String = "",
    @Id val clientId: String = ""
)