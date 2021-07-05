package com.ramble.identity.common

private const val USER_REGISTER_CONFIRM_PATH = "/confirm"

const val USER_INFO_API_BASE_PATH = "/user-info/v1"

const val USER_REGISTER_PATH = "/register"

const val USER_REGISTRATION_CONFIRM_PATH = "$USER_REGISTER_PATH$USER_REGISTER_CONFIRM_PATH"

const val USER_LOGIN_PATH = "/login"

const val USER_INFO_ME_PATH = "/me"

const val SIGN_UP_URL = "$USER_INFO_API_BASE_PATH$USER_REGISTER_PATH"

const val SIGN_UP_CONFIRMATION_URL = "$SIGN_UP_URL$USER_REGISTER_CONFIRM_PATH"

const val AUTHORIZATION_HEADER = "Authorization"