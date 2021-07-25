package com.ramble.identity.common

// API PATHS

const val USER_INFO_API_BASE_PATH = "/user-info/v1"

const val AUTH_API_BASE_PATH = "/auth/v1"

const val USER_REGISTER_PATH = "/register"

const val REFRESH_TOKEN_PATH = "/refresh-token"

const val LOGIN_PATH = "/login"

const val LOGOUT_PATH = "/logout"

const val USER_INFO_ME_PATH = "/me"

private const val USER_REGISTER_CONFIRM_PATH = "/confirm"

const val USER_REGISTRATION_CONFIRM_PATH = "$USER_REGISTER_PATH$USER_REGISTER_CONFIRM_PATH" // -> /register/confirm

const val SIGN_UP_URL = "$AUTH_API_BASE_PATH$USER_REGISTER_PATH" // -> /auth/v1/register

const val SIGN_UP_CONFIRMATION_URL = "$SIGN_UP_URL$USER_REGISTER_CONFIRM_PATH" // -> /auth/v1/register/confirm

const val REFRESH_TOKEN_URL = "$AUTH_API_BASE_PATH$REFRESH_TOKEN_PATH" // -> /auth/v1/refresh-token

// HEADERS
const val AUTHORIZATION_HEADER = "Authorization"

const val CLIENT_ID_HEADER = "ClientId"

// OTHER
const val REGISTER_EMAIL_SUBJECT = "Confirm your email"