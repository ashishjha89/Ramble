package com.ramble.token.repository.persistence.entities

import java.io.Serializable

data class ClientUserId(val clientId: String = "", val userId: String = "") : Serializable