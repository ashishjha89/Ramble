package com.ramble.token.handler.helper

import com.ramble.token.model.TokenDuration
import org.junit.Test
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.test.assertEquals

class TokenDurationGeneratorTest {

    private val tokenDurationGenerator = TokenDurationGenerator()

    @Test
    fun getTokenDurationTest() {
        val issuedInstant = Instant.now()
        val expiryDurationAmount = 30L
        val expiryDurationUnit = ChronoUnit.MINUTES

        val expectedTokenDuration = TokenDuration(
                issuedDate = Date.from(issuedInstant),
                expiryDate = Date.from(issuedInstant.plus(expiryDurationAmount, expiryDurationUnit))
        )

        // Call method and assert
        assertEquals(
                expectedTokenDuration,
                tokenDurationGenerator.getTokenDuration(issuedInstant, expiryDurationAmount, expiryDurationUnit)
        )
    }
}