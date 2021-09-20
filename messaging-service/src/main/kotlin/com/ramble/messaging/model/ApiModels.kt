package com.ramble.messaging.model

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class UserProfile(
    val email: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val nickname: String? = null,
    val fullName: String? = null,
    val age: Int? = null,
    val gender: String? = null
)