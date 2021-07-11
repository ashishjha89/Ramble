package com.ramble.token.handler

import java.util.*

internal class UUIDGenerator {

    fun getRandomUUID() : String = UUID.randomUUID().toString()
}