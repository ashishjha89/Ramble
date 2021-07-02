package com.ramble.token.model

data class RegistrationConfirmationToken(val userId: String, val email: String, val token: String)