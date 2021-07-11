package com.ramble.token.handler

import com.ramble.token.model.TokenDuration
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

internal class AccessTokenDurationGenerator {

    fun getTokenDuration(
            issuedInstant: Instant,
            expiryDurationAmount: Long,
            expiryDurationUnit: ChronoUnit
    ): TokenDuration {
        val expiryInstant = issuedInstant.plus(expiryDurationAmount, expiryDurationUnit)
        return TokenDuration(
                issuedDate = Date.from(issuedInstant),
                expiryDate = Date.from(expiryInstant)
        )
    }
}