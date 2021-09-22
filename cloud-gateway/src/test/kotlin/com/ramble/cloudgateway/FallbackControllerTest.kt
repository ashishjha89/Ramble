package com.ramble.cloudgateway

import com.ramble.cloudgateway.model.takingTooLong
import org.junit.Test
import kotlin.test.assertEquals

class FallbackControllerTest {

    private val fallbackController = FallbackController()

    @Test
    fun serviceFallbackTest() {
        assertEquals(takingTooLong, fallbackController.serviceFallback())
    }

}