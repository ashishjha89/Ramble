package com.ramble.messaging.repo

import org.junit.Test
import org.springframework.http.MediaType
import kotlin.test.assertEquals

class UserApiComponentTest {

    private val userApiComponent = UserApiComponent()

    @Test
    fun getHttpEntityTest() {
        val accessToken = "someAccessToken"

        // Call method
        val result = userApiComponent.getHttpEntity(accessToken)

        // Assert
        assertEquals(listOf("Bearer $accessToken"), result.headers["Authorization"])
        assertEquals(MediaType.APPLICATION_JSON, result.headers.contentType)
    }

    @Test
    fun getUserProfileApiUrlTest() {
        val userId = "someUserId"
        val expectedUrl = "http://user-identity-api/user-identity/user-info/v1/user/$userId"

        // Call method and assert
        assertEquals(expectedUrl, userApiComponent.getUserProfileApiUrl(userId))
    }
}