package com.ramble.token.handler.helper

import java.util.*

internal class UUIDGenerator {

    fun getRandomUUID(): String = UUID.randomUUID().toString()
}