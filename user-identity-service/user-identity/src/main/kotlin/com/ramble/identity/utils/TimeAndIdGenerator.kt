package com.ramble.identity.utils

import org.springframework.stereotype.Service
import java.time.Instant

@Service
class TimeAndIdGenerator {

    fun getCurrentTime(): Instant = Instant.now()

    fun getTimeBasedId(): Long = getCurrentTime().toEpochMilli()

    fun getCurrentTimeInSeconds(): Long = getCurrentTime().epochSecond
}