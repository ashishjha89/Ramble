package com.ramble.identity.models

data class RegisterUserRequest(val email: String, val password: String)

data class LoginUserRequest(val email: String? = null, val password: String? = null)

data class RegisteredUserResponse(val id: String)

data class LoginResponse(val id: String, val email: String, val accessToken: String, val refreshToken: String)

data class UserInfo(val id: String, val email: String)